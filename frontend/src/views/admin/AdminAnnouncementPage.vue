<template>
  <div class="admin-announcement-page">
    <div class="page-header">
      <h2 class="page-title">公告管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 新建公告
      </el-button>
    </div>

    <!-- 搜索 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-input v-model="searchKeyword" placeholder="搜索公告标题" clearable style="width: 250px" @clear="fetchAnnouncements" @keyup.enter="fetchAnnouncements">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="fetchAnnouncements">搜索</el-button>
      </div>
    </el-card>

    <!-- 公告表格 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="announcements" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="isTop" label="置顶" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isTop ? 'danger' : 'info'" size="small">
              {{ row.isTop ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="row.status !== 1" size="small" type="success" @click="handlePublish(row)">发布</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="fetchAnnouncements"
          @current-change="fetchAnnouncements"
        />
      </div>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '新建公告'" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入公告内容" maxlength="5000" show-word-limit />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.isTop" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getAdminAnnouncements,
  createAnnouncement,
  updateAnnouncement,
  publishAnnouncement,
  deleteAnnouncement,
} from '@/api/admin'
import { formatDate } from '@/utils/format'

interface Announcement {
  id: number
  title: string
  content: string
  isTop: boolean
  status: number
  createdAt: string
}

const loading = ref(false)
const announcements = ref<Announcement[]>([])
const total = ref(0)
const searchKeyword = ref('')
const pagination = reactive({ page: 1, size: 10 })

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  title: '',
  content: '',
  isTop: false,
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

async function fetchAnnouncements() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: pagination.page, size: pagination.size }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await getAdminAnnouncements(params)
    announcements.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

function openDialog(item?: Announcement) {
  if (item) {
    isEdit.value = true
    editId.value = item.id
    form.title = item.title
    form.content = item.content
    form.isTop = item.isTop
  } else {
    isEdit.value = false
    editId.value = null
    form.title = ''
    form.content = ''
    form.isTop = false
  }
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateAnnouncement(editId.value, { ...form })
      ElMessage.success('修改成功')
    } else {
      await createAnnouncement({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchAnnouncements()
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handlePublish(item: Announcement) {
  try {
    await ElMessageBox.confirm('确定要发布该公告吗？', '提示', { type: 'info' })
    await publishAnnouncement(item.id)
    ElMessage.success('发布成功')
    fetchAnnouncements()
  } catch {
    // cancelled
  }
}

async function handleDelete(item: Announcement) {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '警告', { type: 'error' })
    await deleteAnnouncement(item.id)
    ElMessage.success('删除成功')
    fetchAnnouncements()
  } catch {
    // cancelled
  }
}

onMounted(() => fetchAnnouncements())
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.admin-announcement-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, $text-primary 0%, $primary-light 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.5px;
}

.filter-card {
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid rgba(148, 163, 184, 0.06) !important;
  border-radius: $border-radius-lg !important;
  transition: all $transition-normal;

  &:hover {
    border-color: rgba($primary-color, 0.2) !important;
    box-shadow: $shadow-glow;
  }

  :deep(.el-card__body) { padding: $spacing-md; }
}

.filter-row {
  display: flex;
  gap: $spacing-sm;
  align-items: center;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-md;
  padding-bottom: $spacing-sm;
}

// 卡片毛玻璃效果
:deep(.el-card) {
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid rgba(148, 163, 184, 0.06) !important;
  border-radius: $border-radius-lg !important;
  transition: all $transition-normal;

  &:hover {
    border-color: rgba($primary-color, 0.2) !important;
    box-shadow: $shadow-glow;
  }
}

// 表格暗色电竞风格
:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: #1e293b;
  --el-table-row-hover-bg-color: rgba($primary-color, 0.08);
  --el-table-border-color: rgba(148, 163, 184, 0.06);
  --el-table-text-color: #f1f5f9;
  --el-table-header-text-color: #94a3b8;
  border-radius: $border-radius-lg;
  overflow: hidden;

  .el-table__row--striped {
    background-color: rgba(30, 41, 59, 0.4) !important;
  }
}

:deep(.el-table__row) {
  transition: all $transition-fast;

  &:hover {
    background-color: rgba($primary-color, 0.08) !important;
  }
}

:deep(.el-table__header-wrapper) {
  th {
    background-color: #1e293b !important;
    font-weight: 600;
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
}

// 对话框美化
:deep(.el-dialog) {
  --el-dialog-bg-color: #0f172a !important;
  --el-dialog-border-color: rgba(148, 163, 184, 0.06) !important;
  --el-dialog-title-color: #f1f5f9 !important;
  background-color: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid rgba(148, 163, 184, 0.06) !important;
  border-radius: $border-radius-xl !important;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5), $shadow-glow;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(148, 163, 184, 0.06) !important;
  background-color: transparent !important;
  padding: $spacing-lg $spacing-xl;
  margin-right: 0;
}

:deep(.el-dialog__body) {
  background-color: transparent !important;
  color: $text-primary !important;
  padding: $spacing-lg $spacing-xl;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid rgba(148, 163, 184, 0.06) !important;
  background-color: transparent !important;
  padding: $spacing-md $spacing-xl;
}

// 按钮渐变效果
:deep(.el-button--primary) {
  background: linear-gradient(135deg, $primary-color, $primary-dark) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba($primary-color, 0.3);

  &:hover {
    background: linear-gradient(135deg, $primary-light, $primary-color) !important;
    box-shadow: 0 4px 16px rgba($primary-color, 0.4);
    transform: translateY(-1px);
  }
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #22c55e, #16a34a) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.3);

  &:hover {
    background: linear-gradient(135deg, #4ade80, #22c55e) !important;
    box-shadow: 0 4px 16px rgba(34, 197, 94, 0.4);
    transform: translateY(-1px);
  }
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #ef4444, #dc2626) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);

  &:hover {
    background: linear-gradient(135deg, #f87171, #ef4444) !important;
    box-shadow: 0 4px 16px rgba(239, 68, 68, 0.4);
    transform: translateY(-1px);
  }
}

// 分页器美化
:deep(.el-pagination) {
  .el-pager li {
    background: rgba(30, 41, 59, 0.6) !important;
    border: 1px solid rgba(148, 163, 184, 0.06) !important;
    color: $text-secondary !important;
    border-radius: $border-radius;
    transition: all $transition-fast;

    &:hover {
      color: $primary-light !important;
      border-color: rgba($primary-color, 0.3) !important;
    }

    &.is-active {
      background: linear-gradient(135deg, $primary-color, $primary-dark) !important;
      border-color: transparent !important;
      color: #fff !important;
      box-shadow: 0 2px 8px rgba($primary-color, 0.4);
    }
  }

  .btn-prev,
  .btn-next {
    background: rgba(30, 41, 59, 0.6) !important;
    border: 1px solid rgba(148, 163, 184, 0.06) !important;
    color: $text-secondary !important;
    border-radius: $border-radius;
  }
}
</style>
