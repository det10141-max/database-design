import http from "../request";
export const getBooks = (params: any) => http.get("/admin/books", { params });
export const getBookDetail = (id: number) => http.get(`/admin/books/${id}`);
export const createBook = (data: any) => http.post("/admin/books", data);
export const updateBook = (id: number, data: any) => http.put(`/admin/books/${id}`, data);
export const deleteBook = (id: number) => http.delete(`/admin/books/${id}`);
