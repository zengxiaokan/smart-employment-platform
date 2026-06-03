import request from '@/utils/request'

/**
 * 获取当前用户的会话列表
 */
export function getConversations() {
  return request({ url: '/chat/conversations', method: 'get' })
}

/**
 * 获取某个会话的历史消息
 * @param {number} conversationId - 会话ID
 * @param {number} page - 页码
 * @param {number} size - 每页条数
 */
export function getMessages(conversationId, page = 1, size = 50) {
  return request({ url: `/chat/conversations/${conversationId}/messages`, method: 'get', params: { page, size } })
}

export function createConversation(targetUserId) {
  return request({ url: '/chat/conversations', method: 'post', data: { targetUserId } })
}

export function deleteConversation(conversationId) {
  return request({ url: `/chat/conversations/${conversationId}`, method: 'delete' })
}
