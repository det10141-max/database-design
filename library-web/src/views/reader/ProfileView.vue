<template>
  <div class="profile-view" v-if="user">
    <div class="page-title">
      <span class="title-bar"></span>
      个人信息
    </div>

    <div class="profile-card">
      <div class="profile-avatar">
        <div class="avatar-circle">{{ user.realName?.charAt(0) || '?' }}</div>
        <div class="avatar-name">{{ user.realName }}</div>
        <el-tag type="primary" size="small">{{ user.role === 'READER' ? '读者' : user.role }}</el-tag>
      </div>
      <div class="profile-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ user.realName }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ user.role === 'READER' ? '读者' : user.role }}</el-descriptions-item>
          <el-descriptions-item label="最大可借">{{ user.maxBorrow }} 本</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ user.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ user.email || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import http from "../../api/request"

const user = ref<any>(null)

onMounted(async () => {
  const r: any = await http.get("/reader/profile")
  user.value = r.data
})
</script>

<style scoped>
.profile-view {
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  display: flex;
  gap: 32px;
  padding: 32px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  transition: background-color var(--transition-normal), border-color var(--transition-normal);
}

.profile-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  flex-shrink: 0;
}

.avatar-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--primary-bg);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  transition: background-color var(--transition-normal);
}

.avatar-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.profile-details {
  flex: 1;
  min-width: 0;
}

@media (max-width: 700px) {
  .profile-card {
    flex-direction: column;
    align-items: center;
  }
}
</style>
