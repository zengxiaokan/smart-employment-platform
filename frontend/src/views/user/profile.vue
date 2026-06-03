<template>
  <div class="profile-root">
    <div class="mobile-toggle" @click="sidebarOpen = !sidebarOpen">
      <el-icon :size="20"><component :is="sidebarOpen ? 'Fold' : 'Expand'" /></el-icon>
      <span>{{ currentMenuLabel }}</span>
    </div>
    <div v-if="sidebarOpen" class="mobile-overlay" @click="sidebarOpen = false"></div>

    <div class="profile-body">
      <aside class="side" :class="{ open: sidebarOpen }">
        <div class="user-block">
          <div class="avatar-wrap" @click="triggerUpload">
            <el-avatar :size="68" :src="userInfo.avatarUrl || ''" :icon="UserFilled"
              style="background: linear-gradient(145deg, #34d399, #059669);" />
            <span class="camera-dot" v-if="!userInfo.avatarUrl"><el-icon :size="12"><Camera /></el-icon></span>
          </div>
          <input ref="fileInput" type="file" accept="image/*" hidden @change="onUpload" />
          <div class="nick">{{ userInfo.nickname }}</div>
          <el-tag size="small" effect="plain" round type="success">求职者</el-tag>
          <div class="stats">
            <div class="st"><strong>{{ stats.applications }}</strong><em>投递</em></div>
            <i></i>
            <div class="st"><strong>{{ stats.favorites }}</strong><em>收藏</em></div>
            <i></i>
            <div class="st"><strong>{{ stats.interviews }}</strong><em>面试</em></div>
          </div>
        </div>

        <nav class="nav">
          <div
            v-for="m in menus"
            :key="m.key"
            class="nav-link"
            :class="{ on: active === m.key }"
            @click="switchTab(m.key)"
          >
            <el-icon class="nav-ico"><component :is="m.icon" /></el-icon>
            <span class="nav-txt">{{ m.label }}</span>
          </div>
        </nav>

        <div class="side-foot">
          <div class="logout" @click="doLogout">
            <el-icon><SwitchButton /></el-icon><span>退出登录</span>
          </div>
        </div>
      </aside>

      <main class="main">
        <div class="bread">
          <el-breadcrumb separator="›">
            <el-breadcrumb-item :to="{ path: '/user' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>个人中心</el-breadcrumb-item>
            <el-breadcrumb-item class="current">{{ currentMenuLabel }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="card">
          <component :is="activeComponent" />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, SwitchButton, Camera } from '@element-plus/icons-vue'
import { uploadAvatar, getUserStats } from '@/api/user/user'
import { useUser } from '@/utils/useUser'
import { disconnectStomp } from '@/services/stomp.js'
import { resetChat } from '@/services/chatInit'
import ProfileEdit from './profile/ProfileEdit.vue'
import MyResume from './profile/MyResume.vue'
import JobApplications from './profile/JobApplications.vue'
import FavoriteJobs from './profile/FavoriteJobs.vue'
import InterviewNotice from './profile/InterviewNotice.vue'
import ChangePassword from './profile/ChangePassword.vue'
import JobIntention from './profile/JobIntention.vue'
import FeedbackView from './profile/FeedbackView.vue'
import HelpGuide from './profile/HelpGuide.vue'

const router = useRouter()
const { userInfo, updateAvatar, updateNickname } = useUser()

const fileInput = ref(null)
const sidebarOpen = ref(false)

const menus = [
  { key: 'profile-edit',  label: '个人资料编辑', icon: 'User' },
  { key: 'job-applications', label: '岗位投递记录', icon: 'List' },
  { key: 'job-intention',  label: '求职意向表',   icon: 'Aim' },
  { key: 'my-resume',       label: '我的简历',     icon: 'Document' },
  { key: 'favorite-jobs',   label: '收藏岗位',     icon: 'Star' },
  { key: 'interview-notice',label: '面试通知',     icon: 'Bell' },
  { key: 'feedback',       label: '意见反馈',     icon: 'ChatDotRound' },
  { key: 'change-pwd-user', label: '修改密码',     icon: 'Lock' },
  { key: 'help-guide',      label: '使用帮助',     icon: 'Notebook' },
]

const map = {
  'profile-edit':    ProfileEdit,
  'my-resume':       MyResume,
  'job-intention':   JobIntention,
  'job-applications':JobApplications,
  'favorite-jobs':   FavoriteJobs,
  'interview-notice':InterviewNotice,
  'feedback':       FeedbackView,
  'change-pwd-user': ChangePassword,
  'help-guide':      HelpGuide,
}

const active = ref('')
const activeComponent = computed(() => map[active.value] || null)
const currentMenuLabel = computed(() => {
  const m = menus.find(m => m.key === active.value)
  return m ? m.label : '个人中心'
})

/* ----- 头像 ----- */
const triggerUpload = () => {
  ElMessageBox.confirm('更换头像？', '提示', { confirmButtonText:'确定', cancelButtonText:'取消', type:'info' })
    .then(() => fileInput.value?.click()).catch(()=>{})
}

const onUpload = async (e) => {
  const f = e.target.files[0]
  if (!f) return
  if (!f.type.startsWith('image/')) return ElMessage.error('仅支持图片')
  if (f.size/1024/1024 > 2) return ElMessage.error('图片不超过2MB')
  const fd = new FormData(); fd.append('file',f)
  try {
    const r = await uploadAvatar(fd)
    if (r.code === 1) { updateAvatar(r.data); ElMessage.success('头像已更新') }
    else ElMessage.error(r.msg||'上传失败')
  } catch { ElMessage.error('网络异常') }
  e.target.value = ''
}

/* ----- 导航 ----- */
const switchTab = (k) => {
  if (k === 'change-pwd-user') {
    ElMessageBox.confirm('确认进入修改密码？','安全确认',{confirmButtonText:'确定',cancelButtonText:'取消',type:'warning'})
      .then(()=>{ active.value = k; if (innerWidth<768) sidebarOpen.value = false }).catch(()=>{})
    return
  }
  active.value = k
  if (window.innerWidth < 768) sidebarOpen.value = false
  fetchStats()
}

const doLogout = () => {
  ElMessageBox.confirm('确认退出？','提示',{confirmButtonText:'确定',cancelButtonText:'取消',type:'warning'})
    .then(()=>{ disconnectStomp(); resetChat(); localStorage.clear(); router.push('/login'); ElMessage.success('已退出') }).catch(()=>{})
}

const stats = ref({ favorites: 0, applications: 0, interviews: 0 })

const fetchStats = async () => {
  try {
    const res = await getUserStats()
    if (res.code === 1 && res.data) {
      stats.value = res.data
    }
  } catch { /* ignore */ }
}

onMounted(() => {
  active.value = menus[0].key
  window.addEventListener('switch-menu', e => active.value = e.detail)
  fetchStats()
})
</script>

<style scoped>
.profile-root { min-height:100vh; background:#f4f7fa; font-family:'Inter','PingFang SC','Microsoft YaHei',sans-serif; }

/* mobile */
.mobile-toggle { display:none; align-items:center; gap:10px; padding:14px 20px; background:#fff; font-size:15px; font-weight:600; color:#334155; cursor:pointer; border-bottom:1px solid #edf2f7; }
.mobile-overlay { display:none; position:fixed; inset:0; background:rgba(15,23,42,.35); z-index:90; }

/* layout */
.profile-body { display:flex; min-height:100vh; max-width:1400px; margin:0 auto; }

/* ====== SIDEBAR ====== */
.side {
  width:270px; min-width:270px; background:#fff; display:flex; flex-direction:column;
  border-right:1px solid #edf2f7; transition:transform .3s; z-index:100;
}
.user-block { text-align:center; padding:36px 20px 24px; border-bottom:1px solid #f1f5f9; }
.avatar-wrap { display:inline-block; position:relative; cursor:pointer; margin-bottom:12px; }
.avatar-wrap :deep(.el-avatar) { transition:transform .25s; }
.avatar-wrap:hover :deep(.el-avatar) { transform:scale(1.06); }
.camera-dot {
  position:absolute; bottom:0; right:0; width:24px; height:24px; border-radius:50%;
  background:linear-gradient(145deg,#34d399,#059669); color:#fff; display:flex; align-items:center;
  justify-content:center; border:2px solid #fff; box-shadow:0 2px 8px rgba(16,185,129,.25);
}
.nick { font-size:16px; font-weight:700; color:#0f172a; margin-bottom:8px; }
.stats { display:flex; align-items:center; justify-content:center; background:#f8fafc; border-radius:12px; padding:14px 0; margin-top:18px; }
.st { flex:1; display:flex; flex-direction:column; gap:2px; }
.st strong { font-size:18px; color:#0f172a; }
.st em { font-size:11px; color:#94a3b8; font-style:normal; }
.stats i { width:1px; height:26px; background:#e2e8f0; }

.nav { flex:1; padding:12px 12px; overflow-y:auto; }
.nav-link {
  display:flex; align-items:center; gap:12px; padding:11px 16px; border-radius:10px;
  cursor:pointer; font-size:14px; color:#475569; transition:all .2s; margin-bottom:2px; font-weight:500;
}
.nav-link:hover { background:#f8fafc; color:#0f172a; }
.nav-link.on { background:#ecfdf5; color:#059669; font-weight:600; }
.nav-ico { font-size:18px; flex-shrink:0; }
.nav-txt { white-space:nowrap; }

.side-foot { padding:16px 20px; border-top:1px solid #f1f5f9; }
.logout { display:flex; align-items:center; justify-content:center; gap:8px; padding:10px; border-radius:10px;
  cursor:pointer; font-size:14px; color:#94a3b8; transition:all .2s; }
.logout:hover { background:#fef2f2; color:#ef4444; }

/* ====== MAIN ====== */
.main { flex:1; padding:32px 36px; overflow-y:auto; min-width:0; }
.bread { margin-bottom:24px; }
.bread :deep(.el-breadcrumb__item) { font-size:13px; }
.bread :deep(.el-breadcrumb__separator) { color:#cbd5e1; }
.bread :deep(.el-breadcrumb__inner) { color:#64748b; }
.current :deep(.el-breadcrumb__inner) { color:#059669; font-weight:600; }

.card { background:#fff; border-radius:18px; padding:36px; box-shadow:0 1px 4px rgba(0,0,0,.03); min-height:calc(100vh - 180px); display:flex; flex-direction:column; }

@media (max-width:767px) {
  .mobile-toggle,.mobile-overlay { display:block; }
  .side { position:fixed; top:0; left:0; bottom:0; transform:translateX(-100%); }
  .side.open { transform:translateX(0); }
  .main { padding:20px 16px; }
  .card { padding:20px; border-radius:14px; min-height:auto; display:flex; flex-direction:column; }
}
@media (min-width:768px) and (max-width:1023px) {
  .side { width:220px; min-width:220px; }
  .main { padding:24px 20px; }
  .card { padding:28px; }
}
</style>
