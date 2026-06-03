<template>
  <div class="job-manage">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="职位名称">
          <el-input v-model="queryForm.title" placeholder="请输入职位名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="所属企业">
          <el-input v-model="queryForm.companyName" placeholder="请输入企业名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待审核" :value="0" />
            <el-option label="招聘中" :value="1" />
            <el-option label="已下架" :value="2" />
            <el-option label="强制下架" :value="3" />
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
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="职位名称" min-width="160" show-overflow-tooltip />
      <el-table-column label="企业" width="150" show-overflow-tooltip>
        <template #default="{ row }">
          <el-button link type="primary" @click="openCompanyDetail(row.companyId)">
            {{ row.companyName || '--' }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="薪资范围" width="130">
        <template #default="{ row }">
          {{ salaryText(row) }}
        </template>
      </el-table-column>
      <el-table-column prop="city" label="工作地点" width="90" />
      <el-table-column label="学历要求" width="90">
        <template #default="{ row }">
          {{ educationMap[row.education] || '不限' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">
            {{ statusMap[row.status] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="在招/总量" width="90">
        <template #default="{ row }">
          {{ row.hasCount ?? '--' }} / {{ row.headcount ?? '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="applyCount" label="投递" width="70" />
      <el-table-column prop="createdAt" label="发布时间" width="160" />
      <el-table-column prop="updatedAt" label="更新时间" width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDetail(row)">查看</el-button>
          <el-button v-if="row.status === 0" link type="warning" @click="handleAudit(row, 1)">审核通过</el-button>
          <el-button v-if="row.status === 1" link type="danger" @click="handleForceDelist(row)">强制下架</el-button>
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

    <!-- ========== 职位详情弹窗 ========== -->
    <el-dialog v-model="detailVisible" title="职位详情" width="760px" :close-on-click-modal="false">
      <el-descriptions v-if="detailRow" :column="3" border size="large">
        <el-descriptions-item label="职位名称" :span="2">
          <strong>{{ detailRow.title }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detailRow.status)" size="small">
            {{ statusMap[detailRow.status] || '未知' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="所属企业">
          <el-button link type="primary" @click="openCompanyDetail(detailRow.companyId)">
            {{ detailRow.companyName || '--' }}
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item label="职位类别">{{ detailRow.category || '--' }}</el-descriptions-item>
        <el-descriptions-item label="经验要求">{{ detailRow.experience || '不限' }}</el-descriptions-item>
        <el-descriptions-item label="工作城市">{{ detailRow.city || '--' }}</el-descriptions-item>
        <el-descriptions-item label="详细地址">{{ detailRow.address || '--' }}</el-descriptions-item>
        <el-descriptions-item label="学历要求">{{ educationMap[detailRow.education] || '不限' }}</el-descriptions-item>
        <el-descriptions-item label="薪资范围">{{ salaryText(detailRow) }}</el-descriptions-item>
        <el-descriptions-item label="招聘人数">{{ detailRow.headcount ?? '--' }}</el-descriptions-item>
        <el-descriptions-item label="在招岗位数">{{ detailRow.hasCount ?? '--' }}</el-descriptions-item>
        <el-descriptions-item label="待审调整数">{{ detailRow.pendingAdjust ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="浏览量">{{ detailRow.viewCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="投递数">{{ detailRow.applyCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="职位标签" :span="3">{{ detailRow.tags || '--' }}</el-descriptions-item>
        <el-descriptions-item label="技能要求" :span="3">{{ detailRow.jobSkills || '--' }}</el-descriptions-item>
        <el-descriptions-item label="岗位职责" :span="3">
          <pre class="desc-pre">{{ detailRow.dutyContent || '--' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="任职要求" :span="3">
          <pre class="desc-pre">{{ detailRow.requireContent || '--' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="福利待遇" :span="3">
          <pre class="desc-pre">{{ detailRow.benefits || '--' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ detailRow.createdAt || '--' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailRow.updatedAt || '--' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="detailRow && detailRow.status === 0" type="warning" @click="handleAudit(detailRow, 1); detailVisible = false">审核通过</el-button>
        <el-button v-if="detailRow && detailRow.status === 1" type="danger" @click="handleForceDelist(detailRow); detailVisible = false">强制下架</el-button>
      </template>
    </el-dialog>

    <!-- ========== 公司详情弹窗（复用组件） ========== -->
    <CompanyDetailDialog v-model="companyVisible" :company-id="selectedCompanyId" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getJobs, auditJob, toggleJobStatus } from '@/api/admin/jobs'
import CompanyDetailDialog from '@/components/CompanyDetailDialog.vue'

const statusMap = { 0: '待审核', 1: '招聘中', 2: '已下架', 3: '强制下架' }
const statusType = (s) => s === 1 ? 'success' : s === 0 ? 'warning' : s === 3 ? 'danger' : 'info'
const educationMap = { 0: '不限', 1: '初中', 2: '高中', 3: '中专', 4: '大专', 5: '本科', 6: '硕士', 7: '博士' }
const salaryText = (row) => {
  if (row.salaryMin && row.salaryMax) return `${row.salaryMin}K-${row.salaryMax}K`
  if (row.salaryMin) return `${row.salaryMin}K起`
  if (row.salaryMax) return `最高${row.salaryMax}K`
  return '面议'
}

const queryForm = reactive({ title: '', companyName: '', status: '', page: 1, size: 10 })
const list = ref([])
const total = ref(0)
const loading = ref(false)

const detailVisible = ref(false)
const detailRow = ref(null)

const companyVisible = ref(false)
const selectedCompanyId = ref(null)

const handleDetail = (row) => {
  detailRow.value = row
  detailVisible.value = true
}

const openCompanyDetail = (companyId) => {
  if (!companyId) return
  selectedCompanyId.value = companyId
  companyVisible.value = true
}

const handleAudit = (row, status) => {
  ElMessageBox.confirm(
    '确认审核通过该职位？审核通过后职位将进入招聘中状态。',
    '审核确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await auditJob(row.id, status)
    row.status = status
    ElMessage.success('审核通过')
  }).catch(() => {})
}

const handleForceDelist = (row) => {
  ElMessageBox.confirm(
    '确认强制下架该职位？下架后该职位将不再对外展示，通常用于违规或举报处理。',
    '强制下架确认',
    { confirmButtonText: '确定下架', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await toggleJobStatus(row.id, 3)
    row.status = 3
    ElMessage.success('已强制下架')
  }).catch(() => {})
}

const handleQuery = () => { queryForm.page = 1; fetchList() }

const resetQuery = () => {
  queryForm.title = ''; queryForm.companyName = ''; queryForm.status = ''
  queryForm.page = 1; fetchList()
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getJobs({
      page: queryForm.page, size: queryForm.size,
      title: queryForm.title || undefined,
      companyName: queryForm.companyName || undefined,
      status: queryForm.status !== '' ? queryForm.status : undefined,
    })
    if (res.code === 1) {
      list.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch {
    list.value = []; total.value = 0
  }
  finally { loading.value = false }
}

onMounted(fetchList)
</script>

<style scoped>
.search-card { margin-bottom: 20px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.toolbar { margin-bottom: 15px; display: flex; gap: 10px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.desc-pre { white-space: pre-wrap; word-break: break-all; font-family: inherit; margin: 0; line-height: 1.7; }
:deep(.el-table) { border-radius: 8px; overflow: hidden; }
</style>
