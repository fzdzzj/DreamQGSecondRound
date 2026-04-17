export function itemStatusText(status?: number | string, statusDesc?: string): string {
  if (statusDesc) return statusDesc

  // 将status转换为数字类型
  const statusNum = typeof status === 'string' ? parseInt(status, 10) : status

  switch (statusNum) {
    case 1:
      return '开放中'
    case 2:
      return '已匹配'
    case 3:
      return '已关闭'
    case 4:
      return '已举报'
    case 5:
      return '已删除'
    default:
      return String(status ?? '-')
  }
}

export function itemTypeText(type?: number | string): string {
  // 将type转换为数字类型
  const typeNum = typeof type === 'string' ? parseInt(type, 10) : type

  switch (typeNum) {
    case 1:
      return '丢失物品'
    case 2:
      return '拾取物品'
    default:
      return String(type ?? '-')
  }
}

export function aiStatusText(aiStatus?: number | string): string {
  // 将aiStatus转换为数字类型
  const aiStatusNum = typeof aiStatus === 'string' ? parseInt(aiStatus, 10) : aiStatus

  switch (aiStatusNum) {
    case 0:
      return '未生成'
    case 1:
      return '生成中'
    case 2:
      return '已生成'
    case 3:
      return '生成失败'
    default:
      return String(aiStatus ?? '-')
  }
}

export function itemStatusTagType(status?: number): '' | 'success' | 'warning' | 'info' | 'danger' {
  switch (status) {
    case 1:
      return 'success'
    case 2:
      return 'warning'
    case 3:
      return 'info'
    case 4:
      return 'danger'
    case 5:
      return 'danger'
    default:
      return ''
  }
}