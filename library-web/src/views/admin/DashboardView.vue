<template>
  <div class="dashboard-page">
    <div class="welcome-section">
      <h2 class="welcome-title">欢迎回来，{{ userName }}</h2>
      <p class="welcome-desc">以下是图书馆的运营概况</p>
    </div>

    <el-row :gutter="16">
      <el-col
        v-for="(s, i) in stats"
        :key="s.label"
        :xs="12"
        :sm="8"
        :md="6"
        :lg="6"
      >
        <div class="stat-card" :style="{ animationDelay: i * 0.06 + 's' }">
          <div class="stat-icon" :style="{ background: s.bgColor }">
            <el-icon :size="24" :style="{ color: s.color }">
              <component :is="s.icon" />
            </el-icon>
          </div>
          <div class="stat-value">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { getDashboard } from "../../api/admin/dashboard"
import { useAuthStore } from "../../stores/auth"
import { Reading, Van, Warning, Calendar, User, Money, Coin, Bell } from "@element-plus/icons-vue"

const auth = useAuthStore()
const userName = computed(() => auth.user?.realName || "管理员")
const stats = ref<any[]>([])

const iconMap = [
  { icon: Reading, color: "var(--primary)", bgColor: "var(--primary-bg)" },
  { icon: Van, color: "var(--info)", bgColor: "var(--info-light)" },
  { icon: Warning, color: "var(--danger)", bgColor: "var(--danger-light)" },
  { icon: Calendar, color: "var(--success)", bgColor: "var(--success-light)" },
  { icon: User, color: "var(--primary)", bgColor: "var(--primary-bg)" },
  { icon: Money, color: "var(--warning)", bgColor: "var(--warning-light)" },
  { icon: Coin, color: "var(--danger)", bgColor: "var(--danger-light)" },
  { icon: Bell, color: "var(--info)", bgColor: "var(--info-light)" }
]

onMounted(async () => {
  const res: any = await getDashboard()
  const d = res.data
  const raw = [
    { label: "图书总数", value: d.totalBooks },
    { label: "借出中", value: d.borrowedCount },
    { label: "已逾期", value: d.overdueCount },
    { label: "今日借阅", value: d.todayBorrow },
    { label: "用户数", value: d.totalUsers },
    { label: "未缴罚款(笔)", value: d.unpaidFines },
    { label: "待收罚款(元)", value: "¥" + (d.totalPendingFine || 0).toFixed(2) },
    { label: "活跃预约", value: d.activeReservations }
  ]
  stats.value = raw.map((item, i) => ({
    ...item,
    ...iconMap[i]
  }))
})
</script>

<style scoped>
.dashboard-page {
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.welcome-section {
  margin-bottom: 28px;
}

.welcome-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.welcome-desc {
  font-size: 14px;
  color: var(--text-secondary);
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px;
  margin-bottom: 16px;
  animation: slideUp 0.5s ease both;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}
</style>
