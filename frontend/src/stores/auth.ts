import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi } from '@/api/auth'
import router from '@/router'

interface UserInfo {
  id: string
  username: string
  realName: string
  role: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref<UserInfo | null>(
    JSON.parse(localStorage.getItem('user') || 'null')
  )

  async function login(username: string, password: string) {
    const res: any = await loginApi({ username, password })
    token.value = res.data.accessToken
    localStorage.setItem('token', res.data.accessToken)
    localStorage.setItem('refreshToken', res.data.refreshToken)

    // Fetch user info after login
    // For now, decode from token or use a placeholder
    user.value = {
      id: '',
      username,
      realName: username,
      role: 'researcher',
    }
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    router.push('/login')
  }

  function setUser(info: UserInfo) {
    user.value = info
    localStorage.setItem('user', JSON.stringify(info))
  }

  return { token, user, login, logout, setUser }
})
