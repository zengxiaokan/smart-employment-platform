<template>
  <div class="im-root">
    <el-row :gutter="16" class="im-stats">
      <el-col :span="6">
        <div class="stat-card stat-pending">
          <strong>{{ stats.pending }}</strong>
          <span>待确认</span>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-today">
          <strong>{{ stats.today }}</strong>
          <span>今日面试</span>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-week">
          <strong>{{ stats.week }}</strong>
          <span>本周面试</span>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-passed">
          <strong>{{ stats.accepted }}</strong>
          <span>已接受</span>
        </div>
      </el-col>
    </el-row>

    <div class="im-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane name="pending">
          <template #label>
            <el-badge :value="tabCounts.pending" :hidden="!tabCounts.pending"
              >待确认</el-badge
            >
          </template>
        </el-tab-pane>
        <el-tab-pane name="accepted">
          <template #label>
            <el-badge :value="tabCounts.accepted" :hidden="!tabCounts.accepted"
              >已接受</el-badge
            >
          </template>
        </el-tab-pane>
        <el-tab-pane name="tentative">
          <template #label>
            <el-badge
              :value="tabCounts.tentative"
              :hidden="!tabCounts.tentative"
              >待定</el-badge
            >
          </template>
        </el-tab-pane>
        <el-tab-pane label="面试成功" name="passed" />
        <el-tab-pane label="不通过" name="rejected" />
        <el-tab-pane label="全部" name="all" />
      </el-tabs>

      <div class="im-search-bar">
        <el-input
          v-model="searchText"
          placeholder="搜索候选人姓名或职位..."
          clearable
          style="width: 300px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button v-if="searchText" @click="searchText = ''"
          >清除筛选</el-button
        >
      </div>

      <el-table :data="filteredList" stripe row-key="id" class="im-table">
        <el-table-column label="候选人" min-width="120">
          <template #default="{ row }">
            <div class="candidate-cell">
              <el-avatar :size="36" :src="row.avatarUrl">{{
                row.userName?.charAt(0)
              }}</el-avatar>
              <span class="candidate-name">{{ row.userName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="jobTitle"
          label="应聘职位"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column
          prop="interviewTime"
          label="面试时间"
          min-width="160"
        />
        <el-table-column label="面试方式" width="80">
          <template #default="{ row }">
            <el-tag
              :type="row.locationType === 1 ? 'success' : 'info'"
              size="small"
            >
              {{ row.locationType === 1 ? "线上" : "线下" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="interviewLocation"
          label="面试地点"
          min-width="160"
          show-overflow-tooltip
        />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{
              statusText(row.status)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="求职者备注"
          min-width="140"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span :class="{ 'im-muted': !row.candidateRemark }">{{
              row.candidateRemark || "-"
            }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0 || row.status === 1 || row.status === 2"
              type="primary"
              size="small"
              @click="openStatusDialog(row)"
              >修改</el-button
            >
            <el-button type="primary" size="small" @click="viewResume(row)"
              >查看简历</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 简历弹窗 -->
    <el-dialog
      v-model="resumeVisible"
      title="候选人简历"
      width="700px"
      top="3vh"
      destroy-on-close
    >
      <div class="rd-box" v-if="currentResume" v-loading="resumeLoading">
        <div class="rd-hero">
          <el-avatar :size="56" :src="currentResume.characterAvatar">{{
            currentResume.name?.charAt(0) || ""
          }}</el-avatar>
          <div>
            <h3>{{ currentResume.name }}</h3>
            <div class="rd-meta">
              <span>{{ genderLabel(currentResume.gender) }}</span>
              <span>{{ currentResume.age }}岁</span>
              <span v-if="currentResume.graduationSchool">{{
                currentResume.graduationSchool
              }}</span>
              <span v-if="currentResume.totalWorkYears != null"
                >{{ currentResume.totalWorkYears }}年</span
              >
              <span v-if="currentResume.maxEducation != null">{{
                maxEducationLabel(currentResume.maxEducation)
              }}</span>
              <span>{{ phoneLabel(currentResume.phone) }}</span>
              <span>{{ currentResume.email || "-" }}</span>
            </div>
          </div>
        </div>

        <div
          class="rd-section"
          v-if="
            currentResume.jobIntention ||
            currentResume.city ||
            currentResume.salaryMin ||
            currentResume.salaryMax
          "
        >
          <h4>求职意向</h4>
          <p>
            {{ currentResume.jobIntention || "-" }} |
            {{ currentResume.city || "-" }} |
            {{ fmtSalary(currentResume) || "面议" }}
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
              v-for="s in parseSkillTags(currentResume.skills)"
              :key="s"
              size="small"
              type="success"
              effect="plain"
              >{{ s }}</el-tag
            >
          </div>
        </div>

        <div
          class="rd-section"
          v-if="currentResume.educations && currentResume.educations.length"
        >
          <h4>教育经历</h4>
          <div
            v-for="(e, i) in currentResume.educations"
            :key="i"
            class="rd-item"
          >
            <strong>{{ e.school }}</strong
            ><span v-if="e.major"> · {{ e.major }}</span>
            <span v-if="e.education != null">
              · {{ educationLabel(e.education) }}</span
            >
            <span v-if="e.startTime || e.endTime">
              · {{ e.startTime || "?" }} — {{ e.endTime || "至今" }}</span
            >
            <p v-if="e.description">{{ e.description }}</p>
          </div>
        </div>

        <div
          class="rd-section"
          v-if="currentResume.experiences && currentResume.experiences.length"
        >
          <h4>工作经历</h4>
          <div
            v-for="(e, i) in currentResume.experiences"
            :key="i"
            class="rd-item"
          >
            <strong>{{ e.company }}</strong
            ><span v-if="e.position"> · {{ e.position }}</span>
            <span v-if="e.startTime || e.endTime">
              · {{ e.startTime || "?" }} — {{ e.endTime || "至今" }}</span
            >
            <p v-if="e.description">{{ e.description }}</p>
          </div>
        </div>

        <div
          class="rd-section"
          v-if="currentResume.projects && currentResume.projects.length"
        >
          <h4>项目经历</h4>
          <div
            v-for="(p, i) in currentResume.projects"
            :key="i"
            class="rd-item"
          >
            <strong>{{ p.name || "未命名项目" }}</strong
            ><span v-if="p.role"> · {{ p.role }}</span>
            <span v-if="p.startTime || p.endTime">
              · {{ p.startTime || "?" }} — {{ p.endTime || "至今" }}</span
            >
            <p v-if="p.description">{{ p.description }}</p>
          </div>
        </div>
      </div>
      <el-empty v-else-if="!resumeLoading" description="暂无简历信息" />
      <template #footer>
        <el-button @click="resumeVisible = false">关闭</el-button>
        <el-button type="primary" @click="reInvite">重新邀请</el-button>
      </template>
    </el-dialog>

    <!-- 邀请面试弹窗 -->
    <el-dialog
      v-model="inviteVisible"
      title="邀请面试"
      width="560px"
      top="5vh"
      destroy-on-close
    >
      <el-form :model="inviteForm" label-width="80px">
        <el-form-item label="面试时间" required>
          <el-date-picker
            v-model="inviteForm.interviewTime"
            type="datetime"
            placeholder="选择面试时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disablePastDate"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="地点类型" required>
          <el-radio-group v-model="inviteForm.locationType">
            <el-radio :value="0">线下</el-radio>
            <el-radio :value="1">线上</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          :label="inviteForm.locationType === 1 ? '会议链接' : '详细地址'"
          required
        >
          <el-input
            v-model="inviteForm.interviewLocation"
            :placeholder="
              inviteForm.locationType === 1
                ? '如腾讯会议/飞书链接'
                : '如武汉市洪山区光谷软件园A栋3层'
            "
          />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input
            v-model="inviteForm.contactPerson"
            placeholder="HR联系人姓名"
          />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input
            v-model="inviteForm.contactPhone"
            placeholder="HR联系电话"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="inviteForm.remark"
            type="textarea"
            :rows="3"
            placeholder="面试备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inviteVisible = false">取消</el-button>
        <el-button type="primary" :loading="inviteSaving" @click="confirmInvite"
          >确认邀请</el-button
        >
      </template>
    </el-dialog>

    <!-- 修改状态弹窗 -->
    <el-dialog
      v-model="statusDialogVisible"
      title="修改面试状态"
      width="480px"
      destroy-on-close
    >
      <el-form :model="statusDialogForm" label-width="80px">
        <el-form-item label="目标状态">
          <el-select
            v-model="statusDialogForm.status"
            placeholder="请选择状态"
            style="width: 100%"
          >
            <el-option
              v-for="opt in dialogStatusOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input
            v-model="statusDialogForm.remark"
            type="textarea"
            :rows="4"
            placeholder="告知候选人状态变更的原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="statusDialogSaving"
          @click="submitStatusChange"
          >确认修改</el-button
        >
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="面试详情"
      width="560px"
      destroy-on-close
    >
      <el-descriptions v-if="currentRow" :column="2" border size="large">
        <el-descriptions-item label="候选人">{{
          currentRow.userName
        }}</el-descriptions-item>
        <el-descriptions-item label="应聘职位">{{
          currentRow.jobTitle
        }}</el-descriptions-item>
        <el-descriptions-item label="面试时间" :span="2">{{
          currentRow.interviewTime
        }}</el-descriptions-item>
        <el-descriptions-item label="面试方式">{{
          currentRow.locationType === 1 ? "线上" : "线下"
        }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{
          statusText(currentRow.status)
        }}</el-descriptions-item>
        <el-descriptions-item label="面试地点" :span="2">{{
          currentRow.interviewLocation
        }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{
          currentRow.contactPerson || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{
          currentRow.contactPhone || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="HR备注" :span="2">{{
          currentRow.remark || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="候选人回复" :span="2">{{
          currentRow.candidateRemark || "暂无回复"
        }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false"
          >关闭</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import {
  getInterviewList,
  updateInterviewStatus,
  createInterviewInvitation,
  getInterviewDetail,
} from "@/api/hr/interview";
import { getResumeByApplication } from "@/api/hr/resume";
import { maxEducationLabel, educationLabel } from "@/utils/format";

const route = useRoute();

const activeTab = ref("pending");
const detailVisible = ref(false);
const currentRow = ref(null);
const list = ref([]);
const searchText = ref("");

const resumeVisible = ref(false);
const resumeLoading = ref(false);
const currentResume = ref(null);

const statusDialogVisible = ref(false);
const statusDialogSaving = ref(false);
const statusDialogForm = reactive({ status: 0, remark: "" });
let statusDialogRow = null;

const inviteVisible = ref(false);
const inviteSaving = ref(false);
const inviteForm = reactive({
  interviewTime: "",
  locationType: 0,
  interviewLocation: "",
  contactPerson: "",
  contactPhone: "",
  remark: "",
});

const dialogStatusOptions = computed(() => {
  if (!statusDialogRow) return [];
  if (statusDialogRow.status === 0)
    return [
      { label: "待定", value: 2 },
      { label: "面试成功", value: 3 },
      { label: "拒绝", value: 4 },
    ];
  if (statusDialogRow.status === 1)
    return [
      { label: "面试成功", value: 3 },
      { label: "待定", value: 2 },
      { label: "拒绝", value: 4 },
    ];
  if (statusDialogRow.status === 2)
    return [
      { label: "面试成功", value: 3 },
      { label: "拒绝", value: 4 },
    ];
  return [];
});

const statusText = (s) =>
  ({
    0: "待确认",
    1: "已接受",
    2: "待定",
    3: "面试成功",
    4: "已拒绝",
    5: "已过期",
    6: "已取消",
  }[s] || "未知");
const statusType = (s) =>
  ({
    0: "warning",
    1: "success",
    3: "success",
    4: "danger",
    5: "info",
    6: "info",
  }[s] || "info");
const genderLabel = (g) => ({ 0: "未知", 1: "男", 2: "女" }[g] || "-");
const phoneLabel = (p) => p || "-";

const parseSkillTags = (skills) => {
  if (!skills) return [];
  if (Array.isArray(skills)) return skills;
  return skills
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean);
};

const fmtSalary = (r) => {
  if (!r.salaryMin && !r.salaryMax) return "";
  return (
    (r.salaryMin || "?").toString() + " - " + (r.salaryMax || "?").toString()
  );
};

// 按 tab 分类后的数据（不受 searchText 影响）
const groupedByTab = computed(() => ({
  pending: list.value.filter((i) => i.status === 0),
  accepted: list.value.filter((i) => i.status === 1),
  tentative: list.value.filter((i) => i.status === 2),
  passed: list.value.filter((i) => i.status === 3),
  rejected: list.value.filter((i) => i.status >= 4),
}));

const tabCounts = computed(() => ({
  pending: groupedByTab.value.pending.length,
  accepted: groupedByTab.value.accepted.length,
  tentative: groupedByTab.value.tentative.length,
  passed: groupedByTab.value.passed.length,
  rejected: groupedByTab.value.rejected.length,
}));

const stats = computed(() => {
  const pending = groupedByTab.value.pending.length;
  const accepted = groupedByTab.value.accepted.length;
  const today = list.value.filter((i) => {
    if (!i.interviewTime) return false;
    return (
      new Date(i.interviewTime).toDateString() === new Date().toDateString()
    );
  }).length;
  const weekStart = new Date();
  weekStart.setDate(weekStart.getDate() - weekStart.getDay() + 1);
  weekStart.setHours(0, 0, 0, 0);
  const week = list.value.filter(
    (i) => i.interviewTime && new Date(i.interviewTime) >= weekStart
  ).length;
  return { pending, accepted, today, week };
});

const filteredList = computed(() => {
  const groupKey = activeTab.value;
  let arr = groupedByTab.value[groupKey] || list.value;
  if (searchText.value) {
    const kw = searchText.value.toLowerCase();
    arr = arr.filter(
      (i) =>
        i.userName?.toLowerCase().includes(kw) ||
        i.jobTitle?.toLowerCase().includes(kw)
    );
  }
  return arr;
});

const fetchList = async () => {
  try {
    const res = await getInterviewList();
    if (res.code === 1) list.value = res.data?.records || res.data || [];
  } catch {}
  if (route.query.search) {
    searchText.value = route.query.search;
  }
};

const viewDetail = (row) => {
  currentRow.value = row;
  detailVisible.value = true;
};

const viewResume = async (row) => {
  currentRow.value = row;
  resumeVisible.value = true;
  resumeLoading.value = true;
  currentResume.value = null;
  try {
    const res = await getResumeByApplication(row.applicationId);
    if (res.code === 1 && res.data) {
      currentResume.value = res.data;
    }
  } catch {
    currentResume.value = null;
  } finally {
    resumeLoading.value = false;
  }
};

const reInvite = async () => {
  if (!currentRow.value) return;
  if (currentRow.value.status === 3) {
    ElMessage.warning("面试已通过，无需重新邀请");
    return;
  }
  resumeVisible.value = false;
  inviteVisible.value = true;
  inviteForm.interviewTime = "";
  inviteForm.locationType = 0;
  inviteForm.interviewLocation = "";
  inviteForm.contactPerson = "";
  inviteForm.contactPhone = "";
  inviteForm.remark = "";
  try {
    const res = await getInterviewDetail(currentRow.value.id);
    if (res.code === 1 && res.data) {
      const d = res.data;
      inviteForm.interviewTime = d.interviewTime || "";
      inviteForm.locationType = d.locationType ?? 0;
      inviteForm.interviewLocation = d.interviewLocation || "";
      inviteForm.contactPerson = d.contactPerson || "";
      inviteForm.contactPhone = d.contactPhone || "";
      inviteForm.remark = d.hrRemark || currentRow.value.remark || "";
    }
  } catch {}
};

const disablePastDate = (date) => {
  return date.getTime() < Date.now() - 86400000;
};

const confirmInvite = async () => {
  if (!currentRow.value) return;
  if (!inviteForm.interviewTime) return ElMessage.warning("请选择面试时间");
  if (!inviteForm.interviewLocation.trim())
    return ElMessage.warning("请填写面试地点/链接");
  inviteSaving.value = true;
  try {
    const res = await createInterviewInvitation({
      id: currentRow.value.id,
      applicationId: currentRow.value.applicationId,
      userId: currentRow.value.userId,
      jobId: currentRow.value.jobId,
      status: currentRow.value.status,
      interviewTime: inviteForm.interviewTime,
      locationType: inviteForm.locationType,
      interviewLocation: inviteForm.interviewLocation,
      contactPerson: inviteForm.contactPerson || undefined,
      contactPhone: inviteForm.contactPhone || undefined,
      hrRemark: inviteForm.remark || undefined,
    });
    if (res.code === 1) {
      currentRow.value.interviewTime = inviteForm.interviewTime;
      currentRow.value.remark = inviteForm.remark || null;
      ElMessage.success("面试邀约已更新");
      inviteVisible.value = false;
      fetchList();
    } else ElMessage.error(res.msg || "操作失败");
  } catch {
    ElMessage.error("网络异常");
  } finally {
    inviteSaving.value = false;
  }
};

const openStatusDialog = (row) => {
  statusDialogRow = row;
  statusDialogForm.status = 0;
  statusDialogForm.remark = row.remark || "";
  statusDialogVisible.value = true;
};

const submitStatusChange = () => {
  if (!statusDialogRow) return;
  if (!statusDialogForm.status) {
    ElMessage.warning("请选择目标状态");
    return;
  }
  const labels = { 2: "待定", 3: "面试成功", 4: "拒绝" };
  const doSubmit = async () => {
    statusDialogSaving.value = true;
    try {
      const res = await updateInterviewStatus(
        statusDialogRow.id,
        statusDialogForm.status,
        statusDialogForm.remark || null
      );
      if (res.code === 1) {
        statusDialogRow.status = statusDialogForm.status;
        statusDialogRow.remark = statusDialogForm.remark || null;
        ElMessage.success(`已${labels[statusDialogForm.status]}`);
        statusDialogVisible.value = false;
      } else {
        ElMessage.error(res.msg || "操作失败");
      }
    } catch {
      ElMessage.error("网络异常");
    } finally {
      statusDialogSaving.value = false;
    }
  };

  if (statusDialogForm.status === 4) {
    ElMessageBox.confirm(
      "确定拒绝该候选人？此操作不可撤回，将同步更新申请表为不合适",
      "拒绝确认",
      {
        confirmButtonText: "确定拒绝",
        cancelButtonText: "再想想",
        type: "warning",
      }
    )
      .then(doSubmit)
      .catch(() => {});
    return;
  }
  doSubmit();
};

onMounted(fetchList);
</script>

<style scoped>
.im-stats {
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px 20px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);
}
.stat-card strong {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 4px;
}
.stat-card span {
  font-size: 13px;
  color: #94a3b8;
}
.stat-pending strong {
  color: #d97706;
}
.stat-today strong {
  color: #3b82f6;
}
.stat-week strong {
  color: #7c3aed;
}
.stat-accepted strong,
.stat-passed strong {
  color: #059669;
}

.im-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);
}
.im-search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.im-table {
  margin-top: 8px;
}
.im-muted {
  color: #c0c4cc;
  font-size: 12px;
}

.candidate-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.candidate-name {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.rd-box {
}
.rd-hero {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 18px;
  margin-bottom: 18px;
  border-bottom: 1px solid #f1f5f9;
}
.rd-hero h3 {
  margin: 0 0 6px;
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
}
.rd-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 14px;
  font-size: 13px;
  color: #64748b;
}

.rd-section {
  margin-bottom: 18px;
}
.rd-section h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1e40af;
  margin: 0 0 8px;
  padding-left: 10px;
  border-left: 3px solid #3b82f6;
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
  gap: 8px;
}

.rd-item {
  padding: 8px 0;
  font-size: 13px;
  color: #475569;
  line-height: 1.6;
  border-bottom: 1px dashed #f1f5f9;
}
.rd-item:last-child {
  border-bottom: none;
}
.rd-item strong {
  color: #0f172a;
  margin-right: 2px;
}
.rd-item p {
  margin: 4px 0 0;
  color: #94a3b8;
  font-size: 12px;
}
</style>
