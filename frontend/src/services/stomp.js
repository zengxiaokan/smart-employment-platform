import { Client } from '@stomp/stompjs'

// 用 window 持久化，防止 Vite HMR 热更新时创建重复连接
const WIN_KEY = '__stomp_state__'

function getState() {
  if (!window[WIN_KEY]) {
    window[WIN_KEY] = {
      client: null,
      connected: false,
      connecting: false,
      connectedToken: '',
      chatSub: null,
      convSub: null,
      vcodeSub: null,
      subscribers: new Map(),
      convSubscribers: new Map(),
      vcodeSubscribers: new Map(),
    }
  }
  return window[WIN_KEY]
}

function getToken() {
  try {
    const u = JSON.parse(localStorage.getItem('loginUser'))
    return u?.token || ''
  } catch {
    return ''
  }
}

function getBrokerURL() {
  const token = getToken()
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/api/ws?token=${token}`
}

export function getStompClient() {
  const state = getState()
  const currentToken = getToken()

  // token 变了（切换账号），断开旧连接后重建
  if (state.client && state.connected && state.connectedToken && state.connectedToken !== currentToken) {
    try { state.client.deactivate() } catch {}
    state.client = null
    state.connected = false
    state.connectedToken = ''
    state.chatSub = null
    state.convSub = null
    state.vcodeSub = null
  }

  // 已连接，直接复用
  if (state.client && state.connected) return state.client

  // 正在连接中，等待同一个 client 连上，不要新建
  if (state.client && state.connecting) return state.client

  // 旧 client 已断线（非连接中状态），清理后重建
  if (state.client) {
    try { state.client.deactivate() } catch {}
    state.client = null
  }

  state.connecting = true

  state.client = new Client({
    brokerURL: getBrokerURL(),
    connectHeaders: {
      token: currentToken,
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: () => {
      state.connected = true
      state.connecting = false
      state.connectedToken = currentToken
      if (!state.chatSub) {
        state.chatSub = state.client.subscribe('/user/queue/chat', (msg) => {
          try {
            const data = JSON.parse(msg.body)
            state.subscribers.forEach((fn) => fn(data))
          } catch {}
        })
      }
      if (!state.convSub) {
        state.convSub = state.client.subscribe('/user/queue/conversations', (msg) => {
          try {
            const data = JSON.parse(msg.body)
            state.convSubscribers.forEach((fn) => fn(data))
          } catch {}
        })
      }
      if (!state.vcodeSub) {
        state.vcodeSub = state.client.subscribe('/user/queue/verification-code', (msg) => {
          try {
            const data = JSON.parse(msg.body)
            state.vcodeSubscribers.forEach((fn) => fn(data))
          } catch {}
        })
      }
    },
    onDisconnect: () => {
      state.connected = false
      state.connecting = false
      state.connectedToken = ''
    },
    onStompError: () => {
      state.connected = false
      state.connecting = false
      state.connectedToken = ''
    },
  })

  state.client.activate()
  return state.client
}

export function isConnected() {
  return getState().connected
}

export function subscribe(fn) {
  const state = getState()
  const id = Symbol('sub')
  state.subscribers.set(id, fn)
  return () => { state.subscribers.delete(id) }
}

export function subscribeConv(fn) {
  const state = getState()
  const id = Symbol('conv')
  state.convSubscribers.set(id, fn)
  return () => { state.convSubscribers.delete(id) }
}

export function subscribeVcode(fn) {
  const state = getState()
  const id = Symbol('vcode')
  state.vcodeSubscribers.set(id, fn)
  return () => { state.vcodeSubscribers.delete(id) }
}

export function sendMessage(destination, body) {
  const state = getState()
  if (!state.client || !state.connected) {
    console.warn('STOMP not connected')
    return
  }
  state.client.publish({ destination, body: JSON.stringify(body) })
}

export function disconnectStomp() {
  const state = getState()
  if (state.client) {
    try { state.client.deactivate() } catch {}
  }
  state.client = null
  state.connected = false
  state.connecting = false
  state.connectedToken = ''
  state.chatSub = null
  state.convSub = null
  state.vcodeSub = null
  state.subscribers.clear()
  state.convSubscribers.clear()
  state.vcodeSubscribers.clear()
}
