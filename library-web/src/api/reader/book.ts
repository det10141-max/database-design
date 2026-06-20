import http from "../request";
export const getBooks = (params: any) => http.get("/reader/books", { params });
export const getBookDetail = (id: number) => http.get(`/reader/books/${id}`);
export const getPopularBooks = (limit = 10) => http.get("/reader/books/popular", { params: { limit } });
