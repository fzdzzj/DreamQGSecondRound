import { ref } from 'vue'
import { DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE } from '@/constants/app'

export function usePagination() {
  const pageNum = ref(DEFAULT_PAGE_NUM)
  const pageSize = ref(DEFAULT_PAGE_SIZE)
  const total = ref(0)

  const setPagination = (value: { pageNum: number; pageSize: number; total: number }) => {
    pageNum.value = value.pageNum
    pageSize.value = value.pageSize
    total.value = value.total
  }

  const resetPagination = () => {
    pageNum.value = DEFAULT_PAGE_NUM
    pageSize.value = DEFAULT_PAGE_SIZE
    total.value = 0
  }

  return {
    pageNum,
    pageSize,
    total,
    setPagination,
    resetPagination
  }
}
