<template>
  <div>
    <div
      v-for="item in list"
      :key="item"
      @click="$emit('select', item)"
      class="item"
    >
      <div class="session-id">{{ item }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  list: string[]
}>()
</script>

<style scoped>
.item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}
.item:hover {
  background: #f7f7f7;
}
.session-id {
  font-size: 12px;
  color: #606266;
  word-break: break-all;
}
</style>
