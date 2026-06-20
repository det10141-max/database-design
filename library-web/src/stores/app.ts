import { defineStore } from "pinia";
import { ref } from "vue";

export type ThemeMode = 'light' | 'dark' | 'auto';

export const useAppStore = defineStore("app", () => {
  const sidebarCollapsed = ref(false);
  const themeMode = ref<ThemeMode>((localStorage.getItem("themeMode") as ThemeMode) || 'auto');

  function getEffectiveTheme(): 'light' | 'dark' {
    if (themeMode.value === 'auto') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    return themeMode.value;
  }

  function applyTheme() {
    const effective = getEffectiveTheme();
    document.documentElement.classList.toggle('dark', effective === 'dark');
  }

  function setThemeMode(mode: ThemeMode) {
    themeMode.value = mode;
    localStorage.setItem("themeMode", mode);
    applyTheme();
  }

  function cycleTheme() {
    const order: ThemeMode[] = ['light', 'dark', 'auto'];
    const idx = order.indexOf(themeMode.value);
    setThemeMode(order[(idx + 1) % order.length]);
  }

  function getThemeIcon(): string {
    if (themeMode.value === 'light') return 'Sunny';
    if (themeMode.value === 'dark') return 'Moon';
    return 'Monitor';
  }

  function getThemeLabel(): string {
    if (themeMode.value === 'light') return '浅色';
    if (themeMode.value === 'dark') return '深色';
    return '跟随系统';
  }

  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
    if (themeMode.value === 'auto') applyTheme();
  });

  applyTheme();

  return { sidebarCollapsed, themeMode, setThemeMode, cycleTheme, getEffectiveTheme, getThemeIcon, getThemeLabel };
});
