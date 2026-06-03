<template>
  <div class="log-manage">
    <!-- 今日统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-num">{{ stats.total ?? '--' }}</div>
          <div class="stat-label">今日操作总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-warn" shadow="hover">
          <div class="stat-num">{{ banCount }}</div>
          <div class="stat-label">封禁/解封</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-info" shadow="hover">
          <div class="stat-num">{{ auditCount }}</div>
          <div class="stat-label">审核操作</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-ok" shadow="hover">
          <div class="stat-num">{{ otherCount }}</div>
          <div class="stat-label">其他业务操作</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="操作人ID">
          <el-input v-model="queryForm.userId" placeholder="用户ID" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="queryForm.action" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="a in actions" :key="a" :label="actionMap[a]" :value="a" />
          </el-select>
        </el-form-item>
        <el-form-item label="对象类型">
          <el-select v-model="queryForm.targetType" placeholder="全部" clearable style="width: 120px">
            <el-option label="用户" value="user" />
            <el-option label="企业" value="company" />
            <el-option label="职位" value="job" />
            <el-option label="面试" value="interview" />
            <el-option label="投递" value="application" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="toolbar">
      <el-button :icon="Refresh" @click="refreshAll">刷新</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="操作人" width="120">
        <template #default="{ row }">
          {{ row.operatorName || row.username || '--' }}
        </template>
      </el-table-column>
      <el-table-column label="操作类型" width="110">
        <template #default="{ row }">
          <el-tag :type="actionStyle(row.action)" size="small">{{ actionMap[row.action] || row.action }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="被操作ID" width="90">
        <template #default="{ row }">
          {{ row.targetId }}
        </template>
      </el-table-column>
      <el-table-column label="被操作对象" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.targetName || (targetTypeMap[row.targetType] || row.targetType) }}
        </template>
      </el-table-column>
      <el-table-column label="IP" width="140">
        <template #default="{ row }">
          {{ row.ipAddress || '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="操作时间" width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">查看</el-button>
          <el-button link type="warning" @click="openRemark(row)">备注</el-button>
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

    <!-- 查看详情 -->
    <el-dialog v-model="detailVisible" title="操作详情" width="520px" :close-on-click-modal="false">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作主题" :span="2">
          <strong>{{ buildSummary(currentRow) }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentRow?.operatorName || currentRow?.username || '--' }}（ID: {{ currentRow?.userId }}）</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="actionStyle(currentRow?.action)" size="small">
            {{ actionMap[currentRow?.action] || currentRow?.action }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="对象类型">{{ targetTypeMap[currentRow?.targetType] || currentRow?.targetType }}</el-descriptions-item>
        <el-descriptions-item label="对象名称">{{ currentRow?.targetName || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="对象ID">#{{ currentRow?.targetId }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentRow?.remark || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentRow?.ipAddress || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentRow?.createdAt }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="warning" @click="detailVisible = false; openRemark(currentRow)">编辑备注</el-button>
      </template>
    </el-dialog>

    <!-- 编辑备注 -->
    <el-dialog v-model="remarkVisible" title="编辑备注" width="450px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="操作主题">
          <span>{{ buildSummary(remarkRow) }}</span>
        </el-form-item>
        <el-form-item label="备注内容">
          <el-input v-model="remarkText" type="textarea" :rows="4" maxlength="255" show-word-limit placeholder="请输入备注内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="remarkVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRemark">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getLogs, updateLogRemark, getTodayStats } from '@/api/admin/logs'

const actions = [
  'USER_REGISTER', 'USER_CHANGE_PWD',
  'JOB_APPLY', 'COMPANY_REGISTER', 'JOB_PUBLISH',
  'INTERVIEW_INVITE', 'INTERVIEW_RESULT',
  'BAN_USER', 'UNBAN_USER',
  'AUDIT_COMPANY', 'AUDIT_JOB', 'TOGGLE_JOB',
  'DELETE_USER', 'DELETE_COMPANY', 'DELETE_JOB', 'DELETE_HR'
]

const actionMap = {
  USER_REGISTER: '用户注册', USER_CHANGE_PWD: '修改密码',
  JOB_APPLY: '投递职位', COMPANY_REGISTER: '企业注册', JOB_PUBLISH: '发布职位',
  INTERVIEW_INVITE: '面试邀请', INTERVIEW_RESULT: '面试结果',
  BAN_USER: '封禁用户', UNBAN_USER: '解封用户',
  AUDIT_COMPANY: '审核企业', AUDIT_JOB: '审核职位', TOGGLE_JOB: '上下架职位',
  DELETE_USER: '删除用户', DELETE_COMPANY: '删除企业', DELETE_JOB: '删除职位', DELETE_HR: '删除HR'
}

const targetTypeMap = { user: '用户', company: '企业', job: '职位', interview: '面试', application: '投递' }

const actionStyle = (a) => {
  if (!a) return 'info'
  if (a.startsWith('BAN') || a.startsWith('UNBAN') || a.startsWith('DELETE')) return 'danger'
  if (a.startsWith('AUDIT') || a.startsWith('TOGGLE')) return 'warning'
  return 'info'
}

const buildSummary = (row) => {
  if (!row) return ''
  const name = row.operatorName || row.username || `用户#${row.userId}`
  const act = actionMap[row.action] || row.action
  const target = row.targetName || `${targetTypeMap[row.targetType] || row.targetType} #${row.targetId}`
  return `${name} ${act}了 ${target}`
}

// 今日统计
const stats = ref({})
const banCount = computed(() => (stats.value?.BAN_USER || 0) + (stats.value?.UNBAN_USER || 0))
const auditCount = computed(() => (stats.value?.AUDIT_COMPANY || 0) + (stats.value?.AUDIT_JOB || 0) + (stats.value?.TOGGLE_JOB || 0))
const otherCount = computed(() => {
  const total = stats.value?.total || 0
  return total - banCount.value - auditCount.value
})

const fetchStats = async () => {
  try {
    const res = await getTodayStats()
    if (res.code === 1) stats.value = res.data
  } catch { stats.value = {} }
}

// 查询
const queryForm = reactive({ userId: '', action: '', targetType: '', page: 1, size: 10 })
const dateRange = ref(null)
const list = ref([])
const total = ref(0)
const loading = ref(false)

const handleQuery = () => { queryForm.page = 1; fetchList() }
const resetQuery = () => {
  queryForm.userId = ''; queryForm.action = ''; queryForm.targetType = ''
  dateRange.value = null; queryForm.page = 1
  fetchList()
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: queryForm.page, size: queryForm.size,
      userId: queryForm.userId || undefined,
      action: queryForm.action || undefined,
      targetType: queryForm.targetType || undefined
    }
    if (dateRange.value?.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await getLogs(params)
    if (res.code === 1) {
      list.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch { list.value = []; total.value = 0 }
  finally { loading.value = false }
}

// 详情
const detailVisible = ref(false)
const currentRow = ref(null)
const openDetail = (row) => { currentRow.value = row; detailVisible.value = true }

// 备注
const remarkVisible = ref(false)
const remarkRow = ref(null)
const remarkText = ref('')
const saving = ref(false)

const openRemark = (row) => {
  remarkRow.value = row
  remarkText.value = row?.remark || ''
  remarkVisible.value = true
}

const saveRemark = async () => {
  if (!remarkRow.value) return
  saving.value = true
  try {
    await updateLogRemark(remarkRow.value.id, remarkText.value)
    remarkRow.value.remark = remarkText.value
    ElMessage.success('备注已保存')
    remarkVisible.value = false
  } catch { /* */ }
  finally { saving.value = false }
}

const refreshAll = () => { fetchList(); fetchStats() }

onMounted(refreshAll)
</script>

<style scoped>
.stats-row { margin-bottom: 20px; }
.stat-card { text-align: center; border-radius: 10px; }
.stat-card .stat-num { font-size: 28px; font-weight: 700; color: #303133; }
.stat-card .stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.stat-warn .stat-num { color: #e6a23c; }
.stat-info .stat-num { color: #409eff; }
.stat-ok .stat-num { color: #67c23a; }

.search-card { margin-bottom: 20px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.toolbar { margin-bottom: 15px; display: flex; gap: 10px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
:deep(.el-table) { border-radius: 8px; overflow: hidden; }
</style>
