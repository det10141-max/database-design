import { defineStore } from "pinia";
import { ref } from "vue";
import { loginApi, logoutApi, refreshApi } from "../api/auth";

export const useAuthStore = defineStore("auth", () => {
  const token = ref<string | null>(localStorage.getItem("accessToken"));
  const refreshToken = ref<string | null>(localStorage.getItem("refreshToken"));
  const user = ref<any>(null);
  try { user.value = JSON.parse(localStorage.getItem("user") || "null"); } catch {}

  async function login(username: string, password: string) {
    const res: any = await loginApi(username, password);
    token.value = res.data.accessToken;
    refreshToken.value = res.data.refreshToken;
    user.value = { id: res.data.userId, username: res.data.username, role: res.data.role, realName: res.data.realName };
    localStorage.setItem("accessToken", token.value!);
    localStorage.setItem("refreshToken", refreshToken.value!);
    localStorage.setItem("user", JSON.stringify(user.value));
  }

  async function refresh() {
    if (!refreshToken.value) return false;
    try {
      const res: any = await refreshApi(refreshToken.value);
      token.value = res.data.accessToken;
      refreshToken.value = res.data.refreshToken;
      localStorage.setItem("accessToken", token.value!);
      localStorage.setItem("refreshToken", refreshToken.value!);
      return true;
    } catch { return false; }
  }

  async function logout() {
    try { await logoutApi(); } catch {}
    const themeMode = localStorage.getItem("themeMode");
    token.value = null; refreshToken.value = null; user.value = null;
    localStorage.clear();
    if (themeMode) localStorage.setItem("themeMode", themeMode);
  }

  return { token, refreshToken, user, login, logout, refresh };
});
