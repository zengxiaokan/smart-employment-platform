<template>
  <div class="record-manage">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="求职者">
          <el-input v-model="queryForm.applicantName" placeholder="请输入求职者姓名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="queryForm.jobTitle" placeholder="请输入职位名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="已投递" :value="0" />
            <el-option label="HR已查看" :value="1" />
            <el-option label="邀请面试" :value="2" />
            <el-option label="不合适" :value="3" />
            <el-option label="用户取消投递" :value="4" />
            <el-option label="已录用" :value="5" />
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
      <el-table-column prop="applicantName" label="求职者" width="100" />
      <el-table-column prop="jobTitle" label="投递职位" min-width="160" show-overflow-tooltip />
      <el-table-column prop="companyName" label="企业" width="160" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="appliedAt" label="投递时间" width="170" />
      <el-table-column prop="updatedAt" label="更新时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="View" @click="handleDetail(row)">详情</el-button>
          <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="detailVisible" title="投递详情" width="640px" :close-on-click-modal="false">
      <el-descriptions v-if="detailRow" :column="2" border size="large">
        <el-descriptions-item label="求职者">{{ detailRow.realName || detailRow.applicantName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="投递状态">
          <el-tag :type="statusType(detailRow.applicationStatus)">{{ statusText(detailRow.applicationStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="职位名称" :span="2">{{ detailRow.title || '--' }}</el-descriptions-item>
        <el-descriptions-item label="职位类别">{{ detailRow.category || '--' }}</el-descriptions-item>
        <el-descriptions-item label="薪资范围">{{ detailRow.salaryMin || '--' }}K - {{ detailRow.salaryMax || '--' }}K</el-descriptions-item>
        <el-descriptions-item label="经验要求">{{ detailRow.experience || '--' }}</el-descriptions-item>
        <el-descriptions-item label="学历要求">{{ educationMap[detailRow.education] || '不限' }}</el-descriptions-item>
        <el-descriptions-item label="招聘人数">{{ detailRow.headcount ?? '--' }}</el-descriptions-item>
        <el-descriptions-item label="职位状态">{{ detailRow.jobStatus || '--' }}</el-descriptions-item>
        <el-descriptions-item label="企业名称" :span="2">{{ detailRow.name || '--' }}</el-descriptions-item>
        <el-descriptions-item label="行业">{{ detailRow.industry || '--' }}</el-descriptions-item>
        <el-descriptions-item label="规模">{{ detailRow.size || '--' }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ detailRow.city || '--' }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ detailRow.address || '--' }}</el-descriptions-item>
        <el-descriptions-item label="投递时间">{{ detailRow.appliedAt || '--' }}</el-descriptions-item>
        <el-descriptions-item label="面试时间">{{ detailRow.interviewTime || '--' }}</el-descriptions-item>
        <el-descriptions-item label="职位描述" :span="2">{{ detailRow.dutyContent || '--' }}</el-descriptions-item>
        <el-descriptions-item label="任职要求" :span="2">{{ detailRow.requireContent || '--' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Refresh, Delete, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApplications, getApplicationDetail, deleteApplication } from '@/api/admin/applications'

const statusText = (s) => {
  const map = { 0: '已投递', 1: 'HR已查看', 2: '邀请面试', 3: '不合适', 4: '用户取消投递', 5: '已录用' }
  if (s >= 6) return `用户第${s - 4}次投递`
  return map[s] || '未知'
}
const statusType = (s) => s === 5 ? 'success' : s === 2 ? 'warning' : s === 3 ? 'danger' : 'info'
const educationMap = { 0: '不限', 1: '初中', 2: '高中', 3: '中专', 4: '大专', 5: '本科', 6: '硕士', 7: '博士' }

const queryForm = reactive({ applicantName: '', jobTitle: '', status: '', page: 1, size: 10 })
const list = ref([])
const total = ref(0)
const loading = ref(false)

const detailVisible = ref(false)
const detailRow = ref(null)

const handleDetail = async (row) => {
  try {
    const res = await getApplicationDetail(row.id)
    if (res.code === 1) detailRow.value = res.data
    detailVisible.value = true
  } catch { /* */ }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除「${row.applicantName}」的投递记录吗？`, '删除确认', {
    confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    await deleteApplication(row.id)
    ElMessage.success('已删除')
    fetchList()
  }).catch(() => {})
}

const handleQuery = () => { queryForm.page = 1; fetchList() }

const resetQuery = () => {
  queryForm.applicantName = ''; queryForm.jobTitle = ''; queryForm.status = ''
  queryForm.page = 1; fetchList()
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getApplications({
      page: queryForm.page, size: queryForm.size,
      applicantName: queryForm.applicantName || undefined,
      jobTitle: queryForm.jobTitle || undefined,
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
</style>
