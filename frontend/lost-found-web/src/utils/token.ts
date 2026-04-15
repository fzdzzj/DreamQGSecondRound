const ACCESS_TOKEN_KEY = 'ACCESS_TOKEN'
const REFRESH_TOKEN_KEY = 'REFRESH_TOKEN'

export function getAccessToken(): string {
  return localStorage.getItem(ACCESS_TOKEN_KEY) || ''
}

export function setAccessToken(token: string): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, token)
}

export function removeAccessToken(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
}

export function getRefreshToken(): string {
  return localStorage.getItem(REFRESH_TOKEN_KEY) || ''
}

export function setRefreshToken(token: string): void {
  localStorage.setItem(REFRESH_TOKEN_KEY, token)
}

export function removeRefreshToken(): void {
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

export function clearToken(): void {
  removeAccessToken()
  removeRefreshToken()
}
