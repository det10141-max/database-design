<template>
  <el-container class="reader-layout">
    <header class="reader-header">
      <div class="header-left" @click="$router.push('/reader/home')">
        <el-icon :size="24" color="var(--primary)"><Reading /></el-icon>
        <span class="header-logo-text">图书管理系统</span>
      </div>

      <nav class="header-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-link"
          :class="{ active: $route.path === item.path }"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="header-right">
        <el-tooltip :content="appStore.getThemeLabel()" placement="bottom">
          <button class="header-icon-btn" @click="appStore.cycleTheme()">
            <el-icon :size="18"><component :is="appStore.getThemeIcon()" /></el-icon>
          </button>
        </el-tooltip>

        <el-popover placement="bottom-end" :width="360" trigger="click">
          <template #reference>
            <button class="header-icon-btn">
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
                <el-icon :size="18"><Bell /></el-icon>
              </el-badge>
            </button>
          </template>
          <div class="announcement-popover">
            <div class="announcement-header">系统公告</div>
            <div v-if="announcements.length === 0" class="announcement-empty">暂无公告</div>
            <div
              v-for="a in announcements"
              :key="a.id"
              class="announcement-item"
              @click="viewAnnouncement(a)"
            >
              <div class="announcement-title">
                <el-icon v-if="a.isPinned" class="pin-icon"><Star /></el-icon>
                {{ a.title }}
              </div>
              <div class="announcement-date">{{ a.createdAt }}</div>
            </div>
          </div>
        </el-popover>

        <el-dropdown trigger="click" @command="handleCommand">
          <div class="header-user">
            <el-icon :size="16"><User /></el-icon>
            <span>{{ auth.user?.realName }}</span>
            <el-icon :size="12"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>个人信息
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="reader-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <el-dialog v-model="detailVisible" title="公告详情" width="520px">
      <template v-if="currentAnnouncement">
        <h2 class="announcement-detail-title">{{ currentAnnouncement.title }}</h2>
        <div class="announcement-detail-date">{{ currentAnnouncement.createdAt }}</div>
        <div class="announcement-detail-content">{{ currentAnnouncement.content }}</div>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { useAuthStore } from "../stores/auth"
import { useAppStore } from "../stores/app"
import { useRouter } from "vue-router"
import { getAnnouncements } from "../api/common/announcement"
import {
  Reading, HomeFilled, Search, Reading as BookIcon,
  Tickets, Clock, Bell, User, ArrowDown, SwitchButton,
  Sunny, Moon, Monitor, Calendar, Wallet, Memo, Star
} from "@element-plus/icons-vue"

const auth = useAuthStore()
const appStore = useAppStore()
const router = useRouter()
const announcements = ref<any[]>([])
const unreadCount = ref(0)
const detailVisible = ref(false)
const currentAnnouncement = ref<any>(null)

const navItems = [
  { path: "/reader/home", label: "首页", icon: HomeFilled },
  { path: "/reader/search", label: "图书搜索", icon: Search },
  { path: "/reader/borrows", label: "我的借阅", icon: Tickets },
  { path: "/reader/history", label: "借阅历史", icon: Clock },
  { path: "/reader/reservations", label: "我的预约", icon: Calendar },
  { path: "/reader/fines", label: "我的罚款", icon: Wallet },
  { path: "/reader/profile", label: "个人信息", icon: Memo },
]

async function fetchAnnouncements() {
  const r: any = await getAnnouncements({ page: 1, pageSize: 5 })
  announcements.value = r.data.records
  const readIds: number[] = JSON.parse(localStorage.getItem("readAnnouncements") || "[]")
  unreadCount.value = announcements.value.filter((a: any) => !readIds.includes(a.id)).length
}

function viewAnnouncement(a: any) {
  currentAnnouncement.value = a
  detailVisible.value = true
  const readIds: number[] = JSON.parse(localStorage.getItem("readAnnouncements") || "[]")
  if (!readIds.includes(a.id)) {
    readIds.push(a.id)
    localStorage.setItem("readAnnouncements", JSON.stringify(readIds))
    unreadCount.value = announcements.value.filter((x: any) => !readIds.includes(x.id)).length
  }
}

function handleCommand(cmd: string) {
  if (cmd === "profile") router.push("/reader/profile")
  else if (cmd === "logout") handleLogout()
}

async function handleLogout() {
  await auth.logout()
  router.push("/login")
}

onMounted(fetchAnnouncements)
</script>

<style scoped>
.reader-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.reader-header {
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
  position: sticky;
  top: 0;
  z-index: 100;
  transition: background var(--transition-normal), border-color var(--transition-normal);
  box-shadow: var(--glass-shadow);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
}

.header-logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary);
  letter-spacing: 0.5px;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 100%;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 16px;
  height: 100%;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  text-decoration: none;
  position: relative;
  transition: color var(--transition-fast);
}

.nav-link:hover {
  color: var(--primary);
}

.nav-link.active {
  color: var(--primary);
}

.nav-link.active::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 16px;
  right: 16px;
  height: 2px;
  background: var(--primary);
  border-radius: 1px 1px 0 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.header-icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  color: var(--text-regular);
  transition: all var(--transition-fast);
}

.header-icon-btn:hover {
  background: var(--glass-bg-soft);
  color: var(--primary);
}

.header-user {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-regular);
  transition: all var(--transition-fast);
}

.header-user:hover {
  background: var(--glass-bg-soft);
  color: var(--primary);
}

.reader-main {
  flex: 1;
  /* 内容区透明，透出全局渐变背景 */
  background: transparent;
  padding: 24px;
  overflow-y: auto;
  transition: background var(--transition-normal);
}

.announcement-popover {
  max-height: 360px;
  overflow-y: auto;
}

.announcement-header {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border);
}

.announcement-empty {
  color: var(--text-placeholder);
  text-align: center;
  padding: 24px 0;
  font-size: 14px;
}

.announcement-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-item:hover {
  background: var(--glass-bg-soft);
  margin: 0 -12px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
}

.announcement-title {
  font-weight: 500;
  font-size: 14px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.pin-icon {
  color: var(--warning);
  font-size: 14px;
}

.announcement-date {
  color: var(--text-placeholder);
  font-size: 12px;
  margin-top: 4px;
}

.announcement-detail-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.announcement-detail-date {
  color: var(--text-placeholder);
  font-size: 13px;
  margin-bottom: 16px;
}

.announcement-detail-content {
  line-height: 1.8;
  white-space: pre-wrap;
  color: var(--text-regular);
}
</style>
