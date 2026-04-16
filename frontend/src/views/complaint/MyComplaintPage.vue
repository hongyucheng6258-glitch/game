<template>
  <div class="my-complaints-page">
    <div class="page-container">
      <div class="page-header">
        <h2 class="page-title">我的投诉</h2>
      </div>

      <el-card shadow="never" class="table-card">
        <el-table :data="complaints" v-loading="loading" stripe>
          <el-table-column prop="id" label="投诉ID" width="100" />
          <el-table-column prop="orderId" label="订单ID" width="100" />
          <el-table-column label="投诉类型" width="140">
            <template #default="{ row }">
              {{ COMPLAINT_TYPE_LABELS[row.type] }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="COMPLAINT_STATUS_TYPES[row.status]">
                {{ COMPLAINT_STATUS_LABELS[row.status] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="投诉内容" show-overflow-tooltip />
          <el-table-column label="提交时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchComplaints"
            @current-change="fetchComplaints"
          />
        </div>
      </el-card>
    </div>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="投诉详情"
      width="600px"
      destroy-on-close
    >
      <template v-if="currentComplaint">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="投诉ID">{{ currentComplaint.id }}</el-descriptions-item>
          <el-descriptions-item label="订单ID">{{ currentComplaint.orderId }}</el-descriptions-item>
          <el-descriptions-item label="投诉类型">
            {{ COMPLAINT_TYPE_LABELS[currentComplaint.type] }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="COMPLAINT_STATUS_TYPES[currentComplaint.status]">
              {{ COMPLAINT_STATUS_LABELS[currentComplaint.status] }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="投诉内容">
            {{ currentComplaint.content }}
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatDate(currentComplaint.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentComplaint.arbitrationResult" label="仲裁结果">
            {{ currentComplaint.arbitrationResult }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentComplaint.arbitratedAt" label="仲裁时间">
            {{ formatDate(currentComplaint.arbitratedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyComplaints, type Complaint } from '@/api/complaint'
import {
  COMPLAINT_STATUS_LABELS,
  COMPLAINT_STATUS_TYPES,
  COMPLAINT_TYPE_LABELS,
} from '@/utils/constants'
import { formatDate } from '@/utils/format'

const loading = ref(false)
const complaints = ref<Complaint[]>([])
const total = ref(0)

const pagination = reactive({
  page: 1,
  size: 10,
})

const showDetailDialog = ref(false)
const currentComplaint = ref<Complaint | null>(null)

async function fetchComplaints() {
  loading.value = true
  try {
    const res = await getMyComplaints({
      page: pagination.page,
      size: pagination.size,
    })
    complaints.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleView(complaint: Complaint) {
  currentComplaint.value = complaint
  showDetailDialog.value = true
}

onMounted(() => {
  fetchComplaints()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.my-complaints-page {
  padding: $spacing-lg 0;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.table-card {
  background: $glass-bg;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $glass-border;
  border-radius: $border-radius-xl;
  box-shadow: $shadow-md;
  overflow: hidden;
  transition: border-color $transition-normal, box-shadow $transition-normal;

  &:hover {
    border-color: rgba(0, 240, 255, 0.15);
    box-shadow: 0 0 16px rgba(0, 240, 255, 0.08), $shadow-md;
  }

  :deep(.el-card__body) {
    padding: 0;
  }

  :deep(.el-table) {
    --el-table-bg-color: transparent;
    --el-table-tr-bg-color: transparent;
    --el-table-header-bg-color: linear-gradient(135deg, rgba(0, 240, 255, 0.08), rgba(99, 102, 241, 0.08));
    --el-table-row-hover-bg-color: rgba(0, 240, 255, 0.04);
    --el-table-border-color: rgba(148, 163, 184, 0.06);
    --el-table-text-color: #{$text-primary};
    --el-table-header-text-color: #{$neon-cyan};

    th.el-table__cell {
      font-weight: 600;
      font-size: 13px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: $neon-cyan;
      text-shadow: 0 0 6px rgba(0, 240, 255, 0.2);
    }

    tr {
      transition: background $transition-normal;

      &:hover > td {
        background: rgba(0, 240, 255, 0.03) !important;
        box-shadow: inset 0 0 20px rgba(0, 240, 255, 0.02);
      }
    }
  }

  // 投诉状态标签增强
  :deep(.el-tag) {
    backdrop-filter: blur(4px);
    transition: all $transition-normal;

    &.el-tag--success {
      background: rgba(57, 255, 20, 0.1);
      color: $neon-green;
      border-color: rgba(57, 255, 20, 0.3);
      box-shadow: 0 0 8px rgba(57, 255, 20, 0.15);
    }

    &.el-tag--danger {
      background: rgba(255, 45, 120, 0.1);
      color: $neon-pink;
      border-color: rgba(255, 45, 120, 0.3);
      box-shadow: 0 0 8px rgba(255, 45, 120, 0.15);
    }

    &.el-tag--warning {
      background: rgba(255, 230, 0, 0.1);
      color: $neon-yellow;
      border-color: rgba(255, 230, 0, 0.3);
      box-shadow: 0 0 8px rgba(255, 230, 0, 0.15);
    }

    &.el-tag--info {
      background: rgba(148, 163, 184, 0.08);
      color: $text-secondary;
      border-color: rgba(148, 163, 184, 0.2);
    }

    &.el-tag--primary {
      background: rgba(0, 240, 255, 0.1);
      color: $neon-cyan;
      border-color: rgba(0, 240, 255, 0.3);
      box-shadow: 0 0 8px rgba(0, 240, 255, 0.15);
    }
  }

  // 操作按钮
  :deep(.el-button--primary.is-link) {
    color: $neon-cyan;
    transition: all $transition-normal;

    &:hover {
      color: $neon-cyan;
      text-shadow: 0 0 8px rgba(0, 240, 255, 0.4);
    }
  }
}

.pagination-container {
  padding: $spacing-md;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid rgba(148, 163, 184, 0.06);
}
</style>
