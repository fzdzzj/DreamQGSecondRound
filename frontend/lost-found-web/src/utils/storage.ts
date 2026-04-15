export function setLocal<T>(key: string, value: T): void {
  localStorage.setItem(key, JSON.stringify(value))
}

export function getLocal<T>(key: string, defaultValue: T): T {
  const raw = localStorage.getItem(key)
  if (!raw) return defaultValue
  try {
    return JSON.parse(raw) as T
  } catch {
    return defaultValue
  }
}

export function removeLocal(key: string): void {
  localStorage.removeItem(key)
}
