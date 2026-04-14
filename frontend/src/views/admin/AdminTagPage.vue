<template>
  <div class="admin-tag-page">
    <div class="page-header">
      <h2 class="page-title">标签管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 新建标签
      </el-button>
    </div>

    <!-- 标签列表 -->
    <el-card shadow="never">
      <div v-loading="loading" class="tag-list">
        <div v-for="tag in tags" :key="tag.id" class="tag-item">
          <div class="tag-info">
            <ServiceTag :label="tag.name" />
            <span class="tag-id">ID: {{ tag.id }}</span>
            <el-tag v-if="tag.useCount !== undefined" size="small" type="info">
              {{ tag.useCount }} 次使用
            </el-tag>
          </div>
          <div class="tag-actions">
            <el-button size="small" @click="openDialog(tag)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(tag)">删除</el-button>
          </div>
        </div>
        <el-empty v-if="tags.length === 0 && !loading" description="暂无标签" />
      </div>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑标签' : '新建标签'" width="400px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="60px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" maxlength="20" show-word-limit />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getAdminTags, createTag, updateTag, deleteTag } from '@/api/admin'
import type { ServiceTag } from '@/types/service'
import ServiceTagComponent from '@/components/business/ServiceTag.vue'

const ServiceTag = ServiceTagComponent

const loading = ref(false)
const tags = ref<ServiceTag[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({ name: '' })

const rules: FormRules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
}

async function fetchTags() {
  loading.value = true
  try {
    const res = await getAdminTags()
    tags.value = res.data || []
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

function openDialog(tag?: ServiceTag) {
  if (tag) {
    isEdit.value = true
    editId.value = tag.id
    form.value.name = tag.name
  } else {
    isEdit.value = false
    editId.value = null
    form.value.name = ''
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
      await updateTag(editId.value, { name: form.value.name })
      ElMessage.success('修改成功')
    } else {
      await createTag({ name: form.value.name })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchTags()
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(tag: ServiceTag) {
  try {
    await ElMessageBox.confirm(`确定要删除标签 "${tag.name}" 吗？`, '警告', { type: 'error' })
    await deleteTag(tag.id)
    ElMessage.success('删除成功')
    fetchTags()
  } catch {
    // cancelled
  }
}

onMounted(() => fetchTags())
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.admin-tag-page {
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

.tag-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  min-height: 200px;
}

.tag-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md;
  border: 1px solid rgba(148, 163, 184, 0.06);
  border-radius: $border-radius;
  transition: all $transition-normal;
  background: rgba(30, 41, 59, 0.3);

  &:hover {
    background: rgba($primary-color, 0.06);
    border-color: rgba($primary-color, 0.2);
    transform: translateX(4px);
    box-shadow: $shadow-glow;
  }
}

.tag-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.tag-id {
  font-size: 13px;
  color: $text-muted;
}

.tag-actions {
  display: flex;
  gap: $spacing-xs;
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
</style>
