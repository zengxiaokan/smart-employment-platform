<template>
  <div class="talent-pool">
    <div class="search-section">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索收藏的人才..."
          size="large"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" size="large" :icon="Search">搜索</el-button>
      </div>
    </div>

    <div class="toolbar">
      <span class="total-count">共 {{ filteredList.length }} 位收藏人才</span>
    </div>

    <div class="talent-grid" v-loading="loading">
      <el-empty
        v-if="!loading && filteredList.length === 0 && searched"
        description="暂无收藏人才"
      />
      <el-empty
        v-else-if="!loading && filteredList.length === 0"
        description="还没有收藏任何人才，去人才搜索看看吧"
      />

      <el-card
        v-for="item in filteredList"
        :key="item.id"
        class="talent-card"
        shadow="hover"
      >
        <div class="card-top">
          <el-avatar :size="56" :src="item.characterAvatar">{{
            item.name?.charAt(0) || "?"
          }}</el-avatar>
          <div class="card-info">
            <div class="name">{{ item.name }}</div>
            <div class="intent">{{ item.jobIntention || "-" }}</div>
            <div class="meta">
              <span>{{
                item.totalWorkYears != null
                  ? item.totalWorkYears > 0
                    ? item.totalWorkYears + "年"
                    : "应届生"
                  : "-"
              }}</span>
              <el-divider direction="vertical" />
              <span>{{ educationLabel(item.maxEducation) }}</span>
            </div>
          </div>
          <el-link
            type="warning"
            underline="never"
            class="remark-link"
            @click="viewRemark(item)"
          >
            {{ item.favoriteReason ? "查看备注" : "无备注" }}
          </el-link>
        </div>
        <div class="card-skills" v-if="item.skills">
          <span
            v-for="s in parseSkillList(item.skills)"
            :key="s"
            class="skill-tag"
            >{{ s }}</span
          >
        </div>
        <div class="card-actions">
          <el-button size="small" @click="handleViewResume(item)"
            >查看简历</el-button
          >
          <el-button
            size="small"
            type="danger"
            plain
            @click="handleWantUnfavorite(item)"
            >取消收藏</el-button
          >
        </div>
      </el-card>
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
      </template>
    </el-dialog>

    <el-dialog
      v-model="remarkVisible"
      title="收藏备注"
      width="480px"
      top="10vh"
      destroy-on-close
      @closed="remarkEditing = false"
    >
      <div v-if="!remarkEditing">
        <p class="remark-full">{{ remarkText }}</p>
      </div>
      <div v-else>
        <el-input
          v-model="editRemarkText"
          type="textarea"
          :rows="4"
          placeholder="请输入新的收藏备注..."
        />
      </div>
      <template #footer>
        <el-button @click="remarkVisible = false">关闭</el-button>
        <el-button v-if="!remarkEditing" type="primary" @click="startEditRemark"
          >修改备注</el-button
        >
        <template v-else>
          <el-button @click="remarkEditing = false">取消</el-button>
          <el-button type="primary" @click="saveRemark">保存</el-button>
        </template>
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
import { ref, computed, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import {
  getFavorites,
  getTalentResumeDetail,
  cancelFavorite,
  updateFavoriteReason,
} from "@/api/hr/talent";
import { educationLabel, parseSkills } from "@/utils/format";

const loading = ref(false);
const searched = ref(false);
const list = ref([]);
const keyword = ref("");

const resumeVisible = ref(false);
const resumeLoading = ref(false);
const currentResume = ref(null);
const remarkVisible = ref(false);
const remarkText = ref("");
const remarkEditing = ref(false);
const editRemarkText = ref("");
const remarkTarget = ref(null);
const unfavoriteVisible = ref(false);
const unfavoriteTarget = ref(null);

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

const filteredList = computed(() => {
  if (!keyword.value) return list.value;
  const k = keyword.value.toLowerCase();
  return list.value.filter(
    (item) =>
      (item.name && item.name.toLowerCase().includes(k)) ||
      (item.jobIntention && item.jobIntention.toLowerCase().includes(k)) ||
      (item.skills && item.skills.toLowerCase().includes(k))
  );
});

const fetchList = async () => {
  loading.value = true;
  try {
    const res = await getFavorites();
    if (res.code === 1 && res.data) {
      list.value = Array.isArray(res.data) ? res.data : res.data.records || [];
    } else {
      list.value = [];
    }
    searched.value = true;
  } catch {
    list.value = [];
    searched.value = true;
    ElMessage.error("获取收藏列表失败");
  } finally {
    loading.value = false;
  }
};

const viewRemark = (item) => {
  remarkTarget.value = item;
  remarkText.value = item.favoriteReason || "无";
  remarkEditing.value = false;
  remarkVisible.value = true;
};

const startEditRemark = () => {
  editRemarkText.value = remarkTarget.value?.favoriteReason || "";
  remarkEditing.value = true;
};

const saveRemark = async () => {
  if (!remarkTarget.value) return;
  try {
    const res = await updateFavoriteReason({
      resumeId: remarkTarget.value.id,
      reason: editRemarkText.value,
    });
    if (res.code === 1) {
      ElMessage.success("备注已更新");
      remarkTarget.value.favoriteReason = editRemarkText.value;
      remarkText.value = editRemarkText.value || "无";
      remarkEditing.value = false;
    } else {
      ElMessage.warning(res.msg || "修改失败");
    }
  } catch {
    ElMessage.error("修改失败");
  }
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

const handleWantUnfavorite = (item) => {
  unfavoriteTarget.value = item;
  unfavoriteVisible.value = true;
};

const handleUnfavorite = async () => {
  if (!unfavoriteTarget.value) return;
  try {
    const res = await cancelFavorite(unfavoriteTarget.value.id);
    if (res.code === 1) {
      ElMessage.success("已取消收藏");
      list.value = list.value.filter((i) => i.id !== unfavoriteTarget.value.id);
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
.talent-pool {
  padding: 0;
}

.search-section {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.search-bar {
  display: flex;
  gap: 12px;
  max-width: 600px;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 20px;
}
.total-count {
  font-size: 14px;
  color: #909399;
}

.talent-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  min-height: 200px;
}
.talent-card {
  border-radius: 8px;
  transition: all 0.3s;
}
.talent-card:hover {
  transform: translateY(-2px);
}
.card-top {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
  position: relative;
}
.card-info {
  flex: 1;
}
.name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}
.intent {
  font-size: 14px;
  color: #1677ff;
  margin-bottom: 4px;
}
.meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
}
.remark-link {
  font-size: 12px;
  cursor: pointer;
  flex-shrink: 0;
}
.card-skills {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}
.skill-tag {
  font-size: 12px;
  color: #606266;
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 4px;
}
.card-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.remark-full {
  font-size: 15px;
  color: #303133;
  line-height: 1.8;
  white-space: pre-wrap;
  margin: 0;
  padding: 8px 0;
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
  border-bottom: 1px solid #f0f2f5;
}
.rd-hero h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #303133;
}
.rd-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  font-size: 13px;
  color: #606266;
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
  color: #303133;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}
.rd-section p {
  margin: 0;
  font-size: 13px;
  color: #606266;
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
  border-radius: 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
.rd-item strong {
  color: #303133;
}
.rd-item p {
  font-size: 12px;
  color: #909399;
  margin: 4px 0 0;
}
</style>
