<script setup>
import { ref, onMounted } from "vue";
import {
  DataBoard,
  User,
  UserFilled,
  OfficeBuilding,
  Briefcase,
  Document,
  Collection,
  Ticket,
  ArrowDown,
  Expand,
  Fold,
  Platform,
  SwitchButton,
  HomeFilled,
  ChatLineRound,
  Bell,
  Notebook,
} from "@element-plus/icons-vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import ChatPanel from "@/components/ChatPanel.vue";
import { initChat } from "@/services/chatInit.js";
import { disconnectStomp } from "@/services/stomp.js";
import { resetChat } from "@/services/chatInit";
import { totalUnread } from "@/services/chatCache.js";

const router = useRouter();
const route = useRoute();

const isCollapse = ref(false);
const chatVisible = ref(false);

const handleCommand = (command) => {
  if (command === "logout") {
    disconnectStomp();
    resetChat();
    localStorage.clear();
    router.push("/login");
    ElMessage.success("已安全退出");
  } else if (command === "account") {
    router.push("/admin/account");
  } else if (command === "messages") {
    chatVisible.value = true;
  }
};

onMounted(() => {
  initChat();
});
</script>

<template>
  <div class="admin-layout">
    <el-container class="root-container">
      <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
        <div class="logo-area" @click="router.push('/admin')">
          <div class="logo-icon">
            <el-icon :size="22"><Platform /></el-icon>
          </div>
          <span v-show="!isCollapse" class="logo-text">智能招聘管理</span>
        </div>

        <el-menu
          :default-active="route.path"
          class="side-menu"
          :collapse="isCollapse"
          router
        >
          <el-menu-item index="/admin">
            <el-icon><DataBoard /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/hr">
            <el-icon><UserFilled /></el-icon>
            <span>HR管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/companies">
            <el-icon><OfficeBuilding /></el-icon>
            <span>企业管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/jobs">
            <el-icon><Briefcase /></el-icon>
            <span>职位管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/resumes">
            <el-icon><Document /></el-icon>
            <span>简历管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><Notebook /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          <el-menu-item index="/admin/records">
            <el-icon><Ticket /></el-icon>
            <span>投递记录</span>
          </el-menu-item>
          <el-menu-item index="/admin/feedbacks">
            <el-icon><Bell /></el-icon>
            <span>反馈与公告</span>
          </el-menu-item>
          <el-menu-item index="/admin/docs">
            <el-icon><Collection /></el-icon>
            <span>项目文档</span>
          </el-menu-item>
          <el-menu-item index="/admin/account">
            <el-icon><HomeFilled /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
        </el-menu>

        <div class="side-foot" v-show="!isCollapse">
          <div class="logout-btn" @click="handleCommand('logout')">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </div>
        </div>
      </el-aside>

      <el-container class="main-area">
        <el-header class="top-bar">
          <div class="top-left">
            <div class="toggle-box" @click="isCollapse = !isCollapse">
              <el-icon :size="20">
                <Expand v-if="isCollapse" />
                <Fold v-else />
              </el-icon>
            </div>
            <el-breadcrumb separator="›">
              <el-breadcrumb-item :to="{ path: '/admin' }"
                >首页</el-breadcrumb-item
              >
              <el-breadcrumb-item>{{
                route.meta.title || "系统管理"
              }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="top-right">
            <el-dropdown @command="handleCommand" trigger="click">
              <el-badge :is-dot="totalUnread > 0">
                <div class="user-trigger">
                  <el-avatar
                    :size="32"
                    src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
                  />
                  <span class="user-name">管理员</span>
                  <el-icon class="arrow-icon"><ArrowDown /></el-icon>
                </div>
              </el-badge>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="account">
                    <el-icon><HomeFilled /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="messages">
                    <el-icon><ChatLineRound /></el-icon>消息
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="content-main">
          <div class="content-view">
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
      :close-on-click-modal="false"
      destroy-on-close
    >
      <ChatPanel />
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
  font-family: "Inter", "PingFang SC", "Microsoft YaHei", sans-serif;
  background: radial-gradient(
      circle at top left,
      rgba(16, 185, 129, 0.12),
      transparent 25%
    ),
    linear-gradient(180deg, #f6fbff 0%, #f5f8ff 55%, #f8fbff 100%);
}

.root-container {
  height: 100%;
}

.aside {
  background: linear-gradient(
    180deg,
    rgba(255, 255, 255, 0.88),
    rgba(247, 250, 255, 0.88)
  );
  border-right: 1px solid rgba(148, 163, 184, 0.16);
  transition: width 0.3s;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 18px 0 50px rgba(15, 23, 42, 0.06);
  backdrop-filter: blur(18px);
}

.logo-area {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
  padding: 0 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
}

.logo-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, #34d399, #059669);
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 18px 34px rgba(5, 150, 105, 0.24);
}

.logo-text {
  font-size: 16px;
  font-weight: 800;
  white-space: nowrap;
  color: #065f46;
  letter-spacing: 0.02em;
}

.side-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 10px 0;
  background: transparent;
}

.side-menu :deep(.el-menu-item) {
  margin: 4px 10px;
  border-radius: 14px;
  height: 46px;
  line-height: 46px;
  color: #475569;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.24s ease;
}

.side-menu :deep(.el-menu-item:hover) {
  transform: translateX(3px);
  background: rgba(16, 185, 129, 0.08) !important;
  color: #047857 !important;
}

.side-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(
    90deg,
    rgba(16, 185, 129, 0.12),
    rgba(96, 165, 250, 0.1)
  ) !important;
  color: #047857 !important;
  box-shadow: inset 0 0 0 1px rgba(16, 185, 129, 0.08);
}

.side-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
}

.side-menu.el-menu--collapse :deep(.el-menu-item) {
  margin: 4px 8px;
  justify-content: center;
  padding: 0 !important;
}

.side-foot {
  padding: 14px 16px;
  border-top: 1px solid rgba(148, 163, 184, 0.14);
  flex-shrink: 0;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border-radius: 14px;
  cursor: pointer;
  font-size: 14px;
  color: #64748b;
  transition: all 0.24s ease;
  background: rgba(255, 255, 255, 0.65);
}

.logout-btn:hover {
  transform: translateY(-2px);
  background: #fef2f2;
  color: #ef4444;
}

.main-area {
  flex-direction: column;
  min-width: 0;
}

.top-bar {
  height: 72px;
  margin: 14px 16px 0;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.72);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(18px);
  flex-shrink: 0;
}

.top-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toggle-box {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  cursor: pointer;
  color: #64748b;
  transition: all 0.24s ease;
  background: rgba(15, 23, 42, 0.05);
}

.toggle-box:hover {
  transform: translateY(-2px);
  background: rgba(16, 185, 129, 0.1);
  color: #065f46;
}

.top-bar :deep(.el-breadcrumb__item) {
  font-size: 13px;
}
.top-bar :deep(.el-breadcrumb__separator) {
  color: #cbd5e1;
  margin: 0 6px;
}
.top-bar :deep(.el-breadcrumb__inner) {
  color: #64748b;
  font-weight: 500;
}
.top-bar :deep(.el-breadcrumb__inner.is-link) {
  color: #64748b;
}
.top-bar :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #0f172a;
  font-weight: 700;
}

.top-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 14px 6px 6px;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.15);
  background: rgba(255, 255, 255, 0.72);
  transition: all 0.24s ease;
}

.user-trigger:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
}

.user-name {
  font-size: 14px;
  color: #334155;
  font-weight: 700;
}

.arrow-icon {
  font-size: 12px;
  color: #94a3b8;
  transition: transform 0.24s ease;
}

.user-trigger:hover .arrow-icon {
  transform: rotate(180deg);
}

.content-main {
  background: transparent;
  padding: 18px 18px 22px;
  overflow-y: auto;
}

.content-view {
  background: rgba(255, 255, 255, 0.72);
  border-radius: 28px;
  padding: 32px;
  min-height: calc(100vh - 188px);
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.08);
  border: 1px solid rgba(148, 163, 184, 0.12);
  backdrop-filter: blur(18px);
}

@media (max-width: 767px) {
  .content-main {
    padding: 16px;
  }
  .content-view {
    padding: 20px;
    border-radius: 18px;
  }
}
</style>
