<template>
  <div class="page-card">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px">
      <h2 style="margin-bottom: 0">{{ detail.title || '物品详情' }}</h2>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="14">
        <el-carousel v-if="detail.imageUrls?.length" height="320px">
          <el-carousel-item v-for="url in detail.imageUrls" :key="url">
            <el-image :src="url" fit="contain" style="width: 100%; height: 320px" />
          </el-carousel-item>
        </el-carousel>

        <div v-else style="height: 320px" class="flex-center">
          <span>暂无图片</span>
        </div>
      </el-col>

      <el-col :span="10">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="标题">
            {{ detail.title || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="物品类型">
            {{ itemTypeText(detail.type) }}
          </el-descriptions-item>

          <el-descriptions-item label="物品状态">
            <StatusTag
              :type="itemStatusTagType(detail.status)"
              :text="itemStatusText(detail.status, detail.statusDesc)"
            />
          </el-descriptions-item>

          <el-descriptions-item label="发生地点">
            {{ detail.location || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="发生时间">
            {{ detail.happenTime || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="联系方式">
            {{ detail.contactMethod || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="AI分类">
            {{ detail.aiCategory || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="AI状态">
            {{ aiStatusText(detail.aiStatus) }}
          </el-descriptions-item>

          <el-descriptions-item label="AI标签">
            <template v-if="detail.aiTags?.length">
              <el-tag v-for="tag in detail.aiTags" :key="tag" style="margin-right: 8px; margin-bottom: 8px">
                {{ tag }}
              </el-tag>
            </template>
            <template v-else>-</template>
          </el-descriptions-item>
        </el-descriptions>
      </el-col>
    </el-row>

    <el-divider>物品描述</el-divider>
    <div style="white-space: pre-wrap; line-height: 1.8">
      {{ detail.description || '-' }}
    </div>

    <el-divider>AI描述</el-divider>
    <div style="white-space: pre-wrap; line-height: 1.8">
      {{ detail.aiDescription || '-' }}
    </div>

    <div style="margin-top: 20px">
      <el-button @click="openClaimDialog">认领</el-button>
      <el-button @click="openReportDialog">举报</el-button>
      <el-button type="warning" @click="openPinDialog">申请置顶</el-button>
    </div>

    <el-divider>发表评论</el-divider>

    <el-input
      v-model="commentContent"
      type="textarea"
      :rows="3"
      placeholder="请输入评论内容"
    />
    <el-button style="margin-top: 8px" type="primary" @click="submitComment">
      发表评论
    </el-button>

    <el-divider>评论列表</el-divider>

    <CommentTree
      v-if="comments.length"
      :list="comments"
      :item-id="itemId"
      @refresh="loadComments"
    />

    <div v-else class="flex-center" style="height: 120px">
      <span>暂无评论</span>
    </div>

    <el-dialog v-model="claimVisible" title="提交认领申请">
      <el-input
        v-model="claimForm.verificationAnswer"
        type="textarea"
        :rows="4"
        placeholder="请输入验证说明"
      />
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
      <el-input
        v-model="pinForm.reason"
        type="textarea"
        :rows="4"
        placeholder="请输入申请理由"
      />
      <template #footer>
        <el-button @click="pinVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPin">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { addCommentApi, getItemCommentsApi } from '@/api/comment'
import { createClaimApi } from '@/api/claim'
import { getItemDetailApi } from '@/api/item'
import { applyPinApi } from '@/api/pin'
import { createReportApi } from '@/api/report'
import { showSuccess } from '@/utils/message'
import { aiStatusText, itemStatusTagType, itemStatusText, itemTypeText } from '@/utils/item'
import StatusTag from '@/components/common/StatusTag.vue'
import CommentTree from '@/components/comment/CommentTree.vue'

const route = useRoute()
const router = useRouter()
const itemId = Number(route.params.id)

const detail = ref<any>({
  imageUrls: [],
  aiTags: []
})
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
  const res = await getItemCommentsApi(itemId, 1, 100)
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