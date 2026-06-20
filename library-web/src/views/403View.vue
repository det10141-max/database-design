<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">4<span class="accent">0</span>3</div>
      <h2 class="error-title">访问受限</h2>
      <p class="error-desc">抱歉，您没有权限访问此页面</p>
      <el-button type="primary" size="large" @click="$router.push('/login')">
        <el-icon style="margin-right:6px"><Back /></el-icon>
        返回登录
      </el-button>
    </div>
    <div class="error-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
  </div>
</template>

<style scoped>
.error-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page);
  position: relative;
  overflow: hidden;
}

.error-content {
  text-align: center;
  position: relative;
  z-index: 1;
}

.error-code {
  font-size: 120px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 16px;
  letter-spacing: -4px;
}

.error-code .accent {
  color: var(--primary);
}

.error-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.error-desc {
  font-size: 15px;
  color: var(--text-secondary);
  margin-bottom: 32px;
}

.error-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  border: 2px solid var(--primary-lighter);
  opacity: 0.3;
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
  animation: pulse 4s ease-in-out infinite;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -80px;
  left: -60px;
  animation: pulse 5s ease-in-out infinite reverse;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: pulse 6s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.2; }
  50% { transform: scale(1.1); opacity: 0.4; }
}
</style>
