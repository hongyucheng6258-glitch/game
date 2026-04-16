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

// 页面标题 - Orbitron字体 + 霓虹渐变
.page-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, $neon-cyan 0%, $primary-light 50%, $neon-purple 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2px;
  text-transform: uppercase;
  position: relative;
  padding-bottom: $spacing-sm;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 60px;
    height: 2px;
    background: linear-gradient(90deg, $neon-cyan, transparent);
    box-shadow: 0 0 8px rgba($neon-cyan, 0.5);
  }
}

.tag-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  min-height: 200px;
}

// 标签项 - 霓虹边框
.tag-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md;
  border: 1px solid $glass-border;
  border-radius: $border-radius;
  transition: all $transition-normal;
  background: rgba(30, 41, 59, 0.3);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, $neon-cyan, transparent);
    opacity: 0;
    transition: opacity $transition-normal;
  }

  &:hover {
    background: rgba($neon-cyan, 0.04);
    border-color: $border-glow;
    transform: translateX(4px);
    box-shadow: $shadow-neon-cyan, $shadow-glow;

    &::before {
      opacity: 1;
    }
  }
}

.tag-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

// 标签颜色预览 - 对应颜色发光
:deep(.tag-info) {
  .service-tag {
    filter: drop-shadow(0 0 6px rgba($neon-cyan, 0.3));
    transition: all $transition-fast;

    &:hover {
      filter: drop-shadow(0 0 12px rgba($neon-cyan, 0.5));
    }
  }
}

.tag-id {
  font-size: 13px;
  color: $text-muted;
  font-family: 'Orbitron', monospace;
  letter-spacing: 0.5px;
}

.tag-actions {
  display: flex;
  gap: $spacing-xs;
}

// 卡片 - neon-border效果
:deep(.el-card) {
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $glass-border !important;
  border-radius: $border-radius-lg !important;
  transition: all $transition-normal;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, $neon-cyan, transparent);
    opacity: 0;
    transition: opacity $transition-normal;
    z-index: 1;
  }

  &:hover {
    border-color: $border-glow !important;
    box-shadow: $shadow-neon-cyan, $shadow-glow;

    &::before {
      opacity: 1;
    }
  }
}

// 对话框 - 霓虹边框
:deep(.el-dialog) {
  --el-dialog-bg-color: #0f172a !important;
  --el-dialog-border-color: $glass-border !important;
  --el-dialog-title-color: #f1f5f9 !important;
  background-color: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $glass-border !important;
  border-radius: $border-radius-xl !important;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5), $shadow-neon-cyan;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, $neon-cyan, $neon-purple, transparent);
    border-radius: $border-radius-xl $border-radius-xl 0 0;
  }
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid rgba($neon-cyan, 0.1) !important;
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
  border-top: 1px solid rgba($neon-cyan, 0.1) !important;
  background-color: transparent !important;
  padding: $spacing-md $spacing-xl;
}

// 搜索框 - focus时cyan发光
:deep(.el-input__wrapper) {
  background: rgba(15, 23, 42, 0.6) !important;
  border: 1px solid rgba(148, 163, 184, 0.08) !important;
  box-shadow: none !important;
  border-radius: $border-radius;
  transition: all $transition-fast;

  &:focus-within {
    border-color: rgba($neon-cyan, 0.5) !important;
    box-shadow: 0 0 0 2px rgba($neon-cyan, 0.1), 0 0 12px rgba($neon-cyan, 0.15) !important;
  }
}

// 操作按钮 - 霓虹发光hover
:deep(.el-button--primary) {
  background: linear-gradient(135deg, $primary-color, $primary-dark) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba($primary-color, 0.3);
  transition: all $transition-normal;

  &:hover {
    background: linear-gradient(135deg, $primary-light, $primary-color) !important;
    box-shadow: 0 0 16px rgba($primary-color, 0.5), 0 4px 16px rgba($primary-color, 0.3);
    transform: translateY(-1px);
  }
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #ef4444, #dc2626) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
  transition: all $transition-normal;

  &:hover {
    background: linear-gradient(135deg, #f87171, #ef4444) !important;
    box-shadow: 0 0 16px rgba(239, 68, 68, 0.5), 0 4px 16px rgba(239, 68, 68, 0.3);
    transform: translateY(-1px);
  }
}

// 状态标签 - 霓虹风格
:deep(.el-tag) {
  border: none;
  font-weight: 500;
  letter-spacing: 0.5px;
}

:deep(.el-tag--info) {
  background: rgba($neon-cyan, 0.12) !important;
  color: $neon-cyan !important;
  box-shadow: 0 0 8px rgba($neon-cyan, 0.2);
}
</style>
