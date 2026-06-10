<script setup>
import { ref, onMounted, computed } from "vue";
import {
  HomeFilled,
  Search,
  FolderOpened,
  Briefcase,
  Bell,
  Message,
  ArrowDown,
  Expand,
  Fold,
  SwitchButton,
  DataAnalysis,
  User,
  Collection,
  Calendar,
  Connection,
} from "@element-plus/icons-vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import {
  companyApproved,
  statusChecked,
  companyRejected,
  auditStatus,
  checkCompanyStatus,
  resetHrStatus,
} from "@/utils/hrStatus";
import { useUser } from "@/utils/useUser";
import ChatPanel from "@/components/ChatPanel.vue";
import { initChat } from "@/services/chatInit.js";
import { disconnectStomp } from "@/services/stomp.js";
import { resetChat } from "@/services/chatInit";
import { totalUnread } from "@/services/chatCache.js";

const router = useRouter();
const route = useRoute();
const isCollapse = ref(false);
const chatVisible = ref(false);
const { userInfo, clearUser, loadUserInfo } = useUser();

const pageDescMap = {
  "/hr": "实时掌握招聘进度、候选人动向和关键待办",
  "/hr/search": "用更高效的方式筛选并触达合适的人才",
  "/hr/jobs": "统一管理职位状态、数据反馈和投放节奏",
  "/hr/applications": "集中处理投递记录和简历状态",
  "/hr/interview": "管理面试安排与流程推进",
  "/hr/analytics": "查看运营数据与招聘效果表现",
  "/hr/account": "维护 HR 账户信息与企业资料",
  "/hr/ai-match": "通过智能分析发现更高匹配候选人",
  "/hr/talent-pool": "沉淀收藏人才，建立长期人才资产",
};

const currentPageDesc = computed(
  () =>
    pageDescMap[route.path] || "统一风格、统一交互、统一效率的 HR 智能工作台"
);

const handleCommand = (command) => {
  if (command === "logout") {
    disconnectStomp();
    resetChat();
    localStorage.clear();
    clearUser();
    resetHrStatus();
    router.push("/login");
    ElMessage.success("已安全退出");
  }
};

onMounted(() => {
  loadUserInfo();
  checkCompanyStatus();
  initChat();
});

// 顶部状态条：根据 auditStatus 区分文案/颜色
const chipText = computed(() => {
  if (!statusChecked.value) return "企业状态查询中";
  if (companyApproved.value) return "企业状态正常";
  if (companyRejected.value) return "企业审核未通过";
  if (auditStatus.value === 0) return "企业资料待审核";
  return "企业状态未知";
});
const chipClass = computed(() => {
  if (companyApproved.value) return "chip-ok";
  if (companyRejected.value) return "chip-bad";
  return "chip-warn";
});
</script>

<template>
  <div class="hr-workbench">
    <div class="hr-ambient ambient-one"></div>
    <div class="hr-ambient ambient-two"></div>
    <div class="hr-grid"></div>
    <el-container class="layout-wrapper">
      <el-aside :width="isCollapse ? '72px' : '240px'" class="aside-container">
        <div class="brand-box">
          <div class="brand-logo">
            <el-icon :size="32" color="#1677ff"><Briefcase /></el-icon>
          </div>
          <div v-show="!isCollapse" class="brand-content">
            <span class="brand-text">智能招聘</span>
            <span class="brand-subtitle">HR工作台</span>
          </div>
        </div>

        <div class="menu-section">
          <el-menu
            :default-active="route.path"
            class="workbench-menu"
            :collapse="isCollapse"
            :collapse-transition="false"
            background-color="transparent"
            text-color="#8c8c8c"
            active-text-color="#1677ff"
            router
          >
            <el-menu-item index="/hr" class="menu-item highlight-item">
              <el-icon class="menu-icon"><HomeFilled /></el-icon>
              <span class="menu-text">首页概览</span>
            </el-menu-item>

            <template v-if="companyApproved">
              <div v-show="!isCollapse" class="group-title">人才管理</div>
              <el-menu-item index="/hr/ai-match" class="menu-item">
                <el-icon class="menu-icon"><Connection /></el-icon>
                <span class="menu-text">智能分析</span>
              </el-menu-item>
              <el-menu-item index="/hr/search" class="menu-item">
                <el-icon class="menu-icon"><Search /></el-icon>
                <span class="menu-text">人才搜索</span>
              </el-menu-item>
              <el-menu-item index="/hr/talent-pool" class="menu-item">
                <el-icon class="menu-icon"><FolderOpened /></el-icon>
                <span class="menu-text">收藏夹</span>
              </el-menu-item>

              <div v-show="!isCollapse" class="group-title">职位管理</div>
              <el-menu-item index="/hr/jobs" class="menu-item">
                <el-icon class="menu-icon"><Briefcase /></el-icon>
                <span class="menu-text">职位管理</span>
              </el-menu-item>

              <div v-show="!isCollapse" class="group-title">流程管理</div>
              <el-menu-item index="/hr/applications" class="menu-item">
                <el-icon class="menu-icon"><Collection /></el-icon>
                <span class="menu-text">简历管理</span>
              </el-menu-item>
              <el-menu-item index="/hr/interview" class="menu-item">
                <el-icon class="menu-icon"><Calendar /></el-icon>
                <span class="menu-text">面试管理</span>
              </el-menu-item>

              <el-menu-item-group>
                <template #title>
                  <span v-show="!isCollapse" class="group-title-text"
                    >运营分析</span
                  >
                </template>
                <el-menu-item index="/hr/analytics" class="menu-item">
                  <el-icon class="menu-icon"><DataAnalysis /></el-icon>
                  <span class="menu-text">数据分析</span>
                </el-menu-item>
                <el-menu-item index="/hr/account" class="menu-item">
                  <el-icon class="menu-icon"><User /></el-icon>
                  <span class="menu-text">账户设置</span>
                </el-menu-item>
              </el-menu-item-group>
            </template>

            <template v-else>
              <el-menu-item index="/hr/account" class="menu-item">
                <el-icon class="menu-icon"><User /></el-icon>
                <span class="menu-text">账户设置</span>
              </el-menu-item>
            </template>
          </el-menu>
        </div>

        <div class="aside-footer" v-show="!isCollapse">
          <div class="user-mini">
            <el-avatar :size="32" :src="userInfo.avatarUrl">{{
              userInfo.nickname?.charAt(0)
            }}</el-avatar>
            <div class="user-info">
              <div class="user-name">{{ userInfo.nickname }}</div>
              <div class="user-role">
                HR<span v-if="userInfo.companyName">
                  · {{ userInfo.companyName }}</span
                >
              </div>
            </div>
          </div>
        </div>
      </el-aside>

      <el-container class="main-container">
        <el-header class="top-header">
          <div class="header-left">
            <div class="toggle-box" @click="isCollapse = !isCollapse">
              <el-icon class="toggle-icon" :size="20">
                <Expand v-if="isCollapse" />
                <Fold v-else />
              </el-icon>
            </div>
            <div class="breadcrumb">
              <div class="page-copy">
                <span class="page-kicker">Smart Recruit Console</span>
                <span class="current-page">{{
                  route.meta.title || "HR 工作台"
                }}</span>
                <span class="page-desc">{{ currentPageDesc }}</span>
              </div>
            </div>
          </div>

          <div class="header-right">
            <router-link v-if="!companyApproved && statusChecked" to="/hr/setup/pending" class="header-chip" :class="chipClass">
              <span class="chip-dot"></span>
              <span>{{ chipText }}</span>
              <span class="chip-arrow">&gt;</span>
            </router-link>
            <div v-else class="header-chip" :class="chipClass">
              <span class="chip-dot"></span>
              <span>{{ chipText }}</span>
            </div>
            <el-badge
              :is-dot="totalUnread > 0"
              class="badge-wrapper"
              style="cursor: pointer"
              @click="chatVisible = true"
            >
              <el-icon :size="20"><Bell /></el-icon>
            </el-badge>

            <el-dropdown @command="handleCommand" trigger="click">
              <div class="user-trigger">
                <el-avatar :size="36" :src="userInfo.avatarUrl">{{
                  userInfo.nickname?.charAt(0)
                }}</el-avatar>
                <div class="user-details">
                  <span class="user-name">{{ userInfo.nickname }}</span>
                  <span class="user-company">{{
                    userInfo.companyName || "未绑定企业"
                  }}</span>
                </div>
                <el-icon class="arrow-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="workbench-main">
          <div class="content-wrapper">
            <router-view></router-view>
          </div>
        </el-main>
      </el-container>
    </el-container>

    <el-dialog
      v-model="chatVisible"
      title="消息"
      width="820px"
      top="3vh"
      draggable
      overflow
      append-to-body
      :modal="false"
      :lock-scroll="false"
      destroy-on-close
    >
      <ChatPanel />
    </el-dialog>
  </div>
</template>

<style scoped>
.hr-workbench {
  position: relative;
  height: 100vh;
  background: linear-gradient(180deg, #f3f5f7 0%, #eef2f5 100%);
  overflow: hidden;
}

.layout-wrapper {
  height: 100%;
  position: relative;
  z-index: 1;
}

.hr-ambient,
.hr-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hr-ambient {
  filter: blur(18px);
  opacity: 0.35;
}

.ambient-one {
  background: radial-gradient(
    circle at 15% 10%,
    rgba(71, 85, 105, 0.05),
    transparent 30%
  );
}

.ambient-two {
  background: radial-gradient(
    circle at 88% 14%,
    rgba(59, 130, 246, 0.04),
    transparent 26%
  );
}

.hr-grid {
  opacity: 0.08;
  background-image: linear-gradient(
      rgba(100, 116, 139, 0.05) 1px,
      transparent 1px
    ),
    linear-gradient(90deg, rgba(100, 116, 139, 0.05) 1px, transparent 1px);
  background-size: 28px 28px;
  mask-image: radial-gradient(
    circle at center,
    rgba(0, 0, 0, 0.92),
    transparent 95%
  );
}

.aside-container {
  background: #eef2f5;
  border-right: 1px solid #dbe3ea;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: none;
}

.brand-box {
  height: 84px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  border-bottom: 1px solid #dde4eb;
  background: transparent;
}

.brand-logo {
  width: 42px;
  height: 42px;
  background: linear-gradient(135deg, #475569 0%, #64748b 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 18px rgba(71, 85, 105, 0.14);
}

.brand-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-text {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}

.brand-subtitle {
  font-size: 12px;
  color: #64748b;
}

.menu-section {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 14px 12px;
}

.group-title,
.group-title-text,
:deep(.el-menu-item-group__title) {
  font-size: 11px;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.group-title {
  padding: 16px 12px 6px;
}

:deep(.el-menu-item-group__title) {
  padding: 14px 12px 6px;
  background: transparent;
}

.workbench-menu {
  border-right: none;
  background: transparent;
}

.workbench-menu:not(.el-menu--collapse) {
  width: 100%;
}

.highlight-item {
  background: linear-gradient(
    90deg,
    rgba(148, 163, 184, 0.18),
    rgba(226, 232, 240, 0.2)
  ) !important;
}

.menu-item {
  height: 48px;
  line-height: 48px;
  margin: 6px 0;
  border-radius: 14px;
  padding: 0 12px !important;
  color: #334155 !important;
  transition: all 0.28s ease;
  position: relative;
  overflow: hidden;
}

.menu-item::before {
  content: "";
  position: absolute;
  inset: 0 auto 0 0;
  width: 3px;
  border-radius: 0 999px 999px 0;
  background: linear-gradient(180deg, #475569, #64748b);
  opacity: 0;
  transition: opacity 0.28s ease;
}

.menu-item:hover {
  transform: translateX(2px);
  background: #f8fafc !important;
  color: #0f172a !important;
}

.menu-item.is-active {
  background: #ffffff !important;
  color: #0f172a !important;
  box-shadow: inset 0 0 0 1px rgba(100, 116, 139, 0.12);
}

.menu-item:hover::before,
.menu-item.is-active::before {
  opacity: 1;
}

.menu-icon {
  font-size: 18px;
  margin-right: 12px;
  transition: transform 0.28s ease;
}

.menu-item:hover .menu-icon {
  transform: scale(1.08);
}

.menu-text {
  font-size: 14px;
  letter-spacing: 0.02em;
}

.aside-footer {
  border-top: 1px solid #dde4eb;
  padding: 14px 12px;
  background: #eef2f5;
}

.user-mini {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #dde4eb;
  transition: transform 0.28s ease, background 0.28s ease;
}

.user-mini:hover {
  transform: translateY(-2px);
  background: rgba(255, 250, 242, 0.98);
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

.user-role {
  font-size: 12px;
  color: #64748b;
}

.top-header {
  margin: 14px 16px 0;
  min-height: 84px;
  border-radius: 24px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.toggle-box {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.28s ease;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.toggle-box:hover {
  transform: translateY(-2px);
  background: #f1f5f9;
}

.toggle-icon {
  color: #475569;
}

.breadcrumb {
  display: flex;
  align-items: center;
}

.page-copy {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.page-kicker {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #64748b;
}

.current-page {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
}

.page-desc {
  font-size: 13px;
  color: #64748b;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 999px;
  color: #334155;
  font-size: 13px;
  border: 1px solid #dbe3ea;
  background: #f8fafc;
}

/* clickable chip: router-link 样式覆盖 */
a.header-chip {
  text-decoration: none;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
a.header-chip:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.chip-arrow {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 700;
}

.header-nav-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 999px;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  transition: all 0.28s ease;
}
.header-nav-link:hover {
  background: #e2e8f0;
  color: #0f172a;
  transform: translateY(-1px);
}

.header-chip.chip-ok {
  color: #166534;
  border-color: #bbf7d0;
  background: #f0fdf4;
}
.header-chip.chip-warn {
  color: #92400e;
  border-color: #fde68a;
  background: #fffbeb;
}
.header-chip.chip-bad {
  color: #991b1b;
  border-color: #fecaca;
  background: #fef2f2;
}

.banner-link {
  color: #1d4ed8;
  font-weight: 700;
  text-decoration: underline;
  margin: 0 2px;
}

.chip-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #64748b;
  box-shadow: 0 0 10px rgba(100, 116, 139, 0.28);
}

.badge-wrapper {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  color: #475569;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  transition: all 0.28s ease;
}

.badge-wrapper:hover {
  transform: translateY(-2px);
  background: #f1f5f9;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 14px 8px 8px;
  border-radius: 18px;
  transition: all 0.28s ease;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.user-trigger:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.05);
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-company {
  font-size: 12px;
  color: #64748b;
}

.arrow-icon {
  color: #64748b;
  transition: transform 0.28s ease;
}

.user-trigger:hover .arrow-icon {
  transform: rotate(180deg);
}

.workbench-main {
  padding: 18px 18px 22px;
  overflow-y: auto;
  background: transparent;
}

.content-wrapper {
  max-width: 1440px;
  margin: 0 auto;
  border-radius: 30px;
  position: relative;
}

:deep(.el-button--primary) {
  background: #475569;
  border-color: #475569;
  color: #ffffff;
  box-shadow: none;
}

:deep(.el-button--primary:hover) {
  background: #334155;
  border-color: #334155;
}

:deep(.el-button.is-link) {
  color: #475569;
}

:deep(.el-button.is-link:hover) {
  color: #0f172a;
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background: #475569;
  color: #ffffff;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focused),
:deep(.el-textarea__inner:focus),
:deep(.el-range-editor.is-active) {
  box-shadow: 0 0 0 1px rgba(71, 85, 105, 0.24) !important;
}

@media (max-width: 980px) {
  .top-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }

  .header-right {
    width: 100%;
    justify-content: space-between;
  }
}

@media (max-width: 640px) {
  .header-chip {
    display: none;
  }

  .current-page {
    font-size: 20px;
  }
}
</style>
