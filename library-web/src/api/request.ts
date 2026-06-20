import axios from "axios";

const http = axios.create({ baseURL: "/api", timeout: 10000 });

http.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (res) => {
    // 后端业务错误：HTTP 200 但 Result.code 非 200，需转为 reject 让 catch 处理
    if (res.data.code !== 200) {
      const error: any = new Error(res.data.msg || "请求失败");
      error.response = { data: res.data };
      return Promise.reject(error);
    }
    return res.data;
  },
  async (err) => {
    // 401 未授权：尝试使用 refreshToken 刷新令牌
    if (err.response?.status === 401 && !err.config._retry) {
      err.config._retry = true;
      const rt = localStorage.getItem("refreshToken");
      if (rt) {
        try {
          const res = await axios.post("/api/auth/refresh", { refreshToken: rt });
          localStorage.setItem("accessToken", res.data.data.accessToken);
          localStorage.setItem("refreshToken", res.data.data.refreshToken);
          err.config.headers.Authorization = `Bearer ${res.data.data.accessToken}`;
          return http(err.config);
        } catch {}
      }
      localStorage.clear();
      window.location.hash = "#/login";
    }
    // 注意：错误提示由各业务页面在 catch 中自行处理，避免重复弹窗
    return Promise.reject(err);
  }
);

export default http;
