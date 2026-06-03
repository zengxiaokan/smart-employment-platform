import { getStompClient, subscribeConv, subscribe, isConnected } from '@/services/stomp'
import { getConversations } from '@/api/chat'
import { setConversations, isConversationsFetched, getCachedConversations, clearChatCache } from '@/services/chatCache'
import { notifyMessage, requestNotificationPermission } from '@/utils/notify'

let initialized = false
let unsubConv = null
let unsubChat = null

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

export async function initChat() {
  if (initialized) return
  initialized = true

  getStompClient()
  requestNotificationPermission()

  unsubChat = subscribe((msg) => {
    if (!msg || msg.type === 'CHAT_ERROR') return

    const uid = (() => {
      try { return JSON.parse(localStorage.getItem('loginUser'))?.userId } catch { return null }
    })()
    if (String(msg.senderId) === String(uid)) return

    const convs = getCachedConversations() || []
    const conv = convs.find((c) => c.id === msg.conversationId)

    notifyMessage({
      senderName: conv?.name || '对方',
      content: msg.content,
    })
  })

  unsubConv = subscribeConv((data) => {
    const list = Array.isArray(data) ? data : (data.records || [])
    if (list.length > 0) {
      const processed = list.map((c) => ({
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
      setConversations(processed)
    }
  })

  await fetchAndCacheConversations()
}

export function resetChat() {
  initialized = false
  if (unsubChat) { unsubChat(); unsubChat = null }
  if (unsubConv) { unsubConv(); unsubConv = null }
  clearChatCache()
}

async function fetchAndCacheConversations() {
  if (isConversationsFetched()) return
  try {
    const res = await getConversations()
    if (res.code === 1 && res.data) {
      const list = Array.isArray(res.data) ? res.data : (res.data.records || [])
      const processed = list.map((c) => ({
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
      setConversations(processed)
    }
  } catch {}
}