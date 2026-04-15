export function statusTagType(status?: number): '' | 'success' | 'warning' | 'danger' | 'info' {
  switch (status) {
    case 1:
      return 'success'
    case 2:
      return 'warning'
    case 3:
      return 'info'
    case 4:
      return 'danger'
    default:
      return ''
  }
}
