const SOUND_PATH = '/sounds/msg.mp3'
let audio = null
let audioUnlocked = false

function initAudio() {
  if (audio) return
  try {
    audio = new Audio(SOUND_PATH)
    audio.preload = 'auto'
    audio.volume = 0.6
    audio.load()
  } catch { /* 音频初始化失败 */ }
}

function unlockAudio() {
  if (audioUnlocked) return
  initAudio()
  if (!audio) return
  // 必须在用户手势中调用一次 play 来解锁 Web Audio API
  audio.muted = true
  audio.play().then(() => {
    audio.pause()
    audio.currentTime = 0
    audio.muted = false
    audioUnlocked = true
  }).catch(() => {})
}

// 在页面首次交互时解锁音频
;['click', 'keydown', 'touchstart'].forEach(evt => {
  document.addEventListener(evt, unlockAudio, { once: true })
})
// 备选：visibility change 时也尝试解锁（用户切回标签页）
document.addEventListener('visibilitychange', () => {
  if (!document.hidden) unlockAudio()
})

export function requestNotificationPermission() {
  if (!('Notification' in window)) return
  if (Notification.permission === 'default') {
    Notification.requestPermission()
  }
}

export function playMessageSound() {
  initAudio()
  if (!audio) return
  audio.currentTime = 0
  audio.play().catch(() => {})
}

export function notifyMessage({ senderName, content, onClick }) {
  if (!document.hidden) {
    playMessageSound()
    return
  }

  playMessageSound()

  if (!('Notification' in window)) return
  if (Notification.permission !== 'granted') return

  try {
    const n = new Notification(`${senderName} 发来消息`, {
      body: content || '[新消息]',
      icon: '/favicon.ico',
      tag: 'chat-message',
    })
    if (onClick) {
      n.onclick = () => {
        window.focus()
        onClick()
        n.close()
      }
    }
    setTimeout(() => n.close(), 5000)
  } catch {}
}