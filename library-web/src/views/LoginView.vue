<template>
  <div class="login-page">
    <!-- three.js 3D 背景画布 -->
    <div ref="bgCanvasRef" class="bg-canvas"></div>

    <!-- 顶部主题切换 -->
    <div class="theme-toggle glass" @click="appStore.cycleTheme()" :title="appStore.getThemeLabel()">
      <el-icon :size="20"><component :is="appStore.getThemeIcon()" /></el-icon>
    </div>

    <!-- 品牌标识（左上） -->
    <div class="brand-mark glass">
      <el-icon :size="28"><Reading /></el-icon>
      <span>图书管理系统</span>
    </div>

    <!-- 中央液态玻璃登录卡片 -->
    <transition name="slide-fade" mode="out-in">
      <div v-if="mode === 'login'" key="login" class="glass-card login-card">
        <div class="card-glow"></div>
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录您的账户以继续</p>
        </div>
        <el-form :model="loginForm" class="login-form" @submit.prevent="doLogin">
          <el-form-item>
            <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large" :prefix-icon="User" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" :prefix-icon="Lock" @keyup.enter="doLogin" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" @click="doLogin" :loading="loginLoading" class="submit-btn">登 录</el-button>
          </el-form-item>
        </el-form>
        <div class="form-footer">
          <span>还没有账号？</span>
          <el-button link type="primary" @click="mode = 'register'">立即注册</el-button>
        </div>
        <div class="demo-hint">
          <span>管理员: admin / admin123</span>
          <span>读者: reader1 / admin123</span>
        </div>
      </div>

      <div v-else key="register" class="glass-card register-card">
        <div class="card-glow"></div>
        <div class="form-header">
          <h2>创建账户</h2>
          <p>注册成为读者，开启阅读之旅</p>
        </div>
        <el-form :model="regForm" class="login-form" @submit.prevent="doRegister">
          <el-form-item>
            <el-input v-model="regForm.username" placeholder="用户名（登录用）" size="large" :prefix-icon="User" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="regForm.password" type="password" placeholder="密码（至少6位）" size="large" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-input v-model="regForm.realName" placeholder="真实姓名（用于借阅登记）" size="large" :prefix-icon="Postcard" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="regForm.phone" placeholder="手机号（选填）" size="large" :prefix-icon="Phone" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="regForm.email" placeholder="邮箱（选填）" size="large" :prefix-icon="Message" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" @click="doRegister" :loading="regLoading" class="submit-btn">注 册</el-button>
          </el-form-item>
        </el-form>
        <div class="form-footer">
          <span>已有账号？</span>
          <el-button link type="primary" @click="mode = 'login'">返回登录</el-button>
        </div>
      </div>
    </transition>

    <!-- 底部特性标签 -->
    <div class="features-bar glass">
      <div class="feature-item">
        <el-icon :size="16"><Search /></el-icon>
        <span>智能检索</span>
      </div>
      <div class="feature-divider"></div>
      <div class="feature-item">
        <el-icon :size="16"><Tickets /></el-icon>
        <span>便捷借阅</span>
      </div>
      <div class="feature-divider"></div>
      <div class="feature-item">
        <el-icon :size="16"><DataAnalysis /></el-icon>
        <span>数据洞察</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { useAppStore } from "../stores/app";
import { ElMessage } from "element-plus";
import { User, Lock, Postcard, Phone, Message, Reading, Search, Tickets, DataAnalysis } from "@element-plus/icons-vue";
import http from "../api/request";
import * as THREE from "three";

const appStore = useAppStore();
const mode = ref<"login" | "register">("login");
const loginForm = ref({ username: "", password: "" });
const regForm = ref({ username: "", password: "", realName: "", phone: "", email: "" });
const loginLoading = ref(false);
const regLoading = ref(false);

const router = useRouter();
const auth = useAuthStore();

async function doLogin() {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning("请输入用户名和密码"); return;
  }
  loginLoading.value = true;
  try {
    await auth.login(loginForm.value.username, loginForm.value.password);
    router.push(auth.user?.role === "ADMIN" ? "/admin" : "/reader");
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "登录失败");
  } finally {
    loginLoading.value = false;
  }
}

async function doRegister() {
  if (!regForm.value.username || !regForm.value.password || !regForm.value.realName) {
    ElMessage.warning("用户名、密码、真实姓名为必填"); return;
  }
  if (regForm.value.password.length < 6) {
    ElMessage.warning("密码至少6位"); return;
  }
  regLoading.value = true;
  try {
    await http.post("/auth/register", regForm.value);
    ElMessage.success("注册成功，请登录");
    regForm.value = { username: "", password: "", realName: "", phone: "", email: "" };
    mode.value = "login";
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "注册失败");
  } finally {
    regLoading.value = false;
  }
}

/* ===================== three.js 3D 交互背景 ===================== */
const bgCanvasRef = ref<HTMLElement | null>(null);
let renderer: THREE.WebGLRenderer | null = null;
let scene: THREE.Scene | null = null;
let camera: THREE.PerspectiveCamera | null = null;
let animationId = 0;
// 粒子群与漂浮书籍的集合，便于统一更新
let particleSystem: THREE.Points | null = null;
let floatingBooks: THREE.Mesh[] = [];
// 鼠标位置（归一化 -1 ~ 1），用于相机视差
const mouse = { x: 0, y: 0, tx: 0, ty: 0 };

// 创建漂浮的"书籍"几何体：用扁平长方体模拟书本
function createBookMesh(): THREE.Mesh {
  // 书本尺寸：宽 高 厚
  const geo = new THREE.BoxGeometry(1.4, 1.9, 0.25);
  // 随机选取柔和色调，营造梦幻感
  const palette = [0x6366f1, 0x8b5cf6, 0xec4899, 0x3b82f6, 0x10b981, 0xf59e0b];
  const color = palette[Math.floor(Math.random() * palette.length)];
  const mat = new THREE.MeshStandardMaterial({
    color,
    metalness: 0.3,
    roughness: 0.4,
    transparent: true,
    opacity: 0.85,
  });
  const mesh = new THREE.Mesh(geo, mat);
  // 随机散布在空间中
  mesh.position.set(
    (Math.random() - 0.5) * 18,
    (Math.random() - 0.5) * 12,
    (Math.random() - 0.5) * 10 - 2
  );
  mesh.rotation.set(
    Math.random() * Math.PI,
    Math.random() * Math.PI,
    Math.random() * Math.PI
  );
  return mesh;
}

// 创建粒子背景：大量微小亮点构成星河
function createParticles(): THREE.Points {
  const count = 1200;
  const positions = new Float32Array(count * 3);
  for (let i = 0; i < count; i++) {
    positions[i * 3] = (Math.random() - 0.5) * 40;
    positions[i * 3 + 1] = (Math.random() - 0.5) * 30;
    positions[i * 3 + 2] = (Math.random() - 0.5) * 30;
  }
  const geo = new THREE.BufferGeometry();
  geo.setAttribute("position", new THREE.BufferAttribute(positions, 3));
  const mat = new THREE.PointsMaterial({
    color: 0xffffff,
    size: 0.06,
    transparent: true,
    opacity: 0.7,
    sizeAttenuation: true,
  });
  return new THREE.Points(geo, mat);
}

function initThree() {
  const container = bgCanvasRef.value;
  if (!container) return;
  const width = container.clientWidth;
  const height = container.clientHeight;

  // 场景与雾化，增强深度感
  scene = new THREE.Scene();
  scene.fog = new THREE.FogExp2(0x0b1020, 0.025);

  // 透视相机
  camera = new THREE.PerspectiveCamera(60, width / height, 0.1, 100);
  camera.position.set(0, 0, 10);

  // 渲染器：开启抗锯齿与透明背景，叠加在 CSS 渐变之上
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setSize(width, height);
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  renderer.setClearColor(0x000000, 0);
  container.appendChild(renderer.domElement);

  // 环境光 + 方向光，让书籍呈现立体感
  scene.add(new THREE.AmbientLight(0x8a8aff, 0.6));
  const dirLight = new THREE.DirectionalLight(0xffffff, 1.0);
  dirLight.position.set(5, 8, 6);
  scene.add(dirLight);
  // 点光源增加色彩氛围
  const pink = new THREE.PointLight(0xec4899, 1.5, 30);
  pink.position.set(-6, -3, 4);
  scene.add(pink);
  const indigo = new THREE.PointLight(0x6366f1, 1.5, 30);
  indigo.position.set(6, 4, 2);
  scene.add(indigo);

  // 添加粒子与漂浮书籍
  particleSystem = createParticles();
  scene.add(particleSystem);
  for (let i = 0; i < 14; i++) {
    const book = createBookMesh();
    floatingBooks.push(book);
    scene.add(book);
  }

  // 鼠标交互：视差 + 书籍轻微响应
  window.addEventListener("mousemove", onMouseMove);
  window.addEventListener("resize", onResize);

  animate();
}

function onMouseMove(e: MouseEvent) {
  // 将鼠标坐标归一化到 [-1, 1]
  mouse.tx = (e.clientX / window.innerWidth) * 2 - 1;
  mouse.ty = -((e.clientY / window.innerHeight) * 2 - 1);
}

function onResize() {
  if (!renderer || !camera || !bgCanvasRef.value) return;
  const w = bgCanvasRef.value.clientWidth;
  const h = bgCanvasRef.value.clientHeight;
  renderer.setSize(w, h);
  camera.aspect = w / h;
  camera.updateProjectionMatrix();
}

const clock = new THREE.Clock();
function animate() {
  animationId = requestAnimationFrame(animate);
  const t = clock.getElapsedTime();

  // 鼠标位置缓动，产生平滑视差
  mouse.x += (mouse.tx - mouse.x) * 0.05;
  mouse.y += (mouse.ty - mouse.y) * 0.05;
  if (camera) {
    camera.position.x = mouse.x * 2.5;
    camera.position.y = mouse.y * 2.0;
    camera.lookAt(0, 0, 0);
  }

  // 粒子缓慢旋转
  if (particleSystem) {
    particleSystem.rotation.y = t * 0.03;
    particleSystem.rotation.x = t * 0.01;
  }

  // 每本书独立漂浮与旋转
  floatingBooks.forEach((book, i) => {
    book.position.y += Math.sin(t * 0.5 + i) * 0.003;
    book.rotation.x += 0.002 + i * 0.0001;
    book.rotation.y += 0.003 + i * 0.00015;
  });

  if (renderer && scene && camera) {
    renderer.render(scene, camera);
  }
}

onMounted(() => {
  initThree();
});

onBeforeUnmount(() => {
  cancelAnimationFrame(animationId);
  window.removeEventListener("mousemove", onMouseMove);
  window.removeEventListener("resize", onResize);
  if (renderer) {
    renderer.dispose();
    renderer.domElement.remove();
    renderer = null;
  }
  scene = null;
  camera = null;
  particleSystem = null;
  floatingBooks = [];
});
</script>

<style scoped>
.login-page {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  /* 低饱和柔和渐变底，与全局风格统一，衬托 3D 场景与玻璃卡片 */
  background: radial-gradient(circle at 15% 15%, #e0e7ff 0%, transparent 45%),
              radial-gradient(circle at 85% 20%, #f0f9ff 0%, transparent 40%),
              radial-gradient(circle at 75% 85%, #fef2f2 0%, transparent 40%),
              linear-gradient(180deg, #f8fafc 0%, #eef2f7 100%);
}

/* three.js 画布容器，铺满全屏并置于底层 */
.bg-canvas {
  position: absolute;
  inset: 0;
  z-index: 0;
}

/* ===================== 液态玻璃通用样式 ===================== */
.glass {
  background: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 32px rgba(15, 23, 42, 0.08);
}

/* 主题切换按钮 */
.theme-toggle {
  position: absolute;
  top: 24px;
  right: 24px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-regular);
  z-index: 10;
  transition: all 0.25s ease;
}
.theme-toggle:hover {
  background: rgba(255, 255, 255, 0.6);
  transform: scale(1.08);
}

/* 左上品牌标识 */
.brand-mark {
  position: absolute;
  top: 24px;
  left: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 18px;
  border-radius: 14px;
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  z-index: 10;
}

/* ===================== 中央玻璃卡片 ===================== */
.glass-card {
  position: relative;
  z-index: 5;
  width: 420px;
  max-width: calc(100vw - 48px);
  padding: 44px 40px 36px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(30px) saturate(200%);
  -webkit-backdrop-filter: blur(30px) saturate(200%);
  border: 1px solid rgba(255, 255, 255, 0.65);
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.12),
              inset 0 1px 0 rgba(255, 255, 255, 0.8);
  /* 居中定位 */
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  overflow: hidden;
}

/* 卡片边缘流光效果 */
.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: conic-gradient(from 0deg,
    transparent 0deg,
    rgba(129, 140, 248, 0.1) 60deg,
    transparent 120deg,
    rgba(236, 72, 153, 0.08) 240deg,
    transparent 300deg);
  animation: rotate-glow 12s linear infinite;
  pointer-events: none;
  z-index: -1;
}

@keyframes rotate-glow {
  to { transform: rotate(360deg); }
}

.register-card {
  width: 460px;
}

.form-header {
  margin-bottom: 28px;
  text-align: center;
}
.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  letter-spacing: 1px;
}
.form-header p {
  font-size: 14px;
  color: var(--text-secondary);
}

/* 表单输入框液态玻璃化 */
.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}
.login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  box-shadow: none;
  transition: all 0.25s ease;
}
.login-form :deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.65);
  border-color: rgba(79, 70, 229, 0.3);
}
.login-form :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.75);
  border-color: rgba(79, 70, 229, 0.5);
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.12);
}
.login-form :deep(.el-input__inner) {
  color: var(--text-primary);
}
.login-form :deep(.el-input__inner::placeholder) {
  color: var(--text-placeholder);
}
.login-form :deep(.el-input__prefix) {
  color: var(--text-secondary);
}
.login-form :deep(.el-input__suffix) {
  color: var(--text-secondary);
}

.submit-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  letter-spacing: 4px;
  border: none;
  border-radius: 12px;
  /* 克制的单色按钮，避免高饱和渐变 */
  background: var(--primary);
  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.25);
  transition: all 0.3s ease;
}
.submit-btn:hover {
  background: var(--primary-light);
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(79, 70, 229, 0.3);
}
.submit-btn:active {
  transform: translateY(0);
}

.form-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: var(--text-secondary);
}
.form-footer :deep(.el-button) {
  color: var(--primary);
  font-weight: 500;
}

.demo-hint {
  margin-top: 24px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.35);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
  border: 1px solid rgba(255, 255, 255, 0.4);
}

/* ===================== 底部特性栏 ===================== */
.features-bar {
  position: absolute;
  bottom: 28px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 12px 28px;
  border-radius: 18px;
  z-index: 10;
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-regular);
  font-size: 13px;
}
.feature-divider {
  width: 1px;
  height: 16px;
  background: rgba(15, 23, 42, 0.12);
}

/* ===================== 过渡动画 ===================== */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-fade-enter-from {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.95);
}
.slide-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.95);
}

/* ===================== 响应式 ===================== */
@media (max-width: 600px) {
  .glass-card {
    width: calc(100vw - 32px);
    padding: 32px 24px 28px;
    border-radius: 22px;
  }
  .register-card {
    width: calc(100vw - 32px);
  }
  .brand-mark {
    font-size: 14px;
    padding: 8px 14px;
  }
  .features-bar {
    gap: 12px;
    padding: 10px 18px;
  }
  .feature-item span {
    display: none;
  }
}
</style>
