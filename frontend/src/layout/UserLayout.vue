<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from "vue";
import {
  Search,
  ArrowDown,
  User,
  SwitchButton,
  ChatLineRound,
  Lightning,
} from "@element-plus/icons-vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import ChatPanel from "@/components/ChatPanel.vue";
import { useUser } from "@/utils/useUser";
import { initChat } from "@/services/chatInit.js";
import { disconnectStomp } from "@/services/stomp.js";
import { resetChat } from "@/services/chatInit";
import { totalUnread } from "@/services/chatCache.js";

const router = useRouter();
const route = useRoute();
const searchText = ref("");
const selectedCity = ref("孝感");
const showMessageDialog = ref(false);
const { userInfo, clearUser } = useUser();
const avatarUrl = computed(() => userInfo.avatarUrl);
const nickname = computed(() => userInfo.nickname || "求职者");
const pageTitle = computed(() => route.meta.title || "智能招聘");

const activeMenu = computed(() => {
  if (route.path.startsWith("/user/jobs")) return "/user/jobs";
  if (route.path.startsWith("/user/resume")) return "/user/resume";
  if (
    route.path.startsWith("/user/messages") ||
    route.path.startsWith("/user/company")
  ) {
    return "/user/messages";
  }
  return "/user";
});

const menuItems = [
  { index: "/user", label: "首页" },
  { index: "/user/jobs", label: "职位" },
  { index: "/user/resume", label: "简历" },
  { index: "/user/messages", label: "公司" },
];

const handleSearch = () => {
  router.push({
    path: "/user/jobs",
    query: {
      keyword: searchText.value,
      city: selectedCity.value,
    },
  });
};

const handleCommand = (command) => {
  if (command === "logout") {
    disconnectStomp();
    resetChat();
    localStorage.clear();
    clearUser();
    router.push("/login");
    ElMessage.success("已退出登录");
  } else if (command === "profile") {
    router.push("/user/profile");
  } else if (command === "messages") {
    showMessageDialog.value = true;
  }
};

// 动态同步顶栏高度到 --nav-height
let navResizeObserver = null;
const onWindowResize = () => {
  const navEl = document.querySelector(".top-nav");
  if (!navEl) return;
  const total = navEl.getBoundingClientRect().height;
  const safe = Math.ceil(total) + 4; // 4px 缓冲
  document.documentElement.style.setProperty("--nav-height", safe + "px");
};

onMounted(() => {
  initChat();
  nextTick(() => {
    const navEl = document.querySelector(".top-nav");
    const innerEl = document.querySelector(".nav-content");
    if (navEl && "ResizeObserver" in window) {
      onWindowResize();
      navResizeObserver = new ResizeObserver(onWindowResize);
      navResizeObserver.observe(innerEl || navEl);
      window.addEventListener("resize", onWindowResize);
    }
  });
});

onBeforeUnmount(() => {
  if (navResizeObserver) {
    navResizeObserver.disconnect();
    navResizeObserver = null;
  }
  window.removeEventListener("resize", onWindowResize);
});
</script>

<template>
  <div class="user-shell">
    <div class="shell-blur blur-one"></div>
    <div class="shell-blur blur-two"></div>

    <header class="top-nav">
      <div class="nav-content glass-nav">
        <div class="nav-left">
          <div class="brand-block" @click="router.push('/user')">
            <div class="brand-mark">
              <span></span>
            </div>
            <div class="brand-copy">
              <strong>智能招聘</strong>
              <span>Smart Career Flow</span>
            </div>
          </div>

          <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            class="main-menu"
            :ellipsis="false"
            router
          >
            <el-menu-item
              v-for="item in menuItems"
              :key="item.index"
              :index="item.index"
            >
              {{ item.label }}
            </el-menu-item>
          </el-menu>
        </div>

        <div class="nav-center">
          <div class="search-box-mini">
            <el-input
              v-model="searchText"
              placeholder="搜索职位 / 城市 / 技能关键词"
              class="nav-search"
              @keyup.enter="handleSearch"
            >
              <template #prepend>
                <el-select v-model="selectedCity" style="width: 90px">
                  <el-option label="孝感" value="孝感" />
                  <el-option label="武汉" value="武汉" />
                </el-select>
              </template>
              <template #append>
                <el-button :icon="Search" @click="handleSearch">探索</el-button>
              </template>
            </el-input>
          </div>
        </div>

        <div class="nav-right">
          <div class="nav-badge">
            <el-icon><Lightning /></el-icon>
            <span>AI 智能匹配中</span>
          </div>

          <el-dropdown @command="handleCommand" trigger="hover">
            <el-badge :is-dot="totalUnread > 0">
              <div class="user-info">
                <el-avatar :size="36" :src="avatarUrl || undefined">
                  {{ nickname.charAt(0) }}
                </el-avatar>
                <div class="user-copy">
                  <span class="username-text">{{ nickname }}</span>
                  <span class="username-sub">在线沟通已开启</span>
                </div>
                <el-icon class="arrow-down"><ArrowDown /></el-icon>
              </div>
            </el-badge>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
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
      </div>
    </header>

    <main class="main-layout">
      <div class="container-inner">
        <section class="page-hero">
          <div class="page-copy">
            <span class="hero-kicker">沉浸式求职体验</span>
            <h2>{{ pageTitle }}</h2>
            <p>
              更流畅的滚动、更立体的层次和更明确的视觉焦点，让你的平台不再像千篇一律的后台页面。
            </p>
          </div>
          <div class="hero-pill-list">
            <span>Glass UI</span>
            <span>Scroll Reveal</span>
            <span>Micro Interaction</span>
          </div>
        </section>

        <section class="content-shell">
          <router-view />
        </section>
      </div>
    </main>

    <el-dialog
      v-model="showMessageDialog"
      title="消息"
      width="750px"
      draggable
      overflow
      append-to-body
      :modal="false"
      :lock-scroll="false"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <ChatPanel compact />
    </el-dialog>
  </div>
</template>

<style scoped>
.user-shell {
  position: relative;
  min-height: 100vh;
  padding: 0 18px 36px;
}

.shell-blur {
  position: fixed;
  border-radius: 999px;
  filter: blur(18px);
  opacity: 0.7;
  pointer-events: none;
  z-index: 0;
}

.blur-one {
  width: 320px;
  height: 320px;
  top: 90px;
  left: 2%;
  background: radial-gradient(
    circle,
    rgba(45, 212, 191, 0.22),
    transparent 70%
  );
}

.blur-two {
  width: 360px;
  height: 360px;
  top: 40px;
  right: 2%;
  background: radial-gradient(
    circle,
    rgba(99, 102, 241, 0.18),
    transparent 70%
  );
}

.top-nav {
  position: relative;
  z-index: 30;
  padding: 12px 18px 0;
}

.nav-content {
  max-width: 1320px;
  margin: 0 auto;
  min-height: 78px;
  padding: 10px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.glass-nav {
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.84),
    rgba(240, 249, 255, 0.72)
  );
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 24px;
  box-shadow: 0 24px 80px rgba(37, 99, 235, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(20px);
}

.nav-left,
.nav-right {
  display: flex;
  align-items: center;
  gap: 18px;
}

.nav-left {
  min-width: 0;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 10px 12px;
  border-radius: 18px;
  transition: transform 0.28s ease, background 0.28s ease;
}

.brand-block:hover {
  transform: translateY(-2px);
  background: rgba(79, 70, 229, 0.06);
}

.brand-mark {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #2dd4bf 0%, #4f46e5 100%);
  box-shadow: 0 12px 28px rgba(79, 70, 229, 0.26);
}

.brand-mark span {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 0 18px rgba(255, 255, 255, 0.95);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-copy strong {
  color: #0f172a;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0.04em;
}

.brand-copy span {
  color: #64748b;
  font-size: 12px;
}

.main-menu {
  border-bottom: none !important;
  background: transparent;
  min-width: 0;
}

.main-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 0 2px;
  border-radius: 14px;
  color: #475569;
  font-size: 15px;
  transition: all 0.28s ease;
}

.main-menu :deep(.el-menu-item:hover) {
  color: #0f766e !important;
  background: rgba(20, 184, 166, 0.08) !important;
}

.main-menu :deep(.el-menu-item.is-active) {
  color: #0f766e !important;
  background: linear-gradient(
    135deg,
    rgba(45, 212, 191, 0.14),
    rgba(99, 102, 241, 0.1)
  ) !important;
  box-shadow: inset 0 0 0 1px rgba(20, 184, 166, 0.12);
}

.main-menu :deep(.el-menu--horizontal > .el-menu-item.is-active) {
  border-bottom: none;
}

.nav-center {
  flex: 1;
  min-width: 0;
  max-width: 520px;
}

.search-box-mini {
  width: 100%;
}

.search-box-mini :deep(.el-input-group__prepend),
.search-box-mini :deep(.el-input-group__append) {
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
}

.search-box-mini :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px 0 0 16px;
  box-shadow: none;
}

.search-box-mini :deep(.el-select__wrapper) {
  box-shadow: none !important;
  background: transparent !important;
}

.search-box-mini :deep(.el-input-group__prepend) {
  border-right: 1px solid rgba(148, 163, 184, 0.18) !important;
  border-radius: 16px 0 0 16px;
}

.search-box-mini :deep(.el-input-group__append) {
  border-radius: 0 16px 16px 0;
}

.search-box-mini :deep(.el-input-group__append .el-button) {
  height: 42px;
  padding: 0 18px;
  border: none;
  color: #fff;
  background: linear-gradient(135deg, #14b8a6 0%, #4f46e5 100%);
  box-shadow: 0 12px 28px rgba(79, 70, 229, 0.25);
}

.nav-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  color: #0f766e;
  font-size: 13px;
  border-radius: 999px;
  border: 1px solid rgba(20, 184, 166, 0.12);
  background: rgba(255, 255, 255, 0.72);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 18px;
  color: #0f172a;
  cursor: pointer;
  transition: transform 0.28s ease, background 0.28s ease, box-shadow 0.28s ease;
}

.user-info:hover {
  background: rgba(79, 70, 229, 0.05);
  box-shadow: inset 0 0 0 1px rgba(99, 102, 241, 0.08);
  transform: translateY(-1px);
}

.user-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.username-text {
  font-size: 14px;
  font-weight: 700;
}

.username-sub {
  font-size: 11px;
  color: #64748b;
}

.arrow-down {
  color: #64748b;
}

.main-layout {
  position: relative;
  z-index: 1;
  padding-top: 18px;
}

.container-inner {
  max-width: 1320px;
  margin: 0 auto;
}

.page-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 24px 10px 18px;
  position: relative;
}

.page-hero::before {
  content: "";
  position: absolute;
  inset: 10px 0 0;
  border-radius: 0;
  background: linear-gradient(
    90deg,
    rgba(20, 184, 166, 0.18),
    rgba(99, 102, 241, 0.12),
    transparent 72%
  );
  height: 1px;
  border: none;
  box-shadow: none;
  pointer-events: none;
}

.page-copy,
.hero-pill-list {
  position: relative;
  z-index: 1;
}

.page-copy {
  max-width: 760px;
  padding: 8px 0;
}

.hero-kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  margin-bottom: 14px;
  border-radius: 999px;
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(45, 212, 191, 0.18);
  box-shadow: 0 12px 30px rgba(20, 184, 166, 0.08);
}

.page-copy h2 {
  margin: 0;
  font-size: clamp(30px, 4vw, 46px);
  line-height: 1.08;
  font-weight: 900;
  color: #0f172a;
}

.page-copy p {
  margin: 14px 0 0;
  font-size: 15px;
  line-height: 1.75;
  color: #64748b;
}

.hero-pill-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.hero-pill-list span {
  padding: 10px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #334155;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(148, 163, 184, 0.14);
  box-shadow: none;
}

.content-shell {
  position: relative;
}

@media (max-width: 1180px) {
  .nav-content {
    flex-wrap: wrap;
  }

  .nav-center {
    order: 3;
    max-width: 100%;
    flex-basis: 100%;
  }

  .page-hero {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 820px) {
  .user-shell {
    padding: 0 10px 28px;
  }

  .nav-content {
    border-radius: 20px;
    padding: 12px;
  }

  .nav-left,
  .nav-right {
    width: 100%;
    justify-content: space-between;
  }

  .nav-left {
    flex-wrap: wrap;
  }

  .main-menu {
    width: 100%;
  }

  .main-menu :deep(.el-menu-item) {
    padding: 0 14px !important;
  }

  .nav-badge {
    display: none;
  }

  .page-copy h2 {
    font-size: 30px;
  }
}

@media (max-width: 560px) {
  .brand-copy span,
  .username-sub {
    display: none;
  }

  .brand-copy strong {
    font-size: 16px;
  }

  .hero-pill-list {
    justify-content: flex-start;
  }
}
</style>
