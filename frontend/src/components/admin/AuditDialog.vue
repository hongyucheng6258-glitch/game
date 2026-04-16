<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="500px"
    @close="$emit('update:modelValue', false)"
  >
    <el-form :model="form" label-width="80px">
      <el-form-item label="审核结果">
        <el-radio-group v-model="form.approved">
          <el-radio :value="true">通过</el-radio>
          <el-radio :value="false">拒绝</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="审核备注">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="4"
          placeholder="请输入审核备注..."
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">确认提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  modelValue: boolean
  title?: string
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [data: { approved: boolean; remark: string }]
}>()

const visible = ref(props.modelValue)
const form = ref({
  approved: true,
  remark: '',
})

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    form.value = { approved: true, remark: '' }
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function handleSubmit() {
  emit('submit', { ...form.value })
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

// 对话框霓虹边框
:deep(.el-dialog) {
  --el-dialog-bg-color: #{$bg-card} !important;
  --el-dialog-border-color: rgba($neon-cyan, 0.15) !important;
  --el-dialog-title-color: #{$text-primary} !important;
  background: #{$bg-card} !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid rgba($neon-cyan, 0.15) !important;
  border-radius: $border-radius-xl !important;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5), 0 0 30px rgba($neon-cyan, 0.08);
  position: relative;
  overflow: hidden;

  // 顶部霓虹光线
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba($neon-cyan, 0.5), rgba($primary-color, 0.3), transparent);
    pointer-events: none;
  }
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid rgba($neon-cyan, 0.08) !important;
  background-color: #{$bg-card} !important;
  padding: $spacing-lg $spacing-xl;
  margin-right: 0;

  .el-dialog__title {
    font-family: 'Orbitron', 'Rajdhani', sans-serif;
    font-weight: 600;
    letter-spacing: 0.5px;
    background: linear-gradient(135deg, $neon-cyan, $primary-light);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

:deep(.el-dialog__body) {
  background-color: #{$bg-card} !important;
  color: $text-primary !important;
  padding: $spacing-lg $spacing-xl;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid rgba($neon-cyan, 0.08) !important;
  background-color: #{$bg-card} !important;
  padding: $spacing-md $spacing-xl;
}

// 表单标签赛博风格
:deep(.el-form-item__label) {
  color: $text-secondary !important;
  font-weight: 500;
  letter-spacing: 0.3px;
}

// 单选按钮霓虹效果
:deep(.el-radio-group) {
  .el-radio {
    color: $text-secondary;

    &.is-checked {
      .el-radio__inner {
        border-color: $neon-cyan !important;
        background: $neon-cyan !important;
        box-shadow: 0 0 8px rgba($neon-cyan, 0.4);
      }

      .el-radio__label {
        color: $neon-cyan !important;
      }
    }

    .el-radio__inner:hover {
      border-color: rgba($neon-cyan, 0.5);
    }
  }
}

// 文本域赛博风格
:deep(.el-textarea__inner) {
  background: $bg-input !important;
  border: 1px solid $border-color !important;
  color: $text-primary !important;
  border-radius: $border-radius;
  transition: border-color $transition-normal, box-shadow $transition-normal;

  &:focus {
    border-color: rgba($neon-cyan, 0.4) !important;
    box-shadow: 0 0 12px rgba($neon-cyan, 0.15);
  }

  &::placeholder {
    color: $text-muted;
  }
}

// 取消按钮
:deep(.el-button:not(.el-button--primary)) {
  background: rgba(30, 41, 59, 0.6) !important;
  border: 1px solid $border-color !important;
  color: $text-secondary !important;
  border-radius: $border-radius;
  transition: all $transition-fast;

  &:hover {
    border-color: rgba($neon-cyan, 0.3) !important;
    color: $text-primary !important;
    box-shadow: 0 0 10px rgba($neon-cyan, 0.1);
  }
}

// 确认按钮霓虹发光
:deep(.el-button--primary) {
  background: linear-gradient(135deg, $neon-cyan, $primary-color) !important;
  border: none !important;
  color: #06080f !important;
  font-weight: 600;
  border-radius: $border-radius;
  box-shadow: 0 2px 12px rgba($neon-cyan, 0.3);
  transition: all $transition-normal;

  &:hover {
    box-shadow: 0 4px 20px rgba($neon-cyan, 0.5), 0 0 30px rgba($neon-cyan, 0.2);
    transform: translateY(-1px);
    background: linear-gradient(135deg, lighten($neon-cyan, 5%), lighten($primary-color, 5%)) !important;
  }
}
</style>
