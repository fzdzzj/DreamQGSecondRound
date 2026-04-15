import { ref } from 'vue'

export function useLoading(defaultValue = false) {
  const loading = ref(defaultValue)

  const startLoading = () => {
    loading.value = true
  }

  const stopLoading = () => {
    loading.value = false
  }

  return {
    loading,
    startLoading,
    stopLoading
  }
}
