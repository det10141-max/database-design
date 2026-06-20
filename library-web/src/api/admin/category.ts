import http from "../request";
export const getCategories = () => http.get("/admin/categories");
export const createCategory = (data: any) => http.post("/admin/categories", data);
export const updateCategory = (id: number, data: any) => http.put(`/admin/categories/${id}`, data);
export const deleteCategory = (id: number) => http.delete(`/admin/categories/${id}`);
