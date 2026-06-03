<template>
  <div>
    <div class="sub-hero">
      <span class="sub-hero-icon">🔒</span>
      <div>
        <h2>修改密码</h2>
        <p>定期更换密码保障账号安全</p>
      </div>
    </div>

    <el-form :model="form" label-width="100px" class="pwd-form">
      <el-form-item label="原密码">
        <el-input v-model="form.oldPwd" type="password" show-password size="large" />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPwd" type="password" show-password size="large" />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input v-model="form.confirmPwd" type="password" show-password size="large" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" @click="handleChange">确认修改</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'

const form = reactive({ oldPwd:'', newPwd:'', confirmPwd:'' })

const handleChange = () => {
  if (form.newPwd !== form.confirmPwd) return ElMessage.error('两次输入的密码不一致')
  ElMessage.success('密码修改成功')
}
</script>

<style scoped>
.sub-hero { display:flex; align-items:center; gap:16px; padding:24px 28px; background:linear-gradient(135deg,#f0fdf4,#ecfdf5); border-radius:14px; margin-bottom:28px; }
.sub-hero-icon { font-size:40px; }
.sub-hero h2 { font-size:20px; font-weight:700; color:#065f46; margin:0 0 4px; }
.sub-hero p { font-size:13px; color:#64748b; margin:0; }
.pwd-form { max-width:480px; }
@media (max-width:767px) { .pwd-form { max-width:100%; } }
</style>
