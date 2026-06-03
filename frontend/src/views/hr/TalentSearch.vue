<template>
  <div class="talent-search">
    <div class="search-section">
      <div class="search-bar">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索姓名、期望职位、技能..."
          size="large"
          clearable
          @keyup.enter="doSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" size="large" :icon="Search" @click="doSearch"
          >搜索</el-button
        >
      </div>

      <div class="filter-group">
        <div class="filter-row">
          <span class="filter-label">意向城市</span>
          <el-input
            v-model="searchForm.city"
            placeholder="如：武汉、北京"
            size="default"
            clearable
          />
        </div>
        <div class="filter-row">
          <span class="filter-label">工作经验</span>
          <el-select
            v-model="searchForm.minWorkYears"
            placeholder="不限"
            size="default"
            clearable
          >
            <el-option label="应届生" :value="0" />
            <el-option label="1-3年" :value="1" />
            <el-option label="3-5年" :value="3" />
            <el-option label="5-10年" :value="5" />
            <el-option label="10年以上" :value="10" />
          </el-select>
        </div>
        <div class="filter-row">
          <span class="filter-label">学历要求</span>
          <el-select
            v-model="searchForm.education"
            placeholder="不限"
            size="default"
            clearable
          >
            <el-option label="初中" :value="0" />
            <el-option label="高中" :value="1" />
            <el-option label="中专" :value="2" />
            <el-option label="大专" :value="3" />
            <el-option label="本科" :value="4" />
            <el-option label="硕士" :value="5" />
            <el-option label="博士" :value="6" />
          </el-select>
        </div>
        <div class="filter-row">
          <span class="filter-label">薪资范围</span>
          <el-select
            v-model="searchForm.salary"
            placeholder="不限"
            size="default"
            clearable
          >
            <el-option label="5K以下" value="0-5" />
            <el-option label="5K-10K" value="5-10" />
            <el-option label="10K-15K" value="10-15" />
            <el-option label="15K-25K" value="15-25" />
            <el-option label="25K以上" value="25-99" />
          </el-select>
        </div>
      </div>
    </div>

    <div class="result-bar" v-if="total > 0">
      <span class="result-count">共 {{ total }} 条结果</span>
    </div>

    <div class="talent-list" v-loading="loading">
      <el-empty
        v-if="!loading && list.length === 0 && searched"
        description="未找到匹配的人才"
      />
      <el-empty
        v-else-if="!loading && list.length === 0"
        description="请设置筛选条件后搜索"
      />

      <el-card
        v-for="item in list"
        :key="item.id"
        class="talent-card"
        shadow="hover"
      >
        <div class="card-content">
          <div class="info-left">
            <el-avatar :size="56" :src="item.characterAvatar">{{
              item.name?.charAt(0) || "?"
            }}</el-avatar>
            <div class="basic-info">
              <div class="name-row">
                <span class="name">{{ item.name }}</span>
                <span class="meta-tag">{{
                  item.gender === 1 ? "男" : "女"
                }}</span>
                <span class="meta-tag" v-if="item.age">{{ item.age }}岁</span>
              </div>
              <div class="intent-row">
                <span class="intent">{{ item.jobIntention || "-" }}</span>
                <span class="salary">{{
                  fmtSalaryShort(item.salaryMin, item.salaryMax)
                }}</span>
              </div>
              <div class="detail-row">
                <span v-if="item.graduationSchool">{{
                  item.graduationSchool
                }}</span>
                <span v-if="item.totalWorkYears != null">{{
                  item.totalWorkYears > 0
                    ? item.totalWorkYears + "年经验"
                    : "应届生"
                }}</span>
                <span>{{ educationLabel(item.maxEducation) }}</span>
              </div>
            </div>
          </div>

          <div class="info-mid">
            <div class="skills" v-if="item.skills">
              <el-tag
                v-for="s in parseSkillList(item.skills)"
                :key="s"
                size="small"
                type="success"
                effect="light"
                >{{ s }}</el-tag
              >
            </div>
            <p class="description" v-if="item.selfDescription">
              {{ item.selfDescription }}
            </p>
          </div>

          <div class="info-right">
            <el-button
              type="primary"
              plain
              size="small"
              class="action-btn"
              @click="handleChat(item)"
              >立即沟通</el-button
            >
            <el-button
              size="small"
              class="action-btn"
              @click="handleViewResume(item)"
              >查看简历</el-button
            >
          </div>
        </div>
      </el-card>
    </div>

    <div class="pager" v-if="total > size">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="size"
        v-model:current-page="page"
        @current-change="fetchList"
      />
    </div>

    <el-dialog
      v-model="resumeVisible"
      title="简历详情"
      width="680px"
      top="3vh"
      destroy-on-close
    >
      <div class="rd-box" v-if="currentResume" v-loading="resumeLoading">
        <div class="rd-hero">
          <el-avatar :size="56" :src="currentResume.characterAvatar">{{
            currentResume.name?.charAt(0) || "?"
          }}</el-avatar>
          <div>
            <h3>{{ currentResume.name }}</h3>
            <div class="rd-meta">
              <span>{{ currentResume.gender === 1 ? "男" : "女" }}</span>
              <span v-if="currentResume.age">{{ currentResume.age }}岁</span>
              <span v-if="currentResume.graduationSchool">{{
                currentResume.graduationSchool
              }}</span>
              <span v-if="currentResume.totalWorkYears != null">{{
                currentResume.totalWorkYears > 0
                  ? currentResume.totalWorkYears + "年"
                  : "应届生"
              }}</span>
              <span>{{ educationLabel(currentResume.maxEducation) }}</span>
              <span>{{ currentResume.phone || "-" }}</span>
              <span>{{ currentResume.email || "-" }}</span>
            </div>
          </div>
        </div>
        <div
          class="rd-section"
          v-if="currentResume.jobIntention || currentResume.city"
        >
          <h4>求职意向</h4>
          <p>
            {{ currentResume.jobIntention || "-" }} |
            {{ currentResume.city || "-" }} |
            {{
              fmtSalaryShort(currentResume.salaryMin, currentResume.salaryMax)
            }}
          </p>
        </div>
        <div class="rd-section" v-if="currentResume.industry">
          <h4>期望行业</h4>
          <p>{{ currentResume.industry }}</p>
        </div>
        <div class="rd-section" v-if="currentResume.selfDescription">
          <h4>自我描述</h4>
          <p>{{ currentResume.selfDescription }}</p>
        </div>
        <div class="rd-section" v-if="currentResume.skills">
          <h4>技能标签</h4>
          <div class="rd-tags">
            <el-tag
              v-for="s in parseSkillList(currentResume.skills)"
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
            <strong>{{ e.school }}</strong> · {{ e.major || "-" }} ·
            {{ educationLabel(e.education) }}
            <span v-if="e.startTime || e.endTime">
              · {{ e.startTime || "?" }} — {{ e.endTime || "至今" }}</span
            >
            <p v-if="e.description">{{ e.description }}</p>
          </div>
        </div>
        <div class="rd-section" v-if="currentResume.experiences?.length">
          <h4>工作经历</h4>
          <div
            v-for="(exp, i) in currentResume.experiences"
            :key="i"
            class="rd-item"
          >
            <strong>{{ exp.company }}</strong> · {{ exp.position }} ·
            {{ exp.startTime }} — {{ exp.endTime || "至今" }}
            <p v-if="exp.description">{{ exp.description }}</p>
          </div>
        </div>
        <div class="rd-section" v-if="currentResume.projects?.length">
          <h4>项目经历</h4>
          <div
            v-for="(proj, i) in currentResume.projects"
            :key="i"
            class="rd-item"
          >
            <strong>{{ proj.name }}</strong
            ><span v-if="proj.role"> · {{ proj.role }}</span>
            <span v-if="proj.startTime || proj.endTime">
              · {{ proj.startTime || "?" }} — {{ proj.endTime || "至今" }}</span
            >
            <p v-if="proj.description">{{ proj.description }}</p>
          </div>
        </div>
      </div>
      <el-empty v-else-if="!resumeLoading" description="暂无简历信息" />
      <template #footer>
        <el-button @click="resumeVisible = false">关闭</el-button>
        <el-button
          v-if="currentResume && currentResume.favorite !== 1"
          type="warning"
          @click="handleWantFavorite"
          >收藏</el-button
        >
        <el-button
          v-if="currentResume && currentResume.favorite === 1"
          type="warning"
          @click="handleWantUnfavorite"
          >已收藏</el-button
        >
      </template>
    </el-dialog>

    <el-dialog
      v-model="chatVisible"
      title="立即沟通"
      width="780px"
      top="3vh"
      draggable
      overflow
      append-to-body
      :modal="false"
      :lock-scroll="false"
      destroy-on-close
    >
      <ChatPanel compact :target-conversation-id="targetConversationId" />
    </el-dialog>

    <el-dialog
      v-model="favoriteVisible"
      title="收藏人才"
      width="420px"
      top="10vh"
      destroy-on-close
    >
      <el-form label-width="80px">
        <el-form-item label="收藏原因">
          <el-input
            v-model="favoriteReason"
            type="textarea"
            :rows="3"
            placeholder="请输入收藏原因..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="favoriteVisible = false">取消</el-button>
        <el-button type="primary" @click="handleFavorite">确认收藏</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="unfavoriteVisible"
      title="取消收藏"
      width="380px"
      top="15vh"
      destroy-on-close
    >
      <p style="text-align: center; font-size: 15px; margin: 12px 0">
        确定要取消收藏该人才吗？
      </p>
      <template #footer>
        <el-button @click="unfavoriteVisible = false">取消</el-button>
        <el-button type="danger" @click="handleUnfavorite">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import {
  searchTalents,
  getTalentResumeDetail,
  addFavorite,
  cancelFavorite,
} from "@/api/hr/talent";
import { educationLabel, parseSkills } from "@/utils/format";
import ChatPanel from "@/components/ChatPanel.vue";
import { createConversation } from "@/api/chat";

const loading = ref(false);
const searched = ref(false);
const list = ref([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);

const resumeVisible = ref(false);
const resumeLoading = ref(false);
const currentResume = ref(null);
const chatVisible = ref(false);
const targetConversationId = ref(null);
const favoriteReason = ref("");
const favoriteVisible = ref(false);
const unfavoriteVisible = ref(false);
const favoriteTarget = ref(null);

const searchForm = reactive({
  keyword: "",
  city: "",
  minWorkYears: null,
  education: null,
  salary: "",
});

const parseSkillList = (skills) => {
  return parseSkills(skills);
};

const fmtSalaryShort = (min, max) => {
  if (!min && !max) return "面议";
  const fmt = (v) => Math.round(v / 1000) + "K";
  if (!min) return fmt(max) + "及以下";
  if (!max) return fmt(min) + "及以上";
  return fmt(min) + "-" + fmt(max);
};

const buildParams = () => {
  const p = { page: page.value, size: size.value };
  if (searchForm.keyword) p.keyword = searchForm.keyword;
  if (searchForm.city) p.city = searchForm.city;
  if (searchForm.minWorkYears !== null && searchForm.minWorkYears !== "")
    p.totalWorkYears = searchForm.minWorkYears;
  if (searchForm.education !== null && searchForm.education !== "")
    p.maxEducation = searchForm.education;
  if (searchForm.salary) {
    const parts = searchForm.salary.split("-");
    p.salaryMin = Number(parts[0]) * 1000;
    p.salaryMax = Number(parts[1]) * 1000;
  }
  return p;
};

const doSearch = async () => {
  page.value = 1;
  await fetchList();
};

const fetchList = async () => {
  loading.value = true;
  searched.value = true;
  try {
    const res = await searchTalents(buildParams());
    if (res.code === 1 && res.data) {
      list.value = res.data.list || res.data.records || [];
      total.value = res.data.total || 0;
    } else {
      list.value = [];
      total.value = 0;
    }
  } catch {
    list.value = [];
    total.value = 0;
    ElMessage.error("搜索失败");
  } finally {
    loading.value = false;
  }
};

const handleChat = async (item) => {
  try {
    const res = await createConversation(item.userId);
    if (res.code === 1 && res.data) {
      targetConversationId.value = res.data.id;
    }
  } catch {}
  chatVisible.value = true;
};

const handleViewResume = async (item) => {
  currentResume.value = null;
  resumeLoading.value = true;
  resumeVisible.value = true;
  try {
    const res = await getTalentResumeDetail(item.id);
    if (res.code === 1 && res.data) {
      currentResume.value = res.data;
    } else {
      currentResume.value = null;
      ElMessage.warning("简历详情获取失败");
    }
  } catch {
    currentResume.value = null;
    ElMessage.error("获取简历详情失败");
  } finally {
    resumeLoading.value = false;
  }
};

const handleWantFavorite = () => {
  favoriteTarget.value = currentResume.value;
  favoriteReason.value = "";
  favoriteVisible.value = true;
};

const handleFavorite = async () => {
  if (!favoriteTarget.value) return;
  try {
    const res = await addFavorite({
      userId: favoriteTarget.value.userId,
      resumeId: favoriteTarget.value.id,
      reason: favoriteReason.value,
    });
    if (res.code === 1) {
      ElMessage.success("收藏成功");
      currentResume.value.favorite = 1;
      favoriteVisible.value = false;
    } else {
      ElMessage.warning(res.msg || "收藏失败");
    }
  } catch {
    ElMessage.error("收藏失败");
  }
};

const handleWantUnfavorite = () => {
  favoriteTarget.value = currentResume.value;
  unfavoriteVisible.value = true;
};

const handleUnfavorite = async () => {
  if (!favoriteTarget.value) return;
  try {
    const res = await cancelFavorite(favoriteTarget.value.id);
    if (res.code === 1) {
      ElMessage.success("已取消收藏");
      currentResume.value.favorite = 0;
      unfavoriteVisible.value = false;
    } else {
      ElMessage.warning(res.msg || "取消失败");
    }
  } catch {
    ElMessage.error("取消失败");
  }
};

onMounted(() => {
  fetchList();
});
</script>

<style scoped>
.talent-search {
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.sub-hero {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 22px 24px;
  background: #ffffff;
  border-radius: 22px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}
.sub-hero-icon {
  font-size: 30px;
}
.sub-hero-text {
  flex: 1;
}
.sub-hero h2 {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 4px;
}
.sub-hero p {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.search-section {
  background: #ffffff;
  padding: 22px 24px;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
  align-items: center;
}
.search-bar .el-input {
  flex: 1;
  max-width: 520px;
}

.filter-group {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 18px;
}
.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 8px 12px;
  transition: border-color 0.2s ease, background 0.2s ease;
}
.filter-row:hover {
  border-color: #cbd5e1;
  background: #f1f5f9;
}
.filter-label {
  font-size: 13px;
  color: #475569;
  width: 72px;
  flex-shrink: 0;
  font-weight: 500;
}
.filter-row .el-input,
.filter-row .el-select {
  flex: 1;
}

.result-bar {
  margin-top: -2px;
}
.result-count {
  font-size: 14px;
  color: #64748b;
}

.talent-list {
  min-height: 200px;
}
.talent-card {
  margin-bottom: 14px;
  border-radius: 18px;
  transition: all 0.22s;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}
.talent-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.05);
}

.card-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}
.info-left {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  min-width: 280px;
  max-width: 320px;
}
.basic-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.name {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}
.meta-tag {
  font-size: 12px;
  color: #64748b;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 999px;
}
.intent-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.intent {
  font-size: 14px;
  color: #475569;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 140px;
}
.salary {
  font-size: 14px;
  color: #334155;
  font-weight: 700;
}
.detail-row {
  font-size: 12px;
  color: #64748b;
  display: flex;
  flex-wrap: wrap;
  gap: 2px 10px;
}
.detail-row span {
  white-space: nowrap;
}

.info-mid {
  flex: 1;
  min-width: 0;
}
.skills {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}
.description {
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
}

.info-right {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 110px;
  flex-shrink: 0;
  justify-content: center;
}
.action-btn {
  width: 100% !important;
  margin: 0 !important;
  text-align: center;
  display: block;
}

.action-btn:not(.el-button--primary) {
  background: #f1f5f9;
  border-color: #cbd5e1;
  color: #334155;
}
.action-btn:not(.el-button--primary):hover {
  background: #e2e8f0;
  border-color: #94a3b8;
  color: #0f172a;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.rd-box {
  min-height: 200px;
}
.rd-hero {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}
.rd-hero h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #0f172a;
}
.rd-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  font-size: 13px;
  color: #64748b;
}
.rd-meta span {
  white-space: nowrap;
}
.rd-section {
  margin-bottom: 18px;
}
.rd-section h4 {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
  padding-left: 8px;
  border-left: 3px solid #94a3b8;
}
.rd-section p {
  margin: 0;
  font-size: 13px;
  color: #475569;
  line-height: 1.6;
}
.rd-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.rd-item {
  margin-bottom: 10px;
  padding: 10px 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 13px;
  color: #334155;
  line-height: 1.6;
}
.rd-item strong {
  color: #0f172a;
}
.rd-item p {
  font-size: 12px;
  color: #64748b;
  margin: 4px 0 0;
}

.search-section :deep(.el-input__wrapper),
.search-section :deep(.el-select__wrapper) {
  background: #ffffff !important;
  box-shadow: none !important;
  border: none !important;
}

.search-section :deep(.el-input__inner),
.search-section :deep(.el-select__selected-item),
.search-section :deep(.el-input__prefix),
.search-section :deep(.el-select__placeholder) {
  color: #334155 !important;
}

.search-section :deep(.el-input__inner::placeholder) {
  color: #94a3b8 !important;
}

.talent-card :deep(.el-card__body) {
  background: transparent;
}

@media (max-width: 980px) {
  .card-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .info-left,
  .info-right {
    max-width: 100%;
    width: 100%;
  }

  .info-right {
    flex-direction: row;
  }
}

@media (max-width: 640px) {
  .search-section {
    padding: 18px;
  }

  .search-bar {
    flex-direction: column;
  }

  .filter-group {
    gap: 12px;
  }

  .filter-row {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
