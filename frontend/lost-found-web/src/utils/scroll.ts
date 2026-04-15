export function scrollToBottom(el?: HTMLElement | null) {
  if (!el) return
  setTimeout(() => {
    el.scrollTop = el.scrollHeight
  }, 50)
}
