<template>
  <el-tag :type="type">{{ text }}</el-tag>
</template>

<script setup lang="ts">
defineProps({
  type: String,
  text: String
})
</script>
