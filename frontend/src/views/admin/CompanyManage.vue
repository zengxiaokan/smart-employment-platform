<template>
  <div class="company-manage">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="企业名称">
          <el-input v-model="queryForm.name" placeholder="请输入企业名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="行业">
          <el-select v-model="queryForm.industry" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="ind in industryOptions" :key="ind" :label="ind" :value="ind" />
          </el-select>
        </el-form-item>
        <el-form-item label="认证状态">
          <el-select v-model="queryForm.auditStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="待审核" :value="0" />
            <el-option label="已认证" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="toolbar">
      <el-button :icon="Refresh" @click="fetchList">刷新</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="LOGO" width="80">
        <template #default="{ row }">
          <el-image
            v-if="row.logoUrl"
            :src="row.logoUrl"
            fit="cover"
            style="width:48px;height:48px;border-radius:4px"
            :preview-src-list="[row.logoUrl]"
          />
          <span v-else class="no-logo">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="企业名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="industry" label="行业" width="100" />
      <el-table-column prop="size" label="规模" width="110" />
      <el-table-column prop="address" label="地址" min-width="150" show-overflow-tooltip />
      <el-table-column label="认证状态" width="100">
        <template #default="{ row }">
          <el-switch
            :model-value="row.auditStatus"
            :active-value="1"
            :inactive-value="0"
            active-text="已认证"
            inactive-text="待审"
            inline-prompt
            @change="(val) => handleAudit(row, val)"
          />
        </template>
      </el-table-column>
      <el-table-column label="注册人" width="110">
        <template #default="{ row }">
          <el-button v-if="row.registrant" link type="primary" @click="handleRegistrantClick(row)">
            {{ row.registrant }}
          </el-button>
          <span v-else class="no-registrant">--</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="入驻时间" width="170" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDetail(row)">查看</el-button>
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

    <!-- 详情弹窗（复用组件） -->
    <CompanyDetailDialog v-model="detailVisible" :company-id="selectedCompanyId" />
  <!-- 注册人信息弹窗 -->
    <el-dialog v-model="registrantVisible" title="注册人信息" width="520px" :close-on-click-modal="false">
      <el-descriptions v-if="registrantUser" :column="2" border size="large" v-loading="registrantLoading">
        <el-descriptions-item label="用户名">{{ registrantUser.username || '--' }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ registrantUser.realname || '--' }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ registrantUser.nickname || '--' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ registrantUser.phone || '--' }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ registrantUser.city || '--' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="registrantUser.role === 2 ? 'danger' : registrantUser.role === 1 ? 'info' : 'success'" size="small">
            {{ roleMap[registrantUser.role] || '未知' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="性别">{{ registrantUser.gender === 0 ? '女' : registrantUser.gender === 1 ? '男' : '--' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ registrantUser.age ?? '--' }}</el-descriptions-item>
        <el-descriptions-item label="头像" :span="2">
          <el-avatar v-if="registrantUser.avatarUrl" :size="64" :src="registrantUser.avatarUrl" />
          <span v-else>--</span>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">{{ registrantUser.createdAt || '--' }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else-if="!registrantLoading" description="未找到该注册人信息" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCompanies, auditCompany, getCompanyRegistrant } from '@/api/admin/companies'
import CompanyDetailDialog from '@/components/CompanyDetailDialog.vue'

const roleMap = { 0: '用户', 1: 'HR', 2: '超级管理员' }

const industryOptions = ['互联网', '金融', '教育', '医疗', '制造', '房地产', '电商', '物流', '新能源', '人工智能']

const queryForm = reactive({ name: '', industry: '', auditStatus: '', page: 1, size: 10 })
const list = ref([])
const total = ref(0)
const loading = ref(false)

const detailVisible = ref(false)
const selectedCompanyId = ref(null)

const registrantVisible = ref(false)
const registrantUser = ref(null)
const registrantLoading = ref(false)

const handleAudit = async (row, val) => {
  try {
    const status = val ? 1 : 0
    await auditCompany(row.id, status)
    row.auditStatus = status
    ElMessage.success(status === 1 ? '已认证' : '已取消认证')
  } catch { /* */ }
}

const handleDetail = (row) => {
  selectedCompanyId.value = row.id
  detailVisible.value = true
}

const handleRegistrantClick = async (row) => {
  registrantVisible.value = true
  registrantUser.value = null
  registrantLoading.value = true
  try {
    const res = await getCompanyRegistrant(row.id)
    if (res.code === 1) {
      registrantUser.value = res.data
    }
  } catch { registrantUser.value = null }
  finally { registrantLoading.value = false }
}

const handleQuery = () => { queryForm.page = 1; fetchList() }

const resetQuery = () => {
  queryForm.name = ''; queryForm.industry = ''; queryForm.auditStatus = ''
  queryForm.page = 1; fetchList()
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getCompanies({
      page: queryForm.page, size: queryForm.size,
      name: queryForm.name || undefined,
      industry: queryForm.industry || undefined,
      auditStatus: queryForm.auditStatus !== '' ? queryForm.auditStatus : undefined,
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
.no-logo { color: #c0c4cc; font-size: 12px; }
.no-registrant { color: #c0c4cc; }
</style>
