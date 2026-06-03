<template>
  <div class="hr-dashboard">
    <section class="dash-hero">
      <div class="dash-copy">
        <span class="dash-kicker">HR Overview</span>
        <h2>招聘概览</h2>
        <p>
          保留清晰的数据结构和重点信息，不再堆太多装饰，让首页更像一套稳重、专业、好用的招聘后台。
        </p>
      </div>
    </section>

    <section class="stats-grid">
      <article class="stat-card cyan-card">
        <div class="stat-info">
          <span class="stat-label">在招职位</span>
          <strong class="stat-value">{{ data.jobCount }}</strong>
          <span class="stat-trend">当前开放岗位数量</span>
        </div>
        <div class="stat-icon blue">
          <el-icon :size="32"><Briefcase /></el-icon>
        </div>
      </article>
      <article class="stat-card green-card">
        <div class="stat-info">
          <span class="stat-label">收到简历</span>
          <strong class="stat-value">{{ data.resumeCount }}</strong>
          <span class="stat-trend">候选人投递持续流入</span>
        </div>
        <div class="stat-icon green">
          <el-icon :size="32"><Document /></el-icon>
        </div>
      </article>
      <article class="stat-card orange-card">
        <div class="stat-info">
          <span class="stat-label">面试安排</span>
          <strong class="stat-value">{{ data.interviewCount }}</strong>
          <span class="stat-trend">需要跟进的面试流程</span>
        </div>
        <div class="stat-icon orange">
          <el-icon :size="32"><Calendar /></el-icon>
        </div>
      </article>
      <article class="stat-card purple-card">
        <div class="stat-info">
          <span class="stat-label">已录用</span>
          <strong class="stat-value">{{ data.hireCount }}</strong>
          <span class="stat-trend">本周期成功转化人数</span>
        </div>
        <div class="stat-icon purple">
          <el-icon :size="32"><Checked /></el-icon>
        </div>
      </article>
    </section>

    <section class="content-grid">
      <div class="section-card table-card">
        <div class="card-head">
          <div>
            <span class="card-kicker">最新动态</span>
            <h3>最新投递</h3>
          </div>
          <el-button
            type="primary"
            size="small"
            @click="router.push('/hr/applications')"
            >查看全部</el-button
          >
        </div>
        <el-table :data="data.latestApplications" stripe class="dash-table">
          <el-table-column prop="applicantName" label="候选人" width="110" />
          <el-table-column prop="jobTitle" label="投递职位" min-width="180" />
          <el-table-column prop="appliedAt" label="投递时间" width="130">
            <template #default="{ row }">{{
              row.appliedAt?.substring(0, 10) || "-"
            }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="110">
            <template #default="{ row }">
              <el-tag
                :type="row.status === 0 ? 'warning' : 'info'"
                size="small"
                effect="plain"
              >
                {{ row.status === 0 ? "待查看" : "已查看" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="viewResume(row)"
                >查看简历</el-button
              >
            </template>
          </el-table-column>
        </el-table>
        <el-empty
          v-if="!data.latestApplications?.length"
          description="暂无投递"
          :image-size="60"
        />
      </div>

      <div class="section-card todo-card">
        <div class="card-head">
          <div>
            <span class="card-kicker">流程提醒</span>
            <h3>待办事项</h3>
          </div>
          <el-button
            type="primary"
            size="small"
            @click="openTodoDialog"
            >全部</el-button
          >
        </div>
        <div class="todo-list">
          <div v-for="item in data.todoList" :key="item.id" class="todo-item">
            <div
              class="todo-mark"
              :style="{ '--todo-color': todoColor(item.content) }"
            >
              <el-icon :size="16">
                <component :is="todoIcon(item.content)" />
              </el-icon>
            </div>
            <div class="todo-copy">
              <span class="todo-text">{{ item.content }}</span>
              <span class="todo-time">{{ relativeTime(item.createdAt) }}</span>
            </div>
          </div>
        </div>
        <el-empty
          v-if="!data.todoList?.length"
          description="暂无待办"
          :image-size="60"
        />
      </div>
    </section>

    <el-dialog
      v-model="resumeVisible"
      title="候选人简历"
      width="680px"
      top="3vh"
      destroy-on-close
    >
      <div v-if="currentResume" v-loading="resumeLoading">
        <div class="rd-hero">
          <el-avatar :size="56" :src="currentResume.characterAvatar">{{
            currentResume.name?.charAt(0)
          }}</el-avatar>
          <div>
            <h3>{{ currentResume.name }}</h3>
            <div class="rd-meta">
              <span>{{ currentResume.gender === 1 ? "男" : "女" }}</span>
              <span>{{ currentResume.age }}岁</span>
              <span>{{ educationLabel(currentResume.education) }}</span>
              <span v-if="currentResume.graduationSchool">{{
                currentResume.graduationSchool
              }}</span>
              <span v-if="currentResume.totalWorkYears != null"
                >{{ currentResume.totalWorkYears }}年</span
              >
              <span v-if="currentResume.maxEducation != null">{{
                maxEducationLabel(currentResume.maxEducation)
              }}</span>
              <span>{{ currentResume.phone }}</span>
              <span>{{ currentResume.email }}</span>
            </div>
          </div>
        </div>
        <div class="rd-section" v-if="currentResume.jobIntention">
          <h4>求职意向</h4>
          <p>
            {{ currentResume.jobIntention }} | {{ currentResume.city }} |
            {{ formatSalary(currentResume.salaryMin, currentResume.salaryMax) }}
          </p>
        </div>
        <div class="rd-section" v-if="currentResume.selfDescription">
          <h4>自我描述</h4>
          <p>{{ currentResume.selfDescription }}</p>
        </div>
        <div class="rd-section" v-if="currentResume.skills">
          <h4>技能标签</h4>
          <div class="rd-tags">
            <el-tag
              v-for="s in parseSkillsArr(currentResume.skills)"
              :key="s"
              size="small"
              >{{ s }}</el-tag
            >
          </div>
        </div>
        <div class="rd-section" v-if="currentResume.educations?.length">
          <h4>教育经历</h4>
          <div
            v-for="(e, i) in currentResume.educations"
            :key="i"
            class="rd-item"
          >
            <strong>{{ e.school }}</strong> · {{ e.major }} ·
            {{ educationLabel(e.education) }} · {{ e.startTime }} —
            {{ e.endTime }}
            <p v-if="e.description">{{ e.description }}</p>
          </div>
        </div>
        <div class="rd-section" v-if="currentResume.experiences?.length">
          <h4>工作经历</h4>
          <div
            v-for="(e, i) in currentResume.experiences"
            :key="i"
            class="rd-item"
          >
            <strong>{{ e.company }}</strong> · {{ e.position }} ·
            {{ e.startTime }} — {{ e.endTime }}
            <p v-if="e.description">{{ e.description }}</p>
          </div>
        </div>
        <div class="rd-section" v-if="currentResume.projects?.length">
          <h4>项目经历</h4>
          <div
            v-for="(p, i) in currentResume.projects"
            :key="i"
            class="rd-item"
          >
            <strong>{{ p.name }}</strong> · {{ p.role }} · {{ p.startTime }} —
            {{ p.endTime }}
            <p v-if="p.description">{{ p.description }}</p>
          </div>
        </div>
      </div>
      <el-empty v-else-if="!resumeLoading" description="暂无简历信息" />
    </el-dialog>

    <el-dialog
      v-model="todoDialogVisible"
      title="全部待办事项"
      width="600px"
      top="5vh"
      destroy-on-close
    >
      <div v-loading="todoLoading">
        <div v-if="allTodos.length > 0" class="todo-dialog-list">
          <div v-for="item in allTodos" :key="item.id" class="todo-item">
            <el-icon :size="16" :color="todoColor(item.content)">
              <component :is="todoIcon(item.content)" />
            </el-icon>
            <span class="todo-text">{{ item.content }}</span>
            <span class="todo-time">{{ relativeTime(item.createdAt) }}</span>
          </div>
        </div>
        <el-empty
          v-else-if="!todoLoading"
          description="暂无待办事项"
          :image-size="60"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  Briefcase,
  Document,
  Calendar,
  Checked,
  Clock,
  ChatLineRound,
} from "@element-plus/icons-vue";
import { getDashboard, getAllTodos } from "@/api/hr/dashboard";
import { getResumeByApplication } from "@/api/hr/resume";
import {
  formatSalary,
  maxEducationLabel,
  educationLabel,
} from "@/utils/format";

const router = useRouter();

const data = reactive({
  jobCount: 0,
  resumeCount: 0,
  interviewCount: 0,
  hireCount: 0,
  latestApplications: [],
  todoList: [],
});

const resumeVisible = ref(false);
const resumeLoading = ref(false);
const currentResume = ref(null);

const todoDialogVisible = ref(false);
const todoLoading = ref(false);
const allTodos = ref([]);

const todoIcon = (content) => {
  if (!content) return Clock;
  if (content.includes("已接受")) return ChatLineRound;
  if (content.includes("面试")) return Clock;
  return Document;
};
const todoColor = (content) => {
  if (!content) return "#1677ff";
  if (content.includes("已接受")) return "#52c41a";
  if (content.includes("面试")) return "#f56c6c";
  return "#1677ff";
};

const relativeTime = (dateStr) => {
  if (!dateStr) return "";
  const now = Date.now();
  const diff = now - new Date(dateStr).getTime();
  const minutes = Math.floor(diff / 60000);
  if (minutes < 1) return "刚刚";
  if (minutes < 60) return `${minutes}分钟前`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}小时前`;
  const days = Math.floor(hours / 24);
  if (days < 30) return `${days}天前`;
  return dateStr.substring(0, 10);
};

const parseSkillsArr = (skills) => {
  if (!skills) return [];
  if (Array.isArray(skills)) return skills;
  return skills.split(",").filter(Boolean);
};

onMounted(async () => {
  try {
    const res = await getDashboard();
    if (res.code === 1 && res.data) {
      Object.assign(data, res.data);
    }
  } catch {}
});

const viewResume = async (row) => {
  resumeVisible.value = true;
  resumeLoading.value = true;
  currentResume.value = null;
  try {
    const res = await getResumeByApplication(row.id);
    if (res.code === 1 && res.data) {
      currentResume.value = res.data;
    }
  } catch {
  } finally {
    resumeLoading.value = false;
  }
};

const openTodoDialog = async () => {
  todoDialogVisible.value = true;
  if (allTodos.value.length > 0) return;
  todoLoading.value = true;
  try {
    const res = await getAllTodos();
    if (res.code === 1 && res.data) {
      allTodos.value = res.data.records || res.data || [];
    }
  } catch {
  } finally {
    todoLoading.value = false;
  }
};
</script>

<style scoped>
.hr-dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dash-hero {
  padding: 24px 28px;
  border-radius: 24px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.dash-copy {
  position: relative;
  z-index: 1;
}

.dash-kicker,
.card-kicker {
  display: inline-flex;
  align-items: center;
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.dash-kicker {
  color: #475569;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.dash-copy h2 {
  margin: 16px 0 12px;
  font-size: clamp(28px, 3vw, 40px);
  line-height: 1.1;
  color: #0f172a;
}

.dash-copy p {
  margin: 0;
  max-width: 760px;
  font-size: 15px;
  line-height: 1.8;
  color: #64748b;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}

.stat-card,
.section-card {
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.05);
}

.cyan-card::after,
.green-card::after,
.orange-card::after,
.purple-card::after {
  content: "";
  position: absolute;
  right: -12px;
  bottom: -12px;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  filter: blur(14px);
}

.cyan-card::after {
  background: radial-gradient(
    circle,
    rgba(148, 163, 184, 0.16),
    transparent 70%
  );
}
.green-card::after {
  background: radial-gradient(
    circle,
    rgba(100, 116, 139, 0.12),
    transparent 70%
  );
}
.orange-card::after {
  background: radial-gradient(
    circle,
    rgba(148, 163, 184, 0.16),
    transparent 70%
  );
}
.purple-card::after {
  background: radial-gradient(
    circle,
    rgba(203, 213, 225, 0.22),
    transparent 70%
  );
}

.stat-info {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-label {
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
}

.stat-value {
  font-size: 38px;
  line-height: 1;
  font-weight: 900;
  color: #0f172a;
}

.stat-trend {
  font-size: 12px;
  color: #64748b;
}

.stat-icon {
  position: relative;
  z-index: 1;
  width: 58px;
  height: 58px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.blue,
.stat-icon.green,
.stat-icon.orange,
.stat-icon.purple {
  background: #f1f5f9;
  color: #475569;
}

.content-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 20px;
}

.section-card {
  padding: 22px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.card-kicker {
  color: #475569;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.card-head h3 {
  margin: 10px 0 0;
  font-size: 24px;
  color: #0f172a;
}

.dash-table :deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-border-color: #e2e8f0;
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #f8fafc;
  --el-table-text-color: #334155;
  --el-table-header-text-color: #64748b;
}

.dash-table :deep(.el-table th.el-table__cell),
.dash-table :deep(.el-table td.el-table__cell) {
  background: transparent;
}

.todo-list,
.todo-dialog-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-height: 360px;
  overflow-y: auto;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 18px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.todo-mark {
  width: 38px;
  height: 38px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--todo-color);
  background: color-mix(in srgb, var(--todo-color) 20%, transparent);
  box-shadow: inset 0 0 0 1px
    color-mix(in srgb, var(--todo-color) 25%, transparent);
  flex-shrink: 0;
}

.todo-copy {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.todo-text {
  font-size: 14px;
  color: #334155;
}
.todo-time {
  font-size: 12px;
  color: #64748b;
}

.rd-hero {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 14px;
  margin-bottom: 14px;
  border-bottom: 1px solid #e2e8f0;
}
.rd-hero h3 {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}
.rd-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  font-size: 12px;
  color: #64748b;
}
.rd-meta span {
  background: #f8fafc;
  padding: 2px 6px;
  border-radius: 4px;
}
.rd-section {
  margin-bottom: 14px;
}
.rd-section h4 {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 6px;
  padding-left: 8px;
  border-left: 2px solid #94a3b8;
}
.rd-section p {
  margin: 0;
  font-size: 13px;
  color: #475569;
  line-height: 1.6;
}
.rd-item {
  padding: 6px 0;
  font-size: 13px;
  color: #475569;
  border-bottom: 1px dashed #e2e8f0;
}
.rd-item:last-child {
  border-bottom: none;
}
.rd-item strong {
  color: #0f172a;
}
.rd-item p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
}
.rd-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 1180px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 680px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .dash-hero,
  .section-card {
    padding: 18px;
  }

  .dash-copy h2 {
    font-size: 28px;
  }
}
</style>
