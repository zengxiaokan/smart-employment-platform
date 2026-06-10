<template>
  <div class="chat-panel" :class="{ compact }">
    <div class="contact-list">
      <div class="list-header">
        <span>消息列表</span>
        <el-badge :value="unreadCount" />
        <el-button v-if="isAdmin" :icon="Plus" size="small" text @click="showNewConvDialog = true" title="发起会话" />
        <el-button :icon="Refresh" size="small" text @click="refreshAll" :loading="refreshing" title="刷新消息" />
      </div>
      <div v-if="announcementList.length > 0" class="ann-section">
        <div class="ann-header" @click="annCollapsed = !annCollapsed">
          <el-icon><Bell /></el-icon>
          <span>系统公告</span>
          <el-tag size="small" type="danger">{{ announcementList.length }}</el-tag>
          <el-icon class="ann-collapse-icon"><component :is="annCollapsed ? ArrowRight : ArrowDown" /></el-icon>
        </div>
        <template v-if="!annCollapsed">
          <div
            v-for="ann in sortedAnnouncements"
            :key="ann.id"
            class="ann-item"
            :class="{ active: selectedAnn?.id === ann.id, pinned: ann.pinned }"
            @click="selectAnnouncement(ann)"
          >
            <div class="ann-title">
              <el-icon><Bell /></el-icon>
              <span>{{ ann.title }}</span>
              <el-tag v-if="ann.pinned" size="small" type="warning" class="pin-tag">置顶</el-tag>
            </div>
          </div>
        </template>
      </div>
      <div class="contact-scroll">
        <div
          v-for="contact in contactList"
          :key="contact.id"
          class="contact-item"
          :class="{ active: currentContact?.id === contact.id }"
          @click="selectContact(contact)"
        >
          <el-badge :is-dot="contact.unread > 0">
            <el-avatar :size="40" :src="contact.avatar" />
          </el-badge>
          <div class="contact-info">
            <div class="contact-name">{{ contact.name }}</div>
            <div class="contact-preview">{{ contact.lastMessage }}</div>
          </div>
          <span class="contact-time">{{ contact.time }}</span>
          <el-button
            class="contact-delete-btn"
            :icon="Delete"
            size="small"
            text
            @click.stop="handleDeleteConversation(contact)"
            title="删除会话"
          />
        </div>
      </div>
    </div>

    <div class="chat-area">
      <div class="chat-header">
        <template v-if="selectedAnn">
          <el-icon><Bell /></el-icon>
          <span>{{ selectedAnn.title }}</span>
          <el-tag size="small" type="warning">系统公告</el-tag>
        </template>
        <span v-else-if="currentContact">{{ currentContact.name }}</span>
        <span v-else class="placeholder">选择联系人开始沟通</span>
      </div>

      <div v-if="selectedAnn" class="ann-detail">
        <div class="ann-detail-meta">
          <span>发布者：管理员</span>
          <span>{{ selectedAnn.sentAt }}</span>
        </div>
        <div class="ann-detail-content">{{ selectedAnn.content }}</div>
      </div>

      <div class="chat-body" v-else-if="currentContact" ref="chatBodyRef">
        <div
          v-for="(msg, idx) in currentMessages"
          :key="idx"
          class="message-item"
          :class="{ self: msg.self }"
        >
          <el-avatar v-if="!msg.self" :size="32" :src="currentContact.avatar" />
          <div
            class="message-bubble"
            :class="{
              self: msg.self,
              'self failed': msg.self && msg.status === 'failed',
            }"
          >
            {{ msg.content }}
            <div class="msg-meta">
              <span v-if="msg.self && msg.status === 'failed'" class="failed-hint" @click="retryMessage(msg)">
                <el-icon><WarningFilled /></el-icon> 发送失败，点击重试
              </span>
              <span class="msg-time">{{ msg.time }}</span>
            </div>
          </div>
          <el-avatar v-if="msg.self" :size="32" :src="myAvatar" />
        </div>
      </div>
      <div v-else class="chat-empty">
        <el-empty description="选择联系人开始沟通" :image-size="compact ? 80 : 120" />
      </div>

      <div class="chat-input" v-if="currentContact && !selectedAnn">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="compact ? 2 : 3"
          placeholder="输入消息..."
          :disabled="sendingDisabled"
          @keyup.enter.prevent="handleSend"
        />
        <el-button type="primary" @click="handleSend" class="send-btn" :disabled="sendingDisabled || !inputText.trim()">发送</el-button>
      </div>
    </div>

    <el-dialog v-model="showNewConvDialog" title="发起会话" width="420px" top="10vh" destroy-on-close>
      <el-input
        v-model="userSearchKeyword"
        placeholder="输入用户名或手机号搜索..."
        size="large"
        clearable
        @input="searchUsers"
      />
      <div class="user-search-results" v-if="userSearchResults.length > 0">
        <div
          v-for="u in userSearchResults"
          :key="u.id"
          class="user-search-item"
          @click="startConversation(u)"
        >
          <el-avatar :size="36" :src="u.avatarUrl">{{ (u.nickname || u.username)?.charAt(0) }}</el-avatar>
          <div class="user-search-info">
            <div class="user-search-name">{{ u.nickname || u.username }}
              <el-tag size="small" :type="u.role === 1 ? 'warning' : u.role === 2 ? 'primary' : ''">
                {{ u.role === 0 ? '求职者' : u.role === 1 ? 'HR' : u.role === 2 ? '管理员' : '未知' }}
              </el-tag>
            </div>
            <div class="user-search-extra">{{ u.phone || '-' }} · {{ u.realname || '未实名' }}</div>
          </div>
        </div>
      </div>
      <el-empty v-else-if="userSearched" description="未找到用户" :image-size="60" />
      <template #footer>
        <el-button @click="showNewConvDialog = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { WarningFilled, Bell, Refresh, Plus, Delete, ArrowDown, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessages, getConversations, createConversation, deleteConversation } from '@/api/chat'
import { getUsers } from '@/api/admin/users'
import { getLatestAnnouncements } from '@/api/announcements'
import { getStompClient, subscribe, subscribeConv, subscribeVcode, sendMessage } from '@/services/stomp'
import { getCachedConversations, setConversations, getCachedMessages, setMessages, appendMessage, recomputeUnread } from '@/services/chatCache'

const props = defineProps({
  compact: { type: Boolean, default: false },
  targetConversationId: { type: Number, default: null },
})

const currentContact = ref(null)
const inputText = ref('')
const contactList = ref([])
const currentMessages = ref([])
const chatBodyRef = ref(null)
const myAvatar = ref('')

let unsubWs = null
let unsubConv = null
let unsubVcode = null
let sending = false
const sendingDisabled = ref(false)
const fetchingSet = new Set()
const announcementList = ref([])
const selectedAnn = ref(null)
const annCollapsed = ref(false)
const refreshing = ref(false)

const loginUser = JSON.parse(localStorage.getItem('loginUser') || '{}')
const isAdmin = loginUser.role === 2 || loginUser.role === 3

const showNewConvDialog = ref(false)
const userSearchKeyword = ref('')
const userSearchResults = ref([])
const userSearched = ref(false)
let searchTimer = null

const searchUsers = () => {
  clearTimeout(searchTimer)
  const kw = userSearchKeyword.value.trim()
  if (!kw) {
    userSearchResults.value = []
    userSearched.value = false
    return
  }
  searchTimer = setTimeout(async () => {
    try {
      const res = await getUsers({ page: 1, size: 20, keyword: kw })
      if (res.code === 1 && res.data) {
        const list = Array.isArray(res.data) ? res.data : (res.data.list || [])
        // 排除自己
        userSearchResults.value = list.filter(u => u.id !== loginUser.userId)
        userSearched.value = true
      }
    } catch {
      userSearchResults.value = []
      userSearched.value = true
    }
  }, 300)
}

const startConversation = async (user) => {
  try {
    const res = await createConversation(user.id)
    if (res.code === 1 && res.data) {
      showNewConvDialog.value = false
      userSearchKeyword.value = ''
      userSearchResults.value = []
      userSearched.value = false
      const conv = res.data
      const contact = {
        id: conv.id,
        name: conv.targetNickname || user.nickname || user.username || '-',
        avatar: conv.targetAvatar || user.avatarUrl || '',
        lastMessage: conv.lastMessage || '',
        time: fmtTime(conv.lastMessageTime),
        unread: 0,
        receiverId: conv.targetUserId,
        lastMessageTime: conv.lastMessageTime,
      }
      const exists = contactList.value.find(c => c.id === contact.id)
      if (!exists) {
        contactList.value.unshift(contact)
        setConversations([...contactList.value])
      }
      selectContact(contact)
    } else {
      ElMessage.error(res.msg || '发起会话失败')
    }
  } catch {
    ElMessage.error('发起会话失败')
  }
}

const selectAnnouncement = (ann) => {
  if (selectedAnn.value?.id === ann.id) {
    selectedAnn.value = null
    return
  }
  selectedAnn.value = ann
  currentContact.value = null
}

try {
  const u = JSON.parse(localStorage.getItem('loginUser'))
  myAvatar.value = u?.avatarUrl || ''
} catch {}

const unreadCount = computed(() => contactList.value.filter((c) => c.unread > 0).length)

const sortedAnnouncements = computed(() =>
  [...announcementList.value].sort((a, b) => {
    if (a.pinned !== b.pinned) return a.pinned ? -1 : 1
    return new Date(b.sentAt) - new Date(a.sentAt)
  })
)

const fmtTime = (dt) => {
  if (!dt) return ''
  const d = new Date(dt)
  const now = new Date()
  const isToday = d.toDateString() === now.toDateString()
  if (isToday) return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (d.toDateString() === yesterday.toDateString()) return '昨天'
  return `${d.getMonth() + 1}/${d.getDate()}`
}

const scrollBottom = () => {
  nextTick(() => {
    const el = chatBodyRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

const sortConversations = () => {
  contactList.value.sort((a, b) => new Date(b.lastMessageTime || 0) - new Date(a.lastMessageTime || 0))
}

const processConvList = (list) => {
  const activeId = currentContact.value?.id
  const activeItem = activeId ? contactList.value.find((c) => c.id === activeId) : null

  contactList.value = list.map((c) => ({
    ...c,
    id: c.id,
    name: c.targetNickname || '-',
    avatar: c.targetAvatar || '',
    lastMessage: c.lastMessage || '',
    time: fmtTime(c.lastMessageTime),
    unread: c.unreadCount || 0,
    receiverId: c.targetUserId,
    lastMessageTime: c.lastMessageTime,
  })).sort((a, b) => new Date(b.lastMessageTime || 0) - new Date(a.lastMessageTime || 0))

  if (activeItem && activeItem.lastMessageTime) {
    const wsItem = contactList.value.find((c) => c.id === activeId)
    if (wsItem) {
      const wsTime = new Date(wsItem.lastMessageTime || 0).getTime()
      const localTime = new Date(activeItem.lastMessageTime || 0).getTime()
      if (localTime > wsTime) {
        wsItem.lastMessage = activeItem.lastMessage
        wsItem.time = activeItem.time
        wsItem.lastMessageTime = activeItem.lastMessageTime
      }
    }
  }

  setConversations(contactList.value)
}

const selectContact = async (contact, forceRefresh = false) => {
  selectedAnn.value = null
  currentContact.value = contact
  contact.unread = 0
  recomputeUnread()
  const idx = contactList.value.findIndex(c => c.id === contact.id)
  if (idx !== -1) {
    contactList.value.splice(idx, 1)
    contactList.value.unshift(contact)
    setConversations([...contactList.value])
  }
  const cached = getCachedMessages(contact.id)
  if (!forceRefresh && cached != null) {
    currentMessages.value = cached
    scrollBottom()
    return
  }
  if (fetchingSet.has(contact.id)) return
  fetchingSet.add(contact.id)
  try {
    const res = await getMessages(contact.id, 1, 50)
    if (res.code === 1 && res.data) {
      const list = Array.isArray(res.data) ? res.data : (res.data.records || [])
      const uid = JSON.parse(localStorage.getItem('loginUser'))?.userId
      const msgs = list.map((m) => ({
        id: m.id,
        content: m.content,
        self: m.senderId === uid,
        time: fmtTime(m.sentAt || m.createdAt),
      }))
      setMessages(contact.id, msgs)
      if (currentContact.value?.id === contact.id) {
        currentMessages.value = msgs
      }
    }
  } catch {} finally {
    fetchingSet.delete(contact.id)
  }
  scrollBottom()
}

let lastSend = 0

const handleDeleteConversation = (conv) => {
  ElMessageBox.confirm('确定要删除该会话吗？删除后聊天记录也将一并清除。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteConversation(conv.id)
      contactList.value = contactList.value.filter(c => c.id !== conv.id)
      if (currentContact.value?.id === conv.id) {
        currentContact.value = null
        currentMessages.value = []
      }
      setConversations([...contactList.value])
      ElMessage.success('会话已删除')
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleSend = () => {
  const ts = Date.now()
  if (ts - lastSend < 500) return
  lastSend = ts

  const text = inputText.value.trim()
  if (!text || sending) return
  const contact = currentContact.value
  if (!contact) return

  sending = true
  sendingDisabled.value = true

  const tempId = 'temp_' + Date.now()

  sendMessage('/app/chat.send', {
    conversationId: contact.id,
    receiverId: contact.receiverId,
    content: text,
    msgType: 0,
    clientMsgId: tempId,
  })

  const now = new Date()
  const time = fmtTime(now.toISOString())
  const newMsg = { id: tempId, content: text, self: true, time }
  const prevCache = getCachedMessages(contact.id) || []
  const updated = [...prevCache, newMsg]
  setMessages(contact.id, updated)
  currentMessages.value = [...currentMessages.value, newMsg]
  contact.lastMessage = text
  contact.time = time
  contact.lastMessageTime = now.toISOString()
  sortConversations()
  setConversations([...contactList.value])
  inputText.value = ''
  scrollBottom()

  setTimeout(() => {
    sending = false
    sendingDisabled.value = false
  }, 800)
}

const retryMessage = (msg) => {
  const contact = currentContact.value
  if (!contact) return

  const cached = getCachedMessages(contact.id)
  if (!cached) return
  const idx = cached.findIndex((m) => m.id === msg.id)
  const updated = [...cached]
  updated.splice(idx, 1)
  setMessages(contact.id, updated)
  if (currentContact.value?.id === contact.id) {
    currentMessages.value = [...updated]
  }

  inputText.value = msg.content
  handleSend()
}

const handleIncoming = (msg) => {
  if (msg.type === 'CHAT_ERROR') {
    const convId = msg.conversationId
    const cached = getCachedMessages(convId)
    if (cached) {
      const idx = cached.findIndex((m) => m.id === msg.clientMsgId && !m.status)
      if (idx !== -1) {
        cached[idx] = { ...cached[idx], status: 'failed' }
        setMessages(convId, [...cached])
        if (currentContact.value?.id === convId) {
          currentMessages.value = [...cached]
        }
      }
    }
    return
  }

  const uid = JSON.parse(localStorage.getItem('loginUser'))?.userId
  const isSelf = String(msg.senderId) === String(uid)
  if (isSelf) return

  const convId = msg.conversationId
  const conv = contactList.value.find((c) => c.id === convId)
  const formatted = {
    id: msg.id,
    content: msg.content,
    self: false,
    time: fmtTime(msg.sentAt || msg.createdAt),
  }

  const appended = appendMessage(convId, formatted)

  if (currentContact.value?.id === convId && appended) {
    currentMessages.value.push(formatted)
    scrollBottom()
  }

  if (conv) {
    if (appended) {
      conv.lastMessage = msg.content
      conv.time = fmtTime(msg.sentAt || msg.createdAt)
      conv.lastMessageTime = msg.sentAt || msg.createdAt
      sortConversations()
    }
    if (currentContact.value?.id !== convId && appended) {
      conv.unread = (conv.unread || 0) + 1
      recomputeUnread()
    }
  }
}

const refreshAll = async () => {
  refreshing.value = true
  try {
    getLatestAnnouncements(5).then(res => {
      if (res.code === 1) announcementList.value = res.data || []
    }).catch(() => {})

    const res = await getConversations()
    if (res.code === 1 && res.data) {
      const list = Array.isArray(res.data) ? res.data : (res.data.records || [])
      processConvList(list)
    }

    const contact = currentContact.value
    if (contact) {
      fetchingSet.delete(contact.id)
      await selectContact(contact, true)
    }
  } catch {} finally {
    refreshing.value = false
  }
}

const trySelectTarget = async () => {
  if (props.targetConversationId && !currentContact.value) {
    let conv = contactList.value.find(c => c.id === props.targetConversationId)
    if (!conv) {
      await refreshAll()
      conv = contactList.value.find(c => c.id === props.targetConversationId)
    }
    if (conv) selectContact(conv)
  }
}

watch(() => props.targetConversationId, async (newId) => {
  if (!newId) return
  if (currentContact.value?.id === newId) return
  const existing = contactList.value.find(c => c.id === newId)
  if (existing) {
    selectContact(existing)
    return
  }
  await refreshAll()
  const found = contactList.value.find(c => c.id === newId)
  if (found) selectContact(found)
})

onMounted(async () => {
  getStompClient()

  getLatestAnnouncements(5).then(res => {
    if (res.code === 1) announcementList.value = res.data || []
  }).catch(() => {})

  unsubWs = subscribe(handleIncoming)
  unsubVcode = subscribeVcode((data) => {
    ElMessage.info('验证码通知：' + data.phone + ' 验证码 ' + data.code + '（5分钟有效）')
  })
  unsubConv = subscribeConv((data) => {
    const list = Array.isArray(data) ? data : (data.records || [])
    if (list.length > 0) {
      processConvList(list)
      trySelectTarget()
    }
  })

  const cached = getCachedConversations()
  if (cached?.length > 0) {
    contactList.value = cached
    trySelectTarget()
  }
})

onUnmounted(() => {
  unsubWs?.()
  unsubConv?.()
  unsubVcode?.()
})
</script>

<style scoped>
.chat-panel { display: flex; height: 500px; gap: 0; }
.chat-panel.compact { height: 450px; }

.contact-list { width: 280px; border-right: 1px solid #f0f2f5; display: flex; flex-direction: column; overflow: hidden; flex-shrink: 0; }
.list-header { padding: 16px; font-weight: 600; font-size: 15px; border-bottom: 1px solid #f0f2f5; display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.list-header .el-button { margin-left: auto; }
.contact-scroll { flex: 1; overflow-y: auto; min-height: 0; }
.contact-item { display: flex; align-items: center; gap: 12px; padding: 12px 16px; cursor: pointer; transition: background 0.2s; }
.contact-item:hover, .contact-item.active { background: #f0f5ff; }
.contact-info { flex: 1; overflow: hidden; }
.contact-name { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 4px; }
.contact-preview { font-size: 12px; color: #909399; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.contact-time { font-size: 11px; color: #c0c4cc; flex-shrink: 0; }

.chat-area { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.chat-header { padding: 12px 16px; font-weight: 600; border-bottom: 1px solid #f0f2f5; display: flex; align-items: center; gap: 8px; }
.chat-body { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 16px; }
.chat-empty { flex: 1; display: flex; align-items: center; justify-content: center; }
.message-item { display: flex; align-items: flex-start; gap: 10px; }
.message-item.self { flex-direction: row-reverse; }
.message-bubble { max-width: 65%; padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.6; background: #f0f2f5; color: #303133; word-break: break-word; }
.message-bubble.self { background: #1677ff; color: #fff; }
.message-bubble.self.failed { background: #ff4d4f; }
.msg-meta { display: flex; align-items: center; gap: 6px; margin-top: 4px; }
.msg-time { font-size: 10px; opacity: 0.6; }
.message-item.self .msg-meta { justify-content: flex-end; }
.failed-hint { font-size: 10px; opacity: 0.9; color: #fff; cursor: pointer; display: inline-flex; align-items: center; gap: 2px; text-decoration: underline; }
.chat-input { padding: 12px 16px; border-top: 1px solid #f0f2f5; display: flex; gap: 10px; align-items: flex-end; }
.send-btn { flex-shrink: 0; }
.placeholder { color: #909399; font-weight: 400; }

.ann-section { border-bottom: 1px solid #f0f2f5; padding-bottom: 6px; }
.ann-header { display: flex; align-items: center; gap: 6px; padding: 10px 16px 6px; font-size: 13px; font-weight: 600; color: #e6a23c; cursor: pointer; user-select: none; }
.ann-collapse-icon { margin-left: auto; font-size: 12px; color: #c0c4cc; transition: transform 0.2s; }
.ann-item { padding: 6px 16px; cursor: pointer; transition: background 0.2s; }
.ann-item:hover { background: #fef8e8; }
.ann-item.active { background: #fef8e8; }
.ann-item.pinned { background: #fdf6ec; }
.ann-title { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #606266; overflow: hidden; }
.ann-title span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ann-title .el-icon { color: #e6a23c; flex-shrink: 0; }
.pin-tag { flex-shrink: 0; }

.contact-delete-btn { opacity: 0; transition: opacity 0.2s; flex-shrink: 0; color: #909399; }
.contact-item:hover .contact-delete-btn { opacity: 1; }
.contact-delete-btn:hover { color: #f56c6c !important; }

.ann-detail { flex: 1; overflow-y: auto; padding: 24px; }
.ann-detail-meta { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #ebeef5; font-size: 13px; color: #909399; }
.ann-detail-content { font-size: 15px; color: #303133; line-height: 1.8; white-space: pre-wrap; word-break: break-word; }

.user-search-results { margin-top: 12px; max-height: 360px; overflow-y: auto; }
.user-search-item { display: flex; align-items: center; gap: 12px; padding: 10px 12px; cursor: pointer; border-radius: 8px; transition: background 0.2s; }
.user-search-item:hover { background: #f0f5ff; }
.user-search-info { flex: 1; }
.user-search-name { font-size: 14px; font-weight: 600; color: #303133; display: flex; align-items: center; gap: 6px; }
.user-search-extra { font-size: 12px; color: #909399; margin-top: 2px; }
</style>
