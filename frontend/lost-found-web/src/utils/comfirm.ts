import { ElMessageBox } from 'element-plus'

export async function confirmDanger(message = '确认执行该操作吗？', title = '提示'): Promise<void> {
  await ElMessageBox.confirm(message, title, {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  })
}
