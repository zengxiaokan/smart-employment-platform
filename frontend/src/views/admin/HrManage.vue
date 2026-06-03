<template>
  <div class="hr-manage">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="姓名">
          <el-input v-model="queryForm.name" placeholder="请输入姓名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="queryForm.auditStatus" placeholder="全部" clearable style="width: 130px">
            <el-option label="审核中" :value="0" />
            <el-option label="已审核" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="正常" :value="0" />
            <el-option label="封号" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增HR</el-button>
      <el-button :icon="Refresh" @click="fetchList">刷新</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="头像" width="70">
        <template #default="{ row }">
          <el-avatar :size="36" :src="row.avatarUrl">
            <el-icon :size="18"><User /></el-icon>
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column label="姓名" width="100">
        <template #default="{ row }">
          {{ row.realname || row.nickname || '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="注册公司" min-width="150">
        <template #default="{ row }">
          <template v-if="row.companyName">
            {{ row.companyName }}
          </template>
          <span v-else class="text-muted">未注册</span>
        </template>
      </el-table-column>
      <el-table-column label="城市" width="100">
        <template #default="{ row }">
          {{ row.city || '--' }}
        </template>
      </el-table-column>
      <el-table-column label="审核状态" width="100">
        <template #default="{ row }">
          <template v-if="row.companyName">
            <el-tag :type="row.auditStatus === 1 ? 'success' : 'warning'" size="small">
              {{ auditStatusMap[row.auditStatus] || '未知' }}
            </el-tag>
          </template>
          <span v-else class="text-muted">未注册</span>
        </template>
      </el-table-column>
      <el-table-column label="账号状态" width="90">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 0"
            @change="(val) => handleStatusChange(row, val ? 0 : 1)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" min-width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit" @click="openDialog(row)">编辑</el-button>
          <el-button v-if="isSuperAdmin" link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @change="fetchList"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑HR' : '新增HR'"
      width="520px"
      :close-on-click-modal="false"
      destroy-on-close
      @closed="resetFormRef"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realname" placeholder="真实姓名（选填）" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="昵称（选填）" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" placeholder="所在城市（选填）" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">封号</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Refresh, Delete, Edit, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getHrs, createHr, updateHr, deleteHr } from '@/api/admin/hrs'

const loginUser = JSON.parse(localStorage.getItem('loginUser') || '{}')
const isSuperAdmin = loginUser.userId === 1 || loginUser.id === 1

const auditStatusMap = { 0: '审核中', 1: '已审核' }

const queryForm = reactive({ name: '', auditStatus: '', status: '', page: 1, size: 10 })
const list = ref([])
const total = ref(0)
const loading = ref(false)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editId = ref(null)

const form = reactive({
  username: '', password: '', phone: '', realname: '', nickname: '',
  city: '', status: 0
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    editId.value = row.id
    Object.assign(form, {
      username: row.username, password: '',
      phone: row.phone || '', realname: row.realname || '',
      nickname: row.nickname || '', city: row.city || '',
      status: row.status
    })
  } else {
    isEdit.value = false
    editId.value = null
    Object.assign(form, {
      username: '', password: '', phone: '', realname: '',
      nickname: '', city: '', status: 0
    })
  }
  dialogVisible.value = true
}

const resetFormRef = () => {
  formRef.value?.resetFields()
}

const submitForm = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = {
      phone: form.phone || undefined,
      realname: form.realname || undefined,
      nickname: form.nickname || undefined,
      city: form.city || undefined,
      status: form.status
    }
    if (isEdit.value) {
      await updateHr(editId.value, payload)
      ElMessage.success('编辑成功')
    } else {
      payload.username = form.username
      payload.password = form.password
      await createHr(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch { /* 请求拦截器已提示 */ }
  finally { submitting.value = false }
}

const handleStatusChange = async (row, status) => {
  try {
    await updateHr(row.id, { status })
    row.status = status
    ElMessage.success(status === 0 ? '已启用' : '已封号')
  } catch { /* */ }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除HR「${row.realname || row.nickname || row.username}」吗？删除后不可恢复。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteHr(row.id)
    ElMessage.success('已删除')
    fetchList()
  }).catch(() => {})
}

const handleQuery = () => {
  queryForm.page = 1
  fetchList()
}

const resetQuery = () => {
  queryForm.name = ''
  queryForm.auditStatus = ''
  queryForm.status = ''
  queryForm.page = 1
  fetchList()
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getHrs({
      page: queryForm.page,
      size: queryForm.size,
      name: queryForm.name || undefined,
      auditStatus: queryForm.auditStatus !== '' ? queryForm.auditStatus : undefined,
      status: queryForm.status !== '' ? queryForm.status : undefined
    })
    if (res.code === 1) {
      list.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch { list.value = []; total.value = 0 }
  finally { loading.value = false }
}

onMounted(fetchList)
</script>

<style scoped>
.search-card { margin-bottom: 20px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.toolbar { margin-bottom: 15px; display: flex; gap: 10px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
:deep(.el-table) { border-radius: 8px; overflow: hidden; }
.text-muted { color: #c0c4cc; font-size: 13px; }
</style>