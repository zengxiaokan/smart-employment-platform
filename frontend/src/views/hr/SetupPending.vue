<template>
  <div class="pending-page">
    <div class="pending-card">
      <div class="status-icon">
        <el-icon :size="64"><Clock /></el-icon>
      </div>
      <h1>申请审核中</h1>
      <p>您的企业绑定申请已提交，请耐心等待审核结果</p>

      <div class="info-box">
        <div class="info-item" v-if="application.companyName">
          <span class="info-label">企业名称</span>
          <span class="info-value">{{ application.companyName }}</span>
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
      </div>

      <div class="pending-actions" v-if="application.status === 2">
        <el-button type="primary" size="large" @click="goBackSetup">重新申请</el-button>
      </div>

      <div class="pending-actions" v-if="application.status === 0">
        <el-button type="primary" size="large" :loading="refreshing" @click="checkStatus">
          <el-icon><Refresh /></el-icon> 刷新状态
        </el-button>
      </div>

      <div class="pending-actions">
        <el-button @click="goToLogin">返回登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Clock, Refresh } from "@element-plus/icons-vue";
import { getApplicationStatus } from "@/api/hr/company";

const router = useRouter();
const refreshing = ref(false);
const application = ref({
  applyType: 1, companyName: "", status: 0, auditRemark: "",
});

const statusText = computed(() => {
  const map = { 0: "待审核", 1: "审核通过", 2: "审核拒绝", 3: "已撤回" };
  return map[application.value.status] || "未知";
});

const statusClass = computed(() => {
  const map = { 0: "s-pending", 1: "s-passed", 2: "s-rejected", 3: "s-cancelled" };
  return map[application.value.status] || "";
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
      application.value.status = c.auditStatus;
      application.value.companyName = c.name;
      if (c.auditStatus === 1) {
        const user = JSON.parse(localStorage.getItem("loginUser") || "{}");
        user.companyName = c.name;
        user.companyId = c.id;
        user.avatarUrl = c.logoUrl || user.avatarUrl;
        localStorage.setItem("loginUser", JSON.stringify(user));
        localStorage.setItem("companyInfo", JSON.stringify(c));
        ElMessage.success("审核已通过，即将跳转");
        setTimeout(() => router.replace("/hr"), 600);
      }
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

const goToLogin = () => {
  localStorage.clear();
  router.replace("/login");
};

onMounted(() => {
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

.pending-card {
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-radius: 20px;
  padding: 48px 36px;
  text-align: center;
  box-shadow: 0 16px 48px rgba(0,0,0,0.08);
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

.pending-card h1 { font-size: 22px; font-weight: 700; color: #1a1a1a; margin: 0 0 8px; }
.pending-card > p { font-size: 14px; color: #909399; margin: 0 0 32px; }

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

.pending-actions { margin-top: 8px; }
</style>
