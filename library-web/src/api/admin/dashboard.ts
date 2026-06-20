import http from "../request";
export const getDashboard = () => http.get("/admin/dashboard");
