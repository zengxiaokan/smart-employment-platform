<template>
  <div class="company-detail" v-loading="loading">
    <div class="back-btn" v-if="showBack">
      <el-button :icon="ArrowLeft" @click="goBack">返回公司列表</el-button>
    </div>

    <div class="company-profile" v-if="companyDetail">
      <div class="profile-header">
        <img :src="companyDetail.logoUrl" class="company-logo" />
        <div class="company-info">
          <h1 class="company-name">{{ companyDetail.name }}</h1>
          <div class="company-tags">
            <el-tag size="small" type="info">{{
              companyDetail.industry
            }}</el-tag>
            <el-tag size="small">{{ companyDetail.size }}</el-tag>
          </div>
          <div class="company-meta">
            <span v-if="companyDetail.address"
              ><el-icon><Location /></el-icon> {{ companyDetail.address }}</span
            >
          </div>
          <el-button
            size="small"
            text
            type="warning"
            :icon="WarningFilled"
            class="btn-report"
            @click="handleReportCompany"
            >举报</el-button
          >
        </div>
      </div>
      <div class="company-desc">
        <h3>公司介绍</h3>
        <p>{{ companyDetail.description }}</p>
      </div>
    </div>

    <div class="company-jobs" v-if="companyDetail">
      <h3 class="section-title">在招职位 ({{ jobsTotal || 0 }} 个)</h3>

      <div class="job-list">
        <div
          v-for="job in jobList"
          :key="job.id"
          class="job-card"
          @click="openJobDetail(job)"
        >
          <div class="job-main">
            <h4 class="job-name">{{ job.title }}</h4>
            <div class="job-tags">
              <el-tag size="small" type="warning" v-if="job.city">{{
                job.city
              }}</el-tag>
              <el-tag size="small" v-if="job.experience">{{
                job.experience
              }}</el-tag>
              <el-tag size="small" v-if="job.education !== undefined">{{
                formatEducation(job.education)
              }}</el-tag>
              <el-tag
                v-for="skill in parseSkills(job.jobSkills)"
                :key="skill"
                size="small"
                effect="plain"
              >
                {{ skill }}
              </el-tag>
              <el-tag
                size="small"
                v-if="job.hasCount !== undefined || job.headcount !== undefined"
                type="success"
                >在招{{ job.hasCount ?? job.headcount }}个岗位</el-tag
              >
            </div>
          </div>
          <div class="job-salary">
            {{ formatSalary(job.salaryMin, job.salaryMax) }}
          </div>
        </div>
        <el-empty v-if="jobList.length === 0" description="暂无在招职位" />
      </div>

      <div class="load-more" v-if="jobList.length > 0 && !noMoreJobs">
        <el-button @click="loadMore" :loading="jobsLoading">加载更多</el-button>
      </div>
      <div class="no-more" v-if="noMoreJobs && jobList.length > 0">
        <span>没有更多岗位了</span>
      </div>
    </div>

    <el-dialog
      v-model="jobDialogVisible"
      :title="currentJob?.title"
      width="700px"
    >
      <div class="job-detail-content" v-if="currentJob">
        <div class="detail-header">
          <div class="detail-salary">
            {{ formatSalary(currentJob.salaryMin, currentJob.salaryMax) }}
          </div>
          <div class="detail-tags">
            <el-tag size="small" v-if="currentJob.city">{{
              currentJob.city
            }}</el-tag>
            <el-tag size="small" v-if="currentJob.experience">{{
              currentJob.experience
            }}</el-tag>
            <el-tag size="small" v-if="currentJob.education !== undefined">{{
              formatEducation(currentJob.education)
            }}</el-tag>
            <el-tag size="small">{{ currentJob.category }}</el-tag>
            <el-tag
              size="small"
              v-if="
                currentJob.hasCount !== undefined ||
                currentJob.headcount !== undefined
              "
              type="success"
              >在招{{
                currentJob.hasCount ?? currentJob.headcount
              }}个岗位</el-tag
            >
            <el-tag
              v-for="skill in parseSkills(currentJob.jobSkills)"
              :key="skill"
              size="small"
              effect="plain"
            >
              {{ skill }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <div class="detail-section">
          <h4>职位详情</h4>
          <p class="detail-text" v-if="currentJob.dutyContent">
            {{ currentJob.dutyContent }}
          </p>
        </div>

        <div class="detail-section" v-if="currentJob.requireContent">
          <h4>岗位要求</h4>
          <p class="detail-text">{{ currentJob.requireContent }}</p>
        </div>

        <div class="detail-section" v-if="currentJob.benefits">
          <h4>福利待遇</h4>
          <div class="benefits-tags">
            <el-tag
              v-for="tag in parseTags(currentJob.benefits)"
              :key="tag"
              size="small"
              type="success"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>

        <div class="detail-section" v-if="currentJob.address">
          <h4>工作地址</h4>
          <p class="detail-text">{{ currentJob.address }}</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="openApplyDialog"
            >投递简历</el-button
          >
          <el-button @click="openChatDialog">立即沟通</el-button>
          <el-button
            text
            type="warning"
            :icon="WarningFilled"
            @click="handleReportJob"
            >举报</el-button
          >
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="jobReportVisible"
      title="举报职位"
      width="460px"
      :close-on-click-modal="false"
    >
      <p style="margin-bottom: 12px; color: #606266">
        举报职位：<strong>{{ currentJob?.title }}</strong>
      </p>
      <el-input
        v-model="jobReportReason"
        type="textarea"
        :rows="4"
        maxlength="300"
        show-word-limit
        placeholder="请详细描述举报原因..."
      />
      <template #footer>
        <el-button @click="jobReportVisible = false">取消</el-button>
        <el-button
          type="danger"
          :loading="jobReportSubmitting"
          @click="doReportJob"
          >提交举报</el-button
        >
      </template>
    </el-dialog>

    <el-dialog v-model="applyDialogVisible" title="选择简历投递" width="500px">
      <div class="apply-dialog-content">
        <el-empty
          v-if="resumeList.length === 0"
          description="您还没有简历，请先创建简历"
        >
          <el-button type="primary" @click="goToCreateResume"
            >去创建简历</el-button
          >
        </el-empty>
        <div v-else>
          <p class="apply-tip">请选择要投递的简历：</p>
          <el-radio-group v-model="selectedResumeId" class="resume-radio-group">
            <el-radio
              v-for="resume in resumeList"
              :key="resume.id"
              :value="resume.id"
              class="resume-radio-item"
            >
              <div class="resume-info">
                <span class="resume-name">{{ resume.name || "默认简历" }}</span>
                <span class="resume-updated" v-if="resume.updatedAt">
                  更新于 {{ formatDate(resume.updatedAt) }}
                </span>
              </div>
            </el-radio>
          </el-radio-group>
        </div>
      </div>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleApply"
          :loading="applyLoading"
          :disabled="!selectedResumeId"
        >
          确认投递
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="chatDialogVisible"
      title="立即沟通"
      width="600px"
      draggable
      overflow
      append-to-body
      :modal="false"
      :lock-scroll="false"
    >
      <div class="chat-dialog-content">
        <div class="chat-notice">
          <el-icon><ChatDotRound /></el-icon>
          <span
            >即将与 HR 沟通职位：<strong>{{ currentJob?.title }}</strong></span
          >
        </div>
        <ChatPanel compact :target-conversation-id="targetConversationId" />
      </div>
    </el-dialog>

    <el-dialog
      v-model="reportVisible"
      title="举报公司"
      width="460px"
      :close-on-click-modal="false"
    >
      <p style="margin-bottom: 12px; color: #606266">
        举报公司：<strong>{{ companyDetail?.name }}</strong>
      </p>
      <el-input
        v-model="reportReason"
        type="textarea"
        :rows="4"
        maxlength="300"
        show-word-limit
        placeholder="请详细描述举报原因..."
      />
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button
          type="danger"
          :loading="reportSubmitting"
          @click="doReportCompany"
          >提交举报</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, defineProps } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  ArrowLeft,
  Location,
  ChatDotRound,
  WarningFilled,
} from "@element-plus/icons-vue";
import { getCompanyDetail, getCompanyJobs } from "@/api/user/company";
import { applyJob } from "@/api/user/job";
import { getResumeList } from "@/api/user/resume";
import { parseSkills, formatSalary } from "@/utils/format";
import { ElMessage } from "element-plus";
import ChatPanel from "@/components/ChatPanel.vue";
import { createConversation } from "@/api/chat";
import { submitFeedback } from "@/api/feedbacks";

const props = defineProps({
  companyId: {
    type: [Number, String],
    default: null,
  },
  showBack: {
    type: Boolean,
    default: true,
  },
  handleClose: {
    type: Function,
    default: null,
  },
});

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const companyDetail = ref(null);
const companyId = ref(props.companyId || route.params.id);

const jobsLoading = ref(false);
const jobList = ref([]);
const jobsTotal = ref(0);
const jobsCurrentPage = ref(1);
const jobsPageSize = ref(10);

const jobDialogVisible = ref(false);
const currentJob = ref(null);

const applyDialogVisible = ref(false);
const applyLoading = ref(false);
const selectedResumeId = ref(null);
const resumeList = ref([]);

const reportVisible = ref(false);
const reportReason = ref("");
const reportSubmitting = ref(false);

const handleReportCompany = () => {
  reportReason.value = "";
  reportVisible.value = true;
};

// ==================== 职位举报 ====================
const jobReportVisible = ref(false);
const jobReportReason = ref("");
const jobReportSubmitting = ref(false);

const handleReportJob = () => {
  jobReportReason.value = "";
  jobReportVisible.value = true;
};

const doReportJob = async () => {
  if (!jobReportReason.value.trim()) {
    ElMessage.warning("请填写举报原因");
    return;
  }
  jobReportSubmitting.value = true;
  try {
    await submitFeedback({
      type: 3,
      title: `举报职位：${currentJob.value?.title || ""}`,
      content: jobReportReason.value,
      targetType: 1,
      targetId: currentJob.value?.id ? Number(currentJob.value.id) : null,
    });
    ElMessage.success("举报已提交");
    jobReportVisible.value = false;
  } catch {
    /* */
  } finally {
    jobReportSubmitting.value = false;
  }
};

const doReportCompany = async () => {
  if (!reportReason.value.trim()) {
    ElMessage.warning("请填写举报原因");
    return;
  }
  reportSubmitting.value = true;
  try {
    await submitFeedback({
      type: 3,
      title: `举报公司：${companyDetail.value?.name || ""}`,
      content: reportReason.value,
      targetType: 2,
      targetId: companyId.value ? Number(companyId.value) : null,
    });
    ElMessage.success("举报已提交");
    reportVisible.value = false;
  } catch {
    /* */
  } finally {
    reportSubmitting.value = false;
  }
};

const chatDialogVisible = ref(false);
const targetConversationId = ref(null);
const noMoreJobs = ref(false);

const openJobDetail = (job) => {
  currentJob.value = job;
  jobDialogVisible.value = true;
};

const openApplyDialog = () => {
  jobDialogVisible.value = false;
  openApplyDialogFn();
};

const openApplyDialogFn = async () => {
  if (!currentJob.value) {
    ElMessage.warning("请先选择一个职位");
    return;
  }
  try {
    const res = await getResumeList();
    if (res.code === 1 && res.data) {
      resumeList.value = res.data.list || [];
      if (resumeList.value.length > 0) {
        selectedResumeId.value = resumeList.value[0].id;
      }
    }
  } catch {
    resumeList.value = [];
  }
  applyDialogVisible.value = true;
};

const handleApply = async () => {
  if (!selectedResumeId.value || !currentJob.value) return;
  applyLoading.value = true;
  try {
    const res = await applyJob({
      jobId: currentJob.value.id,
      resumeId: selectedResumeId.value,
      companyId: companyDetail.value?.id,
    });
    if (res.code === 1) {
      ElMessage.success("简历投递成功");
      applyDialogVisible.value = false;
    } else {
      ElMessage.error(res.msg || "投递失败");
    }
  } catch {
    ElMessage.error("网络异常，投递失败");
  } finally {
    applyLoading.value = false;
  }
};

const goToCreateResume = () => {
  applyDialogVisible.value = false;
  router.push("/user/resume/edit");
};

const openChatDialog = async () => {
  jobDialogVisible.value = false;
  if (!currentJob.value) {
    ElMessage.warning("请先选择一个职位");
    return;
  }
  try {
    const res = await createConversation(currentJob.value.hrUserId);
    if (res.code === 1 && res.data) {
      targetConversationId.value = res.data.id;
    }
  } catch {}
  chatDialogVisible.value = true;
};

const fetchCompanyDetail = async () => {
  loading.value = true;
  try {
    const res = await getCompanyDetail(companyId.value);
    if (res.code === 1 && res.data) {
      companyDetail.value = res.data.companyVO;
      jobList.value = res.data.jobList || [];
      jobsTotal.value =
        res.data.companyVO.jobCount || res.data.jobList?.length || 0;
    }
  } catch (error) {
    console.error("获取公司详情失败:", error);
  } finally {
    loading.value = false;
  }
};

const loadMore = async () => {
  jobsLoading.value = true;
  jobsCurrentPage.value++;
  try {
    const res = await getCompanyJobs(companyId.value, {
      page: jobsCurrentPage.value,
      pageSize: jobsPageSize.value,
    });
    if (res.code === 1 && res.data) {
      const newJobs =
        res.data.jobList || res.data.records || res.data.list || res.data;
      if (!newJobs || newJobs.length === 0) {
        noMoreJobs.value = true;
        jobsLoading.value = false;
        return;
      }
      jobList.value = [...jobList.value, ...newJobs];
      if (jobList.value.length >= jobsTotal) {
        noMoreJobs.value = true;
      }
    }
  } catch (error) {
    console.error("获取公司职位列表失败:", error);
    jobsCurrentPage.value--;
  } finally {
    jobsLoading.value = false;
  }
};

const formatEducation = (level) => {
  const map = {
    0: "不限",
    1: "初中",
    2: "高中",
    3: "中专",
    4: "大专",
    5: "本科",
    6: "硕士",
    7: "博士",
  };
  return map[level] || "不限";
};

const parseTags = (tags) => {
  if (!tags) return [];
  if (Array.isArray(tags)) return tags;
  if (typeof tags === "string") {
    return tags.split(",").filter((t) => t.trim() !== "");
  }
  return [];
};

const formatDate = (dateStr) => {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(
    2,
    "0"
  )}-${String(date.getDate()).padStart(2, "0")}`;
};

const goBack = () => {
  if (props.handleClose) {
    props.handleClose();
  } else {
    router.back();
  }
};

onMounted(() => {
  if (companyId.value) {
    fetchCompanyDetail();
  }
});
</script>

<style scoped>
.company-detail {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

.back-btn {
  margin-bottom: 20px;
}

.company-profile {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  margin-bottom: 24px;
  border: 1px solid #f0f2f5;
}

.profile-header {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.company-logo {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  object-fit: cover;
  border: 1px solid #f0f2f5;
  flex-shrink: 0;
}

.company-info {
  flex: 1;
}

.company-name {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 12px 0;
}

.company-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.company-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  font-size: 14px;
  color: #64748b;
}

.company-desc {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f2f5;
}

.company-desc h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 12px 0;
}

.company-desc p {
  font-size: 14px;
  color: #64748b;
  line-height: 1.8;
  margin: 0;
}

.company-jobs {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  border: 1px solid #f0f2f5;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 20px 0;
}

.job-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 100px;
}

.job-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border: 1px solid #f0f2f5;
  border-radius: 8px;
  transition: all 0.2s;
  cursor: pointer;
}

.job-card:hover {
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
}

.job-main {
  flex: 1;
}

.job-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.job-tags .el-tag {
  background-color: #f5f7fa;
  color: #666;
  border: none;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.job-salary {
  font-size: 16px;
  font-weight: 700;
  color: #ef4444;
  margin-left: 20px;
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.no-more {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  font-size: 14px;
  color: #909399;
}

.job-detail-content {
  padding: 10px 0;
}

.detail-header {
  margin-bottom: 16px;
}

.detail-salary {
  font-size: 24px;
  font-weight: 700;
  color: #ef4444;
  margin-bottom: 12px;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 10px 0;
}

.detail-text {
  font-size: 14px;
  color: #64748b;
  line-height: 1.8;
  margin: 0;
  white-space: pre-line;
}

.benefits-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.apply-dialog-content {
  min-height: 100px;
}

.apply-tip {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 16px 0;
}

.resume-radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.resume-radio-item {
  padding: 12px 16px;
  border: 1px solid #f0f2f5;
  border-radius: 8px;
  width: 100%;
  display: flex;
  align-items: flex-start;
}

.resume-radio-item :deep(.el-radio__label) {
  flex: 1;
}

.resume-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-left: 8px;
}

.resume-name {
  font-weight: 600;
  color: #1e293b;
}

.resume-updated {
  font-size: 12px;
  color: #909399;
}

.chat-dialog-content {
  min-height: 300px;
}

.chat-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f0f9ff;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #64748b;
}

.chat-notice strong {
  color: #1e293b;
}

.btn-report {
  flex-shrink: 0;
  font-size: 12px;
  color: #c0c4cc;
  padding: 2px 6px;
  white-space: nowrap;
  transition: color 0.2s;
}
.btn-report:hover {
  color: #f56c6c !important;
}
.btn-report :deep(.el-icon) {
  font-size: 14px;
}
</style>
