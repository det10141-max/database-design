import http from "./request";
export const loginApi = (username: string, password: string) => http.post("/auth/login", { username, password });
export const logoutApi = () => http.post("/auth/logout");
export const refreshApi = (refreshToken: string) => http.post("/auth/refresh", { refreshToken });
