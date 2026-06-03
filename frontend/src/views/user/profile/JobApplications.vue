<template>
  <div>
    <div class="sub-hero">
      <span class="sub-hero-icon">📋</span>
      <div>
        <h2>岗位投递记录</h2>
        <p>查看您投递的所有岗位及进度</p>
      </div>
    </div>
    <el-table :data="list" style="width: 100%" stripe>
      <el-table-column prop="jobTitle" label="岗位名称" min-width="180">
        <template #default="{ row }">
          <el-button type="primary" link @click="openDetail(row)">{{ row.jobTitle }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="companyName" label="公司" min-width="160" show-overflow-tooltip />
      <el-table-column prop="appliedAt" label="投递时间" min-width="160" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.statusType">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === '面试中'"
            type="success"
            size="small"
            link
            @click="goInterview(row)"
          >
            查看面试通知
          </el-button>
          <el-button
            v-if="row.status === '被拒绝' || row.status === '已取消'"
            type="warning"
            size="small"
            link
            @click="handleReApply(row)"
          >
            重新投递
          </el-button>
          <el-button
            v-if="row.status === '已投递' || row.status === 'HR已查看' || row.status === '二次投递'"
            type="danger"
            size="small"
            link
            @click="handleCancel(row)"
          >
            取消投递
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="投递详情" width="800px" top="6vh" destroy-on-close>
      <div v-if="detailLoading" class="detail-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <div v-else-if="detailData" class="detail-layout">
        <div class="detail-header">
          <div class="detail-title-row">
            <div class="detail-title-left">
              <h2 class="detail-title">{{ detailData.title }}</h2>
              <el-tag :type="currentStatusType" size="large">{{ currentStatusLabel }}</el-tag>
              <el-tag :type="jobStatusMap[detailData.jobStatus]?.type" size="large">{{ jobStatusMap[detailData.jobStatus]?.label }}</el-tag>
            </div>
          </div>
          <div class="detail-meta">
            <span><el-icon><OfficeBuilding /></el-icon> {{ detailData.name }}</span>
            <span><el-icon><Clock /></el-icon> {{ formatDate(detailData.appliedAt) }}</span>
            <span v-if="detailData.realname"><el-icon><User /></el-icon> {{ detailData.realname }}</span>
            <span class="meta-stats">
              <el-icon><View /></el-icon> {{ detailData.viewCount ?? 0 }}
              <el-icon><ChatDotRound /></el-icon> {{ detailData.applyCount ?? 0 }}
            </span>
          </div>
        </div>

        <el-divider content-position="left"><span class="section-label">岗位信息</span></el-divider>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">薪资范围</span>
            <span class="info-value salary">{{ detailData.salaryMin && detailData.salaryMax ? formatSalary(detailData.salaryMin, detailData.salaryMax) : '面议' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">工作地点</span>
            <span class="info-value">{{ detailData.city || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">经验要求</span>
            <span class="info-value">{{ detailData.experience || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">学历要求</span>
            <span class="info-value">{{ educationMap[detailData.education] || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">招聘人数</span>
            <span class="info-value">{{ detailData.headcount ? `${detailData.headcount} 人` : '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">岗位类别</span>
            <span class="info-value">{{ detailData.category || '-' }}</span>
          </div>
        </div>

        <div class="info-tags" v-if="detailData.tags" style="margin-bottom: 16px;">
          <el-tag v-for="tag in parseTags(detailData.tags)" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
        </div>

        <div class="info-section" v-if="detailData.dutyContent">
          <span class="info-label">岗位职责</span>
          <div class="info-text">{{ detailData.dutyContent }}</div>
        </div>
        <div class="info-section" v-if="detailData.requireContent">
          <span class="info-label">任职要求</span>
          <div class="info-text">{{ detailData.requireContent }}</div>
        </div>
        <div class="info-section" v-if="detailData.benefits">
          <span class="info-label">福利待遇</span>
          <div class="info-tags">
            <el-tag v-for="tag in parseTags(detailData.benefits)" :key="tag" size="small" type="info">{{ tag }}</el-tag>
          </div>
        </div>

        <template v-if="detailData.name">
          <el-divider content-position="left"><span class="section-label">公司信息</span></el-divider>
          <div class="company-info-layout">
            <div class="company-logo-area" v-if="detailData.logoUrl">
              <img :src="detailData.logoUrl" class="company-logo-img" />
            </div>
            <div class="company-details">
              <div class="info-grid">
                <div class="info-item" v-if="detailData.size">
                  <span class="info-label">公司规模</span>
                  <span class="info-value">{{ detailData.size }}</span>
                </div>
                <div class="info-item" v-if="detailData.industry">
                  <span class="info-label">所属行业</span>
                  <span class="info-value">{{ detailData.industry }}</span>
                </div>
              </div>
              <div class="info-section" v-if="detailData.description">
                <span class="info-label">公司简介</span>
                <div class="info-text">{{ detailData.description }}</div>
              </div>
              <div class="info-section" v-if="detailData.address">
                <span class="info-label">公司地址</span>
                <div class="info-text">{{ detailData.address }}</div>
              </div>
            </div>
          </div>
        </template>

        <template v-if="detailData.interviewTime">
           <el-divider content-position="left"><span class="section-label">面试安排</span></el-divider>
           <div class="info-item">
             <span class="info-label">面试时间</span>
             <span class="info-value highlight">{{ formatDate(detailData.interviewTime) }}</span>
           </div>
         </template>

        <template v-if="detailData.applicationStatus === 2">
          <el-divider content-position="left"><span class="section-label">面试通知</span></el-divider>
          <div class="interview-notice-card">
            <div class="interview-notice-icon"><el-icon :size="32"><Bell /></el-icon></div>
            <div class="interview-notice-info">
              <div class="interview-notice-title">您已收到面试邀请</div>
              <div class="interview-notice-desc">点击下方按钮查看面试详情及安排</div>
            </div>
            <el-button type="primary" @click="goInterview">前往面试通知</el-button>
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { List, OfficeBuilding, Clock, Loading, User, Bell, View, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getJobApplicationsApi, cancelApplyApi, reApplyApi, getApplicationDetailApi } from '@/api/user/profile'
import { formatSalary } from '@/utils/format'

const router = useRouter()
const list = ref([])
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

const educationMap = {
  0: '不限',
  1: '高中',
  2: '中专',
  3: '大专',
  4: '本科',
  5: '硕士',
  6: '博士',
}

const jobStatusMap = {
  0: { label: '下架', type: 'info' },
  1: { label: '招聘中', type: 'success' },
  2: { label: '审核中', type: 'warning' },
  3: { label: '强制下架', type: 'danger' },
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

const statusMap = {
  0: { label: '已投递', type: 'info' },
  1: { label: 'HR已查看', type: 'warning' },
  2: { label: '面试中', type: 'success' },
  3: { label: '被拒绝', type: 'danger' },
  4: { label: '已取消', type: 'danger' },
  5: { label: '已录用', type: 'success' },
  6: { label: '二次投递', type: 'info' },
}

const parseTags = (str) => {
  if (!str) return []
  return str.split(',').filter(t => t.trim())
}

const currentStatusLabel = computed(() => {
  if (!detailData.value) return ''
  return statusMap[detailData.value.applicationStatus]?.label || '未知'
})

const currentStatusType = computed(() => {
  if (!detailData.value) return 'info'
  return statusMap[detailData.value.applicationStatus]?.type || 'info'
})

const transformStatus = (row) => {
  const mapped = statusMap[row.status] || { label: '未知', type: 'info' }
  row.status = mapped.label
  row.statusType = mapped.type
  return row
}

const openDetail = async (row) => {
  detailData.value = null
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getApplicationDetailApi(row.id)
    if (res.code === 1) {
      detailData.value = res.data
    } else {
      ElMessage.error(res.msg || '加载详情失败')
    }
  } catch {
    ElMessage.error('加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消投递该岗位吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const res = await cancelApplyApi(row.id)
    if (res.code === 1) {
      ElMessage.success('已取消投递')
      row.status = statusMap[4].label
      row.statusType = statusMap[4].type
    } else {
      ElMessage.error(res.msg || '取消失败')
    }
  } catch {}
}

const handleReApply = async (row) => {
  try {
    await ElMessageBox.confirm('确定要重新投递该岗位吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const res = await reApplyApi(row.id)
    if (res.code === 1) {
      ElMessage.success('重新投递成功')
      row.status = statusMap[0].label
      row.statusType = statusMap[0].type
    } else {
      ElMessage.error(res.msg || '重新投递失败')
    }
  } catch {}
}

const goInterview = async (row) => {
  try {
    await ElMessageBox.confirm('是否前往面试通知页面？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
    router.push({ name: 'UserProfile' })
    setTimeout(() => {
      const event = new CustomEvent('switch-menu', { detail: 'interview-notice' })
      window.dispatchEvent(event)
    }, 100)
  } catch {}
}

const fetchData = async () => {
  const res = await getJobApplicationsApi()
  if (res.code == 1 && res.data) {
    list.value = res.data.map(transformStatus)
  } else {
    list.value = []
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.sub-hero { display:flex; align-items:center; gap:16px; padding:24px 28px; background:linear-gradient(135deg,#f0fdf4,#ecfdf5); border-radius:14px; margin-bottom:28px; }
.sub-hero-icon { font-size:40px; }
.sub-hero h2 { font-size:20px; font-weight:700; color:#065f46; margin:0 0 4px; }
.sub-hero p { font-size:13px; color:#64748b; margin:0; }

.detail-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px;
  color: #94a3b8;
}

.detail-layout { display: flex; flex-direction: column; gap: 0; }

.detail-header { margin-bottom: 8px; }
.detail-title-row { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.detail-title-left { display: flex; align-items: center; gap: 10px; }
.detail-title { font-size: 22px; font-weight: 700; color: #1e293b; margin: 0; }
.detail-meta { display: flex; gap: 20px; font-size: 14px; color: #64748b; flex-wrap: wrap; }
.detail-meta span { display: flex; align-items: center; gap: 4px; }
.meta-stats { display: flex; align-items: center; gap: 8px; margin-left: auto; }
.info-section { margin-bottom: 12px; }

.info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px 24px; margin-bottom: 12px; }
.info-item { display: flex; flex-direction: column; gap: 2px; }
.info-label { font-size: 12px; color: #94a3b8; }
.info-value { font-size: 14px; color: #1e293b; font-weight: 500; }
.info-value.salary { color: #ef4444; font-weight: 700; }
.info-value.highlight { color: #3b82f6; font-weight: 600; }

.info-section { margin-bottom: 12px; }
.info-section .info-label { font-size: 12px; color: #94a3b8; display: block; margin-bottom: 4px; }
.info-text { font-size: 14px; color: #475569; line-height: 1.8; white-space: pre-line; }
.info-tags { display: flex; gap: 6px; flex-wrap: wrap; }

.section-label { font-size: 13px; font-weight: 600; color: #3b82f6; }

.company-info-layout { display: flex; gap: 24px; align-items: flex-start; }
.company-logo-area { flex-shrink: 0; }
.company-logo-img { width: 120px; height: 140px; border-radius: 8px; object-fit: cover; border: 1px solid #e2e8f0; background: #f8fafc; }
.company-details { flex: 1; min-width: 0; }

.interview-notice-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
  border: 1px solid #86efac;
  border-radius: 12px;
}
.interview-notice-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  background: #fff;
  border-radius: 50%;
  color: #16a34a;
  flex-shrink: 0;
}
.interview-notice-info { flex: 1; }
.interview-notice-title { font-size: 16px; font-weight: 600; color: #15803d; margin-bottom: 4px; }
.interview-notice-desc { font-size: 13px; color: #166534; }

@media (max-width: 767px) {
  .info-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>