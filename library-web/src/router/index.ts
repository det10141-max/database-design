import { createRouter, createWebHashHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: "/login", name: "Login", component: () => import("../views/LoginView.vue"), meta: { guest: true } },
    {
      path: "/admin", component: () => import("../layouts/AdminLayout.vue"),
      meta: { role: "ADMIN" }, redirect: "/admin/dashboard",
      children: [
        { path: "dashboard",    component: () => import("../views/admin/DashboardView.vue") },
        { path: "books",        component: () => import("../views/admin/BookListView.vue") },
        { path: "books/add",    component: () => import("../views/admin/BookEditView.vue") },
        { path: "books/:id",    component: () => import("../views/admin/BookEditView.vue") },
        { path: "categories",   component: () => import("../views/admin/CategoryView.vue") },
        { path: "borrows",      component: () => import("../views/admin/BorrowListView.vue") },
        { path: "reservations", component: () => import("../views/admin/ReservationListView.vue") },
        { path: "users",        component: () => import("../views/admin/UserListView.vue") },
        { path: "fines",        component: () => import("../views/admin/FineListView.vue") },
        { path: "overdue",      component: () => import("../views/admin/OverdueView.vue") },
        { path: "announcements",component: () => import("../views/admin/AnnouncementView.vue") },
      ]
    },
    {
      path: "/reader", component: () => import("../layouts/ReaderLayout.vue"),
      meta: { role: "READER" }, redirect: "/reader/home",
      children: [
        { path: "home",         component: () => import("../views/reader/HomeView.vue") },
        { path: "search",       component: () => import("../views/reader/SearchView.vue") },
        { path: "books/:id",    component: () => import("../views/reader/BookDetailView.vue") },
        { path: "borrows",      component: () => import("../views/reader/MyBorrowView.vue") },
        { path: "history",      component: () => import("../views/reader/BorrowHistoryView.vue") },
        { path: "reservations", component: () => import("../views/reader/MyReservationView.vue") },
        { path: "fines",        component: () => import("../views/reader/MyFineView.vue") },
        { path: "profile",      component: () => import("../views/reader/ProfileView.vue") },
      ]
    },
    { path: "/403", component: () => import("../views/403View.vue") },
    { path: "/:pathMatch(.*)*", redirect: "/login" }
  ]
});

router.beforeEach((to, from, next) => {
  const auth = useAuthStore();
  if (!auth.token) return to.meta.guest ? next() : next("/login");
  if (to.path === "/login") return next(auth.user?.role === "ADMIN" ? "/admin" : "/reader");
  if (to.meta.role && to.meta.role !== auth.user?.role) return next("/403");
  next();
});

export default router;
