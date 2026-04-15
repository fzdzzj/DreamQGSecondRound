<template>
  <el-button type="danger" @click="handle">
    <slot />
  </el-button>
</template>

<script setup lang="ts">
import { confirmDanger } from '@/utils/comfirm'

const emit = defineEmits(['confirm'])

const handle = async () => {
  await confirmDanger()
  emit('confirm')
}
</script>
