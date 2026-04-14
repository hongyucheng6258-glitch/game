<template>
  <div class="payment-method-selector">
    <el-radio-group v-model="selected" @change="$emit('update:modelValue', selected)">
      <div v-for="method in PAYMENT_METHODS" :key="method.value" class="method-item">
        <el-radio :value="method.value">
          <div class="method-content">
            <el-icon :size="20">
              <Wallet v-if="method.value === 'balance'" />
              <ChatDotRound v-else-if="method.value === 'wechat'" />
              <CreditCard v-else />
            </el-icon>
            <span class="method-label">{{ method.label }}</span>
          </div>
        </el-radio>
      </div>
    </el-radio-group>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Wallet, ChatDotRound, CreditCard } from '@element-plus/icons-vue'
import { PAYMENT_METHODS } from '@/utils/constants'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const selected = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  selected.value = val
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.payment-method-selector {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.method-item {
  padding: $spacing-sm $spacing-md;
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $border-radius;
  transition: border-color 0.2s;

  &:hover {
    border-color: $primary-color;
  }

  :deep(.el-radio) {
    width: 100%;
    margin-right: 0;
  }
}

.method-content {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.method-label {
  font-size: 14px;
  color: $text-primary;
}
</style>
