<template>
  <div class="admin-activity-page">
    <div class="page-header">
      <h2 class="page-title">活动管理</h2>
      <div class="header-actions">
        <el-button @click="handleUpdateStatuses">
          <el-icon><Refresh /></el-icon> 刷新状态
        </el-button>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon> 新建活动
        </el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="activities" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="活动标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="typeTagColor(row.type)" size="small">
              {{ typeLabels[row.type] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discountRate" label="折扣率" width="100">
          <template #default="{ row }">
            <span v-if="row.type !== 2">{{ (row.discountRate * 100).toFixed(0) }}折</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagColor(row.status)" size="small">
              {{ statusLabels[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170">
          <template #default="{ row }">{{ formatDate(row.startTime) }}</template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="170">
          <template #default="{ row }">{{ formatDate(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="row.status === 0" size="small" type="success" @click="handlePublish(row)">上线</el-button>
            <el-button v-if="row.status === 1" size="small" type="warning" @click="handleEnd(row)">结束</el-button>
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
          @size-change="fetchActivities"
          @current-change="fetchActivities"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑活动' : '新建活动'" width="700px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入活动标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="活动描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入活动描述" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="活动类型" prop="type">
          <el-radio-group v-model="form.type" @change="handleTypeChange">
            <el-radio-button :value="0">全场折扣</el-radio-button>
            <el-radio-button :value="1">指定服务折扣</el-radio-button>
            <el-radio-button :value="2">指定服务特价</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.type !== 2" label="折扣率" prop="discountRate">
          <el-input-number v-model="form.discountRate" :min="0.01" :max="0.99" :step="0.01" :precision="2" />
          <span class="form-tip">（如0.8表示8折）</span>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.type !== 0" label="选择服务">
          <div class="service-select-area">
            <el-button size="small" type="primary" @click="showServicePicker = true">添加服务</el-button>
            <div v-if="form.services.length > 0" class="selected-services">
              <div v-for="(item, index) in form.services" :key="item.serviceId" class="selected-service-item">
                <span class="service-name">{{ item.serviceTitle || `服务#${item.serviceId}` }}</span>
                <span v-if="item.servicePrice" class="service-price">原价: ¥{{ item.servicePrice }}</span>
                <el-input-number
                  v-if="form.type === 2"
                  v-model="item.specialPrice"
                  :min="0.01"
                  :precision="2"
                  :step="1"
                  size="small"
                  placeholder="特价"
                  style="width: 150px; margin-left: 8px"
                />
                <el-button size="small" type="danger" text @click="removeService(index)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <el-empty v-else description="暂未选择服务" :image-size="40" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showServicePicker" title="选择服务" width="600px">
      <div class="service-picker">
        <el-input v-model="serviceSearchKeyword" placeholder="搜索服务" clearable style="margin-bottom: 12px" @keyup.enter="fetchServicesForPicker" />
        <div v-loading="pickerLoading" class="service-picker-list">
          <div
            v-for="s in pickerServices"
            :key="s.id"
            class="picker-service-item"
            :class="{ selected: isServiceSelected(s.id) }"
            @click="toggleServiceSelection(s)"
          >
            <div class="picker-service-info">
              <span class="picker-service-title">{{ s.title }}</span>
              <span class="picker-service-price">¥{{ s.price }}</span>
            </div>
            <el-icon v-if="isServiceSelected(s.id)" class="picker-check"><Check /></el-icon>
          </div>
          <el-empty v-if="pickerServices.length === 0" description="暂无服务" :image-size="40" />
        </div>
      </div>
      <template #footer>
        <el-button @click="showServicePicker = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Delete, Check } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getAdminActivities,
  getAdminActivity,
  createActivity,
  updateActivity,
  publishActivity,
  endActivity,
  deleteActivity,
  updateActivityStatuses,
  getAdminServices,
} from '@/api/admin'
import { formatDate } from '@/utils/format'

interface ActivityItem {
  id: number
  title: string
  description: string
  type: number
  discountRate: number
  startTime: string
  endTime: string
  status: number
  image?: string
}

interface ServiceItem {
  serviceId: number
  serviceTitle: string
  servicePrice: number
  specialPrice?: number
}

const typeLabels: Record<number, string> = { 0: '全场折扣', 1: '指定服务折扣', 2: '指定服务特价' }
const statusLabels: Record<number, string> = { 0: '未开始', 1: '进行中', 2: '已结束' }

const loading = ref(false)
const activities = ref<ActivityItem[]>([])
const total = ref(0)
const pagination = reactive({ page: 1, size: 10 })

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  title: '',
  description: '',
  type: 0,
  discountRate: 0.8,
  startTime: '' as any,
  endTime: '' as any,
  image: '',
  services: [] as ServiceItem[],
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  discountRate: [{ required: true, message: '请设置折扣率', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
}

const showServicePicker = ref(false)
const serviceSearchKeyword = ref('')
const pickerLoading = ref(false)
const pickerServices = ref<any[]>([])

function typeTagColor(type: number) {
  if (type === 0) return 'danger'
  if (type === 1) return 'warning'
  return 'success'
}

function statusTagColor(status: number) {
  if (status === 0) return 'info'
  if (status === 1) return 'success'
  return 'danger'
}

async function fetchActivities() {
  loading.value = true
  try {
    const res = await getAdminActivities({ page: pagination.page, size: pagination.size })
    activities.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

async function openDialog(item?: ActivityItem) {
  if (item) {
    isEdit.value = true
    editId.value = item.id
    const res = await getAdminActivity(item.id)
    const detail = res.data
    form.title = detail.title
    form.description = detail.description || ''
    form.type = detail.type
    form.discountRate = detail.discountRate || 0.8
    form.startTime = detail.startTime
    form.endTime = detail.endTime
    form.image = detail.image || ''
    form.services = detail.services || []
  } else {
    isEdit.value = false
    editId.value = null
    form.title = ''
    form.description = ''
    form.type = 0
    form.discountRate = 0.8
    form.startTime = ''
    form.endTime = ''
    form.image = ''
    form.services = []
  }
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

function handleTypeChange() {
  if (form.type === 0) {
    form.services = []
  }
}

function isServiceSelected(serviceId: number) {
  return form.services.some(s => s.serviceId === serviceId)
}

function toggleServiceSelection(s: any) {
  const idx = form.services.findIndex(item => item.serviceId === s.id)
  if (idx >= 0) {
    form.services.splice(idx, 1)
  } else {
    form.services.push({
      serviceId: s.id,
      serviceTitle: s.title,
      servicePrice: s.price,
      specialPrice: s.price,
    })
  }
}

function removeService(index: number) {
  form.services.splice(index, 1)
}

async function fetchServicesForPicker() {
  pickerLoading.value = true
  try {
    const params: Record<string, any> = { page: 1, size: 50, includeAllStatus: true }
    if (serviceSearchKeyword.value) params.keyword = serviceSearchKeyword.value
    const res = await getAdminServices(params)
    pickerServices.value = res.data?.records || []
  } catch {
    // ignore
  } finally {
    pickerLoading.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (form.type !== 0 && form.services.length === 0) {
    ElMessage.warning('请选择参与活动的服务')
    return
  }

  if (form.type === 2) {
    const hasEmptyPrice = form.services.some(s => !s.specialPrice || s.specialPrice <= 0)
    if (hasEmptyPrice) {
      ElMessage.warning('请为所有服务设置特价')
      return
    }
  }

  submitting.value = true
  try {
    const data: Record<string, any> = {
      title: form.title,
      description: form.description,
      type: form.type,
      discountRate: form.type !== 2 ? form.discountRate : null,
      startTime: form.startTime,
      endTime: form.endTime,
      image: form.image || null,
      services: form.type !== 0 ? form.services.map(s => ({
        serviceId: s.serviceId,
        specialPrice: form.type === 2 ? s.specialPrice : null,
      })) : null,
    }

    if (isEdit.value && editId.value) {
      await updateActivity(editId.value, data)
      ElMessage.success('修改成功')
    } else {
      await createActivity(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchActivities()
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handlePublish(item: ActivityItem) {
  try {
    await ElMessageBox.confirm('确定要上线该活动吗？', '提示', { type: 'info' })
    await publishActivity(item.id)
    ElMessage.success('上线成功')
    fetchActivities()
  } catch {
    // cancelled
  }
}

async function handleEnd(item: ActivityItem) {
  try {
    await ElMessageBox.confirm('确定要结束该活动吗？', '警告', { type: 'warning' })
    await endActivity(item.id)
    ElMessage.success('活动已结束')
    fetchActivities()
  } catch {
    // cancelled
  }
}

async function handleDelete(item: ActivityItem) {
  try {
    await ElMessageBox.confirm('确定要删除该活动吗？', '警告', { type: 'error' })
    await deleteActivity(item.id)
    ElMessage.success('删除成功')
    fetchActivities()
  } catch {
    // cancelled
  }
}

async function handleUpdateStatuses() {
  try {
    await updateActivityStatuses()
    ElMessage.success('状态已刷新')
    fetchActivities()
  } catch {
    // ignore
  }
}

onMounted(() => fetchActivities())
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.admin-activity-page {
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

.header-actions {
  display: flex;
  gap: $spacing-sm;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-md;
}

.form-tip {
  margin-left: 8px;
  color: $text-secondary;
  font-size: 13px;
}

.service-select-area {
  width: 100%;
}

.selected-services {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.selected-service-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: $bg-hover;
  border-radius: $border-radius;

  .service-name {
    font-weight: 500;
    color: $text-primary;
  }

  .service-price {
    color: $text-secondary;
    font-size: 13px;
  }
}

.service-picker-list {
  max-height: 400px;
  overflow-y: auto;
}

.picker-service-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border: 1px solid $border-color;
  border-radius: $border-radius;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: $primary-color;
  }

  &.selected {
    border-color: $primary-color;
    background: rgba($primary-color, 0.1);
  }
}

.picker-service-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.picker-service-title {
  font-weight: 500;
  color: $text-primary;
}

.picker-service-price {
  color: $warning-color;
  font-size: 14px;
}

.picker-check {
  color: $primary-color;
  font-size: 18px;
}

:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: #1e293b;
  --el-table-row-hover-bg-color: #334155;
  --el-table-border-color: #334155;
  --el-table-text-color: #f1f5f9;
  --el-table-header-text-color: #94a3b8;
}
</style>
