import { defineStore } from 'pinia'

interface AppState {
  sidebarCollapsed: boolean
  loading: boolean
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    sidebarCollapsed: false,
    loading: false
  }),
  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    setLoading(loading: boolean) {
      this.loading = loading
    }
  }
})
