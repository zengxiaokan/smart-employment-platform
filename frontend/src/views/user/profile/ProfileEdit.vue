<template>
  <div class="pe-root" v-loading="saving">
    <!-- 头部卡片 -->
    <div class="pe-hero">
      <span class="pe-hero-icon">👤</span>
      <div>
        <h2>个人资料编辑</h2>
        <p>完善您的个人信息，让企业更好地了解您</p>
      </div>
    </div>

    <!-- 表单 -->
    <div class="pe-form-wrap">
      <!-- 基本信息块 -->
      <div class="pe-block">
        <h4 class="pe-label">基本信息</h4>
        <div class="pe-grid">
          <div class="pe-field">
            <label>真实姓名</label>
            <el-input v-model="form.realname" placeholder="请输入姓名" size="large" />
          </div>
          <div class="pe-field">
            <label>昵称</label>
            <el-input v-model="form.nickname" placeholder="请输入昵称" size="large" />
          </div>
          <div class="pe-field">
            <label>性别</label>
            <div class="pe-radio-group">
              <span class="pe-radio" :class="{on:form.gender===1}" @click="form.gender=1">👨 男</span>
              <span class="pe-radio" :class="{on:form.gender===0}" @click="form.gender=0">👩 女</span>
            </div>
          </div>
          <div class="pe-field">
            <label>年龄</label>
            <el-input-number v-model="form.age" :min="16" :max="65" placeholder="年龄" size="large" style="width:100%" controls-position="right" />
          </div>
        </div>
      </div>

      <!-- 联系方式块 -->
      <div class="pe-block">
        <h4 class="pe-label">联系方式</h4>
        <div class="pe-grid">
          <div class="pe-field">
            <label>手机号</label>
            <el-input v-model="form.phone" placeholder="请输入手机号" size="large">
              <template #prefix>📱</template>
            </el-input>
          </div>
          <div class="pe-field">
            <label>邮箱</label>
            <el-input v-model="form.email" placeholder="请输入邮箱" size="large">
              <template #prefix>📧</template>
            </el-input>
          </div>
          <div class="pe-field">
            <label>所在城市</label>
            <el-input v-model="form.city" placeholder="如：武汉" size="large">
              <template #prefix>📍</template>
            </el-input>
          </div>
        </div>
      </div>

      <!-- 操作 -->
      <div class="pe-actions">
        <el-button type="primary" size="large" @click="handleSave" :loading="saving">保存修改</el-button>
        <el-button size="large" @click="handleReset">重置为原始值</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserProfileApi, updateUserProfileApi } from '@/api/user/profile'
import { useUser } from '@/utils/useUser'

const form = reactive({ realname:'', nickname:'', gender:1, age:null, phone:'', email:'', city:'' })
const original = reactive({ realname:'', nickname:'', gender:1, age:null, phone:'', email:'', city:'' })
const loading = ref(false)
const saving = ref(false)
const { updateNickname } = useUser()

const defs = { realname:'', nickname:'', gender:1, age:null, phone:'', email:'', city:'' }

onMounted(async () => {
  loading.value = true
  try {
    const res = await getUserProfileApi()
    if (res.code===1 && res.data) {
      Object.assign(form, res.data)
      Object.assign(original, res.data)
    } else {
      Object.assign(form, defs)
      Object.assign(original, defs)
      if (res.msg) ElMessage.warning(res.msg)
    }
  } catch {
    Object.assign(form, defs)
    Object.assign(original, defs)
    ElMessage.error('获取个人信息失败')
  } finally { loading.value = false }
})

const handleSave = async () => {
  saving.value = true
  try {
    const res = await updateUserProfileApi({...form})
    if (res.code === 1) { Object.assign(original, form); updateNickname(form.nickname); ElMessage.success('保存成功') }
    else ElMessage.error(res.msg||'保存失败')
  } catch { ElMessage.error('网络异常') }
  finally { saving.value = false }
}

const handleReset = () => { Object.assign(form, original); ElMessage.info('已恢复') }
</script>

<style scoped>
.pe-root {  }

.pe-hero {
  display:flex; align-items:center; gap:16px; padding:24px 28px;
  background:linear-gradient(135deg,#f0fdf4,#ecfdf5); border-radius:14px; margin-bottom:28px;
}
.pe-hero-icon { font-size:40px; }
.pe-hero h2 { font-size:20px; font-weight:700; color:#065f46; margin:0 0 4px; }
.pe-hero p { font-size:13px; color:#64748b; margin:0; }

.pe-form-wrap {  }
.pe-block { margin-bottom:28px; }
.pe-block:last-child { margin-bottom:0; }
.pe-label { font-size:15px; font-weight:700; color:#0f172a; margin:0 0 18px; padding-bottom:10px; border-bottom:2px solid #dcfce7; }

.pe-grid { display:grid; grid-template-columns:1fr 1fr; gap:20px; }

.pe-field { display:flex; flex-direction:column; gap:6px; }
.pe-field > label { font-size:13px; font-weight:600; color:#64748b; }

.pe-radio-group { display:flex; gap:12px; }
.pe-radio {
  flex:1; padding:10px 16px; border:2px solid #e2e8f0; border-radius:10px;
  text-align:center; cursor:pointer; font-size:14px; font-weight:500; color:#64748b;
  transition:all .2s;
}
.pe-radio:hover { border-color:#86efac; }
.pe-radio.on { border-color:#22c55e; background:#f0fdf4; color:#15803d; font-weight:600; }

.pe-actions { display:flex; gap:12px; padding-top:8px; }

@media (max-width:767px) {
  .pe-grid { grid-template-columns:1fr; }
  .pe-hero { padding:20px; }
}
</style>
