import http from "../request";
export const createReview = (data: any) => http.post("/reader/reviews", data);
