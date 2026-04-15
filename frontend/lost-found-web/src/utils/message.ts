import { ElMessage } from 'element-plus'

export const showSuccess = (msg: string) => ElMessage.success(msg)
export const showError = (msg: string) => ElMessage.error(msg)
export const showWarning = (msg: string) => ElMessage.warning(msg)
