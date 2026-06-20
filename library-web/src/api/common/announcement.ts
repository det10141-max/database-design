import http from "../request";
export const getAnnouncements = (params: any) => http.get("/common/announcements", { params });
