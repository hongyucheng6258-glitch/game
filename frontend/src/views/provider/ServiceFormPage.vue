<template>
  <div class="service-form-page">
    <h2 class="page-title">{{ isEdit ? '编辑服务' : '发布服务' }}</h2>

    <el-card shadow="never">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="top"
        class="service-form"
      >
        <el-form-item label="服务标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入服务标题" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="游戏类型" prop="gameType">
          <el-select v-model="form.gameType" placeholder="请选择游戏类型" style="width: 100%">
            <el-option v-for="game in GAME_TYPES" :key="game" :label="game" :value="game" />
          </el-select>
        </el-form-item>

        <el-form-item label="服务类型" prop="serviceType">
          <el-radio-group v-model="form.serviceType">
            <el-radio-button :value="0">陪玩</el-radio-button>
            <el-radio-button :value="1">代打</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="服务价格（元/小时）" prop="price">
          <el-input-number
            v-model="form.price"
            :min="1"
            :precision="2"
            :step="1"
            controls-position="right"
            style="width: 100%"
          />
          <div v-if="levelInfo" class="price-limit-info">
            <el-tag type="info" size="small">{{ levelInfo.currentLevelName }}会员</el-tag>
            <span class="limit-text">最高可发布 ¥{{ maxPrice }} 的服务</span>
          </div>
          <el-alert
            v-if="isPriceExceeded"
            title="价格超出限制"
            type="error"
            :description="`您当前等级（${levelInfo?.currentLevelName}）最高可发布 ¥${maxPrice} 的服务，请降低价格或升级等级！`"
            show-icon
            class="price-exceed-alert"
          />
        </el-form-item>

        <el-form-item label="服务时长（分钟）" prop="duration">
          <el-select v-model="form.duration" placeholder="请选择服务时长" style="width: 100%">
            <el-option :value="30" label="30分钟" />
            <el-option :value="60" label="1小时" />
            <el-option :value="90" label="1.5小时" />
            <el-option :value="120" label="2小时" />
            <el-option :value="180" label="3小时" />
            <el-option :value="240" label="4小时" />
            <el-option :value="360" label="6小时" />
          </el-select>
        </el-form-item>

        <el-form-item label="服务描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请详细描述您的服务内容、优势、注意事项等"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="服务标签">
          <div class="tags-area">
            <el-tag
              v-for="tag in selectedTags"
              :key="tag"
              closable
              @close="removeTag(tag)"
              class="tag-item"
            >
              {{ tag }}
            </el-tag>
          </div>
          <div class="tag-selector">
            <el-checkbox-group v-model="selectedTagIds" @change="handleTagChange">
              <el-checkbox
                v-for="tag in availableTags"
                :key="tag.id"
                :value="tag.id"
                :disabled="!selectedTags.includes(tag.name) && selectedTags.length >= 5"
              >
                {{ tag.name }}
                <el-tag v-if="tag.useCount !== undefined && tag.useCount > 0" size="small" type="info" class="use-count">
                  {{ tag.useCount }}次使用
                </el-tag>
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button @click="$router.back()">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              {{ isEdit ? '保存修改' : '发布服务' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createService, updateService, getServiceDetail, getServiceTags } from '@/api/service'
import { getMyLevelInfo } from '@/api/level'
import type { UserLevelInfo } from '@/api/level'
import type { ServiceTag } from '@/types/service'
import { GAME_TYPES } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)

const availableTags = ref<ServiceTag[]>([])
const selectedTagIds = ref<number[]>([])
const selectedTags = ref<string[]>([])
const levelInfo = ref<UserLevelInfo | null>(null)

const isEdit = computed(() => !!route.params.id)
const serviceId = computed(() => Number(route.params.id))

const maxPrice = computed(() => {
  if (!levelInfo.value) return 9999
  return levelInfo.value.maxServicePrice
})

const isPriceExceeded = computed(() => {
  return form.price > maxPrice.value
})

const form = reactive({
  title: '',
  gameType: '',
  serviceType: 0,
  price: 50,
  duration: 60,
  description: '',
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入服务标题', trigger: 'blur' }],
  gameType: [{ required: true, message: '请选择游戏类型', trigger: 'change' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }],
  price: [
    { required: true, message: '请输入服务价格', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value > maxPrice.value) {
          callback(new Error(`您当前等级最高可发布${maxPrice.value}元的服务`))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  duration: [{ required: true, message: '请选择服务时长', trigger: 'change' }],
  description: [{ required: true, message: '请输入服务描述', trigger: 'blur' }],
}

async function fetchTags() {
  try {
    const res = await getServiceTags()
    availableTags.value = res.data || []
  } catch {
    ElMessage.error('获取标签列表失败')
  }
}

async function fetchUserLevelInfo() {
  try {
    const res = await getMyLevelInfo()
    levelInfo.value = res.data
  } catch {
    // ignore
  }
}

async function fetchServiceDetail() {
  if (!serviceId.value) return
  try {
    const res = await getServiceDetail(serviceId.value)
    const data = res.data
    form.title = data.title
    form.gameType = data.gameType
    form.serviceType = data.serviceType
    form.price = data.price
    form.duration = data.duration
    form.description = data.description
    
    // 设置标签
    selectedTags.value = data.tags || []
    
    // 同步选中的标签ID
    await fetchTags()
    selectedTagIds.value = availableTags.value
      .filter(tag => selectedTags.value.includes(tag.name))
      .map(tag => tag.id)
  } catch {
    ElMessage.error('获取服务信息失败')
  }
}

function handleTagChange() {
  // 根据选中的标签ID更新标签名称列表
  selectedTags.value = availableTags.value
    .filter(tag => selectedTagIds.value.includes(tag.id))
    .map(tag => tag.name)
}

function removeTag(tag: string) {
  selectedTags.value = selectedTags.value.filter(t => t !== tag)
  
  // 同步移除对应的标签ID
  const tagToRemove = availableTags.value.find(t => t.name === tag)
  if (tagToRemove) {
    selectedTagIds.value = selectedTagIds.value.filter(id => id !== tagToRemove.id)
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = { 
      ...form,
      tags: selectedTags.value,
      tagIds: selectedTagIds.value
    }
    if (isEdit.value) {
      await updateService(serviceId.value, data)
      ElMessage.success('修改成功')
    } else {
      await createService(data)
      ElMessage.success('发布成功！服务已提交审核，审核通过后将在平台显示')
    }
    router.push('/provider/services')
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await fetchTags()
  await fetchUserLevelInfo()
  if (isEdit.value) {
    fetchServiceDetail()
  }
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.service-form-page {
  max-width: 800px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  font-family: 'Orbitron', sans-serif;
  color: $text-primary;
  margin-bottom: $spacing-lg;
  background: linear-gradient(135deg, $neon-cyan 0%, $primary-light 50%, $neon-purple 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2px;
  text-transform: uppercase;
  position: relative;
  padding-bottom: $spacing-md;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 120px;
    height: 2px;
    background: linear-gradient(90deg, $neon-cyan, $primary-light, $neon-purple);
    box-shadow: 0 0 12px rgba(0, 240, 255, 0.5), 0 0 24px rgba(191, 90, 242, 0.3);
    border-radius: 2px;
  }
}

:deep(.el-card) {
  background: $glass-bg;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $border-glow;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-glow;
  position: relative;
  overflow: hidden;
  transition: border-color $transition-normal, box-shadow $transition-normal;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, $neon-cyan, $primary-light, $neon-purple);
    box-shadow: 0 0 12px rgba(0, 240, 255, 0.3);
  }

  &:hover {
    border-color: rgba(0, 240, 255, 0.25);
    box-shadow:
      0 0 30px rgba(0, 240, 255, 0.08),
      $shadow-glow;
  }
}

.service-form {
  :deep(.el-form-item__label) {
    color: $text-primary;
    font-weight: 500;
    letter-spacing: 0.3px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    background: $bg-input;
    border: 1px solid $border-color;
    border-radius: $border-radius;
    box-shadow: none;
    transition: all $transition-normal;

    &:hover {
      border-color: $border-color-hover;
    }

    &.is-focus,
    &:focus {
      border-color: rgba(0, 240, 255, 0.5);
      box-shadow:
        0 0 0 1px rgba(0, 240, 255, 0.2),
        0 0 12px rgba(0, 240, 255, 0.15),
        0 0 24px rgba(0, 240, 255, 0.05);
    }
  }

  :deep(.el-select .el-input__wrapper) {
    background: $bg-input;
    border: 1px solid $border-color;
    box-shadow: none;
    transition: all $transition-normal;

    &:hover {
      border-color: $border-color-hover;
    }

    &.is-focus {
      border-color: rgba(0, 240, 255, 0.5);
      box-shadow:
        0 0 0 1px rgba(0, 240, 255, 0.2),
        0 0 12px rgba(0, 240, 255, 0.15);
    }
  }

  :deep(.el-input-number .el-input__wrapper) {
    background: $bg-input;
    border: 1px solid $border-color;
    box-shadow: none;
    transition: all $transition-normal;

    &:hover {
      border-color: $border-color-hover;
    }

    &.is-focus {
      border-color: rgba(0, 240, 255, 0.5);
      box-shadow:
        0 0 0 1px rgba(0, 240, 255, 0.2),
        0 0 12px rgba(0, 240, 255, 0.15);
    }
  }

  :deep(.el-radio-button__inner) {
    background: $bg-input;
    border-color: $border-color;
    color: $text-secondary;
    transition: all $transition-normal;

    &:hover {
      color: $neon-cyan;
    }
  }

  :deep(.el-radio-button.is-active .el-radio-button__inner) {
    background: linear-gradient(135deg, $primary-color, $neon-cyan);
    border-color: rgba(0, 240, 255, 0.4);
    color: #fff;
    box-shadow: 0 0 12px rgba(0, 240, 255, 0.2);
  }
}

.tags-area {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
  align-items: center;
  margin-bottom: $spacing-md;
}

.tag-item {
  background: rgba(0, 240, 255, 0.1);
  border-color: rgba(0, 240, 255, 0.35);
  color: $neon-cyan;
  box-shadow: 0 0 8px rgba(0, 240, 255, 0.1);
  transition: all $transition-fast;

  &:hover {
    background: rgba(0, 240, 255, 0.18);
    box-shadow: 0 0 12px rgba(0, 240, 255, 0.2);
  }
}

.tag-selector {
  margin-top: $spacing-sm;
  padding: $spacing-md;
  background: rgba(0, 240, 255, 0.03);
  border: 1px solid rgba(0, 240, 255, 0.08);
  border-radius: $border-radius;
  transition: border-color $transition-normal;

  &:hover {
    border-color: rgba(0, 240, 255, 0.15);
  }

  :deep(.el-checkbox) {
    margin-right: $spacing-lg;
    margin-bottom: $spacing-sm;

    &:not(.is-disabled):hover {
      .el-checkbox__label {
        color: $neon-cyan;
      }
    }

    &.is-checked {
      .el-checkbox__label {
        color: $neon-cyan;
      }
    }
  }
}

.use-count {
  margin-left: $spacing-xs;
  font-size: 12px;
}

.form-actions {
  display: flex;
  gap: $spacing-md;
  width: 100%;
  justify-content: flex-end;

  :deep(.el-button--default) {
    background: rgba(51, 65, 85, 0.5);
    border: 1px solid $border-glow;
    color: $text-secondary;
    transition: all $transition-normal;

    &:hover {
      background: rgba(0, 240, 255, 0.08);
      color: $neon-cyan;
      border-color: rgba(0, 240, 255, 0.4);
      box-shadow: 0 0 12px rgba(0, 240, 255, 0.15);
      transform: translateY(-1px);
    }
  }

  :deep(.el-button--primary) {
    background: linear-gradient(135deg, $primary-color, $neon-cyan);
    border: 1px solid rgba(0, 240, 255, 0.3);
    box-shadow: 0 2px 8px rgba(0, 240, 255, 0.2);
    font-weight: 600;
    letter-spacing: 0.5px;
    transition: all $transition-normal;

    &:hover {
      box-shadow:
        0 0 16px rgba(0, 240, 255, 0.4),
        0 0 32px rgba(0, 240, 255, 0.15),
        0 0 48px rgba(99, 102, 241, 0.1);
      transform: translateY(-2px);
      border-color: rgba(0, 240, 255, 0.6);
    }
  }
}

.price-limit-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-top: $spacing-sm;

  :deep(.el-tag--info) {
    background: rgba(99, 102, 241, 0.15);
    border-color: rgba(99, 102, 241, 0.4);
    color: $primary-light;
  }
}

.limit-text {
  color: $text-secondary;
  font-size: 13px;
}

.price-exceed-alert {
  margin-top: 12px;

  :deep(.el-alert) {
    background: rgba(255, 45, 120, 0.08);
    border: 1px solid rgba(255, 45, 120, 0.25);
    border-radius: $border-radius;
  }
}
</style>
