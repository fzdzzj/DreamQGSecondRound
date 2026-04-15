<template>
  <div class="page-card">
    <h2>{{ detail.title }}</h2>

    <el-carousel v-if="detail.imageUrls?.length" height="300px">
      <el-carousel-item v-for="url in detail.imageUrls" :key="url">
        <el-image :src="url" fit="contain" style="width: 100%; height: 300px" />
      </el-carousel-item>
    </el-carousel>

    <p style="margin-top: 16px">{{ detail.description }}</p>
    <p>地点：{{ detail.location }}</p>
    <p>时间：{{ detail.happenTime }}</p>
    <p>状态：{{ detail.statusDesc }}</p>
    <p>AI分类：{{ detail.aiCategory || '-' }}</p>
    <p>AI描述：{{ detail.aiDescription || '-' }}</p>

    <div style="margin-top: 16px">
      <el-button @click="openClaimDialog">认领</el-button>
      <el-button @click="openReportDialog">举报</el-button>
      <el-button type="warning" @click="openPinDialog">申请置顶</el-button>
    </div>

    <el-divider>评论</el-divider>

    <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="请输入评论" />
    <el-button style="margin-top: 8px" type="primary" @click="submitComment">发表评论</el-button>

    <div style="margin-top: 16px">
      <div v-for="item in comments" :key="item.id" style="padding: 12px 0; border-bottom: 1px solid #f0f0f0">
        <div>{{ item.nickname }}：{{ item.content }}</div>
      </div>
    </div>

    <el-dialog v-model="claimVisible" title="提交认领申请">
      <el-input v-model="claimForm.verificationAnswer" type="textarea" :rows="4" placeholder="请输入验证说明" />
      <template #footer>
        <el-button @click="claimVisible = false">取消</el-button>
        <el-button type="primary" @click="submitClaim">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reportVisible" title="提交举报">
      <el-select v-model="reportForm.reason" style="width: 100%">
        <el-option :value="1" label="虚假信息" />
        <el-option :value="2" label="恶意内容" />
        <el-option :value="3" label="其他" />
      </el-select>
      <el-input
        v-model="reportForm.detail"
        type="textarea"
        :rows="4"
        placeholder="举报说明"
        style="margin-top: 12px"
      />
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReport">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pinVisible" title="申请置顶">
      <el-input v-model="pinForm.reason" type="textarea" :rows="4" placeholder="请输入申请理由" />
      <template #footer>
        <el-button @click="pinVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPin">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { addCommentApi, getItemCommentsApi } from '@/api/comment'
import { createClaimApi } from '@/api/claim'
import { getItemDetailApi } from '@/api/item'
import { applyPinApi } from '@/api/pin'
import { createReportApi } from '@/api/report'
import { showSuccess } from '@/utils/message'

const route = useRoute()
const itemId = Number(route.params.id)

const detail = ref<any>({})
const comments = ref<any[]>([])
const commentContent = ref('')

const claimVisible = ref(false)
const reportVisible = ref(false)
const pinVisible = ref(false)

const claimForm = reactive({
  verificationAnswer: ''
})

const reportForm = reactive({
  reason: 1,
  detail: ''
})

const pinForm = reactive({
  reason: ''
})

const loadDetail = async () => {
  detail.value = await getItemDetailApi(itemId)
}

const loadComments = async () => {
  const res = await getItemCommentsApi(itemId, 1, 20)
  comments.value = res.list
}

onMounted(async () => {
  await loadDetail()
  await loadComments()
})

const submitComment = async () => {
  if (!commentContent.value.trim()) return
  await addCommentApi({
    itemId,
    content: commentContent.value
  })
  commentContent.value = ''
  showSuccess('评论成功')
  await loadComments()
}

const openClaimDialog = () => {
  claimVisible.value = true
}

const submitClaim = async () => {
  await createClaimApi({
    itemId,
    verificationAnswer: claimForm.verificationAnswer
  })
  claimVisible.value = false
  claimForm.verificationAnswer = ''
  showSuccess('认领申请已提交')
}

const openReportDialog = () => {
  reportVisible.value = true
}

const submitReport = async () => {
  await createReportApi({
    itemId,
    reason: reportForm.reason,
    detail: reportForm.detail
  })
  reportVisible.value = false
  reportForm.reason = 1
  reportForm.detail = ''
  showSuccess('举报已提交')
}

const openPinDialog = () => {
  pinVisible.value = true
}

const submitPin = async () => {
  await applyPinApi({
    itemId,
    reason: pinForm.reason
  })
  pinVisible.value = false
  pinForm.reason = ''
  showSuccess('置顶申请已提交')
}
</script>
