import type { Router } from 'vue-router'
import { WHITE_ROUTE_LIST } from '@/constants/route'
import { getAccessToken } from '@/utils/token'
import { useUserStore } from '@/stores/user'
import { isAdminRole } from '@/utils/auth'

export function setupRouterGuards(router: Router): void {
  router.beforeEach(async (to, _from, next) => {
    document.title = (to.meta.title as string) || 'Lost & Found'

    if (WHITE_ROUTE_LIST.includes(to.path)) {
      next()
      return
    }

    const token = getAccessToken()
    if (!token) {
      next('/login')
      return
    }

    const userStore = useUserStore()

    if (!userStore.userInfo) {
      try {
        await userStore.fetchProfile()
      } catch {
        userStore.logout()
        next('/login')
        return
      }
    }

    if (to.meta.adminOnly && !isAdminRole(userStore.role)) {
      next('/403')
      return
    }

    next()
  })
}
