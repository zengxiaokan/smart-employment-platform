<template>
  <div class="am-root">
    <div class="am-actions">
      <el-button @click="doRefresh">刷新</el-button>
    </div>

    <div class="am-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane name="pending">
          <template #label>
            <el-badge :value="tabCounts.pending" :hidden="!tabCounts.pending"
              >待处理</el-badge
            >
          </template>
        </el-tab-pane>
        <el-tab-pane name="interviewing">
          <template #label>
            <el-badge
              :value="tabCounts.interviewing"
              :hidden="!tabCounts.interviewing"
              >面试中</el-badge
            >
          </template>
        </el-tab-pane>
        <el-tab-pane name="eliminated" label="已淘汰" />
        <el-tab-pane name="hired" label="已录用" />
      </el-tabs>

      <div class="am-search-bar">
        <el-input
          v-model="searchText"
          placeholder="搜索申请人或职位..."
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

      <el-table
        :data="filteredList"
        stripe
        v-loading="loading"
        class="am-table"
      >
        <el-table-column label="编号" width="70">
          <template #default="{ $index }">{{ $index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column
          prop="jobTitle"
          label="投递职位"
          min-width="160"
          show-overflow-tooltip
        />
        <el-table-column label="简历" width="110">
          <template #default="{ row }">
            <el-button size="small" @click="viewResume(row)"
              >查看简历</el-button
            >
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="appliedAt" label="投递时间" width="120">
          <template #default="{ row }">{{
            row.appliedAt?.substring(0, 10) || "-"
          }}</template>
        </el-table-column>
        <el-table-column label="面试时间" width="130">
          <template #default="{ row }">
            <span v-if="row.interviewTime">{{
              row.interviewTime?.substring(0, 10) || "-"
            }}</span>
            <span v-else class="am-muted">暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="HR备注" min-width="160">
          <template #default="{ row }">
            <template v-if="row.hrRemark">
              <span class="am-remark-text" @click="openRemark(row)">{{
                row.hrRemark
              }}</span>
            </template>
            <template v-else>
              <el-button size="small" @click="openRemark(row)"
                >添加备注</el-button
              >
            </template>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template
              v-if="
                row.status === 0 ||
                row.status === 1 ||
                row.status >= 6 ||
                row.status === 3
              "
            >
              <el-button
                type="primary"
                size="small"
                @click="inviteInterview(row)"
                >邀请面试</el-button
              >
              <el-button type="danger" size="small" @click="markReject(row)"
                >不合适</el-button
              >
            </template>
            <el-button
              v-if="row.status === 2"
              size="small"
              @click="viewInterview(row)"
              >查看面试</el-button
            >
          </template>
        </el-table-column>
      </el-table>
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
      <template #footer>
        <el-button @click="resumeVisible = false">关闭</el-button>
        <el-button
          type="primary"
          @click="inviteFromDetail"
          v-if="
            currentRow &&
            (currentRow.status === 0 ||
              currentRow.status === 1 ||
              currentRow.status >= 6 ||
              currentRow.status === 3)
          "
          >邀请面试</el-button
        >
      </template>
    </el-dialog>

    <el-dialog
      v-model="remarkVisible"
      :title="remarkRow?.hrRemark ? '修改备注' : '添加备注'"
      width="480px"
      top="10vh"
      destroy-on-close
    >
      <el-input
        v-model="remarkText"
        type="textarea"
        :rows="4"
        placeholder="输入备注信息..."
      />
      <template #footer>
        <el-button @click="remarkVisible = false">取消</el-button>
        <el-button type="primary" :loading="remarkSaving" @click="saveRemark"
          >保存</el-button
        >
      </template>
    </el-dialog>

    <el-dialog
      v-model="interviewVisible"
      title="邀请面试"
      width="560px"
      top="5vh"
      destroy-on-close
    >
      <el-form :model="interviewForm" label-width="80px">
        <el-form-item label="面试时间" required>
          <el-date-picker
            v-model="interviewForm.interviewTime"
            type="datetime"
            placeholder="选择面试时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disablePastDate"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="地点类型" required>
          <el-radio-group
            v-model="interviewForm.locationType"
            @change="onLocationTypeChange"
          >
            <el-radio :value="0">线下</el-radio>
            <el-radio :value="1">线上</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          :label="interviewForm.locationType === 1 ? '会议链接' : '详细地址'"
          required
        >
          <el-input
            v-model="interviewForm.interviewLocation"
            :placeholder="
              interviewForm.locationType === 1
                ? '如腾讯会议/飞书链接'
                : '如武汉市洪山区光谷软件园A栋3层'
            "
          />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input
            v-model="interviewForm.contactPerson"
            placeholder="HR联系人姓名"
          />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input
            v-model="interviewForm.contactPhone"
            placeholder="HR联系电话"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="interviewForm.remark"
            type="textarea"
            :rows="3"
            placeholder="面试备注：如需要携带的材料、注意事项等"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="interviewVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="interviewSaving"
          @click="confirmInterview"
          >确认邀请</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import {
  getApplicationList,
  updateApplicationStatus,
  updateApplicationRemark,
} from "@/api/hr/application";
import { getResumeDetail } from "@/api/hr/resume";
import {
  createInterviewInvitation,
  getInterviewByJobAndUser,
} from "@/api/hr/interview";
import {
  formatSalary,
  maxEducationLabel,
  educationLabel,
} from "@/utils/format";

const router = useRouter();

const loading = ref(false);
const list = ref([]);
const activeTab = ref("all");
const searchText = ref("");

const resumeVisible = ref(false);
const currentResume = ref(null);
const currentRow = ref(null);

const remarkVisible = ref(false);
const remarkRow = ref(null);
const remarkText = ref("");
const remarkSaving = ref(false);

const interviewVisible = ref(false);
const interviewForm = reactive({
  interviewTime: "",
  locationType: 0,
  interviewLocation: "",
  contactPerson: "",
  contactPhone: "",
  remark: "",
});
const interviewSaving = ref(false);
const interviewRow = ref(null);
const existingInterviewId = ref(null);
const existingInterviewStatus = ref(null);

const onLocationTypeChange = () => {
  interviewForm.interviewLocation = "";
};

const disablePastDate = (date) => {
  return date.getTime() < Date.now() - 86400000; // 禁用昨天及之前的日期
};

const statusText = (v) => {
  if (v >= 6) return "再次投递";
  return (
    {
      0: "待查看",
      1: "已查看",
      2: "面试中",
      3: "不合适",
      4: "已取消",
      5: "已录用",
    }[v] || "-"
  );
};
const statusTagType = (v) => {
  if (v >= 6) return "warning";
  return (
    { 0: "info", 1: "", 2: "primary", 3: "danger", 4: "info", 5: "success" }[
      v
    ] || "info"
  );
};

const parseSkillsArr = (skills) => {
  if (!skills) return [];
  if (Array.isArray(skills)) return skills;
  return skills.split(",").filter(Boolean);
};

const tabCounts = computed(() => ({
  pending: list.value.filter(
    (i) => i.status === 0 || i.status === 1 || i.status >= 6
  ).length,
  interviewing: list.value.filter((i) => i.status === 2).length,
  eliminated: list.value.filter((i) => i.status === 3 || i.status === 4).length,
  hired: list.value.filter((i) => i.status === 5).length,
}));

const filteredList = computed(() => {
  let arr = list.value;
  if (searchText.value) {
    const kw = searchText.value.toLowerCase();
    arr = arr.filter(
      (i) =>
        i.applicantName?.toLowerCase().includes(kw) ||
        i.jobTitle?.toLowerCase().includes(kw)
    );
  }
  if (activeTab.value === "all") return arr;
  if (activeTab.value === "interviewing")
    return arr.filter((i) => i.status === 2);
  if (activeTab.value === "hired") return arr.filter((i) => i.status === 5);
  if (activeTab.value === "eliminated")
    return arr.filter((i) => i.status === 3 || i.status === 4);
  return arr.filter((i) => i.status === 0 || i.status === 1 || i.status >= 6);
});

const fetch = async () => {
  loading.value = true;
  try {
    const res = await getApplicationList({});
    if (res.code === 1 && res.data) {
      list.value = Array.isArray(res.data) ? res.data : res.data.records || [];
    } else {
      list.value = [];
    }
  } catch {
    list.value = [];
  } finally {
    loading.value = false;
  }
};

const doRefresh = () => {
  fetch();
};

const resumeLoading = ref(false);
const viewResume = async (row) => {
  currentRow.value = row;
  resumeLoading.value = true;
  resumeVisible.value = true;
  try {
    const res = await getResumeDetail(row.resumeId, row.id);
    if (res.code === 1 && res.data) {
      currentResume.value = res.data;
      if (row.status === 0) row.status = 1;
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

const inviteInterview = async (row) => {
  interviewRow.value = row;
  interviewVisible.value = true;
  existingInterviewId.value = null;
  existingInterviewStatus.value = null;
  interviewForm.interviewTime = "";
  interviewForm.locationType = 0;
  interviewForm.interviewLocation = "";
  interviewForm.contactPerson = "";
  interviewForm.contactPhone = "";
  interviewForm.remark = "";
  interviewSaving.value = true;
  try {
    const res = await getInterviewByJobAndUser(row.jobId, row.userId);
    if (res.code === 1 && res.data) {
      const d = res.data;
      existingInterviewId.value = d.id || null;
      existingInterviewStatus.value = d.status;
      interviewForm.interviewTime = d.interviewTime || "";
      interviewForm.locationType = d.locationType ?? 0;
      interviewForm.interviewLocation = d.interviewLocation || "";
      interviewForm.contactPerson = d.contactPerson || "";
      interviewForm.contactPhone = d.contactPhone || "";
      interviewForm.remark = d.hrRemark || d.remark || row.hrRemark || "";
    }
  } catch {
  } finally {
    interviewSaving.value = false;
  }
};

const confirmInterview = async () => {
  if (!interviewForm.interviewTime) return ElMessage.warning("请选择面试时间");
  if (!interviewForm.interviewLocation.trim())
    return ElMessage.warning("请填写面试地点/链接");
  interviewSaving.value = true;
  try {
    const payload = {
      applicationId: interviewRow.value.id,
      jobId: interviewRow.value.jobId,
      userId: interviewRow.value.userId,
      interviewTime: interviewForm.interviewTime,
      locationType: interviewForm.locationType,
      interviewLocation: interviewForm.interviewLocation,
      contactPerson: interviewForm.contactPerson || undefined,
      contactPhone: interviewForm.contactPhone || undefined,
      hrRemark: interviewForm.remark || undefined,
    };
    if (existingInterviewId.value) {
      payload.id = existingInterviewId.value;
      payload.status = existingInterviewStatus.value;
    }
    const res = await createInterviewInvitation(payload);
    if (res.code === 1) {
      interviewRow.value.status = 2;
      interviewRow.value.interviewTime = interviewForm.interviewTime;
      interviewRow.value.hrRemark = interviewForm.remark || null;
      ElMessage.success("面试邀约已发送");
      interviewVisible.value = false;
    } else ElMessage.error(res.msg || "操作失败");
  } catch {
    ElMessage.error("网络异常");
  } finally {
    interviewSaving.value = false;
  }
};

const inviteFromDetail = () => {
  if (!currentRow.value) return;
  resumeVisible.value = false;
  inviteInterview(currentRow.value);
};

const viewInterview = (row) => {
  router.push({ path: "/hr/interview", query: { search: row.applicantName } });
};

const openRemark = (row) => {
  remarkRow.value = row;
  remarkText.value = row.hrRemark || "";
  remarkVisible.value = true;
};

const saveRemark = async () => {
  remarkSaving.value = true;
  try {
    const res = await updateApplicationRemark(
      remarkRow.value.id,
      remarkText.value || null
    );
    if (res.code === 1) {
      remarkRow.value.hrRemark = remarkText.value || null;
      ElMessage.success("备注已保存");
      remarkVisible.value = false;
    } else ElMessage.error(res.msg || "操作失败");
  } catch {
    ElMessage.error("网络异常");
  } finally {
    remarkSaving.value = false;
  }
};

const markHired = (row) => {
  ElMessageBox.confirm(`确定录用「${row.applicantName}」？`, "录用确认", {
    type: "success",
  })
    .then(async () => {
      try {
        const res = await updateApplicationStatus(row.id, 5);
        if (res.code === 1) {
          row.status = 5;
          ElMessage.success("已录用");
        } else ElMessage.error(res.msg || "操作失败");
      } catch {
        ElMessage.error("网络异常");
      }
    })
    .catch(() => {});
};

const markReject = (row) => {
  ElMessageBox.confirm(
    `确定标记「${row.applicantName}」为不合适？`,
    "拒绝确认",
    { type: "error" }
  )
    .then(async () => {
      try {
        const res = await updateApplicationStatus(row.id, 3);
        if (res.code === 1) {
          row.status = 3;
          ElMessage.success("已标记");
        } else ElMessage.error(res.msg || "操作失败");
      } catch {
        ElMessage.error("网络异常");
      }
    })
    .catch(() => {});
};

onMounted(fetch);
</script>

<style scoped>
.am-root {
}
.am-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
  justify-content: flex-end;
}

.am-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);
}
.am-search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 16px 0;
}
.am-muted {
  color: #c0c4cc;
  font-size: 12px;
}
.am-remark-text {
  color: #3b82f6;
  cursor: pointer;
  font-size: 13px;
  line-height: 1.5;
}
.am-remark-text:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

.rd-box {
}
.rd-hero {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}
.rd-hero h3 {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px;
}
.rd-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 13px;
  color: #64748b;
}
.rd-meta span {
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
}
.rd-section {
  margin-bottom: 20px;
}
.rd-section h4 {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin: 0 0 8px;
  padding-left: 10px;
  border-left: 3px solid #3b82f6;
}
.rd-section p {
  font-size: 13px;
  color: #475569;
  line-height: 1.7;
  margin: 0;
}
.rd-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.rd-item {
  margin-bottom: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  font-size: 13px;
  color: #475569;
}
.rd-item strong {
  color: #1e293b;
}
.rd-item p {
  margin: 4px 0 0;
  color: #94a3b8;
}
</style>
