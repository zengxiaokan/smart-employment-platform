<template>
  <div>
    <div class="sub-hero">
      <span class="sub-hero-icon">⭐</span>
      <div>
        <h2>收藏岗位</h2>
        <p>您收藏的心仪岗位，随时查看和投递</p>
      </div>
    </div>

    <div v-loading="loading">
      <el-empty
        v-if="!loading && list.length === 0"
        description="暂无收藏岗位"
      />
      <el-skeleton v-else-if="loading" :rows="4" animated />
      <div v-else class="job-list">
        <div
          v-for="item in list"
          :key="item.job.id"
          class="job-card"
          :class="{ active: item.job.id === activeJobId }"
          @click="selectJob(item)"
        >
          <div class="job-card-header">
            <span class="job-name">{{ item.job.title }}</span>
            <span class="job-salary">{{
              formatSalary(item.job.salaryMin, item.job.salaryMax)
            }}</span>
          </div>
          <div class="job-card-tags">
            <span
              v-if="item.job.tags"
              v-for="tag in parseTags(item.job.tags)"
              :key="tag"
              class="job-tag"
              >{{ tag }}</span
            >
            <span v-else class="job-tag">{{ item.job.category }}</span>
            <span class="job-tag">{{ item.job.experience }}</span>
            <span class="job-tag">{{
              jobEducationLabel(item.job.education)
            }}</span>
          </div>
          <div class="job-card-footer">
            <span class="company-name">{{
              item.job.company || "某互联网公司"
            }}</span>
            <span class="job-loc">{{ item.job.city }}</span>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="detailVisible"
      title="职位详情"
      width="700px"
      destroy-on-close
    >
      <div v-if="currentItem" class="detail-content">
        <div class="detail-header">
          <h2 class="detail-title">{{ currentItem.job.title }}</h2>
          <el-button
            size="small"
            text
            type="warning"
            :icon="WarningFilled"
            class="btn-report"
            @click="handleReport"
            >举报</el-button
          >
          <span class="detail-salary">{{
            formatSalary(currentItem.job.salaryMin, currentItem.job.salaryMax)
          }}</span>
        </div>
        <div class="detail-tags">
          <el-tag
            v-if="currentItem.job.tags"
            v-for="tag in parseTags(currentItem.job.tags)"
            :key="tag"
            size="small"
            >{{ tag }}</el-tag
          >
          <el-tag size="small" type="info">{{
            currentItem.job.experience
          }}</el-tag>
          <el-tag size="small" type="info">{{
            jobEducationLabel(currentItem.job.education)
          }}</el-tag>
        </div>
        <div class="detail-info-row">
          <span
            ><el-icon><MapLocation /></el-icon> {{ currentItem.job.city }}</span
          >
          <span
            ><el-icon><OfficeBuilding /></el-icon>
            {{ currentItem.company?.name || "某互联网公司" }}</span
          >
          <span
            ><el-icon><Place /></el-icon> {{ currentItem.job.address }}</span
          >
        </div>
        <el-divider />
        <div class="detail-section" v-if="currentItem.job.dutyContent">
          <h4>岗位职责</h4>
          <div class="detail-text">{{ currentItem.job.dutyContent }}</div>
        </div>
        <div class="detail-section" v-if="currentItem.job.requireContent">
          <h4>任职要求</h4>
          <div class="detail-text">{{ currentItem.job.requireContent }}</div>
        </div>
        <div class="detail-section" v-if="currentItem.job.benefits">
          <h4>福利待遇</h4>
          <div class="detail-text">{{ currentItem.job.benefits }}</div>
        </div>
        <el-divider />
        <div class="detail-actions">
          <el-button type="primary" @click="handleChat">立即沟通</el-button>
          <el-button
            type="success"
            :disabled="currentItem.submitted"
            @click="handleApply"
          >
            {{ currentItem.submitted ? "已投递" : "投递简历" }}
          </el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="applyDialogVisible"
      title="选择简历投递"
      width="500px"
      destroy-on-close
    >
      <div v-if="resumeList.length === 0" class="no-resume-tip">
        <el-empty description="暂无可用简历" />
        <div class="create-resume-tip">
          <span>还没有简历？</span>
          <el-button type="primary" link @click="goToCreateResume"
            >去创建简历</el-button
          >
        </div>
      </div>
      <div v-else class="resume-list">
        <div
          v-for="resume in resumeList"
          :key="resume.id"
          class="resume-card"
          :class="{ selected: selectedResumeId === resume.id }"
          @click="selectedResumeId = resume.id"
        >
          <div
            class="resume-radio-dot"
            :class="{ active: selectedResumeId === resume.id }"
          ></div>
          <div class="resume-info">
            <div class="resume-name">{{ resume.resumeName || "默认简历" }}</div>
            <div class="resume-meta">
              <span v-if="resume.jobIntention">{{ resume.jobIntention }}</span>
              <span v-if="resume.city"
                ><el-icon><Location /></el-icon>{{ resume.city }}</span
              >
              <span v-if="resume.salaryMin || resume.salaryMax"
                >{{ resume.salaryMin / 1000 }}K-{{
                  resume.salaryMax / 1000
                }}K</span
              >
            </div>
            <div class="resume-desc" v-if="resume.selfDescription">
              {{ resume.selfDescription }}
            </div>
          </div>
          <el-icon
            v-if="selectedResumeId === resume.id"
            class="resume-check-icon"
            ><Check
          /></el-icon>
        </div>
      </div>
      <template #footer v-if="resumeList.length > 0">
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applyLoading" @click="confirmApply"
          >确认投递</el-button
        >
      </template>
    </el-dialog>

    <el-dialog
      v-model="chatVisible"
      title="立即沟通"
      width="750px"
      draggable
      overflow
      append-to-body
      :modal="false"
      :lock-scroll="false"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <ChatPanel compact :target-conversation-id="targetConversationId" />
    </el-dialog>

    <el-dialog
      v-model="reportVisible"
      title="举报职位"
      width="460px"
      :close-on-click-modal="false"
    >
      <p style="margin-bottom: 12px; color: #606266">
        举报职位：<strong>{{ currentItem?.job?.title }}</strong>
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
        <el-button type="danger" :loading="reportSubmitting" @click="doReport"
          >提交举报</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import {
  MapLocation,
  OfficeBuilding,
  Place,
  Check,
  Location,
  WarningFilled,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { getFavoriteJobs, applyJob } from "@/api/user/job";
import { getResumeList } from "@/api/user/resume";
import { useRouter } from "vue-router";
import { jobEducationLabel, formatSalary } from "@/utils/format";
import { createConversation } from "@/api/chat";
import { submitFeedback } from "@/api/feedbacks";
import ChatPanel from "@/components/ChatPanel.vue";

const router = useRouter();
const loading = ref(true);
const list = ref([]);
const activeJobId = ref(null);
const currentItem = ref(null);
const detailVisible = ref(false);
const applyDialogVisible = ref(false);
const applyLoading = ref(false);
const selectedResumeId = ref(null);
const resumeList = ref([]);

const chatVisible = ref(false);
const targetConversationId = ref(null);

// ==================== 举报 ====================
const reportVisible = ref(false);
const reportReason = ref("");
const reportSubmitting = ref(false);

const handleReport = () => {
  reportReason.value = "";
  reportVisible.value = true;
};

const doReport = async () => {
  if (!reportReason.value.trim()) {
    ElMessage.warning("请填写举报原因");
    return;
  }
  reportSubmitting.value = true;
  try {
    await submitFeedback({
      type: 3,
      title: `举报职位：${currentItem.value?.job?.title || ""}`,
      content: reportReason.value,
      targetType: 1,
      targetId: currentItem.value?.job?.id
        ? Number(currentItem.value.job.id)
        : null,
    });
    ElMessage.success("举报已提交");
    reportVisible.value = false;
  } catch {
    /* */
  } finally {
    reportSubmitting.value = false;
  }
};

const parseTags = (tags) => {
  if (!tags) return [];
  if (Array.isArray(tags)) return tags;
  if (typeof tags === "string") return tags.split(",").filter((t) => t.trim());
  return [];
};

const selectJob = (item) => {
  activeJobId.value = item.job.id;
  currentItem.value = item;
  detailVisible.value = true;
};

const handleChat = async () => {
  if (!currentItem.value?.job) return;
  try {
    const res = await createConversation(currentItem.value.job.hrUserId);
    if (res.code === 1 && res.data) {
      targetConversationId.value = res.data.id;
    }
  } catch {}
  chatVisible.value = true;
};

const handleApply = async () => {
  try {
    const res = await getResumeList();
    if (res.code === 1 && res.data) {
      const records = res.data.list || res.data;
      resumeList.value = records;
      if (resumeList.value.length > 0) {
        selectedResumeId.value = resumeList.value[0].id;
        applyDialogVisible.value = true;
      } else {
        ElMessage.warning("请先创建简历");
      }
    } else {
      ElMessage.warning("请先创建简历");
    }
  } catch {
    ElMessage.error("网络异常");
  }
};

const confirmApply = async () => {
  if (!selectedResumeId.value) {
    ElMessage.warning("请选择简历");
    return;
  }
  applyLoading.value = true;
  try {
    const res = await applyJob({
      jobId: currentItem.value.job.id,
      resumeId: selectedResumeId.value,
      companyId: currentItem.value.job.companyId,
    });
    if (res.code === 1) {
      ElMessage.success("简历投递成功");
      currentItem.value.submitted = true;
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

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getFavoriteJobs();
    if (res.code === 1) {
      list.value = res.data || [];
    }
  } catch {
    list.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.sub-hero {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px 28px;
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
  border-radius: 14px;
  margin-bottom: 28px;
}
.sub-hero-icon {
  font-size: 40px;
}
.sub-hero h2 {
  font-size: 20px;
  font-weight: 700;
  color: #92400e;
  margin: 0 0 4px;
}
.sub-hero p {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.job-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.job-card {
  padding: 16px 20px;
  border: 1px solid #e8eef4;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fafbfc;
}

.job-card:hover {
  border-color: #3b82f6;
  background: #eff6ff;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
}

.job-card.active {
  border-color: #3b82f6;
  background: #eff6ff;
}

.job-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.job-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.job-salary {
  font-size: 15px;
  font-weight: 700;
  color: #ef4444;
}

.job-card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.job-tag {
  font-size: 12px;
  color: #64748b;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
}

.job-card-footer {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #94a3b8;
}

.detail-content {
  padding: 8px 0;
}
.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.btn-report {
  flex-shrink: 0;
  font-size: 12px;
  color: #c0c4cc;
  padding: 2px 6px;
  transition: color 0.2s;
}
.btn-report:hover {
  color: #f56c6c !important;
}
.btn-report :deep(.el-icon) {
  font-size: 14px;
}
.detail-title {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}
.detail-salary {
  font-size: 20px;
  font-weight: 700;
  color: #ef4444;
  white-space: nowrap;
}
.detail-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.detail-info-row {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #64748b;
  flex-wrap: wrap;
}
.detail-info-row span {
  display: flex;
  align-items: center;
  gap: 4px;
}
.detail-section {
  margin-bottom: 16px;
}
.detail-section h4 {
  font-size: 15px;
  font-weight: 600;
  color: #3b82f6;
  margin: 0 0 8px 0;
}
.detail-text {
  font-size: 14px;
  color: #475569;
  line-height: 1.8;
  white-space: pre-line;
}
.detail-actions {
  display: flex;
  gap: 12px;
}

.no-resume-tip {
  text-align: center;
  padding: 20px 0;
}
.create-resume-tip {
  margin-top: 12px;
  font-size: 14px;
  color: #64748b;
}
.resume-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.resume-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #e8eef4;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fafbfc;
}
.resume-card:hover {
  border-color: #3b82f6;
  background: #eff6ff;
}
.resume-card.selected {
  border-color: #3b82f6;
  background: #eff6ff;
}
.resume-radio-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #d1d5db;
  flex-shrink: 0;
  transition: all 0.2s;
}
.resume-radio-dot.active {
  border-color: #3b82f6;
  background: #3b82f6;
}
.resume-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.resume-name {
  font-weight: 600;
  font-size: 15px;
  color: #1e293b;
}
.resume-meta {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #64748b;
  flex-wrap: wrap;
}
.resume-meta span {
  display: flex;
  align-items: center;
  gap: 3px;
}
.resume-desc {
  font-size: 12px;
  color: #94a3b8;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.resume-check-icon {
  color: #3b82f6;
  font-size: 18px;
  flex-shrink: 0;
}

@media (max-width: 767px) {
}
</style>
