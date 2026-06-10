<template>
  <div :class="embedded ? 'pending-inline' : 'pending-page'">
    <div class="pending-card">
      <div class="status-icon">
        <el-icon :size="embedded ? 48 : 64"><Clock /></el-icon>
      </div>
      <h1>{{ titleText }}</h1>
      <p>{{ subtitleText }}</p>

      <div class="info-box">
        <div class="info-item" v-if="companyName">
          <span class="info-label">企业名称</span>
          <span class="info-value">{{ companyName }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">申请类型</span>
          <span class="info-value">注册新企业</span>
        </div>
        <div class="info-item">
          <span class="info-label">审核状态</span>
          <span class="info-value status-tag" :class="statusClass">
            {{ statusText }}
          </span>
        </div>
        <div v-if="auditStatus === 2 && auditRemark" class="remark-block">
          <div class="remark-label">管理员拒绝理由</div>
          <div class="remark-content">{{ auditRemark }}</div>
        </div>
      </div>

      <div class="pending-actions" v-if="(auditStatus === 2 || auditStatus === 0) && !embedded">
        <el-button type="primary" size="large" :loading="reapplying" @click="handleReapply">
          {{ auditStatus === 2 ? '重新申请' : '刷新申请' }}
        </el-button>
        <el-button size="large" @click="goToDashboard">返回控制台</el-button>
      </div>

      <div class="pending-actions" v-else-if="auditStatus === 1 && !embedded">
        <el-button type="success" size="large" @click="goToWorkbench">进入 HR 工作台</el-button>
        <el-button size="large" @click="goToDashboard">返回控制台</el-button>
      </div>

      <!-- embedded 模式：只有重新申请按钮 -->
      <div class="pending-actions" v-else-if="(auditStatus === 2 || auditStatus === 0) && embedded">
        <el-button type="primary" size="large" :loading="reapplying" @click="handleReapply">
          {{ auditStatus === 2 ? '重新申请' : '刷新申请' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Clock, Refresh } from "@element-plus/icons-vue";
import { getApplicationStatus, reapplyCompany } from "@/api/hr/company";
import {
  auditStatus,
  companyApproved,
  companyRejected,
  checkCompanyStatus,
  statusChecked,
} from "@/utils/hrStatus";

// embedded=true:嵌入到 HrLayout 内(由父组件控制跳转,不自动跳走)
// embedded=false:独立页面模式(从 /hr/setup 提交后跳过来用,自动检测并跳到 /hr)
const props = defineProps({
  embedded: { type: Boolean, default: false },
});

const router = useRouter();
const refreshing = ref(false);
const reapplying = ref(false);

const companyName = computed(() => {
  try {
    const info = JSON.parse(localStorage.getItem("companyInfo") || "{}");
    return info.name || "";
  } catch { return ""; }
});

const auditRemark = computed(() => {
  try {
    const info = JSON.parse(localStorage.getItem("companyInfo") || "{}");
    return info.auditRemark || "";
  } catch { return ""; }
});

const statusText = computed(() => {
  const map = { "-1": "未知", 0: "待审核", 1: "审核通过", 2: "审核拒绝", 3: "已撤回" };
  return map[String(auditStatus.value)] || "未知";
});

const statusClass = computed(() => {
  const map = { 0: "s-pending", 1: "s-passed", 2: "s-rejected", 3: "s-cancelled" };
  return map[auditStatus.value] || "";
});

const titleText = computed(() => {
  if (auditStatus.value === 2) return "企业审核未通过";
  if (auditStatus.value === 1) return "🎉 恭喜！您的企业已通过审核";
  return "申请审核中";
});

const subtitleText = computed(() => {
  if (auditStatus.value === 2) return "请根据下方理由调整资料后重新提交申请";
  if (auditStatus.value === 1) return "现在可以使用 HR 工作台的全部功能,快去发布职位、搜索人才吧";
  return "您的企业绑定申请已提交，点击「刷新申请」提高审核优先级，让管理员更早看到";
});

const forceLogout = (msg) => {
  ElMessage.error(msg || "请重新登录");
  localStorage.clear();
  router.replace("/login");
};

const checkStatus = async () => {
  refreshing.value = true;
  try {
    const res = await getApplicationStatus();
    if (res.code === 1 && res.data) {
      const c = res.data;
      // 同步全局状态(hrStatus.js 的 ref),embedded 模式下父组件会自动切到 dashboard
      auditStatus.value = typeof c.auditStatus === "number" ? c.auditStatus : -1;
      companyApproved.value = c.auditStatus === 1;
      companyRejected.value = c.auditStatus === 2;
      localStorage.setItem("companyInfo", JSON.stringify(c));
      // 同步 loginUser
      const user = JSON.parse(localStorage.getItem("loginUser") || "{}");
      if (c.name) {
        user.companyName = c.name;
        user.companyId = c.id;
        if (c.logoUrl) user.avatarUrl = c.logoUrl;
        localStorage.setItem("loginUser", JSON.stringify(user));
      }
      // status=1 不再 setTimeout 自动跳,让用户看到成功页后自己点"进入工作台"
    } else {
      forceLogout(res.msg || "请重新登录获取状态");
    }
  } catch {
    forceLogout("获取状态失败，请重新登录");
  } finally {
    refreshing.value = false;
  }
};

const goBackSetup = () => {
  router.replace("/hr/setup");
};

/**
 * 重新申请：
 * - status=0: 仅刷新 updatedAt 顶时间
 * - status=2: 重置为待审核(0),清空拒绝理由
 * 后端有 Redis 24h 锁,一天只能操作一次
 */
const handleReapply = async () => {
  if (reapplying.value) return;
  reapplying.value = true;
  try {
    const res = await reapplyCompany();
    if (res.code === 1) {
      ElMessage.success(
        auditStatus.value === 2
          ? "已重新提交申请，请等待管理员审核"
          : "已刷新申请时间，请耐心等待审核"
      );
      await checkStatus();
    } else {
      ElMessage.error(res.msg || "操作失败");
    }
  } catch {
    // request.js 已经提示过
  } finally {
    reapplying.value = false;
  }
};

const goToWorkbench = () => {
  router.replace("/hr");
};

const goToDashboard = () => {
  router.replace("/hr");
};

const goToLogin = () => {
  localStorage.clear();
  router.replace("/login");
};

onMounted(() => {
  // embedded 模式下 HrLayout 已经在 onMounted 调过 checkCompanyStatus,不要重复
  if (props.embedded && statusChecked.value) return;
  // 否则用 checkStatus 同步全局 + 模板
  checkStatus();
});
</script>

<style scoped>
.pending-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 50%, #e0f2fe 100%);
  padding: 40px 20px;
}

/* 嵌入模式:在父容器里正常流式布局,去掉全屏背景和固定卡片宽度 */
.pending-inline {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

.pending-card {
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-radius: 20px;
  padding: 48px 36px;
  text-align: center;
  box-shadow: 0 16px 48px rgba(0,0,0,0.08);
}

.pending-inline .pending-card {
  max-width: 560px;
  padding: 32px 28px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.04);
}

.status-icon {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: #1677ff;
}

.pending-inline .status-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 14px;
}

.pending-card h1 { font-size: 22px; font-weight: 700; color: #1a1a1a; margin: 0 0 8px; }
.pending-card > p { font-size: 14px; color: #909399; margin: 0 0 32px; }

.pending-inline .pending-card h1 { font-size: 18px; margin: 0 0 6px; }
.pending-inline .pending-card > p { font-size: 13px; margin: 0 0 20px; }

.info-box {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 20px;
  text-align: left;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.info-item + .info-item { border-top: 1px solid #e8ecf0; }

.info-label { font-size: 13px; color: #909399; }
.info-value { font-size: 14px; font-weight: 600; color: #1a1a1a; }

.status-tag { border-radius: 6px; padding: 2px 10px; font-size: 12px; }
.s-pending { color: #d97706; background: #fef3c7; }
.s-passed { color: #16a34a; background: #dcfce7; }
.s-rejected { color: #ef4444; background: #fee2e2; }
.s-cancelled { color: #6b7280; background: #f3f4f6; }

.remark-block {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #fef2f2;
  border: 1px solid #fecaca;
}
.remark-label {
  font-size: 12px;
  color: #991b1b;
  font-weight: 700;
  margin-bottom: 6px;
  letter-spacing: 0.04em;
}
.remark-content {
  font-size: 13px;
  line-height: 1.7;
  color: #7f1d1d;
  white-space: pre-wrap;
  word-break: break-word;
}

.pending-actions {
  margin-top: 8px;
  display: flex;
  justify-content: center;
  gap: 16px;
}
.pending-actions + .pending-actions { margin-top: 4px; }
</style>
