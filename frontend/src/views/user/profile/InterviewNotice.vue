<template>
  <div>
    <div class="sub-hero">
      <span class="sub-hero-icon">🔔</span>
      <div>
        <h2>面试通知</h2>
        <p>查看企业发送的面试邀请，及时确认或拒绝</p>
      </div>
    </div>

    <div v-loading="loading">
      <el-tabs v-model="activeTab" class="im-tabs">
        <el-tab-pane name="active">
          <template #label>
            <el-badge :value="activeInterviews.length" :hidden="!activeInterviews.length">
              面试中
            </el-badge>
          </template>
          <el-timeline v-if="activeInterviews.length > 0">
            <el-timeline-item
              v-for="item in activeInterviews"
              :key="item.id"
              :timestamp="item.interviewTime"
              placement="top"
              :type="statusMap[item.status]?.timeline || 'primary'"
            >
              <el-card shadow="hover" class="tl-card">
                <div class="tl-header">
                  <h4>{{ item.jobTitle }}</h4>
                  <el-tag :type="statusMap[item.status]?.type" size="small">
                    {{ statusMap[item.status]?.text || '未知' }}
                  </el-tag>
                </div>
                <div class="tl-body">
                  <p><span class="tl-label">面试方式：</span>{{ item.locationType === 1 ? '线上' : '线下' }}</p>
                  <p><span class="tl-label">面试地点：</span>{{ item.interviewLocation }}</p>
                  <p v-if="item.contactPerson || item.contactPhone">
                    <span class="tl-label">联系人：</span>{{ item.contactPerson || '-' }}{{ item.contactPhone ? ' (' + item.contactPhone + ')' : '' }}
                  </p>
                  <p v-if="item.responseTime">
                    <span class="tl-label">回复时间：</span>{{ item.responseTime }}
                  </p>
                  <p class="tl-remark-row">
                    <span class="tl-label">HR备注：</span>
                    <span class="tl-remark">{{ item.remark || '-' }}</span>
                  </p>
                  <p class="tl-remark-row">
                    <span class="tl-label">我的备注：</span>
                    <span :class="{ 'tl-empty': !item.candidateRemark }" class="tl-remark">{{ item.candidateRemark || '-' }}</span>
                  </p>
                </div>
                <div class="tl-footer" v-if="item.status === 0">
                  <el-button type="danger" size="small" plain @click="handleRespond(item, 6)">拒绝</el-button>
                  <el-button type="success" size="small" @click="handleRespond(item, 1)">接受面试</el-button>
                </div>
                <div class="tl-footer" v-if="item.status === 1">
                  <el-button type="warning" size="small" plain @click="cancelInterview(item)">取消面试</el-button>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无进行中的面试" />
        </el-tab-pane>

        <el-tab-pane label="已完成" name="completed">
          <el-timeline v-if="completedInterviews.length > 0">
            <el-timeline-item
              v-for="item in completedInterviews"
              :key="item.id"
              :timestamp="item.interviewTime"
              placement="top"
              :type="statusMap[item.status]?.timeline || 'success'"
            >
              <el-card shadow="hover" class="tl-card">
                <div class="tl-header">
                  <h4>{{ item.jobTitle }}</h4>
                  <el-tag :type="statusMap[item.status]?.type" size="small">
                    {{ statusMap[item.status]?.text || '未知' }}
                  </el-tag>
                </div>
                <div class="tl-body">
                  <p><span class="tl-label">面试方式：</span>{{ item.locationType === 1 ? '线上' : '线下' }}</p>
                  <p><span class="tl-label">面试地点：</span>{{ item.interviewLocation }}</p>
                  <p v-if="item.contactPerson || item.contactPhone">
                    <span class="tl-label">联系人：</span>{{ item.contactPerson || '-' }}{{ item.contactPhone ? ' (' + item.contactPhone + ')' : '' }}
                  </p>
                  <p v-if="item.responseTime">
                    <span class="tl-label">回复时间：</span>{{ item.responseTime }}
                  </p>
                  <p class="tl-remark-row">
                    <span class="tl-label">HR备注：</span>
                    <span class="tl-remark">{{ item.remark || '-' }}</span>
                  </p>
                  <p class="tl-remark-row">
                    <span class="tl-label">我的备注：</span>
                    <span :class="{ 'tl-empty': !item.candidateRemark }" class="tl-remark">{{ item.candidateRemark || '-' }}</span>
                  </p>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无已完成的面试" />
        </el-tab-pane>

        <el-tab-pane label="已结束" name="cancelled">
          <el-timeline v-if="cancelledInterviews.length > 0">
            <el-timeline-item
              v-for="item in cancelledInterviews"
              :key="item.id"
              :timestamp="item.interviewTime"
              placement="top"
              :type="statusMap[item.status]?.timeline || 'info'"
            >
              <el-card shadow="hover" class="tl-card">
                <div class="tl-header">
                  <h4>{{ item.jobTitle }}</h4>
                  <el-tag :type="statusMap[item.status]?.type" size="small">
                    {{ statusMap[item.status]?.text || '未知' }}
                  </el-tag>
                </div>
                <div class="tl-body">
                  <p><span class="tl-label">面试方式：</span>{{ item.locationType === 1 ? '线上' : '线下' }}</p>
                  <p><span class="tl-label">面试地点：</span>{{ item.interviewLocation }}</p>
                  <p v-if="item.contactPerson || item.contactPhone">
                    <span class="tl-label">联系人：</span>{{ item.contactPerson || '-' }}{{ item.contactPhone ? ' (' + item.contactPhone + ')' : '' }}
                  </p>
                  <p v-if="item.responseTime">
                    <span class="tl-label">回复时间：</span>{{ item.responseTime }}
                  </p>
                  <p class="tl-remark-row">
                    <span class="tl-label">HR备注：</span>
                    <span class="tl-remark">{{ item.remark || '-' }}</span>
                  </p>
                  <p class="tl-remark-row">
                    <span class="tl-label">我的备注：</span>
                    <span :class="{ 'tl-empty': !item.candidateRemark }" class="tl-remark">{{ item.candidateRemark || '-' }}</span>
                  </p>
                </div>
                <div class="tl-footer">
                  <el-button type="primary" size="small" @click="handleReApply(item)">重新投递</el-button>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无已结束的面试" />
        </el-tab-pane>
      </el-tabs>

      <el-empty v-if="list.length === 0 && !loading" description="暂无面试通知" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserInterviews, respondInterview } from '@/api/user/interview'
import { reApplyApi } from '@/api/user/profile'

const loading = ref(false)
const list = ref([])
const activeTab = ref('active')

const statusMap = {
  0: { text: '待确认', type: 'warning', timeline: 'warning' },
  1: { text: '已接受', type: 'success', timeline: 'success' },
  2: { text: '待定', type: '', timeline: 'primary' },
  3: { text: '面试成功', type: 'success', timeline: 'success' },
  4: { text: '已拒绝', type: 'danger', timeline: 'danger' },
  5: { text: '已过期', type: 'info', timeline: 'info' },
  6: { text: '已取消', type: 'info', timeline: 'info' },
}

const activeInterviews = computed(() => list.value.filter(i => [0, 1, 2].includes(i.status)))
const completedInterviews = computed(() => list.value.filter(i => i.status === 3))
const cancelledInterviews = computed(() => list.value.filter(i => [4, 5, 6].includes(i.status)))

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const res = await getUserInterviews()
    if (res.code === 1) {
      list.value = res.data?.records || res.data || []
    }
  } catch {
    ElMessage.error('获取面试通知失败')
  } finally {
    loading.value = false
  }
}

const handleRespond = (item, status) => {
  const action = status === 1 ? '接受' : '拒绝'
  ElMessageBox.prompt(
    `确定${action}该面试邀请？请填写备注（可选）：`,
    '提示',
    {
      confirmButtonText: `确认${action}`,
      cancelButtonText: '取消',
      type: status === 1 ? 'success' : 'warning',
      inputValue: item.candidateRemark || '',
      inputPlaceholder: '输入备注信息...',
    }
  )
    .then(async ({ value: remark }) => {
      try {
        const res = await respondInterview(item.id, status, remark || '')
        if (res.code === 1) {
          item.status = status
          item.candidateRemark = remark || ''
          ElMessage.success(`已${action}面试邀请`)
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch {
        ElMessage.error('网络异常')
      }
    })
    .catch(() => {})
}

const cancelInterview = (item) => {
  ElMessageBox.confirm('确定取消该面试？取消后可在"已取消"标签页重新投递。', '取消面试', {
    confirmButtonText: '确定取消',
    cancelButtonText: '返回',
    type: 'warning',
  })
    .then(async () => {
      try {
        const res = await respondInterview(item.id, 6, item.candidateRemark || '')
        if (res.code === 1) {
          item.status = 6
          ElMessage.success('已取消该面试')
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch {
        ElMessage.error('网络异常')
      }
    })
    .catch(() => {})
}

const handleReApply = (item) => {
  if (!item.applicationId) {
    ElMessage.warning('无法重新投递，缺少投递记录')
    return
  }
  ElMessageBox.confirm('确定重新投递该职位？', '重新投递', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info',
  })
    .then(async () => {
      try {
        const res = await reApplyApi(item.applicationId)
        if (res.code === 1) {
          ElMessage.success('重新投递成功')
          fetchList()
        } else {
          ElMessage.error(res.msg || '重新投递失败')
        }
      } catch {
        ElMessage.error('网络异常')
      }
    })
    .catch(() => {})
}
</script>

<style scoped>
.sub-hero { display: flex; align-items: center; gap: 16px; padding: 24px 28px; background: linear-gradient(135deg, #f0fdf4, #ecfdf5); border-radius: 14px; margin-bottom: 28px; }
.sub-hero-icon { font-size: 40px; }
.sub-hero h2 { font-size: 20px; font-weight: 700; color: #065f46; margin: 0 0 4px; }
.sub-hero p { font-size: 13px; color: #64748b; margin: 0; }

.im-tabs { margin-bottom: 16px; }

.tl-card { border: none; border-radius: 10px; margin-bottom: 2px; }
.tl-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.tl-header h4 { margin: 0; font-size: 15px; font-weight: 600; color: #0f172a; }

.tl-body p { margin: 4px 0; font-size: 13px; color: #475569; line-height: 1.5; }
.tl-label { color: #94a3b8; }
.tl-remark-row { display: flex; align-items: flex-start; }
.tl-remark { color: #64748b; flex: 1; }
.tl-empty { color: #cbd5e1; }

.tl-footer { margin-top: 8px; padding-top: 8px; border-top: 1px solid #f1f5f9; display: flex; justify-content: flex-end; gap: 8px; }
</style>
