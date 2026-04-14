<template>
  <div class="my-review-page">
    <h2 class="page-title">{{ isProvider ? '收到的评价' : '我的评价' }}</h2>

    <div v-loading="loading" class="review-list">
      <template v-if="reviews.length > 0">
        <el-card
          v-for="review in reviews"
          :key="review.id"
          shadow="never"
          class="review-card"
        >
          <div class="review-header">
            <div class="review-service">
              <span class="service-label">{{ isProvider ? '用户评价' : '服务订单' }}</span>
              <span class="order-id">订单ID: {{ review.orderId }}</span>
            </div>
            <span class="review-time">{{ formatDate(review.createdAt) }}</span>
          </div>

          <div class="review-body">
            <div v-if="isProvider" class="review-user">
              <span class="user-label">评价用户:</span>
              <span class="user-name">{{ review.userName || (review.isAnonymous ? '匿名用户' : '用户') }}</span>
            </div>
            <div class="review-rating">
              <el-rate :model-value="review.rating" disabled size="small" />
              <span class="rating-text">{{ review.rating }}.0 分</span>
            </div>
            <p v-if="review.content" class="review-content">{{ review.content }}</p>
            <div v-if="review.isAnonymous" class="anonymous-tag">
              <el-tag size="small" effect="plain">匿名评价</el-tag>
            </div>
          </div>

          <div v-if="review.reply" class="review-reply">
            <div class="reply-header">
              <el-icon><ChatDotRound /></el-icon>
              <span class="reply-title">{{ isProvider ? '我的回复' : '商家回复' }}</span>
            </div>
            <p class="reply-content">{{ review.reply }}</p>
          </div>

          <div v-if="isProvider && !review.reply" class="review-actions">
            <el-button type="primary" size="small" @click="showReplyDialog(review)">
              回复评价
            </el-button>
          </div>
        </el-card>
      </template>
      <el-empty v-else :description="isProvider ? '暂无收到的评价' : '暂无评价记录'" />
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        :page-sizes="[10, 20, 30]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="fetchReviews"
        @current-change="fetchReviews"
      />
    </div>

    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复评价"
      width="500px"
    >
      <el-form :model="replyForm">
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReply" :loading="replyLoading">
            提交回复
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'
import { get, post } from '@/api/request'
import type { Review } from '@/types/review'
import type { PageData } from '@/types/common'
import { formatDate } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const isProvider = computed(() => userStore.isProvider)

const loading = ref(false)
const reviews = ref<Review[]>([])
const total = ref(0)

const pagination = reactive({
  page: 1,
  size: 10,
})

// 回复相关
const replyDialogVisible = ref(false)
const replyLoading = ref(false)
const currentReview = ref<Review | null>(null)
const replyForm = reactive({
  content: '',
})

async function fetchReviews() {
  loading.value = true
  try {
    let res
    if (isProvider.value) {
      // 服务者查看收到的评价
      res = await get<PageData<Review>>(`/reviews/provider/${userStore.userInfo?.id}`, {
        page: pagination.page,
        size: pagination.size,
      })
    } else {
      // 普通用户查看自己的评价
      res = await get<PageData<Review>>('/reviews/my', {
        page: pagination.page,
        size: pagination.size,
      })
    }
    console.log('Reviews API response:', res)
    reviews.value = res.data.records
    total.value = res.data.total
    console.log('Fetched reviews:', reviews.value)
    console.log('Total reviews:', total.value)
  } catch (error) {
    console.error('Error fetching reviews:', error)
  } finally {
    loading.value = false
  }
}

function showReplyDialog(review: Review) {
  currentReview.value = review
  replyForm.content = ''
  replyDialogVisible.value = true
}

async function submitReply() {
  if (!currentReview.value || !replyForm.content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  replyLoading.value = true
  try {
    await post(`/reviews/${currentReview.value.id}/reply`, {
      reply: replyForm.content.trim(),
    })
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    // 重新获取评价列表
    fetchReviews()
  } catch (error) {
    console.error('Error replying to review:', error)
  } finally {
    replyLoading.value = false
  }
}

onMounted(() => {
  fetchReviews()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.my-review-page {
  padding: $spacing-lg 0;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: $spacing-lg;
}

// 评价列表
.review-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  min-height: 300px;
}

.review-card {
  :deep(.el-card__body) {
    padding: $spacing-lg;
  }
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-sm;
  border-bottom: 1px solid $border-color;
}

.review-service {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.service-label {
  font-size: 14px;
  font-weight: 600;
  color: $text-primary;
}

.order-id {
  font-size: 13px;
  color: $text-muted;
}

.review-time {
  font-size: 13px;
  color: $text-muted;
  white-space: nowrap;
}

.review-body {
  margin-bottom: $spacing-md;
}

.review-user {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
  font-size: 14px;
}

.user-label {
  color: $text-muted;
}

.user-name {
  color: $text-primary;
  font-weight: 500;
}

.review-rating {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}

.rating-text {
  color: $warning-color;
  font-size: 14px;
  font-weight: 600;
}

.review-content {
  color: $text-secondary;
  font-size: 14px;
  line-height: 1.8;
  margin: 0;
}

.anonymous-tag {
  margin-top: $spacing-sm;
}

// 商家回复
.review-reply {
  background: $bg-hover;
  border-radius: $border-radius;
  padding: $spacing-md;
  margin-top: $spacing-sm;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  margin-bottom: $spacing-sm;
  border-bottom: none;
  padding-bottom: 0;
  margin-bottom: $spacing-sm;
}

.reply-title {
  font-size: 13px;
  font-weight: 600;
  color: $primary-light;
}

.reply-content {
  color: $text-secondary;
  font-size: 13px;
  line-height: 1.6;
  margin: 0;
}

// 评价操作
.review-actions {
  margin-top: $spacing-md;
  padding-top: $spacing-md;
  border-top: 1px solid $border-color;
  display: flex;
  justify-content: flex-end;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-xl;
}

// 对话框样式
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}

// 响应式
@media (max-width: 768px) {
  .review-header {
    flex-direction: column;
    align-items: flex-start;
    gap: $spacing-xs;
  }
  
  .review-actions {
    justify-content: center;
  }
}
</style>
