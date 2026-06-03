<template>
  <div class="app-shell">
    <div class="app-ambient ambient-left"></div>
    <div class="app-ambient ambient-right"></div>
    <div class="app-grid"></div>
    <router-view v-slot="{ Component, route }">
      <transition name="page-fade" mode="out-in">
        <component :is="Component" :key="route.fullPath" />
      </transition>
    </router-view>
  </div>
</template>

<script setup>
// 根应用只负责承载全局背景和页面切换动画
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: "Inter", "Helvetica Neue", Helvetica, "PingFang SC",
    "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
}

#app,
.app-shell {
  min-height: 100vh;
}

.app-shell {
  position: relative;
  isolation: isolate;
}

.app-ambient,
.app-grid {
  pointer-events: none;
  position: fixed;
  inset: 0;
}

.app-ambient {
  z-index: -2;
  filter: blur(20px);
}

.ambient-left {
  background:
    radial-gradient(circle at 15% 20%, rgba(34, 197, 94, 0.18), transparent 24%),
    radial-gradient(circle at 35% 75%, rgba(59, 130, 246, 0.16), transparent 26%);
}

.ambient-right {
  z-index: -3;
  background:
    radial-gradient(circle at 80% 15%, rgba(99, 102, 241, 0.16), transparent 22%),
    radial-gradient(circle at 92% 68%, rgba(16, 185, 129, 0.12), transparent 18%);
}

.app-grid {
  z-index: -1;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.08) 1px, transparent 1px);
  background-size: 28px 28px;
  mask-image: radial-gradient(circle at center, rgba(0, 0, 0, 0.8), transparent 95%);
}

.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.38s ease, transform 0.38s ease;
}

.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(18px) scale(0.985);
}
</style>
