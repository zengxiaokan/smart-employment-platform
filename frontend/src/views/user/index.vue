<template>
  <div ref="rootRef" class="home-root" v-loading="loading">
    <section class="hero reveal-item">
      <div class="hero-rings"></div>
      <div class="hero-rings rings-alt"></div>
      <div class="hero-grid section-container">
        <div class="hero-copy">
          <div class="hero-badge">智能就业服务平台</div>
          <h1 class="hero-title">
            让求职首页
            <span class="hero-highlight">更像未来产品</span>
          </h1>
          <p class="hero-desc">
            不只是职位列表，而是更有节奏感的浏览体验。滚动有层次、点击有反馈、内容有记忆点，让平台第一眼就更有质感。
          </p>
          <div class="hero-actions">
            <el-button type="primary" size="large" round @click="goToJobsPage"
              >浏览职位</el-button
            >
            <el-button
              size="large"
              round
              class="hero-btn-outline"
              @click="scrollToJobs"
              >滚动探索</el-button
            >
          </div>
          <div class="hero-tags">
            <span>实时沟通</span>
            <span>AI 匹配</span>
            <span>沉浸式交互</span>
          </div>
        </div>

        <div class="hero-visual">
          <div class="visual-panel main-panel">
            <div class="panel-top">
              <span class="status-dot"></span>
              <strong>智能推荐引擎</strong>
            </div>
            <div class="signal-list">
              <div class="signal-item">
                <span>岗位匹配度</span>
                <strong>92%</strong>
              </div>
              <div class="signal-item">
                <span>在线企业响应</span>
                <strong>1.8h</strong>
              </div>
              <div class="signal-item">
                <span>本周活跃职位</span>
                <strong>{{ stats.jobs }}</strong>
              </div>
            </div>
            <div class="signal-bars">
              <span
                v-for="i in 8"
                :key="i"
                :style="{ animationDelay: `${i * 0.12}s` }"
              ></span>
            </div>
          </div>

          <div class="visual-card float-card card-a">
            <span>热门行业</span>
            <strong>互联网 / 制造 / 新能源</strong>
          </div>
          <div class="visual-card float-card card-b">
            <span>本周趋势</span>
            <strong>岗位热度持续上升</strong>
          </div>
          <div class="visual-orb"></div>
        </div>
      </div>
    </section>

    <section class="feature-strip section-container reveal-item">
      <div class="feature-strip-card">
        <div class="feature-item">
          <div class="feature-icon feature-icon-green">
            <el-icon :size="24"><TrendCharts /></el-icon>
          </div>
          <div>
            <h3>智能匹配</h3>
            <p>根据岗位画像与候选人能力做更精准的推荐排序。</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon feature-icon-purple">
            <el-icon :size="24"><ChatDotRound /></el-icon>
          </div>
          <div>
            <h3>实时沟通</h3>
            <p>内置在线消息，缩短等待与沟通路径。</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon feature-icon-blue">
            <el-icon :size="24"><Monitor /></el-icon>
          </div>
          <div>
            <h3>双端协作</h3>
            <p>求职者、HR、管理员三端体验风格统一但角色清晰。</p>
          </div>
        </div>
      </div>
    </section>

    <section class="stats section-container reveal-item">
      <div class="section-heading">
        <div>
          <span class="section-kicker">平台数据</span>
          <h2>正在发生的职业连接</h2>
        </div>
        <p>更清晰的数据层和更强的卡片反馈，让数字本身也具备展示感。</p>
      </div>
      <div class="stats-grid">
        <div class="stat-card">
          <span class="stat-label">在招岗位</span>
          <strong class="stat-number">{{ stats.jobs }}</strong>
          <em>高质量机会持续上新</em>
        </div>
        <div class="stat-card">
          <span class="stat-label">入驻企业</span>
          <strong class="stat-number">{{ stats.companies }}</strong>
          <em>覆盖更多真实招聘场景</em>
        </div>
        <div class="stat-card">
          <span class="stat-label">求职者</span>
          <strong class="stat-number">{{ stats.candidates }}</strong>
          <em>人才画像不断丰富</em>
        </div>
        <div class="stat-card accent-card">
          <span class="stat-label">成功匹配</span>
          <strong class="stat-number">{{ stats.matches }}</strong>
          <em>从浏览到沟通再到投递更顺滑</em>
        </div>
      </div>
    </section>

    <section class="jobs-area section-container reveal-item" ref="jobsRef">
      <div class="section-header">
        <div class="section-header-left">
          <span class="section-kicker">岗位发现</span>
          <h2>发现更适合你的工作</h2>
          <p>推荐与热门双通道切换，点击卡片即可快速查看职位详情。</p>
        </div>
        <div class="toggle-pill">
          <button
            class="toggle-btn"
            :class="{ active: activeTab === 'recommend' }"
            @click="activeTab = 'recommend'"
          >
            <el-icon><Star /></el-icon> 推荐
          </button>
          <button
            class="toggle-btn"
            :class="{ active: activeTab === 'hot' }"
            @click="activeTab = 'hot'"
          >
            <el-icon><TrendCharts /></el-icon> 热门
          </button>
          <div
            class="toggle-slider"
            :class="{ right: activeTab === 'hot' }"
          ></div>
        </div>
      </div>

      <div v-if="activeTab === 'recommend'" class="jobs-grid">
        <div
          v-for="(item, index) in recommendedJobs"
          :key="(item.job || item).id"
          class="job-card reveal-item"
          :style="{ transitionDelay: `${Math.min(index, 5) * 70}ms` }"
          @click="openJobDetail(item.job || item, item.companyName || '')"
        >
          <div class="job-card-top">
            <div
              class="job-card-badge match-badge"
              :class="getMatchClass(item.matchScore || 0)"
            >
              {{ item.matchScore || 0 }}% 匹配
            </div>
            <span class="job-arrow">Explore</span>
          </div>
          <h4 class="job-card-title">{{ (item.job || item).title }}</h4>
          <div class="job-card-meta">
            <span
              ><el-icon><MapLocation /></el-icon
              >{{ (item.job || item).city }}</span
            >
            <span>{{ (item.job || item).experience || "经验不限" }}</span>
            <span>{{ jobEducationLabel((item.job || item).education) }}</span>
          </div>
          <div class="job-card-tags" v-if="(item.job || item).jobSkills">
            <span
              v-for="s in parseSkills((item.job || item).jobSkills)"
              :key="s"
              class="job-tag"
            >
              {{ s }}
            </span>
          </div>
          <div class="job-card-footer">
            <span class="job-salary">
              {{
                formatSalary(
                  (item.job || item).salaryMin,
                  (item.job || item).salaryMax
                )
              }}
            </span>
            <span class="job-company-name" v-if="item.companyName" @click.stop="goToCompanyDetail((item.job || item).companyId)">{{ item.companyName }}</span>
            <span class="job-stats-mini">
              <el-icon><View /></el-icon
              >{{ (item.job || item).viewCount || 0 }}
              <el-icon><ChatDotRound /></el-icon
              >{{ (item.job || item).applyCount || 0 }}
            </span>
          </div>
        </div>
      </div>

      <div v-if="activeTab === 'hot'" class="jobs-grid">
        <div
          v-for="(item, index) in hotJobs"
          :key="(item.job || item).id"
          class="job-card reveal-item"
          :style="{ transitionDelay: `${Math.min(index, 5) * 70}ms` }"
          @click="openJobDetail(item.job || item, item.companyName || '')"
        >
          <div class="job-card-top">
            <div class="job-card-badge hot-badge">HOT</div>
            <span class="job-arrow">Trending</span>
          </div>
          <h4 class="job-card-title">{{ (item.job || item).title }}</h4>
          <div class="job-card-meta">
            <span
              ><el-icon><MapLocation /></el-icon>{{ (item.job || item).city }}</span
            >
            <span>{{ (item.job || item).experience || "经验不限" }}</span>
            <span>{{ jobEducationLabel((item.job || item).education) }}</span>
          </div>
          <div class="job-card-tags" v-if="(item.job || item).jobSkills">
            <span
              v-for="s in parseSkills((item.job || item).jobSkills)"
              :key="s"
              class="job-tag"
              >{{ s }}</span
            >
          </div>
          <div class="job-card-footer">
            <span class="job-salary">{{
              formatSalary((item.job || item).salaryMin, (item.job || item).salaryMax)
            }}</span>
            <span class="job-company-name" v-if="item.companyName" @click.stop="goToCompanyDetail((item.job || item).companyId)">{{ item.companyName }}</span>
            <span class="job-stats-mini">
              <el-icon><View /></el-icon>{{ (item.job || item).viewCount || 0 }}
              <el-icon><ChatDotRound /></el-icon>{{ (item.job || item).applyCount || 0 }}
            </span>
          </div>
        </div>
      </div>

      <el-empty
        v-if="activeTab === 'recommend' && recommendedJobs.length === 0"
        description="暂无推荐岗位"
      />
      <el-empty
        v-if="activeTab === 'hot' && hotJobs.length === 0"
        description="暂无热门岗位"
      />

      <div class="jobs-view-all">
        <el-button size="large" round @click="goToJobsPage">
          查看全部职位 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </section>

    <section class="why-us section-container reveal-item">
      <div class="section-heading">
        <div>
          <span class="section-kicker">平台亮点</span>
          <h2>为什么它会比普通后台更耐看</h2>
        </div>
        <p>不是堆特效，而是让信息层级、动作反馈和视觉节奏一起服务于体验。</p>
      </div>
      <div class="why-grid">
        <div class="why-card">
          <div class="why-icon-wrap">
            <el-icon :size="24"><Compass /></el-icon>
          </div>
          <h4>开发初心</h4>
          <p>
            打破信息不对称，利用智能算法连接人才与企业，让求职不再盲目、招聘不再低效。
          </p>
        </div>
        <div class="why-card">
          <div class="why-icon-wrap why-icon-purple">
            <el-icon :size="24"><UserFilled /></el-icon>
          </div>
          <h4>角色受益</h4>
          <div class="why-roles">
            <span><strong>求职者</strong> 获得更精准的岗位推荐与反馈路径</span>
            <span><strong>企业 HR</strong> 更快发现候选人并建立联系</span>
            <span
              ><strong>管理员</strong> 更直观感知系统运行状态与生态数据</span
            >
          </div>
        </div>
        <div class="why-card">
          <div class="why-icon-wrap why-icon-blue">
            <el-icon :size="24"><Lightning /></el-icon>
          </div>
          <h4>视觉优势</h4>
          <div class="why-features">
            <span>滚动显现与悬浮反馈增强浏览节奏</span>
            <span>大面积渐变与玻璃卡片营造科技感</span>
            <span>核心信息更聚焦，不会显得花但无重点</span>
            <span>保留 Element Plus 的稳定性与开发效率</span>
          </div>
        </div>
      </div>
    </section>

    <section class="journey section-container reveal-item">
      <div class="journey-panel">
        <div class="journey-copy">
          <span class="section-kicker">体验路径</span>
          <h2>从浏览到沟通，一条更顺滑的求职链路</h2>
        </div>
        <div class="journey-steps">
          <div class="journey-step">
            <strong>01</strong>
            <span>浏览推荐职位</span>
          </div>
          <div class="journey-step">
            <strong>02</strong>
            <span>查看详情与技能标签</span>
          </div>
          <div class="journey-step">
            <strong>03</strong>
            <span>立即沟通或投递简历</span>
          </div>
          <div class="journey-step">
            <strong>04</strong>
            <span>进入后续面试流程</span>
          </div>
        </div>
      </div>
    </section>

    <el-dialog
      v-model="detailVisible"
      :title="detailJob?.title || '职位详情'"
      width="680px"
      top="5vh"
      append-to-body
      destroy-on-close
    >
      <div class="detail-dialog" v-if="detailJob">
        <div class="detail-top">
          <div class="detail-salary-badge">
            {{ formatSalary(detailJob.salaryMin, detailJob.salaryMax) }}
          </div>
          <div class="detail-tags-row">
            <el-tag size="small" type="info">{{ detailJob.city }}</el-tag>
            <el-tag size="small">{{
              detailJob.experience || "经验不限"
            }}</el-tag>
            <el-tag size="small">{{
              jobEducationLabel(detailJob.education)
            }}</el-tag>
            <el-tag v-if="detailJob.category" size="small">{{
              detailJob.category
            }}</el-tag>
          </div>
        </div>
        <div class="detail-company" v-if="detailCompanyName" @click="goToCompanyDetail(detailJob.companyId)">
          <el-icon><OfficeBuilding /></el-icon>
          <span>{{ detailCompanyName }}</span>
          <el-icon class="company-arrow"><ArrowRight /></el-icon>
        </div>
        <el-divider />
        <div class="detail-section" v-if="detailJob.dutyContent">
          <h4>岗位职责</h4>
          <p class="detail-text">{{ detailJob.dutyContent }}</p>
        </div>
        <div class="detail-section" v-if="detailJob.requireContent">
          <h4>任职要求</h4>
          <p class="detail-text">{{ detailJob.requireContent }}</p>
        </div>
        <div class="detail-section" v-if="detailJob.benefits">
          <h4>福利待遇</h4>
          <div class="benefits-wrap">
            <el-tag
              v-for="tag in parseTags(detailJob.benefits)"
              :key="tag"
              size="small"
              type="success"
              >{{ tag }}</el-tag
            >
          </div>
        </div>
        <div class="detail-section" v-if="detailJob.tags">
          <h4>职位亮点</h4>
          <div class="benefits-wrap">
            <el-tag
              v-for="tag in parseTags(detailJob.tags)"
              :key="tag"
              size="small"
              >{{ tag }}</el-tag
            >
          </div>
        </div>
        <div class="detail-section" v-if="detailJob.address">
          <h4>工作地址</h4>
          <p class="detail-text">
            <el-icon><MapLocation /></el-icon>{{ detailJob.address }}
          </p>
        </div>
        <div class="detail-stats">
          <span
            ><el-icon><View /></el-icon>{{ detailJob.viewCount }} 浏览</span
          >
          <span
            ><el-icon><ChatDotRound /></el-icon
            >{{ detailJob.applyCount }} 投递</span
          >
          <span
            ><el-icon><Calendar /></el-icon>在招
            {{ detailJob.hasCount ?? detailJob.headcount ?? 0 }} 个岗位</span
          >
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="success" @click="openChatFromDetail"
          >立即沟通</el-button
        >
        <el-button type="primary" @click="openApplyFromDetail"
          >投递简历</el-button
        >
        <el-button @click="goToJobsPage">查看更多职位</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 弹窗：沟通 ==================== -->
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

    <!-- ==================== 弹窗：投递 ==================== -->
    <el-dialog v-model="applyVisible" title="投递简历" width="520px" top="10vh">
      <div class="apply-body">
        <el-alert
          :title="'投递岗位：' + (detailJob?.title || '')"
          :closable="false"
          show-icon
          class="apply-alert"
        />
        <p class="apply-label">请选择要投递的简历：</p>
        <el-radio-group v-model="selectedResumeId" class="resume-radios">
          <el-radio
            v-for="r in resumeList"
            :key="r.id"
            :value="r.id"
            class="resume-radio"
            border
          >
            <span>{{ r.name || "未命名简历" }}</span>
            <span class="resume-edu">{{ r.education || "本科" }}</span>
          </el-radio>
        </el-radio-group>
        <el-empty
          v-if="resumeList.length === 0"
          description="暂无简历，请先创建简历"
          :image-size="60"
        >
          <el-button type="primary" @click="goCreateResume"
            >去创建简历</el-button
          >
        </el-empty>
      </div>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="applyLoading"
          :disabled="!selectedResumeId"
          @click="handleApplySubmit"
          >确认投递</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive, nextTick, watch } from "vue";
import { useRouter } from "vue-router";
import {
  Compass,
  UserFilled,
  Lightning,
  Star,
  MapLocation,
  View,
  ChatDotRound,
  Calendar,
  ArrowRight,
  TrendCharts,
  Monitor,
  OfficeBuilding,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { getRecommendedJobs, applyJob, getHotJobs } from "@/api/user/job";
import { getHomeStats } from "@/api/user/user";
import { getResumeList } from "@/api/user/resume";
import { parseSkills, formatSalary, jobEducationLabel, parseTags } from "@/utils/format";
import ChatPanel from "@/components/ChatPanel.vue";
import { createConversation } from "@/api/chat";

const router = useRouter();
const loading = ref(false);
const activeTab = ref("recommend");
const recommendedJobs = ref([]);
const hotJobs = ref([]);
const detailVisible = ref(false);
const detailJob = ref(null);
const detailCompanyName = ref("");
const chatVisible = ref(false);
const targetConversationId = ref(null);
const applyVisible = ref(false);
const applyLoading = ref(false);
const selectedResumeId = ref(null);
const resumeList = ref([]);
const jobsRef = ref(null);
const rootRef = ref(null);
let revealObserver = null;

const stats = reactive({
  jobs: "--",
  companies: "--",
  candidates: "--",
  matches: "--",
});

const formatStatsNum = (n) => {
  if (n == null) return "--";
  return n.toLocaleString("en-US") + "+";
};

const fetchHomeStats = async () => {
  try {
    const res = await getHomeStats();
    if (res?.data) {
      stats.jobs = formatStatsNum(res.data.jobs);
      stats.companies = formatStatsNum(res.data.companies);
      stats.candidates = formatStatsNum(res.data.candidates);
      stats.matches = formatStatsNum(res.data.matches);
    }
  } catch (e) {
    // 降级保持默认值
  }
};

const getMatchClass = (score) => {
  if (score >= 80) return "match-high";
  if (score >= 50) return "match-mid";
  return "match-low";
};

const fetchRecommendedJobs = async () => {
  loading.value = true;
  try {
    const res = await getRecommendedJobs();
    if (res.code === 1 && res.data) {
      recommendedJobs.value =
        res.data.records || res.data.list || res.data || [];
      nextTick(setupRevealAnimation);
    }
  } catch (error) {
    console.error("获取推荐岗位失败:", error);
  } finally {
    loading.value = false;
  }
};

const fetchHotJobs = async () => {
  try {
    const res = await getHotJobs(6);
    if (res.code === 1 && res.data) {
      hotJobs.value = res.data || [];
      nextTick(setupRevealAnimation);
    }
  } catch {
    /* ignore */
  }
};

const openJobDetail = (job, companyName = "") => {
  detailJob.value = job;
  detailCompanyName.value = companyName;
  detailVisible.value = true;
};

const goToCompanyDetail = (companyId) => {
  if (!companyId) {
    ElMessage.warning("暂无公司信息");
    return;
  }
  detailVisible.value = false;
  router.push(`/user/company/${companyId}`);
};

const scrollToJobs = () => {
  jobsRef.value?.scrollIntoView({ behavior: "smooth", block: "start" });
};

const goToJobsPage = () => {
  detailVisible.value = false;
  router.push({ path: "/user/jobs" });
};

const openChatFromDetail = async () => {
  if (!detailJob.value) return;
  if (!detailJob.value.hrUserId) {
    ElMessage.warning("该职位缺少HR信息，无法发起沟通");
    return;
  }
  try {
    const res = await createConversation(detailJob.value.hrUserId);
    if (res.code === 1 && res.data) {
      targetConversationId.value = res.data.id;
      chatVisible.value = true;
    } else {
      ElMessage.error(res.msg || "创建会话失败");
    }
  } catch {
    ElMessage.error("网络异常，无法创建会话");
  }
};

const openApplyFromDetail = async () => {
  if (!detailJob.value) return;
  try {
    const res = await getResumeList();
    if (res.code === 1 && res.data) {
      resumeList.value = res.data.list || [];
      if (resumeList.value.length > 0)
        selectedResumeId.value = resumeList.value[0].id;
    }
  } catch {
    resumeList.value = [];
  }
  applyVisible.value = true;
};

const handleApplySubmit = async () => {
  if (!selectedResumeId.value || !detailJob.value) return;
  applyLoading.value = true;
  try {
    const res = await applyJob({
      jobId: detailJob.value.id,
      resumeId: selectedResumeId.value,
      companyId: detailJob.value.companyId,
    });
    if (res.code === 1) {
      ElMessage.success("简历投递成功");
      applyVisible.value = false;
    } else ElMessage.error(res.msg || "投递失败");
  } catch {
    ElMessage.error("网络异常，投递失败");
  } finally {
    applyLoading.value = false;
  }
};

const goCreateResume = () => {
  applyVisible.value = false;
  router.push("/user/resume/edit");
};

const setupRevealAnimation = () => {
  const root = rootRef.value;
  if (!root) return;

  if (revealObserver) {
    revealObserver.disconnect();
  }

  const nodes = root.querySelectorAll(".reveal-item");
  revealObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add("revealed");
          revealObserver?.unobserve(entry.target);
        }
      });
    },
    { threshold: 0.16 }
  );

  nodes.forEach((node) => {
    if (!node.classList.contains("revealed")) {
      revealObserver.observe(node);
    }
  });
};

onMounted(() => {
  fetchRecommendedJobs();
  fetchHotJobs();
  fetchHomeStats();
  nextTick(setupRevealAnimation);
});

watch(activeTab, () => {
  nextTick(setupRevealAnimation);
});

onUnmounted(() => {
  revealObserver?.disconnect();
});
</script>

<style scoped>
.home-root {
  min-height: calc(100vh - 100px);
  padding-bottom: 48px;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.reveal-item {
  opacity: 0;
  transform: translateY(34px) scale(0.985);
  transition: opacity 0.6s ease, transform 0.6s ease;
}

.reveal-item.revealed {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.section-kicker {
  display: inline-flex;
  align-items: center;
  padding: 7px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: #0f766e;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(20, 184, 166, 0.14);
}

.section-heading,
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 18px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}

.section-heading h2,
.section-header-left h2 {
  margin: 10px 0 0;
  font-size: clamp(28px, 3vw, 38px);
  line-height: 1.1;
  font-weight: 900;
  color: #0f172a;
}

.section-heading p,
.section-header-left p {
  margin: 0;
  max-width: 520px;
  color: #64748b;
  line-height: 1.75;
}

.hero {
  position: relative;
  overflow: hidden;
  margin-bottom: 28px;
  padding: 28px 0 14px;
}

.hero-grid {
  position: relative;
  display: grid;
  grid-template-columns: 1.08fr 0.92fr;
  gap: 30px;
  align-items: center;
  padding-top: 10px;
}

.hero-rings {
  position: absolute;
  top: 50px;
  left: 8%;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  border: 1px solid rgba(99, 102, 241, 0.12);
  box-shadow: 0 0 0 42px rgba(99, 102, 241, 0.04),
    0 0 0 92px rgba(45, 212, 191, 0.03);
  filter: blur(0.3px);
}

.rings-alt {
  left: auto;
  right: 6%;
  top: 110px;
  width: 300px;
  height: 300px;
  border-color: rgba(45, 212, 191, 0.1);
}

.hero-copy {
  position: relative;
  z-index: 1;
  max-width: 640px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: #0f766e;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(15, 118, 110, 0.12);
  box-shadow: 0 18px 40px rgba(255, 255, 255, 0.35);
}

.hero-title {
  margin: 18px 0 16px;
  font-size: clamp(40px, 6vw, 68px);
  line-height: 1.03;
  font-weight: 900;
  color: #0f172a;
}

.hero-highlight {
  display: block;
  margin-top: 6px;
  background: linear-gradient(135deg, #0f766e 0%, #4f46e5 52%, #f97316 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-desc {
  max-width: 560px;
  margin: 0 0 28px;
  font-size: 16px;
  line-height: 1.9;
  color: #64748b;
}

.hero-actions {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.hero-actions :deep(.el-button--primary) {
  padding-inline: 26px;
  background: linear-gradient(135deg, #14b8a6 0%, #4f46e5 100%);
  border: none;
  box-shadow: 0 18px 35px rgba(79, 70, 229, 0.24);
}

.hero-btn-outline {
  border: 1px solid rgba(148, 163, 184, 0.22) !important;
  color: #334155 !important;
  background: rgba(255, 255, 255, 0.82) !important;
  box-shadow: 0 18px 40px rgba(148, 163, 184, 0.15);
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 22px;
}

.hero-tags span {
  padding: 10px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #312e81;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(99, 102, 241, 0.14);
}

.hero-visual {
  position: relative;
  min-height: 430px;
}

.visual-panel,
.visual-card,
.feature-strip-card,
.stat-card,
.job-card,
.why-card,
.journey-panel {
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid rgba(148, 163, 184, 0.14);
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(22px);
}

.main-panel {
  position: absolute;
  inset: 40px 20px 40px 40px;
  border-radius: 30px;
  padding: 28px;
  background: linear-gradient(
      160deg,
      rgba(15, 23, 42, 0.94),
      rgba(30, 41, 59, 0.92)
    ),
    rgba(15, 23, 42, 0.95);
  color: #fff;
  overflow: hidden;
}

.main-panel::after {
  content: "";
  position: absolute;
  inset: auto -40px -60px auto;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(45, 212, 191, 0.3), transparent 70%);
}

.panel-top {
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(226, 232, 240, 0.9);
  font-size: 14px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 16px rgba(52, 211, 153, 0.9);
}

.signal-list {
  display: grid;
  gap: 16px;
  margin-top: 28px;
}

.signal-item {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.signal-item span {
  color: rgba(226, 232, 240, 0.68);
  font-size: 13px;
}

.signal-item strong {
  font-size: 22px;
  font-weight: 800;
}

.signal-bars {
  position: absolute;
  right: 28px;
  bottom: 28px;
  left: 28px;
  height: 92px;
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.signal-bars span {
  flex: 1;
  border-radius: 999px 999px 10px 10px;
  background: linear-gradient(
    180deg,
    rgba(45, 212, 191, 0.95),
    rgba(79, 70, 229, 0.55)
  );
  animation: equalize 1.9s ease-in-out infinite alternate;
}

.signal-bars span:nth-child(1) {
  height: 30%;
}
.signal-bars span:nth-child(2) {
  height: 58%;
}
.signal-bars span:nth-child(3) {
  height: 45%;
}
.signal-bars span:nth-child(4) {
  height: 82%;
}
.signal-bars span:nth-child(5) {
  height: 52%;
}
.signal-bars span:nth-child(6) {
  height: 70%;
}
.signal-bars span:nth-child(7) {
  height: 40%;
}
.signal-bars span:nth-child(8) {
  height: 88%;
}

.float-card {
  position: absolute;
  min-width: 180px;
  padding: 18px;
  border-radius: 22px;
  animation: floatUpDown 4.6s ease-in-out infinite;
}

.float-card span {
  display: block;
  color: #64748b;
  font-size: 12px;
  margin-bottom: 8px;
}

.float-card strong {
  color: #0f172a;
  font-size: 16px;
  line-height: 1.5;
}

.card-a {
  top: 12px;
  right: 22px;
}

.card-b {
  bottom: 6px;
  left: 0;
  animation-delay: 0.6s;
}

.visual-orb {
  position: absolute;
  right: 30px;
  bottom: 32px;
  width: 110px;
  height: 110px;
  border-radius: 50%;
  background: radial-gradient(
    circle,
    rgba(249, 115, 22, 0.4),
    rgba(249, 115, 22, 0.02) 72%
  );
  filter: blur(6px);
}

.feature-strip {
  margin-top: 12px;
}

.feature-strip-card {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  padding: 20px;
  border-radius: 26px;
}

.feature-item {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  padding: 18px;
  border-radius: 22px;
  background: rgba(248, 250, 252, 0.8);
}

.feature-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.feature-icon-green {
  background: #dcfce7;
  color: #16a34a;
}
.feature-icon-purple {
  background: #ede9fe;
  color: #7c3aed;
}
.feature-icon-blue {
  background: #dbeafe;
  color: #2563eb;
}

.feature-item h3 {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}

.feature-item p {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

.stats {
  padding-top: 54px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}

.stat-card {
  border-radius: 26px;
  padding: 26px;
}

.accent-card {
  background: linear-gradient(
    135deg,
    rgba(15, 118, 110, 0.92),
    rgba(79, 70, 229, 0.86)
  );
}

.accent-card .stat-label,
.accent-card .stat-number,
.accent-card em {
  color: #fff;
}

.stat-label {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
}

.stat-number {
  display: block;
  margin: 12px 0 8px;
  font-size: 40px;
  line-height: 1;
  font-weight: 900;
  color: #0f172a;
}

.stat-card em {
  display: block;
  font-style: normal;
  color: #94a3b8;
  line-height: 1.7;
}

.jobs-area {
  padding-top: 56px;
}

.toggle-pill {
  position: relative;
  display: inline-flex;
  padding: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(148, 163, 184, 0.16);
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.06);
}

.toggle-btn {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: #64748b;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.3s ease;
}

.toggle-btn.active {
  color: #0f172a;
}

.toggle-slider {
  position: absolute;
  inset: 4px auto 4px 4px;
  width: calc(50% - 4px);
  border-radius: 999px;
  background: linear-gradient(
    135deg,
    rgba(45, 212, 191, 0.2),
    rgba(99, 102, 241, 0.18)
  );
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.toggle-slider.right {
  transform: translateX(100%);
}

.jobs-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.job-card {
  position: relative;
  overflow: hidden;
  border-radius: 24px;
  padding: 22px;
  cursor: pointer;
  transition: transform 0.32s ease, box-shadow 0.32s ease,
    border-color 0.32s ease;
}

.job-card::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(45, 212, 191, 0.08),
    transparent 45%,
    rgba(99, 102, 241, 0.08)
  );
  opacity: 0;
  transition: opacity 0.32s ease;
}

.job-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 28px 70px rgba(15, 23, 42, 0.11);
  border-color: rgba(45, 212, 191, 0.2);
}

.job-card:hover::before {
  opacity: 1;
}

.job-card-top,
.job-card-footer {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.job-card-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 800;
}

.match-badge.match-high {
  background: #dcfce7;
  color: #15803d;
}
.match-badge.match-mid {
  background: #fef3c7;
  color: #b45309;
}
.match-badge.match-low {
  background: #e2e8f0;
  color: #475569;
}
.hot-badge {
  background: linear-gradient(135deg, #fb7185, #f97316);
  color: #fff;
}

.job-arrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: #94a3b8;
  text-transform: uppercase;
}

.job-card-title {
  position: relative;
  z-index: 1;
  margin: 18px 0 10px;
  font-size: 19px;
  font-weight: 800;
  color: #0f172a;
}

.job-card-meta {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 14px;
  color: #64748b;
  font-size: 13px;
}

.job-card-meta span,
.job-stats-mini {
  display: flex;
  align-items: center;
  gap: 5px;
}

.job-card-tags {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 18px;
}

.job-tag {
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: #0f766e;
  background: rgba(16, 185, 129, 0.09);
  border: 1px solid rgba(16, 185, 129, 0.14);
}

.job-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16px;
  border-top: 1px solid rgba(226, 232, 240, 0.9);
  gap: 12px;
}

.job-salary {
  font-size: 20px;
  font-weight: 900;
  color: #ef4444;
  flex-shrink: 0;
}

.job-company-name {
  font-size: 13px;
  color: #3b82f6;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}
.job-company-name:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

.job-stats-mini {
  color: #94a3b8;
  font-size: 12px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.jobs-view-all {
  margin-top: 34px;
  text-align: center;
}

.why-us,
.journey {
  padding-top: 60px;
}

.why-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.why-card {
  border-radius: 24px;
  padding: 28px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.why-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.09);
}

.why-icon-wrap {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 18px;
  color: #16a34a;
  background: #dcfce7;
}

.why-icon-purple {
  color: #7c3aed;
  background: #ede9fe;
}
.why-icon-blue {
  color: #2563eb;
  background: #dbeafe;
}

.why-card h4 {
  margin: 0 0 10px;
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}

.why-card > p,
.why-roles span,
.why-features span {
  color: #64748b;
  line-height: 1.8;
}

.why-roles,
.why-features {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.why-roles strong {
  color: #0f172a;
}

.why-features span {
  position: relative;
  padding-left: 14px;
}

.why-features span::before {
  content: "";
  position: absolute;
  left: 0;
  top: 11px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #14b8a6;
}

.journey-panel {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 30px;
  border-radius: 28px;
}

.journey-copy {
  max-width: 360px;
}

.journey-copy h2 {
  margin: 10px 0 0;
  font-size: 32px;
  line-height: 1.15;
  color: #0f172a;
}

.journey-steps {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.journey-step {
  padding: 20px 18px;
  border-radius: 22px;
  background: linear-gradient(
    180deg,
    rgba(248, 250, 252, 0.95),
    rgba(241, 245, 249, 0.82)
  );
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.journey-step strong {
  display: block;
  margin-bottom: 10px;
  font-size: 28px;
  font-weight: 900;
  color: #4f46e5;
}

.journey-step span {
  color: #475569;
  line-height: 1.7;
}

.detail-dialog {
  padding: 10px 0;
}
.detail-top {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.detail-salary-badge {
  font-size: 28px;
  font-weight: 700;
  color: #ef4444;
}
.detail-tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.detail-section {
  margin-bottom: 22px;
}
.detail-section h4 {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 10px;
  padding-left: 10px;
  border-left: 3px solid #22c55e;
}
.detail-text {
  font-size: 14px;
  color: #475569;
  line-height: 1.8;
  margin: 0;
  white-space: pre-line;
}
.benefits-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.detail-stats {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #94a3b8;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}
.detail-stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}
.apply-body {
  padding: 4px 0;
}
.apply-alert {
  margin-bottom: 20px;
}
.apply-label {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 12px;
}
.resume-radios {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}
.resume-radio {
  width: 100% !important;
  margin-right: 0 !important;
  padding: 14px 16px;
  border-radius: 10px;
}
.resume-edu {
  font-size: 13px;
  color: #64748b;
  background: #f1f5f9;
  padding: 2px 10px;
  border-radius: 4px;
  margin-left: 8px;
}

@keyframes equalize {
  from {
    transform: scaleY(0.72);
    transform-origin: bottom;
  }
  to {
    transform: scaleY(1.05);
    transform-origin: bottom;
  }
}

@keyframes floatUpDown {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@media (max-width: 1080px) {
  .hero-grid,
  .feature-strip-card,
  .stats-grid,
  .jobs-grid,
  .why-grid,
  .journey-steps {
    grid-template-columns: repeat(2, 1fr);
  }

  .hero-grid {
    gap: 22px;
  }

  .journey-panel {
    flex-direction: column;
  }
}

@media (max-width: 760px) {
  .section-container {
    padding: 0 16px;
  }

  .hero-grid,
  .feature-strip-card,
  .stats-grid,
  .jobs-grid,
  .why-grid,
  .journey-steps {
    grid-template-columns: 1fr;
  }

  .hero {
    padding-top: 12px;
  }

  .hero-visual {
    min-height: 360px;
  }

  .main-panel {
    inset: 24px 12px 54px 12px;
  }

  .card-a {
    top: -6px;
    right: 10px;
  }

  .card-b {
    left: 8px;
    right: 8px;
    min-width: auto;
  }

  .section-heading h2,
  .section-header-left h2,
  .journey-copy h2 {
    font-size: 28px;
  }
}
</style>
