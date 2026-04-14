<template>
  <div class="review-item">
    <div class="review-header">
      <div class="review-user">
        <UserAvatar :avatar="review.userAvatar" :username="review.userName" :size="40" />
        <div class="review-user-info">
          <span class="review-username">{{ review.isAnonymous ? '匿名用户' : (review.userName || '用户') }}</span>
          <span class="review-time">{{ formatDate(review.createdAt) }}</span>
        </div>
      </div>
      <RatingStar :value="review.rating" />
    </div>
    <div class="review-content">
      {{ review.content || '用户未填写评价内容' }}
    </div>
    <div v-if="review.reply" class="review-reply">
      <div class="reply-header">
        <el-icon><ChatLineSquare /></el-icon>
        <span>服务者回复</span>
      </div>
      <div class="reply-content">{{ review.reply }}</div>
    </div>
    <slot name="actions" :review="review" />
  </div>
</template>

<script setup lang="ts">
import { ChatLineSquare } from '@element-plus/icons-vue'
import type { Review } from '@/types/review'
import UserAvatar from './UserAvatar.vue'
import RatingStar from './RatingStar.vue'
import { formatDate } from '@/utils/format'

defineProps<{ review: Review }>()
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.review-item {
  padding: $spacing-md;
  border-bottom: 1px solid $border-color;

  &:last-child {
    border-bottom: none;
  }
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-sm;
}

.review-user {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.review-user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.review-username {
  font-size: 14px;
  font-weight: 600;
  color: $text-primary;
}

.review-time {
  font-size: 12px;
  color: $text-muted;
}

.review-content {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.6;
  margin-bottom: $spacing-sm;
}

.review-reply {
  background: rgba(99, 102, 241, 0.08);
  border-radius: $border-radius;
  padding: $spacing-sm $spacing-md;
  margin-top: $spacing-sm;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: $primary-light;
  font-weight: 600;
  margin-bottom: $spacing-xs;
}

.reply-content {
  font-size: 13px;
  color: $text-secondary;
  line-height: 1.5;
}
</style>
