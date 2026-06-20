import { createApp } from "vue";
import { createPinia } from "pinia";
import ElementPlus from "element-plus";
import * as Icons from "@element-plus/icons-vue";
import "element-plus/dist/index.css";
import "element-plus/theme-chalk/dark/css-vars.css";
import "./styles/theme.css";
import "./styles/global.css";
import App from "./App.vue";
import router from "./router";
import "./api/request";

const app = createApp(App);
app.use(createPinia());
app.use(ElementPlus);
app.use(router);
for (const [name, comp] of Object.entries(Icons))
  app.component(name, comp);
app.mount("#app");
