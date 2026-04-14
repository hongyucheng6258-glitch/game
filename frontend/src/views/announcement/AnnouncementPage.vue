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
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  margin: 0;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  min-height: 300px;
}

.announcement-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-md;
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
}

.announcement-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-time {
  font-size: 13px;
  color: $text-muted;
  flex-shrink: 0;
  margin-left: $spacing-md;
}

.announcement-summary {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.6;
}

.detail-meta {
  font-size: 13px;
  color: $text-muted;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-md;
  border-bottom: 1px solid $border-color;
}

.detail-content {
  font-size: 14px;
  color: $text-primary;
  line-height: 1.8;
  white-space: pre-wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
}
</style>
