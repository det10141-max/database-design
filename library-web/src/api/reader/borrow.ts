import http from "../request";
export const borrowBook = (bookId: number) => http.post("/reader/borrows", { bookId });
export const getCurrent = (params: any) => http.get("/reader/borrows/current", { params });
export const getHistory = (params: any) => http.get("/reader/borrows/history", { params });
export const renew = (id: number) => http.put(`/reader/borrows/${id}/renew`);
export const returnBook = (id: number) => http.put(`/reader/borrows/${id}/return`);
export const checkEligibility = () => http.get("/reader/borrows/eligibility");
