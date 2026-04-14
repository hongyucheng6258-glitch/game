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
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  margin: 0;
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
  border: 1px solid $border-color;
  border-radius: $border-radius;
  transition: background 0.2s;

  &:hover {
    background: $bg-hover;
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
</style>
