<template>
  <div class="aa-root">
    <div class="sub-hero">
      <span class="sub-hero-icon">⚙️</span>
      <div>
        <h2>个人中心</h2>
        <p>管理个人资料、系统设置及权限</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="aa-tabs">
      <!-- ============ 个人资料 ============ -->
      <el-tab-pane label="个人资料" name="profile">
        <div class="pe-form-wrap">
          <div class="pe-avatar-row">
            <el-avatar :size="72" :src="profile.avatarUrl">{{ profile.realname?.charAt(0) || profile.nickname?.charAt(0) || '管' }}</el-avatar>
            <div class="pe-avatar-info">
              <div class="pe-avatar-title">个人头像</div>
              <el-upload
                :show-file-list="false"
                :http-request="handleAvatarUpload"
                accept="image/*"
              >
                <el-button size="small" type="primary">
                  <el-icon><Upload /></el-icon> 更换头像
                </el-button>
              </el-upload>
              <div class="pe-avatar-tip">支持 JPG/PNG，&lt;2MB</div>
            </div>
          </div>
          <div class="pe-block">
            <h4 class="pe-label">基本信息</h4>
            <div class="pe-grid">
              <div class="pe-field">
                <label>用户名</label>
                <el-input v-model="profile.username" size="large" disabled />
              </div>
              <div class="pe-field">
                <label>真实姓名</label>
                <el-input v-model="profile.realname" placeholder="请输入姓名" size="large" />
              </div>
              <div class="pe-field">
                <label>昵称</label>
                <el-input v-model="profile.nickname" placeholder="请输入昵称" size="large" />
              </div>
              <div class="pe-field">
                <label>性别</label>
                <div class="pe-radio-group">
                  <span class="pe-radio" :class="{on:profile.gender===1}" @click="profile.gender=1">👨 男</span>
                  <span class="pe-radio" :class="{on:profile.gender===0}" @click="profile.gender=0">👩 女</span>
                </div>
              </div>
              <div class="pe-field">
                <label>年龄</label>
                <el-input-number v-model="profile.age" :min="16" :max="65" size="large" style="width:100%" controls-position="right" />
              </div>
            </div>
          </div>
          <div class="pe-block">
            <h4 class="pe-label">联系方式</h4>
            <div class="pe-grid">
              <div class="pe-field">
                <label>手机号</label>
                <el-input v-model="profile.phone" placeholder="手机号" size="large" />
              </div>
              <div class="pe-field">
                <label>邮箱</label>
                <el-input v-model="profile.email" placeholder="邮箱" size="large" />
              </div>
              <div class="pe-field">
                <label>所在城市</label>
                <el-input v-model="profile.city" placeholder="如：武汉" size="large" />
              </div>
            </div>
          </div>
          <div class="pe-actions">
            <el-button type="primary" size="large" :loading="profileSaving" @click="saveProfile">保存资料</el-button>
          </div>
        </div>
      </el-tab-pane>

      <!-- ============ 修改密码 ============ -->
      <el-tab-pane label="修改密码" name="password">
        <el-form :model="pwdForm" label-width="100px" class="pwd-form">
          <el-form-item label="原密码">
            <el-input v-model="pwdForm.oldPwd" type="password" show-password size="large" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwdForm.newPwd" type="password" show-password size="large" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="pwdForm.confirmPwd" type="password" show-password size="large" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="pwdSaving" @click="changePwd">确认修改</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- ============ 系统设置 ============ -->
      <el-tab-pane label="系统设置" name="settings">
        <el-tabs v-model="settingTab" type="border-card">
          <el-tab-pane label="基本设置" name="basic">
            <el-form :model="basicForm" label-width="120px" class="settings-form">
              <el-form-item label="系统名称">
                <el-input v-model="basicForm.systemName" />
              </el-form-item>
              <el-form-item label="系统Logo">
                <el-upload action="#" :show-file-list="false">
                  <el-button type="primary">上传Logo</el-button>
                </el-upload>
              </el-form-item>
              <el-form-item label="客服电话">
                <el-input v-model="basicForm.servicePhone" />
              </el-form-item>
              <el-form-item label="客服邮箱">
                <el-input v-model="basicForm.serviceEmail" />
              </el-form-item>
              <el-form-item label="系统公告">
                <el-input v-model="basicForm.announcement" type="textarea" :rows="3" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveBasic">保存设置</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="安全设置" name="security">
            <el-form :model="securityForm" label-width="140px" class="settings-form">
              <el-form-item label="登录失败锁定次数">
                <el-input-number v-model="securityForm.lockCount" :min="3" :max="10" />
              </el-form-item>
              <el-form-item label="锁定时间(分钟)">
                <el-input-number v-model="securityForm.lockTime" :min="5" :max="60" />
              </el-form-item>
              <el-form-item label="会话超时(分钟)">
                <el-input-number v-model="securityForm.sessionTimeout" :min="10" :max="120" />
              </el-form-item>
              <el-form-item label="密码最小长度">
                <el-input-number v-model="securityForm.passwordMinLen" :min="6" :max="20" />
              </el-form-item>
              <el-form-item label="强制密码复杂度">
                <el-switch v-model="securityForm.strongPassword" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveSecurity">保存设置</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="邮件设置" name="email">
            <el-form :model="emailForm" label-width="120px" class="settings-form">
              <el-form-item label="SMTP服务器">
                <el-input v-model="emailForm.smtpHost" />
              </el-form-item>
              <el-form-item label="SMTP端口">
                <el-input-number v-model="emailForm.smtpPort" :min="1" :max="65535" />
              </el-form-item>
              <el-form-item label="发件人邮箱">
                <el-input v-model="emailForm.fromEmail" />
              </el-form-item>
              <el-form-item label="邮箱密码">
                <el-input v-model="emailForm.password" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveEmail">保存设置</el-button>
                <el-button @click="testEmail">测试发送</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="公告管理" name="announcement">
            <div class="ann-section">
              <div class="ann-toolbar">
                <el-button type="primary" @click="openAnnCreate">发布公告</el-button>
              </div>
              <el-table :data="annList" border stripe v-loading="annLoading">
                <el-table-column prop="id" label="ID" width="70" />
                <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
                <el-table-column label="置顶" width="80" align="center">
                  <template #default="{ row }">
                    <el-tooltip :content="row.pinned ? '点击取消置顶' : '点击置顶'">
                      <el-button link @click="handleTogglePin(row)" :type="row.pinned ? 'warning' : 'info'">
                        <span style="font-size:18px">{{ row.pinned ? '📌' : '📍' }}</span>
                      </el-button>
                    </el-tooltip>
                  </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="发布时间" width="170" />
                <el-table-column label="操作" width="160" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="openAnnEdit(row)">编辑</el-button>
                    <el-button link type="danger" @click="handleDeleteAnn(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div class="pagination">
                <el-pagination
                  v-model:current-page="annPage"
                  v-model:page-size="annSize"
                  :page-sizes="[10, 20, 50]"
                  layout="total, sizes, prev, pager, next"
                  :total="annTotal"
                  @change="fetchAnnouncements"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-tab-pane>

      <!-- ============ 权限管理 ============ -->
      <el-tab-pane label="权限管理" name="permissions">
        <el-row :gutter="20">
          <el-col :span="10">
            <el-card class="role-card">
              <template #header>
                <div class="card-header">
                  <span>角色列表</span>
                  <el-button type="primary" size="small" :icon="Plus">新增角色</el-button>
                </div>
              </template>
              <el-table :data="roleList" highlight-current-row @current-change="handleRoleChange" stripe>
                <el-table-column prop="name" label="角色名称" />
                <el-table-column prop="description" label="描述" />
                <el-table-column prop="userCount" label="用户数" width="80" />
                <el-table-column label="操作" width="120">
                  <template #default="{ row }">
                    <el-button link type="primary" :icon="Edit" @click="handleEditRole(row)">编辑</el-button>
                    <el-button link type="danger" :icon="Delete" @click="handleDeleteRole(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
          <el-col :span="14">
            <el-card class="perm-card">
              <template #header>
                <div class="card-header">
                  <span>权限分配 - {{ currentRole ? currentRole.name : '请选择角色' }}</span>
                  <el-button type="primary" size="small" @click="savePermissions" :disabled="!currentRole">保存权限</el-button>
                </div>
              </template>
              <el-tree
                :data="permTree"
                show-checkbox
                node-key="id"
                default-expand-all
                :default-checked-keys="checkedKeys"
                ref="treeRef"
              />
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="annFormVisible" :title="annEditingId ? '编辑公告' : '发布公告'" width="560px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="annFormTitle" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="annFormContent" type="textarea" :rows="6" placeholder="请输入公告内容" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="annFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAnn">{{ annEditingId ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { Upload, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { getAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncement, togglePinAnnouncement } from '@/api/admin/announcements'

const activeTab = ref('profile')
const settingTab = ref('basic')

/* ====== 个人资料 ====== */
const profileSaving = ref(false)
const profile = reactive({
  username: '', realname: '', nickname: '', gender: 1, age: null,
  phone: '', email: '', city: '', avatarUrl: '',
})

const loadProfile = async () => {
  try {
    const res = await request({ url: '/user/profile', method: 'get' })
    if (res.code === 1 && res.data) {
      Object.assign(profile, res.data)
    }
  } catch {
    ElMessage.error('获取个人资料失败')
  }
}

const saveProfile = async () => {
  if (!profile.realname.trim()) { ElMessage.warning('请输入姓名'); return }
  profileSaving.value = true
  try {
    const res = await request({ url: '/user/profile', method: 'put', data: { ...profile } })
    if (res.code === 1) {
      const user = JSON.parse(localStorage.getItem('loginUser') || '{}')
      user.nickname = profile.nickname
      user.avatarUrl = profile.avatarUrl
      localStorage.setItem('loginUser', JSON.stringify(user))
      ElMessage.success('资料保存成功')
    } else { ElMessage.error(res.msg || '保存失败') }
  } catch { ElMessage.error('网络异常') }
  finally { profileSaving.value = false }
}

const handleAvatarUpload = async (options) => {
  const file = options.file
  if (!file.type.startsWith('image/')) { ElMessage.warning('只能上传图片文件'); return }
  if (file.size / 1024 / 1024 > 2) { ElMessage.warning('图片大小不能超过 2MB'); return }
  const fd = new FormData(); fd.append('file', file)
  try {
    const res = await request({ url: '/upload/avatar', method: 'post', data: fd, headers: { 'Content-Type': 'multipart/form-data' } })
    if (res.code === 1 && res.data) {
      profile.avatarUrl = res.data
      ElMessage.success('头像上传成功')
    } else ElMessage.error(res.msg || '上传失败')
  } catch { ElMessage.error('头像上传失败') }
}

/* ====== 修改密码 ====== */
const pwdSaving = ref(false)
const pwdForm = reactive({ oldPwd: '', newPwd: '', confirmPwd: '' })
const changePwd = async () => {
  if (pwdForm.newPwd !== pwdForm.confirmPwd) { ElMessage.error('两次输入的密码不一致'); return }
  pwdSaving.value = true
  try {
    const res = await request({ url: '/login/changepwd', method: 'post', data: { oldPassword: pwdForm.oldPwd, newPassword: pwdForm.newPwd } })
    if (res.code === 1) { ElMessage.success('密码修改成功'); pwdForm.oldPwd = ''; pwdForm.newPwd = ''; pwdForm.confirmPwd = '' }
    else ElMessage.error(res.msg || '修改失败')
  } catch { ElMessage.error('网络异常') }
  finally { pwdSaving.value = false }
}

/* ====== 系统设置 ====== */
const basicForm = reactive({
  systemName: '智能就业服务平台',
  servicePhone: '400-888-8888',
  serviceEmail: 'service@smartjob.com',
  announcement: '欢迎使用智能就业服务平台！',
})
const securityForm = reactive({
  lockCount: 5, lockTime: 30, sessionTimeout: 60,
  passwordMinLen: 8, strongPassword: true,
})
const emailForm = reactive({
  smtpHost: 'smtp.example.com', smtpPort: 465,
  fromEmail: 'noreply@smartjob.com', password: '',
})
const saveBasic = () => ElMessage.success('基本设置保存成功')
const saveSecurity = () => ElMessage.success('安全设置保存成功')
const saveEmail = () => ElMessage.success('邮件设置保存成功')
const testEmail = () => ElMessage.info('测试邮件已发送')

/* ====== 公告管理 ====== */
const annList = ref([])
const annLoading = ref(false)
const annPage = ref(1)
const annTotal = ref(0)
const annSize = ref(10)
const annFormVisible = ref(false)
const annFormTitle = ref('')
const annFormContent = ref('')
const annEditingId = ref(null)

const fetchAnnouncements = async () => {
  annLoading.value = true
  try {
    const res = await getAnnouncements({ page: annPage.value, size: annSize.value })
    if (res.code === 1 && res.data) {
      annList.value = res.data.list || []
      annTotal.value = res.data.total || 0
    }
  } catch {} finally { annLoading.value = false }
}

const openAnnCreate = () => {
  annEditingId.value = null
  annFormTitle.value = ''
  annFormContent.value = ''
  annFormVisible.value = true
}

const openAnnEdit = (row) => {
  annEditingId.value = row.id
  annFormTitle.value = row.title || ''
  annFormContent.value = row.content || ''
  annFormVisible.value = true
}

const saveAnn = async () => {
  if (!annFormTitle.value.trim()) { ElMessage.warning('请输入公告标题'); return }
  if (!annFormContent.value.trim()) { ElMessage.warning('请输入公告内容'); return }
  try {
    if (annEditingId.value) {
      await updateAnnouncement(annEditingId.value, { title: annFormTitle.value, content: annFormContent.value })
      ElMessage.success('已更新')
    } else {
      await createAnnouncement({ title: annFormTitle.value, content: annFormContent.value })
      ElMessage.success('已发布')
    }
    annFormVisible.value = false
    annPage.value = 1
    fetchAnnouncements()
  } catch { ElMessage.error('操作失败') }
}

const handleDeleteAnn = (row) => {
  ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteAnnouncement(row.id)
      ElMessage.success('已删除')
      fetchAnnouncements()
    } catch { ElMessage.error('删除失败') }
  }).catch(() => {})
}

const handleTogglePin = async (row) => {
  try {
    await togglePinAnnouncement(row.id, !row.pinned)
    row.pinned = !row.pinned
    ElMessage.success(row.pinned ? '已置顶' : '已取消置顶')
  } catch { ElMessage.error('操作失败') }
}

/* ====== 权限管理 ====== */
const treeRef = ref(null)
const currentRole = ref(null)
const checkedKeys = ref([])

const roleList = ref([
  { id: 1, name: '超级管理员', description: '拥有系统全部权限', userCount: 2 },
  { id: 2, name: '普通管理员', description: '拥有部分管理权限', userCount: 5 },
  { id: 3, name: 'HR经理', description: 'HR管理权限', userCount: 12 },
  { id: 4, name: 'HR专员', description: '基础HR操作权限', userCount: 28 },
])

const permTree = ref([
  { id: 1, label: '数据概览', children: [
    { id: 11, label: '查看数据面板' }, { id: 12, label: '导出数据报表' },
  ]},
  { id: 2, label: '用户管理', children: [
    { id: 21, label: '查看用户列表' }, { id: 22, label: '新增用户' },
    { id: 23, label: '编辑用户' }, { id: 24, label: '删除用户' },
  ]},
  { id: 3, label: 'HR管理', children: [
    { id: 31, label: '查看HR列表' }, { id: 32, label: '新增HR' },
    { id: 33, label: '编辑HR' }, { id: 34, label: '禁用/启用HR' },
  ]},
  { id: 4, label: '企业/公司管理', children: [
    { id: 41, label: '查看企业列表' }, { id: 42, label: '新增企业' },
    { id: 43, label: '编辑企业' }, { id: 44, label: '企业认证' },
  ]},
  { id: 5, label: '职位管理', children: [
    { id: 51, label: '查看职位列表' }, { id: 52, label: '审核职位' },
    { id: 53, label: '删除职位' },
  ]},
  { id: 6, label: '系统设置', children: [
    { id: 61, label: '基本设置' }, { id: 62, label: '安全设置' },
    { id: 63, label: '邮件设置' },
  ]},
])

const rolePermMap = {
  1: [11, 12, 21, 22, 23, 24, 31, 32, 33, 34, 41, 42, 43, 44, 51, 52, 53, 61, 62, 63],
  2: [11, 21, 31, 41, 51],
  3: [11, 21, 31, 32, 33, 41, 43, 51, 52],
  4: [11, 21, 31, 41, 51],
}

const handleRoleChange = (row) => {
  currentRole.value = row
  checkedKeys.value = rolePermMap[row.id] || []
}
const handleEditRole = (row) => console.log('编辑角色:', row)
const handleDeleteRole = (row) => console.log('删除角色:', row)
const savePermissions = () => {
  const keys = treeRef.value.getCheckedKeys()
  const halfKeys = treeRef.value.getHalfCheckedKeys()
  console.log('保存权限:', [...keys, ...halfKeys])
  ElMessage.success('权限保存成功')
}

watch(settingTab, (tab) => {
  if (tab === 'announcement' && annList.value.length === 0) fetchAnnouncements()
})

onMounted(loadProfile)
</script>

<style scoped>
.aa-root {  }
.sub-hero { display:flex; align-items:center; gap:16px; padding:24px 28px; background:linear-gradient(135deg,#eff6ff,#dbeafe); border-radius:14px; margin-bottom:28px; }
.sub-hero-icon { font-size:40px; }
.sub-hero h2 { font-size:20px; font-weight:700; color:#1e40af; margin:0 0 4px; }
.sub-hero p { font-size:13px; color:#64748b; margin:0; }

.aa-tabs {  }
.aa-tabs :deep(.el-tabs__header) { margin-bottom: 20px; }
.aa-tabs :deep(.el-tabs__item) { font-size: 15px; font-weight: 600; }

/* 个人资料 */
.pe-form-wrap { max-width:640px; }
.pe-avatar-row { display:flex; align-items:center; gap:16px; margin-bottom:24px; padding-bottom:20px; border-bottom:1px solid #f0f0f0; }
.pe-avatar-info { display:flex; flex-direction:column; gap:4px; }
.pe-avatar-title { font-size:14px; font-weight:600; color:#0f172a; }
.pe-avatar-tip { font-size:12px; color:#94a3b8; }
.pe-block { margin-bottom:28px; }
.pe-label { font-size:15px; font-weight:700; color:#0f172a; margin:0 0 18px; padding-bottom:10px; border-bottom:2px solid #dbeafe; }
.pe-grid { display:grid; grid-template-columns:1fr 1fr; gap:20px; }
.pe-field { display:flex; flex-direction:column; gap:6px; }
.pe-field > label { font-size:13px; font-weight:600; color:#64748b; }
.pe-radio-group { display:flex; gap:12px; }
.pe-radio { flex:1; padding:10px 16px; border:2px solid #e2e8f0; border-radius:10px; text-align:center; cursor:pointer; font-size:14px; font-weight:500; color:#64748b; transition:all .2s; }
.pe-radio:hover { border-color:#93c5fd; }
.pe-radio.on { border-color:#3b82f6; background:#eff6ff; color:#1d4ed8; font-weight:600; }
.pe-actions { display:flex; gap:12px; padding-top:8px; }

/* 密码 */
.pwd-form { max-width:480px; }

/* 系统设置 */
.settings-form { max-width: 600px; padding: 20px 0; }
.ann-section { padding: 10px 0; }
.ann-toolbar { margin-bottom: 16px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }

/* 权限管理 */
.role-card, .perm-card { border-radius: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

@media (max-width:767px) { .pe-grid { grid-template-columns:1fr; } }
</style>
