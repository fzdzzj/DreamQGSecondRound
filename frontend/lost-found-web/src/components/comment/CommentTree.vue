<template>
  <div class="comment-tree">
    <div
      v-for="item in list"
      :key="item.id"
      class="comment-node"
      :style="{ marginLeft: `${level * 20}px` }"
    >
      <div class="comment-card">
        <div class="comment-header">
          <div class="comment-user">
            <el-avatar :size="32" :src="item.avatar || ''">
              {{ (item.nickname || '用户').slice(0, 1) }}
            </el-avatar>
            <div class="comment-meta">
              <div class="comment-name">{{ item.nickname || '用户' }}</div>
              <div class="comment-time">{{ item.createTime || '-' }}</div>
            </div>
          </div>

          <div class="comment-actions">
            <el-button link type="primary" @click="toggleReply(item.id)">
              {{ replyVisibleMap[item.id] ? '取消回复' : '回复' }}
            </el-button>
          </div>
        </div>

        <div class="comment-content">
          {{ item.content }}
        </div>

        <div v-if="replyVisibleMap[item.id]" class="reply-box">
          <el-input
            v-model="replyMap[item.id]"
            type="textarea"
            :rows="2"
            placeholder="请输入回复内容"
          />
          <div style="margin-top: 8px">
            <el-button type="primary" size="small" @click="submitReply(item)">
              提交回复
            </el-button>
          </div>
        </div>
      </div>

      <CommentTree
        v-if="item.children && item.children.length"
        :list="item.children"
        :item-id="itemId"
        :level="level + 1"
        @refresh="$emit('refresh')"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { addCommentApi } from '@/api/comment'
import { showSuccess, showWarning } from '@/utils/message'
import type { CommentStatVO } from '@/types/comment'

defineOptions({
  name: 'CommentTree'
})

const props = defineProps<{
  list: CommentStatVO[]
  itemId: number
  level?: number
}>()

const emit = defineEmits<{
  (e: 'refresh'): void
}>()

const replyVisibleMap = reactive<Record<number, boolean>>({})
const replyMap = reactive<Record<number, string>>({})

const level = props.level ?? 0

const toggleReply = (commentId: number) => {
  replyVisibleMap[commentId] = !replyVisibleMap[commentId]
}

const submitReply = async (comment: CommentStatVO) => {
  const content = (replyMap[comment.id] || '').trim()
  if (!content) {
    showWarning('请输入回复内容')
    return
  }

  await addCommentApi({
    itemId: props.itemId,
    content,
    parentId: comment.id
  })

  showSuccess('回复成功')
  replyMap[comment.id] = ''
  replyVisibleMap[comment.id] = false
  emit('refresh')
}
</script>

<style scoped>
.comment-node {
  margin-top: 12px;
}

.comment-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-meta {
  display: flex;
  flex-direction: column;
}

.comment-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-content {
  margin-top: 10px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #606266;
}

.reply-box {
  margin-top: 12px;
}
</style>
