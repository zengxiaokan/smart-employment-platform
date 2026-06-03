package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.alibaba.fastjson2.JSON;
import com.itzk.SmartEmploymentPlatform.mapper.ConversationMapper;
import com.itzk.SmartEmploymentPlatform.mapper.MessageMapper;
import com.itzk.SmartEmploymentPlatform.mapper.UserMapper;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Conversation;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Message;
import com.itzk.SmartEmploymentPlatform.pojo.entry.User;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.SendMessageDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ConversationVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.MessageVO;
import com.itzk.SmartEmploymentPlatform.service.ChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private ConversationMapper conversationMapper;
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /** 在线状态 key 前缀 */
    private static final String ONLINE_KEY_PREFIX = "online:user:";
    /** 会话列表缓存 key 前缀 */
    private static final String CONV_LIST_KEY_PREFIX = "conv:list:";
    /** 会话列表缓存过期时间 */
    private static final Duration CONV_LIST_TTL = Duration.ofMinutes(30);

    /**
     * 获取或创建两个用户之间的会话。
     * 为保证唯一性，始终令 user1_id < user2_id，配合数据库 UNIQUE KEY (user1_id, user2_id)。
     * 并发安全策略：先查，查不到 insert，捕获 DuplicateKeyException 后回退为查询。
     */
    @Override
    @Transactional
    public Conversation getOrCreateConversation(Long user1Id, Long user2Id) {
        Long smaller = Math.min(user1Id, user2Id);
        Long larger = Math.max(user1Id, user2Id);

        Conversation conv = conversationMapper.selectByUsers(smaller, larger);
        if (conv != null) {
            log.info("会话已存在: id={}, user1Id={}, user2Id={}", conv.getId(), conv.getUser1Id(), conv.getUser2Id());
            return conv;
        }

        log.info("创建新会话: smaller={}, larger={}", smaller, larger);
        User u1 = userMapper.getUserById(smaller);
        User u2 = userMapper.getUserById(larger);
        log.info("查询用户: u1={}, u2={}", u1 != null ? u1.getId() : "null", u2 != null ? u2.getId() : "null");

        conv = new Conversation();
        conv.setUser1Id(smaller);
        conv.setUser2Id(larger);
        conv.setUser1Nickname(getDisplayName(u1));
        conv.setUser1Avatar(u1 != null ? u1.getAvatarUrl() : null);
        conv.setUser2Nickname(getDisplayName(u2));
        conv.setUser2Avatar(u2 != null ? u2.getAvatarUrl() : null);

        try {
            conversationMapper.insert(conv);
            log.info("会话创建成功: id={}", conv.getId());
            // 新建会话后清除双方缓存
            evictConvListCache(smaller);
            evictConvListCache(larger);
            return conv;
        } catch (DuplicateKeyException e) {
            // 并发场景：另一个线程在 select 返回 null 之后、当前 insert 之前已插入，回退查询
            log.warn("会话已由并发线程创建，回退查询");
            return conversationMapper.selectByUsers(smaller, larger);
        }
    }

    private String getDisplayName(User u) {
        if (u == null) return null;
        if (u.getNickname() != null && !u.getNickname().isEmpty()) return u.getNickname();
        if (u.getPhone() != null && !u.getPhone().isEmpty()) return u.getPhone();
        if (u.getUsername() != null && !u.getUsername().isEmpty()) return u.getUsername();
        return "用户" + u.getId();
    }

    /**
     * 保存消息并原子更新会话元数据。
     * 合并 last_message 更新和未读计数增加为一条 SQL（CASE WHEN），不再需要应用层判断 user1/user2。
     */
    @Override
    @Transactional
    public Message saveMessage(Long conversationId, Long senderId, SendMessageDTO dto) {
        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setReceiverId(dto.getReceiverId());
        msg.setContent(dto.getContent());
        msg.setMsgType(dto.getMsgType() != null ? dto.getMsgType() : 0);
        msg.setIsRead(0);
        messageMapper.insert(msg);

        // 一次 UPDATE：同时更新 last_message 和接收方未读计数
        conversationMapper.updateLastMessageAndIncrUnread(conversationId, dto.getContent(), dto.getReceiverId());

        // 清除收发双方的会话列表缓存，下次请求时重新查库
        evictConvListCache(senderId);
        evictConvListCache(dto.getReceiverId());

        return msg;
    }

    /** 清除指定用户的会话列表缓存 */
    private void evictConvListCache(Long userId) {
        try {
            stringRedisTemplate.delete(CONV_LIST_KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("清除会话列表缓存失败, userId={}, msg={}", userId, e.getMessage());
        }
    }

    /**
     * 查询会话列表（先查 Redis 缓存，未命中查 MySQL 并回写缓存）。
     * 缓存 TTL 30 分钟，发消息时自动清除。
     * Redis 不可用时直接查 MySQL 降级。
     */
    @Override
    public List<ConversationVO> getConversations(Long userId) {
        String cacheKey = CONV_LIST_KEY_PREFIX + userId;

        // 1. 尝试从 Redis 缓存读取
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isEmpty()) {
                List<ConversationVO> list = JSON.parseArray(cached, ConversationVO.class);
                // 在线状态实时查，不缓存
                for (ConversationVO vo : list) {
                    vo.setIsOnline(isUserOnline(vo.getTargetUserId()));
                }
                return list;
            }
        } catch (Exception e) {
            log.warn("Redis 读取会话列表缓存失败, userId={}, msg={}", userId, e.getMessage());
        }

        // 2. 缓存未命中，查 MySQL
        List<ConversationVO> list = conversationMapper.selectByUserId(userId);

        // 3. 写回 Redis 缓存（不含 isOnline，isOnline 实时查）
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(list), CONV_LIST_TTL);
        } catch (Exception e) {
            log.warn("Redis 写入会话列表缓存失败, userId={}, msg={}", userId, e.getMessage());
        }

        // 4. 填充在线状态
        for (ConversationVO vo : list) {
            vo.setIsOnline(isUserOnline(vo.getTargetUserId()));
        }
        return list;
    }

    /**
     * 查询历史消息并在事务内标记已读。
     * 两步操作：先在 messages 表中标记 is_read，再清零 conversations 表中的未读计数。
     */
    @Override
    public List<MessageVO> getMessages(Long conversationId, Long userId) {
        // 标记消息已读
        messageMapper.markAsRead(conversationId, userId);

        // 清零会话未读计数
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv != null) {
            if (conv.getUser1Id().equals(userId)) {
                conversationMapper.clearUser1Unread(conv.getId());
            } else {
                conversationMapper.clearUser2Unread(conv.getId());
            }
        }

        return messageMapper.selectByConversationId(conversationId);
    }

    /**
     * 判断用户是否在线。Redis 超时或不可用时降级返回 false。
     */
    @Override
    public boolean isUserOnline(Long userId) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(ONLINE_KEY_PREFIX + userId));
        } catch (Exception e) {
            log.warn("Redis 查询在线状态失败, userId={}, msg={}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 获取用户所有会话中最新的未读消息（每个会话取最新一条）。
     * 用于用户登录后批量推送离线期间收到的消息。
     */
    @Override
    public List<Message> getLatestUnreadMessages(Long userId) {
        return messageMapper.selectLatestUnreadByReceiver(userId);
    }

    @Override
    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) return;
        if (!conv.getUser1Id().equals(userId) && !conv.getUser2Id().equals(userId)) return;
        messageMapper.deleteByConversationId(conversationId);
        conversationMapper.deleteById(conversationId);
        evictConvListCache(conv.getUser1Id());
        evictConvListCache(conv.getUser2Id());
    }
}
