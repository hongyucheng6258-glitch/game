<template>
  <div class="announcement-page">
    <h2 class="page-title">系统公告</h2>

    <div v-loading="loading" class="announcement-list">
      <template v-if="announcements.length > 0">
        <el-card
          v-for="item in announcements"
          :key="item.id"
          shadow="never"
          class="announcement-card"
          @click="handleView(item)"
        >
          <div class="announcement-header">
            <div class="announcement-title-row">
              <el-tag v-if="item.isTop" type="danger" size="small" class="top-tag">置顶</el-tag>
              <h3 class="announcement-title">{{ item.title }}</h3>
            </div>
            <span class="announcement-time">{{ formatDate(item.createdAt) }}</span>
          </div>
          <div class="announcement-summary">
            {{ item.content?.substring(0, 150) || '暂无内容' }}{{ (item.content?.length || 0) > 150 ? '...' : '' }}
          </div>
        </el-card>
      </template>
      <el-empty v-else description="暂无公告" />
    </div>

    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        :page-sizes="[10, 20, 30]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="fetchAnnouncements"
        @current-change="fetchAnnouncements"
      />
    </div>

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentAnnouncement?.title" width="700px">
      <div class="detail-meta">
        <span>{{ formatDate(currentAnnouncement?.createdAt ?? null) }}</span>
      </div>
      <div class="detail-content">
        {{ currentAnnouncement?.content }}
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAnnouncements } from '@/api/announcement'
import { formatDate } from '@/utils/format'

interface Announcement {
  id: number
  title: string
  content: string
  isTop: boolean
  createdAt: string
}

const loading = ref(false)
const announcements = ref<Announcement[]>([])
const total = ref(0)
const pagination = reactive({ page: 1, size: 10 })
const detailVisible = ref(false)
const currentAnnouncement = ref<Announcement | null>(null)

async function fetchAnnouncements() {
  loading.value = true
  try {
    const res = await getAnnouncements({ page: pagination.page, size: pagination.size })
    announcements.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

function handleView(item: Announcement) {
  currentAnnouncement.value = item
  detailVisible.value = true
}

onMounted(() => {
  fetchAnnouncements()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.announcement-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.page-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 32px;
  font-weight: 900;
  margin: 0;
  background: linear-gradient(135deg, $neon-cyan 0%, $primary-light 50%, $neon-purple 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 3px;
  text-transform: uppercase;
  position: relative;
  display: inline-block;

  &::after {
    content: '';
    position: absolute;
    bottom: -6px;
    left: 0;
    width: 60%;
    height: 2px;
    background: linear-gradient(90deg, $neon-cyan, transparent);
    box-shadow: 0 0 8px rgba(0, 240, 255, 0.5);
  }
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  min-height: 300px;
}

.announcement-card {
  cursor: pointer;
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $glass-border !important;
  border-radius: $border-radius-lg !important;
  transition: all $transition-normal;

  &:hover {
    transform: translateY(-3px);
    box-shadow: $shadow-neon-cyan, 0 8px 28px rgba(0, 0, 0, 0.25);
    border-color: rgba(0, 240, 255, 0.25) !important;
  }

  :deep(.el-card__body) {
    padding: $spacing-lg;
  }
}

.announcement-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: $spacing-sm;
}

.announcement-title-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  flex: 1;
  min-width: 0;
}

.top-tag {
  flex-shrink: 0;
  animation: glow-tag 2s ease-in-out infinite;
}

@keyframes glow-tag {
  0%, 100% {
    box-shadow: 0 0 4px rgba(239, 68, 68, 0.3);
  }
  50% {
    box-shadow: 0 0 12px rgba(239, 68, 68, 0.6);
  }
}

.announcement-title {
  font-size: 16px;
  font-weight: 700;
  color: $text-primary;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color $transition-fast, text-shadow $transition-fast;

  .announcement-card:hover & {
    color: $neon-cyan;
    text-shadow: 0 0 8px rgba(0, 240, 255, 0.3);
  }
}

.announcement-time {
  font-size: 13px;
  color: $text-muted;
  flex-shrink: 0;
  margin-left: $spacing-md;
  font-variant-numeric: tabular-nums;
}

.announcement-summary {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.6;
  position: relative;
  padding-left: $spacing-md;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 4px;
    bottom: 4px;
    width: 2px;
    background: linear-gradient(180deg, $neon-cyan, transparent);
    border-radius: 1px;
    box-shadow: 0 0 4px rgba(0, 240, 255, 0.3);
  }
}

.detail-meta {
  font-size: 13px;
  color: $text-muted;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-md;
  border-bottom: 1px solid $glass-border;
}

.detail-content {
  font-size: 14px;
  color: $text-primary;
  line-height: 1.8;
  white-space: pre-wrap;
}

:deep(.el-dialog) {
  background: $glass-bg;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $glass-border;
  border-radius: $border-radius-xl !important;
  box-shadow: 0 0 40px rgba(0, 240, 255, 0.1), 0 16px 48px rgba(0, 0, 0, 0.4);

  .el-dialog__header {
    border-bottom: 1px solid $glass-border;
    padding: $spacing-lg;
  }

  .el-dialog__title {
    color: $text-primary;
    font-weight: 700;
  }

  .el-dialog__body {
    padding: $spacing-lg;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: $spacing-md 0;

  :deep(.el-pagination) {
    .el-pager li {
      background: $glass-bg;
      border: 1px solid $glass-border;
      border-radius: $border-radius;
      color: $text-secondary;
      font-weight: 600;
      transition: all $transition-fast;

      &:hover {
        border-color: rgba(0, 240, 255, 0.3);
        color: $neon-cyan;
      }

      &.is-active {
        background: rgba(0, 240, 255, 0.1);
        border-color: $neon-cyan;
        color: $neon-cyan;
        box-shadow: 0 0 8px rgba(0, 240, 255, 0.3);
      }
    }

    button {
      background: $glass-bg;
      border: 1px solid $glass-border;
      border-radius: $border-radius;
      color: $text-secondary;
      transition: all $transition-fast;

      &:hover {
        border-color: rgba(0, 240, 255, 0.3);
        color: $neon-cyan;
      }

      &:disabled {
        opacity: 0.4;
      }
    }

    .el-pagination__total {
      color: $text-secondary;
    }

    .el-pagination__sizes {
      .el-select .el-input__wrapper {
        background: $glass-bg;
        border: 1px solid $glass-border;
        box-shadow: none;

        &:hover {
          border-color: rgba(0, 240, 255, 0.3);
        }
      }
    }
  }
}
</style>
