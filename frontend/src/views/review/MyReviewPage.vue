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
  font-size: 28px;
  font-weight: 700;
  font-family: 'Orbitron', sans-serif;
  background: linear-gradient(135deg, $neon-cyan, $primary-light, $neon-purple);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: none;
  filter: drop-shadow(0 0 12px rgba(0, 240, 255, 0.25));
  margin-bottom: $spacing-lg;
  position: relative;
  padding-left: 16px;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 28px;
    border-radius: 2px;
    background: linear-gradient(180deg, $neon-cyan, $neon-purple);
    box-shadow: 0 0 10px rgba(0, 240, 255, 0.4);
  }
}

// 评价列表
.review-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  min-height: 300px;

  :deep(.el-empty__image svg) {
    filter: drop-shadow(0 0 6px rgba(0, 240, 255, 0.2));
  }
}

.review-card {
  background: $glass-bg;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $glass-border;
  border-left: 3px solid $neon-cyan;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-md;
  transition: transform $transition-normal, box-shadow $transition-normal, border-color $transition-normal;

  &:hover {
    transform: translateY(-2px);
    border-left-color: $neon-purple;
    box-shadow: 0 0 16px rgba(0, 240, 255, 0.1), $shadow-lg;
  }

  &:nth-child(2n) {
    border-left-color: $neon-pink;

    &:hover {
      border-left-color: $neon-cyan;
    }
  }

  &:nth-child(3n) {
    border-left-color: $neon-purple;

    &:hover {
      border-left-color: $neon-green;
    }
  }

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
  border-bottom: 1px solid rgba(148, 163, 184, 0.06);
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
  font-family: 'Courier New', monospace;
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
  color: $neon-cyan;
  font-weight: 500;
  text-shadow: 0 0 6px rgba(0, 240, 255, 0.2);
}

.review-rating {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}

.rating-text {
  color: $neon-yellow;
  font-size: 14px;
  font-weight: 600;
  text-shadow: 0 0 10px rgba(255, 230, 0, 0.35);
}

.review-content {
  color: $text-secondary;
  font-size: 14px;
  line-height: 1.8;
  margin: 0;
}

.anonymous-tag {
  margin-top: $spacing-sm;

  :deep(.el-tag) {
    background: rgba(191, 90, 242, 0.1);
    color: $neon-purple;
    border-color: rgba(191, 90, 242, 0.3);
    box-shadow: 0 0 8px rgba(191, 90, 242, 0.15);
  }
}

// 商家回复
.review-reply {
  background: rgba(0, 240, 255, 0.04);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(0, 240, 255, 0.08);
  border-left: 3px solid $neon-cyan;
  border-radius: $border-radius;
  padding: $spacing-md;
  margin-top: $spacing-sm;
  box-shadow: 0 0 8px rgba(0, 240, 255, 0.04);
}

.reply-header {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  margin-bottom: $spacing-sm;
  border-bottom: none;
  padding-bottom: 0;

  :deep(.el-icon) {
    color: $neon-cyan;
    filter: drop-shadow(0 0 4px rgba(0, 240, 255, 0.3));
  }
}

.reply-title {
  font-size: 13px;
  font-weight: 600;
  color: $neon-cyan;
  text-shadow: 0 0 6px rgba(0, 240, 255, 0.2);
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
  border-top: 1px solid rgba(148, 163, 184, 0.06);
  display: flex;
  justify-content: flex-end;

  :deep(.el-button--primary) {
    background: linear-gradient(135deg, $primary-color, $neon-purple);
    border: 1px solid rgba(0, 240, 255, 0.15);
    box-shadow: 0 4px 12px rgba(0, 240, 255, 0.12);
    transition: all $transition-normal;

    &:hover {
      background: linear-gradient(135deg, $neon-cyan, $primary-color);
      box-shadow: 0 6px 20px rgba(0, 240, 255, 0.3);
      border-color: $neon-cyan;
      transform: translateY(-1px);
    }
  }
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
