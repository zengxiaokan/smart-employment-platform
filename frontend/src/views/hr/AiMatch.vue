<template>
  <div class="am-root">
    <div class="am-page-head">
      <div class="am-head-left">
        <h1 class="am-head-title">智能匹配</h1>
        <p class="am-head-desc">AI 智能分析，精准推荐最合适的人才</p>
      </div>
    </div>

    <el-row :gutter="16" class="am-stats">
      <el-col :span="6">
        <el-card shadow="hover" class="am-stat-card">
          <div class="am-stat-inner">
            <div class="am-stat-text">
              <div class="am-stat-label">在招岗位</div>
              <div class="am-stat-value">{{ totalHeadcount }}</div>
              <div class="am-stat-unit">个</div>
            </div>
            <div class="am-stat-icon s-blue">
              <el-icon :size="32"><Briefcase /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="am-stat-card">
          <div class="am-stat-inner">
            <div class="am-stat-text">
              <div class="am-stat-label">当前岗位候选</div>
              <div class="am-stat-value">{{ matchCount ?? '-' }}</div>
              <div class="am-stat-unit">人</div>
            </div>
            <div class="am-stat-icon s-green">
              <el-icon :size="32"><User /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="am-stat-card">
          <div class="am-stat-inner">
            <div class="am-stat-text">
              <div class="am-stat-label">高匹配人才</div>
              <div class="am-stat-value">{{ highMatchCount }}</div>
              <div class="am-stat-unit">人</div>
            </div>
            <div class="am-stat-icon s-orange">
              <el-icon :size="32"><Star /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="am-stat-card">
          <div class="am-stat-inner">
            <div class="am-stat-text">
              <div class="am-stat-label">最高匹配分</div>
              <div class="am-stat-value">{{ maxScore }}</div>
              <div class="am-stat-unit">分</div>
            </div>
            <div class="am-stat-icon s-purple">
              <el-icon :size="32"><Trophy /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="am-main">
      <div class="am-left">
        <div class="am-left-bar">
          <span class="am-left-title">公司岗位</span>
          <el-button size="small" text :icon="Refresh" :loading="jobLoading" @click="fetchJobs" />
        </div>
        <div class="am-job-scroll" v-loading="jobLoading">
          <div v-if="jobs.length === 0 && !jobLoading" class="am-empty">暂无岗位</div>
          <div
            v-for="job in jobs"
            :key="job.id"
            class="am-job-card"
            :class="{ active: job.id === selectedJobId }"
            @click="selectJob(job)"
          >
            <div class="am-job-top">
              <span class="am-job-name">{{ job.title }}</span>
              <span class="am-job-salary">{{ salaryShort(job.salaryMin, job.salaryMax) }}</span>
            </div>
            <div class="am-job-tags" v-if="job.jobSkills">
              <el-tag v-for="s in parseSkillList(job.jobSkills)" :key="s" size="small" effect="plain">{{ s }}</el-tag>
            </div>
            <div class="am-job-bot">
              <span>{{ job.city || '-' }}</span>
              <span>{{ job.experience || '经验不限' }}</span>
              <span class="am-job-hot">{{ job.applyCount || 0 }}人投递</span>
            </div>
          </div>
        </div>
      </div>

      <div class="am-right">
        <div v-if="!selectedJobId" class="am-placeholder">
          <el-empty description="请从左侧选择一个岗位，查看 AI 匹配结果" />
        </div>

        <template v-else>
          <div class="am-right-bar">
            <div class="am-right-title">
              <span>{{ selectedJob?.title }}</span>
              <el-tag v-if="matchCount !== null" size="small" effect="plain">{{ matchCount }}位候选人</el-tag>
            </div>
          </div>

          <div class="am-candidate-list">
            <div v-if="matchLoading" class="am-c-loading-placeholder">
              <div class="am-c-loading-box">
                <el-icon class="am-c-loading-icon" :size="32"><Loading /></el-icon>
                <p class="am-c-loading-text">智能分析中，请稍候...</p>
                <p class="am-c-loading-sub">正在分析匹配候选人数据</p>
              </div>
            </div>
            <div v-else-if="matchList.length === 0" class="am-placeholder">
              <el-empty description="暂无匹配结果" />
            </div>

            <div v-for="m in matchList" :key="m.matchId" class="am-c-card" :class="scoreClass(m.totalScore)">
              <div class="am-c-top">
                <div class="am-c-left">
                  <el-avatar :size="48" :src="m.avatar">{{ m.name?.charAt(0) || '?' }}</el-avatar>
                  <div class="am-c-info">
                    <div class="am-c-name">{{ m.name }}</div>
                    <div class="am-c-meta">
                      <span v-if="m.education">{{ m.education }}</span>
                    </div>
                    <div class="am-c-skills" v-if="m.matchedSkills">
                      <el-tag v-for="s in parseSkillList(m.matchedSkills)" :key="s" size="small" type="success" effect="light">{{ s }}</el-tag>
                    </div>
                  </div>
                </div>
                <div class="am-c-score" :class="scoreClass(m.totalScore)">
                  <span class="am-c-score-num">{{ m.totalScore }}</span>
                  <span class="am-c-score-unit">分</span>
                  <span class="am-c-score-tag">{{ scoreLabel(m.totalScore) }}</span>
                </div>
              </div>

              <div class="am-c-breakdown">
                <div class="am-c-bar">
                  <span class="am-c-bar-label">技能</span>
                  <el-progress :percentage="(m.skillScore / 30) * 100" :stroke-width="6" :show-text="false" color="#67c23a" />
                  <span class="am-c-bar-val">{{ m.skillScore }}</span>
                </div>
                <div class="am-c-bar">
                  <span class="am-c-bar-label">经验</span>
                  <el-progress :percentage="(m.experienceScore / 25) * 100" :stroke-width="6" :show-text="false" color="#409eff" />
                  <span class="am-c-bar-val">{{ m.experienceScore }}</span>
                </div>
                <div class="am-c-bar">
                  <span class="am-c-bar-label">学历</span>
                  <el-progress :percentage="(m.educationScore / 30) * 100" :stroke-width="6" :show-text="false" color="#e6a23c" />
                  <span class="am-c-bar-val">{{ m.educationScore }}</span>
                </div>
                <div class="am-c-bar">
                  <span class="am-c-bar-label">薪资</span>
                  <el-progress :percentage="(m.salaryScore / 15) * 100" :stroke-width="6" :show-text="false" color="#f56c6c" />
                  <span class="am-c-bar-val">{{ m.salaryScore }}</span>
                </div>
              </div>

              <div class="am-c-actions">
                <el-button type="primary" size="small" @click="viewResume(m)">查看简历</el-button>
                <el-button size="small" @click="openReport(m)">查看报告</el-button>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>

    <el-dialog v-model="resumeVisible" title="简历详情" width="700px" destroy-on-close>
      <div v-loading="resumeLoading" style="min-height:200px">
        <div v-if="currentResume" class="rd-root">
          <div class="rd-head">
            <el-avatar :size="64" :src="currentResume.characterAvatar">{{ currentResume.name?.charAt(0) || '?' }}</el-avatar>
            <div class="rd-head-info">
              <div class="rd-name">{{ currentResume.name }}</div>
              <div class="rd-meta">
                <span>{{ genderLabel(currentResume.gender) }}</span>
                <span>{{ currentResume.age }}岁</span>
                <span>{{ educationLabel(currentResume.education) }}</span>
                <span v-if="currentResume.graduationSchool">{{ currentResume.graduationSchool }}</span>
                <span v-if="currentResume.totalWorkYears != null">{{ currentResume.totalWorkYears }}年</span>
                <span v-if="currentResume.maxEducation != null">{{ maxEducationLabel(currentResume.maxEducation) }}</span>
                <span>{{ currentResume.phone }}</span>
                <span>{{ currentResume.email || '-' }}</span>
              </div>
            </div>
          </div>
          <div class="rd-row" v-if="currentResume.jobIntention || currentResume.city">
            <span class="rd-label">求职意向</span>
            <span>{{ currentResume.jobIntention || '-' }} · {{ currentResume.city || '-' }} · {{ salaryLabel(currentResume.salaryMin, currentResume.salaryMax) }}</span>
          </div>
          <div class="rd-row" v-if="currentResume.skills">
            <span class="rd-label">技能标签</span>
            <span><el-tag v-for="s in parseSkillsArr(currentResume.skills)" :key="s" size="small">{{ s }}</el-tag></span>
          </div>
          <div class="rd-row" v-if="currentResume.selfDescription">
            <span class="rd-label">自我描述</span>
            <span>{{ currentResume.selfDescription }}</span>
          </div>
          <div class="rd-section" v-if="currentResume.educations?.length">
            <div class="rd-section-title">教育经历</div>
            <div v-for="e in currentResume.educations" :key="e.id" class="rd-section-item">
              <strong>{{ e.school }}</strong> · {{ e.major }} · {{ educationLabel(e.education) }} · {{ e.startTime }} — {{ e.endTime }}
            </div>
          </div>
          <div class="rd-section" v-if="currentResume.experiences?.length">
            <div class="rd-section-title">工作经历</div>
            <div v-for="e in currentResume.experiences" :key="e.id" class="rd-section-item">
              <strong>{{ e.company }}</strong> · {{ e.position }} · {{ e.startTime }} — {{ e.endTime }}
              <div v-if="e.description" class="rd-desc">{{ e.description }}</div>
            </div>
          </div>
          <div class="rd-section" v-if="currentResume.projects?.length">
            <div class="rd-section-title">项目经历</div>
            <div v-for="p in currentResume.projects" :key="p.id" class="rd-section-item">
              <strong>{{ p.name }}</strong> · {{ p.role }} · {{ p.startTime }} — {{ p.endTime }}
              <div v-if="p.description" class="rd-desc">{{ p.description }}</div>
            </div>
          </div>
        </div>
        <div v-else-if="!resumeLoading" class="am-placeholder"><el-empty description="简历详情获取失败" /></div>
      </div>
    </el-dialog>

    <el-dialog v-model="interviewVisible" title="发送面试邀约" width="500px" destroy-on-close>
      <el-form :model="interviewForm" label-width="100px">
        <el-form-item label="面试时间" required>
          <el-date-picker v-model="interviewForm.interviewTime" type="datetime" placeholder="选择面试时间" :disabled-date="disablePastDate" style="width:100%" />
        </el-form-item>
        <el-form-item label="面试方式">
          <el-radio-group v-model="interviewForm.locationType">
            <el-radio :label="0">线下面试</el-radio>
            <el-radio :label="1">视频面试</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="面试地点/链接" required>
          <el-input v-model="interviewForm.interviewLocation" :placeholder="interviewForm.locationType === 0 ? '如：公司大楼3层会议室' : '如：腾讯会议、Zoom链接'" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="interviewForm.contactPerson" placeholder="HR联系人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="interviewForm.contactPhone" placeholder="联系电话" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="interviewForm.remark" type="textarea" :rows="2" placeholder="给求职者的备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="interviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="interviewSaving" @click="confirmInterview">确认发送</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reportVisible" title="匹配报告" width="550px" destroy-on-close>
      <div v-loading="reportLoading" style="min-height:120px">
        <div v-if="reportLoading" class="md-loading">
          <p class="md-loading-text">加载匹配报告...</p>
        </div>
        <div v-else-if="reportData" class="md-root">
          <div class="md-total">
            <span class="md-total-label">综合得分</span>
            <span class="md-total-value" :class="scoreClass(reportData.totalScore)">{{ reportData.totalScore }}</span>
            <span class="md-total-unit">分</span>
          </div>
          <div class="md-bar">
            <div class="md-bar-item">
              <span class="md-bar-label">技能 (满分30)</span>
              <el-progress :percentage="(reportData.skillScore / 30) * 100" :stroke-width="8" :show-text="false" color="#67c23a" style="flex:1;margin:0 10px" />
              <span class="md-bar-val">{{ reportData.skillScore }}</span>
            </div>
            <div class="md-bar-item">
              <span class="md-bar-label">经验 (满分25)</span>
              <el-progress :percentage="(reportData.experienceScore / 25) * 100" :stroke-width="8" :show-text="false" color="#409eff" style="flex:1;margin:0 10px" />
              <span class="md-bar-val">{{ reportData.experienceScore }}</span>
            </div>
            <div class="md-bar-item">
              <span class="md-bar-label">学历 (满分30)</span>
              <el-progress :percentage="(reportData.educationScore / 30) * 100" :stroke-width="8" :show-text="false" color="#e6a23c" style="flex:1;margin:0 10px" />
              <span class="md-bar-val">{{ reportData.educationScore }}</span>
            </div>
            <div class="md-bar-item">
              <span class="md-bar-label">薪资 (满分15)</span>
              <el-progress :percentage="(reportData.salaryScore / 15) * 100" :stroke-width="8" :show-text="false" color="#f56c6c" style="flex:1;margin:0 10px" />
              <span class="md-bar-val">{{ reportData.salaryScore }}</span>
            </div>
          </div>
          <div class="md-skills" v-if="reportData.skillDetails?.length">
            <div class="md-section-title">技能匹配明细</div>
            <div class="md-skill-row" v-for="sd in reportData.skillDetails" :key="sd.skillName">
              <span class="md-skill-name">{{ sd.skillName }}</span>
              <el-tag :type="sd.matched === 1 ? 'success' : 'danger'" size="small">{{ sd.matched === 1 ? '✓ 匹配' : '✗ 不匹配' }}</el-tag>
            </div>
          </div>

          <div class="md-summary" v-if="reportData.matchSummary">
            <div class="md-section-title">AI 推荐语</div>
            <p>{{ reportData.matchSummary }}</p>
          </div>

          <div class="md-ai-action">
            <el-button type="primary" :loading="aiSummaryLoading" :icon="MagicStick" @click="generateAiSummary">
              {{ aiSummaryLoading ? 'AI分析中...' : (reportData.matchSummary ? '再次总结' : 'AI分析') }}
            </el-button>
            <span class="md-ai-hint">{{ reportData.matchSummary ? '重新生成智能分析评语' : '点击生成智能分析评语' }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, MagicStick, Briefcase, User, Star, Trophy, Loading } from '@element-plus/icons-vue'
import { getMatchJobs, getMatchList, getMatchDetail, refreshMatchSummary } from '@/api/hr/match'
import { getResumeDetail } from '@/api/hr/resume'
import { createInterviewInvitation, getInterviewByJobAndUser } from '@/api/hr/interview'
import { maxEducationLabel, educationLabel } from '@/utils/format'

const jobLoading = ref(false)
const jobs = ref([])
const selectedJobId = ref(null)
const selectedJob = ref(null)

const matchLoading = ref(false)
const matchList = ref([])
const matchCount = ref(null)

const resumeVisible = ref(false)
const resumeLoading = ref(false)
const currentResume = ref(null)

const interviewVisible = ref(false)
const interviewSaving = ref(false)
const interviewRow = ref(null)
const existingInterviewId = ref(null)
const existingInterviewStatus = ref(null)
const interviewForm = ref({
  interviewTime: '',
  locationType: 0,
  interviewLocation: '',
  contactPerson: '',
  contactPhone: '',
  remark: '',
})

const reportVisible = ref(false)
const reportLoading = ref(false)
const reportData = ref(null)
const aiSummaryLoading = ref(false)
let currentReportMatchId = null

const highMatchCount = computed(() => matchList.value.filter(m => m.totalScore >= 85).length)
const maxScore = computed(() => {
  if (matchList.value.length === 0) return '-'
  return Math.max(...matchList.value.map(m => m.totalScore))
})
const totalHeadcount = computed(() => jobs.value.reduce((s, j) => s + (j.hasCount || 0), 0))

const fetchJobs = async () => {
  jobLoading.value = true
  try {
    const res = await getMatchJobs()
    if (res.code === 1 && res.data) {
      jobs.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
    }
  } catch { jobs.value = [] }
  finally { jobLoading.value = false }
}

const selectJob = (job) => {
  selectedJobId.value = job.id
  selectedJob.value = job
  matchList.value = []
  matchCount.value = null
  fetchMatchList(job.id)
}

const fetchMatchList = async (jobId) => {
  matchLoading.value = true
  try {
    const res = await getMatchList(jobId)
    if (res.code === 1 && res.data) {
      matchList.value = res.data.records || res.data || []
      matchCount.value = matchList.value.length
    } else {
      matchList.value = []
      matchCount.value = 0
    }
  } catch { matchList.value = []; matchCount.value = null }
  finally { matchLoading.value = false }
}

const scoreClass = (score) => {
  if (score >= 80) return 'high'
  if (score >= 50) return 'mid'
  return 'low'
}

const scoreLabel = (score) => {
  if (score >= 80) return '高度匹配'
  if (score >= 50) return '较匹配'
  return '待考察'
}

const parseSkillList = (skills) => {
  if (!skills) return []
  if (Array.isArray(skills)) return skills
  return skills.split(/[,，、]/).filter(Boolean).map(s => s.trim())
}

const parseSkillsArr = (skills) => {
  if (!skills) return []
  if (Array.isArray(skills)) return skills
  return skills.split(',').filter(Boolean)
}

const salaryShort = (min, max) => {
  if (!min && !max) return '面议'
  if (!min) return max + 'K'
  if (!max) return min + 'K+'
  return min + '-' + max + 'K'
}

const genderLabel = (v) => (v === 1 ? '男' : '女')
const salaryLabel = (min, max) => {
  if (!min && !max) return '面议'
  if (!min) return max + '及以下'
  if (!max) return min + '及以上'
  return min + '-' + max
}

const viewResume = async (m) => {
  resumeVisible.value = true
  resumeLoading.value = true
  currentResume.value = null
  try {
    const res = await getResumeDetail(m.resumeId, m.applicationId)
    if (res.code === 1 && res.data) {
      currentResume.value = res.data
    } else {
      ElMessage.warning('简历详情获取失败')
    }
  } catch { ElMessage.error('获取简历详情失败') }
  finally { resumeLoading.value = false }
}

const inviteInterview = async (m) => {
  interviewRow.value = m
  interviewVisible.value = true
  existingInterviewId.value = null
  existingInterviewStatus.value = null
  interviewForm.value = {
    interviewTime: '',
    locationType: 0,
    interviewLocation: '',
    contactPerson: '',
    contactPhone: '',
    remark: '',
  }
  interviewSaving.value = true
  try {
    const res = await getInterviewByJobAndUser(selectedJobId.value, m.userId)
    if (res.code === 1 && res.data) {
      const d = res.data
      existingInterviewId.value = d.id || null
      existingInterviewStatus.value = d.status
      interviewForm.value.interviewTime = d.interviewTime || ''
      interviewForm.value.locationType = d.locationType ?? 0
      interviewForm.value.interviewLocation = d.interviewLocation || ''
      interviewForm.value.contactPerson = d.contactPerson || ''
      interviewForm.value.contactPhone = d.contactPhone || ''
      interviewForm.value.remark = d.hrRemark || d.remark || ''
    }
  } catch {}
  finally { interviewSaving.value = false }
}

const disablePastDate = (date) => {
  return date.getTime() < Date.now() - 86400000;
};

const confirmInterview = async () => {
  if (!interviewForm.value.interviewTime) return ElMessage.warning('请选择面试时间')
  if (!interviewForm.value.interviewLocation.trim()) return ElMessage.warning('请填写面试地点/链接')
  interviewSaving.value = true
  try {
    const payload = {
      applicationId: interviewRow.value.applicationId,
      jobId: selectedJobId.value,
      userId: interviewRow.value.userId,
      interviewTime: interviewForm.value.interviewTime,
      locationType: interviewForm.value.locationType,
      interviewLocation: interviewForm.value.interviewLocation,
      contactPerson: interviewForm.value.contactPerson || undefined,
      contactPhone: interviewForm.value.contactPhone || undefined,
      remark: interviewForm.value.remark || undefined,
    }
    if (existingInterviewId.value) {
      payload.id = existingInterviewId.value
      payload.status = existingInterviewStatus.value
    }
    const res = await createInterviewInvitation(payload)
    if (res.code === 1) {
      ElMessage.success('面试邀约已发送')
      interviewVisible.value = false
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } catch { ElMessage.error('网络异常') }
  finally { interviewSaving.value = false }
}

const openReport = async (m) => {
  reportVisible.value = true
  reportLoading.value = true
  reportData.value = null
  currentReportMatchId = m.matchId
  try {
    const res = await getMatchDetail(m.matchId)
    if (res.code === 1 && res.data) {
      reportData.value = res.data
    } else {
      ElMessage.warning('匹配详情获取失败')
    }
  } catch { ElMessage.error('获取匹配详情失败') }
  finally { reportLoading.value = false }
}

const generateAiSummary = async () => {
  if (!currentReportMatchId) return
  aiSummaryLoading.value = true
  try {
    const res = await refreshMatchSummary(currentReportMatchId)
    if (res.code === 1 && res.data) {
      reportData.value.matchSummary = res.data.matchSummary || res.data
      ElMessage.success('AI 分析完成')
    } else {
      ElMessage.warning(res.msg || 'AI 分析失败')
    }
  } catch { ElMessage.error('AI 分析失败') }
  finally { aiSummaryLoading.value = false }
}

onMounted(() => {
  fetchJobs()
})
</script>

<style scoped>
.am-root { padding: 0; }
.am-page-head { margin-bottom: 20px; }
.am-head-title { font-size: 22px; font-weight: 700; margin: 0 0 4px; color: #1a1a1a; }
.am-head-desc { font-size: 14px; color: #909399; margin: 0; }

.am-stats { margin-bottom: 20px; }
.am-stat-card :deep(.el-card__body) { padding: 18px 20px; }
.am-stat-inner { display: flex; justify-content: space-between; align-items: center; }
.am-stat-text { flex: 1; }
.am-stat-label { font-size: 13px; color: #909399; margin-bottom: 4px; }
.am-stat-value { font-size: 28px; font-weight: 700; color: #1a1a1a; line-height: 1.2; }
.am-stat-unit { font-size: 12px; color: #c0c4cc; }
.am-stat-icon { width: 52px; height: 52px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.am-stat-icon.s-blue { background: #ecf5ff; color: #409eff; }
.am-stat-icon.s-green { background: #f0f9eb; color: #67c23a; }
.am-stat-icon.s-orange { background: #fdf6ec; color: #e6a23c; }
.am-stat-icon.s-purple { background: #f4f0fe; color: #9b59b6; }

.am-main { display: flex; gap: 20px; height: calc(100vh - 270px); min-height: 400px; }
.am-left { width: 300px; min-width: 300px; background: #fff; border-radius: 8px; display: flex; flex-direction: column; overflow: hidden; }
.am-left-bar { display: flex; justify-content: space-between; align-items: center; padding: 14px 16px; border-bottom: 1px solid #ebeef5; }
.am-left-title { font-size: 15px; font-weight: 600; }
.am-job-scroll { flex: 1; overflow-y: auto; padding: 8px 12px; }
.am-empty { padding: 40px 0; text-align: center; color: #c0c4cc; font-size: 13px; }

.am-job-card {
  padding: 12px 14px;
  margin-bottom: 8px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #ebeef5;
  transition: all .15s;
}
.am-job-card:hover { border-color: #409eff; background: #f0f5ff; }
.am-job-card.active { border-color: #409eff; background: #ecf5ff; }
.am-job-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.am-job-name { font-size: 14px; font-weight: 600; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 180px; }
.am-job-salary { font-size: 14px; font-weight: 600; color: #f56c6c; white-space: nowrap; }
.am-job-tags { display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 6px; }
.am-job-bot { display: flex; gap: 12px; font-size: 12px; color: #909399; }
.am-job-hot { color: #f56c6c; margin-left: auto; }

.am-right { flex: 1; background: #fff; border-radius: 8px; display: flex; flex-direction: column; overflow: hidden; }
.am-placeholder { display: flex; justify-content: center; align-items: center; flex: 1; }
.am-c-loading-placeholder { display: flex; justify-content: center; align-items: center; flex: 1; min-height: 300px; }
.am-c-loading-box { display: flex; flex-direction: column; align-items: center; }
.am-c-loading-icon { color: #409eff; animation: am-spin 1s linear infinite; }
@keyframes am-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.am-c-loading-text { font-size: 16px; font-weight: 600; color: #303133; margin: 16px 0 6px; }
.am-c-loading-sub { font-size: 13px; color: #909399; margin: 0; }
.am-right-bar { display: flex; justify-content: space-between; align-items: center; padding: 14px 20px; border-bottom: 1px solid #ebeef5; }
.am-right-title { display: flex; align-items: center; gap: 10px; font-size: 16px; font-weight: 600; }

.am-candidate-list { flex: 1; overflow-y: auto; padding: 16px 20px; display: flex; flex-direction: column; gap: 14px; min-height: 0; }
.am-c-card {
  flex-shrink: 0;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 16px 18px;
  transition: all .25s;
  position: relative;
  overflow: hidden;
  background: #fff;
}
.am-c-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: #dcdfe6;
  border-radius: 10px 0 0 10px;
  transition: width .2s;
}
.am-c-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,.08);
  transform: translateY(-1px);
  border-color: #c6e2ff;
}

.am-c-card.high { background: #fafdf8; }
.am-c-card.high::before { background: #95d475; }
.am-c-card.high:hover { border-color: #b3e19d; box-shadow: 0 4px 16px rgba(103,194,58,.1); }

.am-c-card.mid { background: #fefcf5; }
.am-c-card.mid::before { background: #e6a23c; }
.am-c-card.mid:hover { border-color: #f0c78a; box-shadow: 0 4px 16px rgba(230,162,60,.1); }

.am-c-card.low { background: #fefafa; }
.am-c-card.low::before { background: #fab6b6; }
.am-c-card.low:hover { border-color: #fbc4c4; box-shadow: 0 4px 16px rgba(245,108,108,.08); }

.am-c-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.am-c-left { display: flex; align-items: flex-start; gap: 12px; flex: 1; }
.am-c-info { flex: 1; }
.am-c-name { font-size: 15px; font-weight: 600; color: #303133; margin-bottom: 2px; }
.am-c-meta { font-size: 12px; color: #909399; margin-bottom: 6px; }
.am-c-skills { display: flex; flex-wrap: wrap; gap: 4px; }

.am-c-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 72px;
  padding: 6px 10px;
  border-radius: 8px;
  background: #f5f7fa;
}
.am-c-score.high { background: #f0f9eb; }
.am-c-score.mid { background: #fdf6ec; }
.am-c-score.low { background: #fef0f0; }
.am-c-score-num { font-size: 26px; font-weight: 700; line-height: 1; }
.am-c-score.high .am-c-score-num { color: #67c23a; }
.am-c-score.mid .am-c-score-num { color: #e6a23c; }
.am-c-score.low .am-c-score-num { color: #f56c6c; }
.am-c-score-unit { font-size: 11px; color: #909399; }
.am-c-score-tag { font-size: 10px; color: #c0c4cc; margin-top: 2px; }

.am-c-breakdown { margin-bottom: 10px; }
.am-c-bar { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.am-c-bar-label { width: 28px; font-size: 11px; color: #909399; text-align: right; flex-shrink: 0; }
.am-c-bar .el-progress { flex: 1; }
.am-c-bar-val { width: 20px; font-size: 11px; color: #606266; text-align: right; font-weight: 500; flex-shrink: 0; }

.am-c-summary { background: #f0f5ff; padding: 8px 10px; border-radius: 6px; font-size: 13px; color: #606266; margin-bottom: 12px; display: flex; align-items: flex-start; gap: 4px; line-height: 1.5; }
.am-c-summary .el-icon { margin-top: 2px; color: #409eff; flex-shrink: 0; }

.am-c-actions { display: flex; gap: 8px; }

.rd-root { font-size: 14px; }
.rd-head { display: flex; align-items: center; gap: 14px; margin-bottom: 18px; }
.rd-head-info { flex: 1; }
.rd-name { font-size: 18px; font-weight: 600; margin-bottom: 6px; }
.rd-meta { display: flex; flex-wrap: wrap; gap: 4px 14px; color: #606266; font-size: 13px; }
.rd-row { display: flex; margin-bottom: 12px; }
.rd-label { width: 80px; min-width: 80px; color: #909399; font-size: 13px; }
.rd-section { margin-top: 16px; }
.rd-section-title { font-weight: 600; margin-bottom: 8px; border-left: 3px solid #409eff; padding-left: 8px; }
.rd-section-item { padding: 6px 0; font-size: 13px; color: #606266; }
.rd-desc { color: #999; font-size: 12px; margin-top: 4px; }

.md-root { font-size: 14px; }
.md-total { text-align: center; margin-bottom: 20px; }
.md-total-label { font-size: 13px; color: #909399; display: block; margin-bottom: 4px; }
.md-total-value { font-size: 48px; font-weight: 700; color: #409eff; }
.md-total-value.high { color: #67c23a; }
.md-total-value.mid { color: #e6a23c; }
.md-total-value.low { color: #f56c6c; }
.md-total-unit { font-size: 16px; color: #909399; margin-left: 4px; }
.md-bar { display: flex; flex-direction: column; gap: 10px; margin-bottom: 20px; }
.md-bar-item { display: flex; justify-content: space-between; align-items: center; }
.md-bar-label { color: #606266; }
.md-bar-val { font-weight: 600; color: #409eff; }
.md-skills { margin-bottom: 16px; }
.md-section-title { font-weight: 600; margin-bottom: 8px; border-left: 3px solid #409eff; padding-left: 8px; }
.md-skill-row { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; }
.md-skill-name { font-size: 13px; color: #606266; }
.md-summary p { color: #606266; line-height: 1.6; margin: 0; }
.md-ai-action { display: flex; align-items: center; gap: 12px; padding: 16px 0 0; border-top: 1px solid #ebeef5; margin-top: 8px; }
.md-ai-hint { font-size: 13px; color: #909399; }
.md-loading { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 40px 0; }
.md-loading-text { font-size: 16px; font-weight: 600; color: #303133; margin: 0; }
</style>
