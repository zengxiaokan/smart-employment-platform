<script setup>
import { ref, watch, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  queryAllJobs,
  applyJob,
  favoriteJob,
  unfavoriteJob,
  recordBrowse,
} from "@/api/user/job";
import { getResumeList } from "@/api/user/resume";
import { parseSkills, formatSalary, parseTags, jobEducationLabel } from "@/utils/format";
import { ElMessage } from "element-plus";
import {
  MapLocation,
  Star,
  Reading,
  Calendar,
  View,
  ChatDotRound,
  ArrowRight,
  WarningFilled,
} from "@element-plus/icons-vue";
import ChatPanel from "@/components/ChatPanel.vue";
import CompanyDetail from "./CompanyDetail.vue";
import { createConversation } from "@/api/chat";
import ReportDialog from "@/components/ReportDialog.vue";

const route = useRoute();
const router = useRouter();

// 1. 数据定义
const jobList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(5);
const loading = ref(false);

// 2. 选中职位逻辑
const activeJobId = ref(null);
const activeItem = ref(null);
const currentJob = computed(() => activeItem.value?.job);
const currentCompany = computed(
  () => activeItem.value?.company || activeItem.value?.conpany
);

const selectJob = async (item) => {
  activeJobId.value = item.job.id;
  activeItem.value = item;
  try {
    await recordBrowse(item.job.id);
  } catch {}
};

// ==================== 立即沟通弹窗 ====================
const chatDialogVisible = ref(false);
const targetConversationId = ref(null);

const openChatDialog = async () => {
  if (!currentJob.value) {
    ElMessage.warning("请先选择一个职位");
    return;
  }
  if (!currentJob.value.hrUserId) {
    ElMessage.warning("该职位缺少HR信息，无法发起沟通");
    return;
  }
  try {
    const res = await createConversation(currentJob.value.hrUserId);
    if (res.code === 1 && res.data) {
      targetConversationId.value = res.data.id;
      chatDialogVisible.value = true;
    } else {
      ElMessage.error(res.msg || "创建会话失败");
      return;
    }
  } catch {
    ElMessage.error("网络异常，无法创建会话");
  }
};

// ==================== 公司详情弹窗 ====================
const companyDialogVisible = ref(false);
const openCompanyDialog = () => {
  const cid = currentCompany.value?.id || currentJob.value?.companyId;
  if (!cid) {
    ElMessage.warning("暂无公司信息");
    return;
  }
  companyDialogVisible.value = true;
};

// ==================== 投递简历弹窗 ====================
const applyDialogVisible = ref(false);
const applyLoading = ref(false);
const selectedResumeId = ref(null);
const resumeList = ref([]);

const openApplyDialog = async () => {
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
      companyId: currentJob.value.companyId,
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

const reportVisible = ref(false);

const handleReport = () => {
  if (!activeItem.value) return;
  reportVisible.value = true;
};

const toggleFavorite = async () => {
  if (!activeItem.value) return;
  const jobId = activeItem.value.job.id;
  const isFav = activeItem.value.favorite;
  try {
    let res;
    if (isFav) {
      res = await unfavoriteJob(jobId);
      if (res.code !== 1) {
        ElMessage.error(res.msg || "取消收藏失败");
        return;
      }
      activeItem.value.favorite = false;
    } else {
      res = await favoriteJob(jobId);
      if (res.code !== 1) {
        ElMessage.error(res.msg || "收藏失败");
        return;
      }
      activeItem.value.favorite = true;
    }
  } catch (error) {
    ElMessage.error(isFav ? "取消收藏失败" : "收藏失败");
  }
};

// 3. 获取数据逻辑
const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: route.query.keyword || "",
      city: route.query.city || "",
    };

    // 调用 APIFox 模拟接口
    const res = await queryAllJobs(params);

    // 适配 APIFox 返回的数据结构 (假设为 { code: 200, data: { list: [], total: 0 } })
    if (res.code === 1) {
      const records = res.data.list || [];
      jobList.value = records;
      total.value = res.data.total;

      if (records.length > 0) {
        selectJob(records[0]);
      } else {
        activeItem.value = null;
        activeJobId.value = null;
      }
    }
  } catch (error) {
    console.error("获取职位列表失败:", error);
  } finally {
    loading.value = false;
  }
};

// 4. 监听逻辑
// 监听路由查询参数变化 (搜索关键词或城市)
watch(
  () => route.query,
  () => {
    currentPage.value = 1; // 重置页码
    fetchData();
  },
  { deep: true }
);

// 处理分页切换
const handlePageChange = (val) => {
  currentPage.value = val;
  fetchData();
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: "smooth" });
};

// 初始化
onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="layout-container" v-loading="loading">
    <!-- 左侧：职位列表 -->
    <aside class="job-list-pane">
      <div v-if="jobList.length > 0">
        <div
          v-for="item in jobList"
          :key="item.job.id"
          class="job-item-card"
          :class="{ active: item.job.id === activeJobId }"
          @click="selectJob(item)"
        >
          <div class="job-card-header">
            <h3 class="job-name">{{ item.job.title }}</h3>
            <span class="job-salary">{{
              formatSalary(item.job.salaryMin, item.job.salaryMax)
            }}</span>
          </div>

          <div class="job-skills-row" v-if="item.job.jobSkills">
            <span
              v-for="skill in parseSkills(item.job.jobSkills)"
              :key="skill"
              class="job-tag skill-tag"
            >
              {{ skill }}
            </span>
          </div>
          <div class="job-card-footer">
            <span class="company-name">{{
              item.company?.name || "某互联网公司"
            }}</span>
            <span class="job-loc">{{
              item.job.city || item.job.location
            }}</span>
          </div>
        </div>

        <!-- 分页组件 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <!-- 无数据状态 -->
      <el-empty v-else description="暂无匹配职位" />
    </aside>

    <!-- 右侧：详情预览 -->
    <section class="job-detail-pane" v-if="currentJob">
      <div class="detail-content-card">
        <!-- 头部信息 -->
        <div class="detail-main-header">
          <div class="header-info">
            <div class="title-row">
              <h1 class="main-title">{{ currentJob.title }}</h1>
              <el-button
                size="small"
                text
                type="warning"
                :icon="WarningFilled"
                class="btn-report"
                @click="handleReport"
                >举报</el-button
              >
              <span class="main-salary">{{
                formatSalary(currentJob.salaryMin, currentJob.salaryMax)
              }}</span>
            </div>
            <div class="sub-info">
              <span
                ><el-icon><MapLocation /></el-icon>
                {{ currentJob.city || currentJob.location }}</span
              >
              <span
                ><el-icon><Calendar /></el-icon>
                {{ currentJob.experience || "3-5年" }}</span
              >
              <span
                ><el-icon><Reading /></el-icon>
                {{ jobEducationLabel(currentJob.education) }}</span
              >
            </div>
          </div>
          <div class="header-actions">
            <div class="action-btns">
              <el-button type="primary" class="btn-chat" @click="openChatDialog"
                >立即沟通</el-button
              >
              <el-button
                type="success"
                class="btn-apply"
                :disabled="activeItem?.submitted"
                @click="openApplyDialog"
              >
                {{ activeItem?.submitted ? "已投递" : "投递简历" }}
              </el-button>
            </div>
            <div class="action-bottom-row">
              <el-button
                class="btn-fav"
                :class="{ 'is-favorited': activeItem?.favorite }"
                @click="toggleFavorite"
              >
                <el-icon><Star /></el-icon>
                {{ activeItem?.favorite ? "已收藏" : "收藏" }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 职位描述 -->
        <div class="detail-body">
          <div class="section-header-row">
            <div class="section-label-group">
              <h4 class="section-label">职位描述</h4>
              <span
                v-if="currentJob.hasCount !== undefined"
                class="has-count-inline"
                >在招 {{ currentJob.hasCount }} 个岗位</span
              >
            </div>
            <div class="job-stats" v-if="currentJob.viewCount !== undefined">
              <span
                ><el-icon><View /></el-icon>
                {{ currentJob.viewCount }} 浏览</span
              >
              <span
                ><el-icon><ChatDotRound /></el-icon>
                {{ currentJob.applyCount }} 投递</span
              >
            </div>
          </div>
          <div class="jd-text">
            <template
              v-if="currentJob.dutyContent || currentJob.requireContent"
            >
              <div class="content-section">
                <p class="content-title">岗位职责：</p>
                <div class="content-body">{{ currentJob.dutyContent }}</div>
              </div>
              <div class="content-section">
                <p class="content-title">任职要求：</p>
                <div class="content-body">{{ currentJob.requireContent }}</div>
              </div>
              <div class="content-section" v-if="currentJob.benefits">
                <p class="content-title">福利待遇：</p>
                <div class="benefits-tags">
                  <el-tag
                    v-for="tag in parseTags(currentJob.benefits)"
                    :key="tag"
                    size="small"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
            </template>
            <div
              v-else-if="currentJob.description"
              v-html="currentJob.description"
            ></div>
            <template v-else>
              <p>岗位职责：</p>
              <ol>
                <li>负责公司核心业务系统的前端架构设计与开发维护；</li>
                <li>主导 Vue3 + TypeScript 技术栈的落地与组件库建设；</li>
                <li>优化前端性能，提升用户体验及系统稳定性；</li>
                <li>配合产品经理完成需求分析，并提供高质量的技术方案。</li>
              </ol>
              <p>任职要求：</p>
              <ul>
                <li>本科及以上学历，3年以上前端开发经验；</li>
                <li>精通 Vue3、React 等主流框架，有大型项目实战经验；</li>
                <li>熟悉工程化工具，如 Vite、Webpack 等；</li>
                <li>具备良好的团队协作能力和技术钻研精神。</li>
              </ul>
            </template>
          </div>
        </div>

        <!-- 公司信息 -->
        <div class="detail-footer">
          <div class="section-header-row">
            <h4 class="section-label company-link" @click="openCompanyDialog">
              公司信息
              <el-icon><ArrowRight /></el-icon>
            </h4>
            <div class="comp-tags">
              <el-tag
                v-for="tag in parseTags(currentJob.tags)"
                :key="tag"
                size="small"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
          <div class="comp-box">
            <img
              :src="
                currentCompany?.logoUrl ||
                'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
              "
              class="comp-logo"
            />
            <div class="comp-info">
              <div class="comp-name-full">
                {{ currentCompany?.name || "某互联网公司" }}
              </div>
              <div class="comp-meta">
                {{ currentCompany?.industry || "互联网" }} ·
                {{ currentCompany?.size || "100-499人" }}
              </div>
              <div class="comp-address" v-if="currentCompany?.address">
                <el-icon><MapLocation /></el-icon>
                {{ currentCompany.address }}
              </div>
              <div class="comp-tags" v-if="currentCompany?.tags">
                <span
                  v-for="tag in parseTags(currentCompany.tags)"
                  :key="tag"
                  class="comp-tag"
                >
                  {{ tag }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <el-dialog
      v-model="chatDialogVisible"
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
      v-model="applyDialogVisible"
      title="投递简历"
      width="520px"
      top="10vh"
    >
      <div class="apply-dialog-body">
        <el-alert
          :title="'投递岗位：' + (currentJob?.title || '')"
          :closable="false"
          show-icon
          class="apply-alert"
        />
        <p class="apply-label">请选择要投递的简历：</p>
        <el-radio-group v-model="selectedResumeId" class="resume-radio-group">
          <el-radio
            v-for="resume in resumeList"
            :key="resume.id"
            :value="resume.id"
            class="resume-radio-item"
            border
          >
            <div class="resume-item-content">
              <span class="resume-item-name">{{
                resume.name || "未命名简历"
              }}</span>
              <span class="resume-item-education">{{
                resume.education || "本科"
              }}</span>
            </div>
          </el-radio>
        </el-radio-group>
        <el-empty
          v-if="resumeList.length === 0"
          description="暂无简历，请先创建简历"
          :image-size="60"
        >
          <el-button type="primary" @click="goToCreateResume"
            >去创建简历</el-button
          >
        </el-empty>
      </div>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="applyLoading"
          :disabled="!selectedResumeId"
          @click="handleApply"
        >
          确认投递
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="companyDialogVisible"
      title="公司详情"
      width="1000px"
      top="5vh"
      destroy-on-close
    >
      <CompanyDetail
        v-if="companyDialogVisible"
        :company-id="currentCompany?.id || currentJob?.companyId"
        :show-back="false"
        :handle-close="() => (companyDialogVisible = false)"
      />
    </el-dialog>

    <ReportDialog v-model:visible="reportVisible" :target-type="1" :target-id="activeItem?.job?.id" :target-name="activeItem?.job?.title || ''" label="职位" />
  </div>
</template>

<style scoped>
.layout-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 20px;
  padding: 0 20px;
}

/* 左侧列表 */
.job-list-pane {
  width: 380px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.job-item-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
  margin-bottom: 12px;
}

.job-item-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.job-item-card.active {
  border-color: #4ade80;
  background-color: #f0fdf4;
}

.job-name {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.job-salary {
  color: #fd7240;
  font-weight: 700;
  font-size: 16px;
}

.job-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.job-card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 12px 0;
}

.job-card-info {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.job-skills-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.job-tag {
  background-color: #f5f7fa;
  color: #666;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.skill-tag {
  background-color: #f5f7fa;
  color: #666;
}

.job-card-footer {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #888;
  padding-top: 12px;
  border-top: 1px solid #f2f2f2;
}

.pagination-wrapper {
  margin-top: 10px;
  display: flex;
  justify-content: center;
  padding: 10px 0;
}

/* 右侧详情 */
.job-detail-pane {
  flex: 1;
}

.detail-content-card {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  min-height: 600px;
  position: sticky;
  top: 84px;
}

.detail-main-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 25px;
  border-bottom: 1px solid #f2f2f2;
}

.header-info {
  flex: 1;
  min-width: 0; /* 允许子元素缩放 */
  margin-right: 30px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 12px;
  flex-wrap: wrap; /* 允许薪资在极小屏幕下换行 */
}

.main-title {
  font-size: 24px; /* 稍微缩小标题字号防止挤压 */
  margin: 0;
  font-weight: 700;
  color: #333;
  line-height: 1.3;
}

.main-salary {
  font-size: 22px;
  color: #fd7240;
  font-weight: 700;
  white-space: nowrap;
}

.sub-info {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.sub-info span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  flex-shrink: 0;
}

.action-btns {
  display: flex;
  gap: 12px;
}

.action-bottom-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.job-stats {
  display: flex;
  gap: 15px;
  color: #909399;
  font-size: 13px;
  white-space: nowrap;
}

.section-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 25px 0 20px;
}

.section-header-row .section-label {
  margin: 0;
}

.section-label-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.has-count-inline {
  font-size: 12px;
  color: #22c55e;
  background: #f0fdf4;
  padding: 2px 10px;
  border-radius: 10px;
  font-weight: 500;
}

.section-header-row .section-label.company-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.section-header-row .section-label.company-link:hover {
  color: #22c55e;
}

.job-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.btn-fav {
  border-color: #e0e0e0;
  color: #666;
}

.btn-fav:hover {
  border-color: #4ade80;
  color: #16a34a;
}

.btn-fav.is-favorited {
  border-color: #22c55e;
  color: #16a34a;
  background: #f0fdf4;
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
.title-row .btn-report {
  margin-right: 8px;
}

.btn-chat {
  background-color: #00bfa5;
  border: none;
  width: 140px;
}

.btn-chat:hover {
  background-color: #00a696;
}

.btn-apply {
  width: 120px;
}

.section-label {
  font-size: 18px;
  font-weight: 700;
  margin: 25px 0 20px;
  position: relative;
  padding-left: 12px;
}

.section-label::before {
  content: "";
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 4px;
  background-color: #4ade80;
  border-radius: 2px;
}

.jd-text {
  line-height: 1.8;
  color: #555;
  font-size: 15px;
}

.content-section {
  margin-bottom: 25px;
}

.content-title {
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
  font-size: 16px;
}

.content-body {
  white-space: pre-wrap;
  color: #606266;
  line-height: 1.6;
}

.jd-text p {
  font-weight: 600;
  margin-top: 15px;
}

.jd-text ol,
.jd-text ul {
  padding-left: 20px;
  margin: 10px 0;
}

.comp-box {
  display: flex;
  align-items: center;
  gap: 15px;
  background: #f8f9fb;
  padding: 20px;
  border-radius: 8px;
}

.comp-logo {
  width: 56px;
  height: 56px;
  border-radius: 8px;
}

.comp-name-full {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 4px;
}

.comp-meta {
  color: #888;
  font-size: 13px;
}

.comp-address {
  color: #666;
  font-size: 13px;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.benefits-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.comp-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.comp-tag {
  background-color: #f5f7fa;
  color: #666;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

/* 响应式适配 */
@media (max-width: 1024px) {
  .layout-container {
    flex-direction: column;
  }

  .job-list-pane {
    width: 100%;
  }

  .detail-content-card {
    position: static;
  }
}

/* ==================== 投递弹窗样式 ==================== */
.apply-dialog-body {
  padding: 4px 0;
}

.apply-alert {
  margin-bottom: 20px;
}

.apply-alert :deep(.el-alert__title) {
  font-weight: 700;
}
/* 标题加粗 */
:deep(.apply-alert .el-alert__title) {
  font-weight: 700;
  color: #0c4a6e; /* 深蓝色文字，和蓝色背景更搭配 */
}

:deep(.apply-alert) {
  background: linear-gradient(135deg, #dcfce7 0%, #f0fdf4 100%);
  border-color: #86efac;
  border-left: 4px solid #22c55e;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.1);
}

.apply-label {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 12px 0;
}

.resume-radio-group {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.resume-radio-item {
  width: 100%;
  margin-right: 0 !important;
  padding: 14px 16px;
  border-radius: 10px;
}

.resume-item-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 12px;
}

.resume-item-name {
  font-size: 15px;

  color: #1e293b;
}

.resume-item-education {
  font-size: 13px;
  color: #64748b;
  background: #f1f5f9;
  padding: 2px 10px;
  border-radius: 4px;
}
</style>
