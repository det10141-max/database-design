import http from "../request";
export const reserve = (bookId: number) => http.post("/reader/reservations", { bookId });
export const getReservations = () => http.get("/reader/reservations");
export const cancelReservation = (id: number) => http.delete(`/reader/reservations/${id}`);
/** 预约到书后取书借阅 */
export const pickupReservation = (id: number) => http.put(`/reader/reservations/${id}/pickup`);
