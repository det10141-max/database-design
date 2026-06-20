<template>
  <el-container class="admin-layout">
    <aside
      class="sidebar"
      :class="{ collapsed: appStore.sidebarCollapsed }"
    >
      <div class="sidebar-logo">
        <el-icon :size="28"><Reading /></el-icon>
        <span v-show="!appStore.sidebarCollapsed" class="logo-text">图书管理系统</span>
      </div>
      <el-menu
        router
        :default-active="$route.path"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        class="sidebar-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>数据看板</template>
        </el-menu-item>
        <el-menu-item index="/admin/books">
          <el-icon><Reading /></el-icon>
          <template #title>图书管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Collection /></el-icon>
          <template #title>分类管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/borrows">
          <el-icon><Tickets /></el-icon>
          <template #title>借阅管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/reservations">
          <el-icon><Clock /></el-icon>
          <template #title>预约管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <template #title>读者管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/fines">
          <el-icon><Money /></el-icon>
          <template #title>罚款管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/overdue">
          <el-icon><WarningFilled /></el-icon>
          <template #title>逾期管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/announcements">
          <el-icon><Bell /></el-icon>
          <template #title>公告管理</template>
        </el-menu-item>
      </el-menu>
    </aside>

    <el-container class="main-container">
      <header class="header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            @click="appStore.sidebarCollapsed = !appStore.sidebarCollapsed"
          >
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <span class="header-page-title">{{ currentPageTitle }}</span>
        </div>
        <div class="header-right">
          <el-tooltip :content="appStore.getThemeLabel()" placement="bottom">
            <el-button
              class="theme-btn"
              :icon="appStore.getThemeIcon()"
              circle
              @click="appStore.cycleTheme()"
            />
          </el-tooltip>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <div class="user-avatar">{{ avatarLetter }}</div>
              <span class="user-name">{{ auth.user?.realName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <el-icon><User /></el-icon>
                  {{ auth.user?.role === 'ADMIN' ? '管理员' : '读者' }}
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useAuthStore } from "../stores/auth"
import { useAppStore } from "../stores/app"
import {
  Reading,
  DataAnalysis,
  Collection,
  Tickets,
  Clock,
  User,
  Money,
  WarningFilled,
  Bell,
  Fold,
  Expand,
  ArrowDown,
  SwitchButton
} from "@element-plus/icons-vue"

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const appStore = useAppStore()

const avatarLetter = computed(() => {
  const name = auth.user?.realName || auth.user?.username || "U"
  return name.charAt(0).toUpperCase()
})

const pageTitles: Record<string, string> = {
  "/admin/dashboard": "数据看板",
  "/admin/books": "图书管理",
  "/admin/categories": "分类管理",
  "/admin/borrows": "借阅管理",
  "/admin/reservations": "预约管理",
  "/admin/users": "读者管理",
  "/admin/fines": "罚款管理",
  "/admin/overdue": "逾期管理",
  "/admin/announcements": "公告管理"
}

const currentPageTitle = computed(() => {
  const path = route.path
  if (path.includes("/admin/books/add")) return "新增图书"
  if (path.match(/\/admin\/books\/\d+/)) return "编辑图书"
  return pageTitles[path] || "管理后台"
})

async function handleCommand(command: string) {
  if (command === "logout") {
    await auth.logout()
    router.push("/login")
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: var(--sidebar-width);
  /* 侧边栏液态玻璃：半透明深色 + 模糊 */
  background: rgba(30, 27, 75, 0.65);
  backdrop-filter: blur(var(--glass-blur-strong)) saturate(var(--glass-saturate));
  -webkit-backdrop-filter: blur(var(--glass-blur-strong)) saturate(var(--glass-saturate));
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-normal);
  overflow: hidden;
  flex-shrink: 0;
}

.sidebar.collapsed {
  width: var(--sidebar-collapsed-width);
}

.sidebar-logo {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 16px;
  color: var(--sidebar-active-text);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
  overflow: hidden;
  white-space: nowrap;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
}

.sidebar.collapsed .logo-text {
  display: none;
}

.sidebar-menu {
  flex: 1;
  border-right: none !important;
  background: transparent !important;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: var(--sidebar-text);
  --el-menu-active-color: var(--sidebar-active-text);
  --el-menu-hover-bg-color: var(--sidebar-hover-bg);
  padding: 8px;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-menu .el-menu-item {
  height: 44px;
  line-height: 44px;
  margin: 2px 0;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  font-size: 14px;
}

.sidebar-menu .el-menu-item:hover {
  background: var(--sidebar-hover-bg) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background: var(--sidebar-active-bg) !important;
  color: var(--sidebar-active-text) !important;
  font-weight: 600;
}

.sidebar-menu .el-menu-item .el-icon {
  font-size: 18px;
}

.main-container {
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: var(--header-height);
  /* 头部液态玻璃 */
  background: var(--glass-bg-strong);
  backdrop-filter: blur(var(--glass-blur-strong)) saturate(var(--glass-saturate));
  -webkit-backdrop-filter: blur(var(--glass-blur-strong)) saturate(var(--glass-saturate));
  border-bottom: 1px solid var(--glass-border-soft);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
  box-shadow: var(--glass-shadow);
  z-index: 10;
  transition: background var(--transition-normal), border-color var(--transition-normal);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--text-regular);
  transition: color var(--transition-fast);
  padding: 4px;
  border-radius: var(--radius-sm);
}

.collapse-btn:hover {
  color: var(--primary);
  background: var(--primary-bg);
}

.header-page-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.theme-btn {
  border: 1px solid var(--glass-border-soft) !important;
  background: var(--glass-bg-soft) !important;
  color: var(--text-regular) !important;
  transition: all var(--transition-fast) !important;
}

.theme-btn:hover {
  color: var(--primary) !important;
  border-color: var(--primary-light) !important;
  background: var(--primary-bg) !important;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: var(--radius-lg);
  transition: background var(--transition-fast);
}

.user-info:hover {
  background: var(--glass-bg-soft);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.user-info .el-icon {
  font-size: 12px;
  color: var(--text-secondary);
}

.main-content {
  flex: 1;
  overflow-y: auto;
  /* 内容区透明，透出全局渐变背景 */
  background: transparent;
  padding: 24px;
  transition: background var(--transition-normal);
}
</style>
