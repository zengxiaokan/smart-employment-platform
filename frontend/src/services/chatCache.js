import { ref } from 'vue'

let conversations = null
let conversationsFetched = false
const messageMap = {}

export const totalUnread = ref(0)

export function recomputeUnread() {
  if (!conversations) {
    totalUnread.value = 0
    return
  }
  totalUnread.value = conversations.reduce((sum, c) => sum + (c.unread || 0), 0)
}

export function getCachedConversations() {
  return conversations
}

export function setConversations(list) {
  conversations = list
  conversationsFetched = true
  recomputeUnread()
}

export function isConversationsFetched() {
  return conversationsFetched
}

export function getCachedMessages(convId) {
  return messageMap[convId] || null
}

export function setMessages(convId, msgs) {
  messageMap[convId] = msgs
}

export function appendMessage(convId, msg) {
  if (!messageMap[convId]) {
    messageMap[convId] = []
  }
  const exists = messageMap[convId].some((m) => m.id === msg.id)
  if (!exists) {
    messageMap[convId].push(msg)
    return true
  }
  return false
}

export function clearChatCache() {
  conversations = null
  conversationsFetched = false
  for (const key of Object.keys(messageMap)) {
    delete messageMap[key]
  }
}