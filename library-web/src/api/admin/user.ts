import http from "../request";
export const getUsers = (params: any) => http.get("/admin/users", { params });
export const updateUserStatus = (id: number, status: number) => http.put(`/admin/users/${id}/status`, { status });
