import http from "../request";
export const getBorrows = (params: any) => http.get("/admin/borrows", { params });
export const returnBook = (id: number) => http.post(`/admin/borrows/${id}/return`);
export const markLost = (id: number) => http.put(`/admin/borrows/${id}/lost`);
export const fulfillReservation = (id: number) => http.put(`/admin/borrows/reservations/${id}/fulfill`);
export const getOverdue = (params: any) => http.get("/admin/borrows/overdue", { params });
