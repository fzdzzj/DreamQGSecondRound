import { defineStore } from 'pinia'

interface PermissionState {
  permissions: string[]
}

export const usePermissionStore = defineStore('permission', {
  state: (): PermissionState => ({
    permissions: []
  }),
  actions: {
    setPermissions(list: string[]) {
      this.permissions = list
    },
    hasPermission(permission: string) {
      return this.permissions.includes(permission)
    },
    clear() {
      this.permissions = []
    }
  }
})
