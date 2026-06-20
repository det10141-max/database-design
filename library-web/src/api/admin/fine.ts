import http from "../request";
export const getFines = (params: any) => http.get("/admin/fines", { params });
export const createFine = (data: any) => http.post("/admin/fines", data);
export const payFine = (id: number) => http.put(`/admin/fines/${id}/pay`);
