<template>
  <div class="admin-settings-page">
    <h2 class="page-title">系统设置</h2>

    <div class="settings-grid">
      <!-- 基本设置 -->
      <el-card shadow="never">
        <template #header>
          <span class="card-title">基本设置</span>
        </template>
        <el-form label-width="140px" label-position="top">
          <el-form-item label="平台名称">
            <el-input v-model="settings.siteName" placeholder="请输入平台名称" />
          </el-form-item>
          <el-form-item label="平台描述">
            <el-input v-model="settings.siteDescription" type="textarea" :rows="3" placeholder="请输入平台描述" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 服务设置 -->
      <el-card shadow="never">
        <template #header>
          <span class="card-title">服务设置</span>
        </template>
        <el-form label-width="140px" label-position="top">
          <el-form-item label="平台抽成比例（%）">
            <el-input-number v-model="settings.commissionRate" :min="0" :max="100" :precision="1" :step="0.5" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="最低服务价格（元）">
            <el-input-number v-model="settings.minPrice" :min="1" :max="999" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="最高服务价格（元）">
            <el-input-number v-model="settings.maxPrice" :min="1" :max="999999" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="自动确认收货天数">
            <el-input-number v-model="settings.autoConfirmDays" :min="1" :max="30" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 提现设置 -->
      <el-card shadow="never">
        <template #header>
          <span class="card-title">提现设置</span>
        </template>
        <el-form label-width="140px" label-position="top">
          <el-form-item label="最低提现金额（元）">
            <el-input-number v-model="settings.minWithdrawAmount" :min="1" :max="10000" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="单笔最高提现（元）">
            <el-input-number v-model="settings.maxWithdrawAmount" :min="100" :max="500000" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="每日提现次数上限">
            <el-input-number v-model="settings.dailyWithdrawLimit" :min="1" :max="10" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="提现抽成比例（%）">
            <el-input-number v-model="settings.withdrawCommissionRate" :min="0" :max="100" :precision="1" :step="0.5" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 注册设置 -->
      <el-card shadow="never">
        <template #header>
          <span class="card-title">注册设置</span>
        </template>
        <el-form label-width="140px" label-position="top">
          <el-form-item label="允许注册">
            <el-switch v-model="settings.allowRegister" />
          </el-form-item>
          <el-form-item label="新用户默认角色">
            <el-radio-group v-model="settings.defaultRole">
              <el-radio :value="0">普通用户</el-radio>
              <el-radio :value="1">服务者</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 超时设置 -->
      <el-card shadow="never">
        <template #header>
          <span class="card-title">超时设置</span>
        </template>
        <el-form label-width="140px" label-position="top">
          <el-form-item label="订单支付超时（分钟）">
            <el-input-number v-model="settings.orderPaymentTimeoutMinutes" :min="5" :max="1440" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="等待服务超时（分钟）">
            <el-input-number v-model="settings.orderPendingServiceTimeoutMinutes" :min="5" :max="1440" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="服务超时（小时）">
            <el-input-number v-model="settings.serviceTimeoutHours" :min="1" :max="720" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 惩罚机制 -->
      <el-card shadow="never">
        <template #header>
          <span class="card-title">惩罚机制</span>
        </template>
        <el-form label-width="140px" label-position="top">
          <el-form-item label="启用服务者惩罚">
            <el-switch v-model="settings.providerPenaltyEnabled" />
          </el-form-item>
          <el-form-item label="信誉分扣除">
            <el-input-number v-model="settings.providerPenaltyScore" :min="0" :max="100" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="罚金金额（元）">
            <el-input-number v-model="settings.providerPenaltyAmount" :min="0" :max="1000" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { get, put } from '@/api/request'
import { useSystemStore } from '@/stores/system'

const systemStore = useSystemStore()
const saving = ref(false)

const settings = reactive({
  siteName: '电竞陪玩平台',
  siteDescription: '',
  commissionRate: 10,
  minPrice: 1,
  maxPrice: 999999,
  autoConfirmDays: 7,
  minWithdrawAmount: 10,
  maxWithdrawAmount: 50000,
  dailyWithdrawLimit: 3,
  withdrawCommissionRate: 5,
  allowRegister: true,
  defaultRole: 0,
  orderPaymentTimeoutMinutes: 30,
  orderPendingServiceTimeoutMinutes: 60,
  serviceTimeoutHours: 24,
  providerPenaltyEnabled: true,
  providerPenaltyScore: 5,
  providerPenaltyAmount: 10,
})

async function fetchSettings() {
  try {
    const res = await get<any>('/admin/settings')
    if (res.data) {
      Object.assign(settings, res.data)
      Object.assign(systemStore.settings, res.data)
    }
  } catch {
    // ignore
  }
}

async function handleSave() {
  saving.value = true
  try {
    await put('/admin/settings', { ...settings })
    Object.assign(systemStore.settings, settings)
    ElMessage.success('保存成功')
  } catch {
    // error handled by interceptor
  } finally {
    saving.value = false
  }
}

onMounted(() => fetchSettings())
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.admin-settings-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
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

.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
  position: relative;
  padding-left: 12px;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 16px;
    background: linear-gradient(180deg, $neon-cyan, $primary-color);
    border-radius: 2px;
    box-shadow: 0 0 8px rgba($neon-cyan, 0.4);
  }
}

// 设置项分组卡片 - 霓虹边框
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
    transform: translateY(-2px);

    &::before {
      opacity: 1;
    }
  }

  .el-card__header {
    border-bottom: 1px solid rgba($neon-cyan, 0.08) !important;
    padding: $spacing-md $spacing-lg;
  }

  .el-card__body {
    padding: $spacing-lg;
  }
}

:deep(.el-form-item__label) {
  color: $text-primary;
  font-weight: 500;
}

// 输入框 - focus时cyan发光
:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  background: rgba(15, 23, 42, 0.6) !important;
  border: 1px solid rgba(148, 163, 184, 0.08) !important;
  color: $text-primary;
  box-shadow: none !important;
  border-radius: $border-radius;
  transition: all $transition-fast;

  &:focus,
  &:focus-within {
    border-color: rgba($neon-cyan, 0.5) !important;
    box-shadow: 0 0 0 2px rgba($neon-cyan, 0.1), 0 0 12px rgba($neon-cyan, 0.15) !important;
  }
}

:deep(.el-input-number) {
  .el-input__wrapper {
    background: rgba(15, 23, 42, 0.6) !important;
  }
}

// 保存按钮 - 霓虹发光
:deep(.el-button--primary) {
  background: linear-gradient(135deg, $primary-color, $primary-dark) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba($primary-color, 0.3);
  transition: all $transition-normal;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    inset: -1px;
    border-radius: inherit;
    background: linear-gradient(135deg, $neon-cyan, $primary-color, $neon-purple);
    opacity: 0;
    z-index: -1;
    transition: opacity $transition-normal;
    filter: blur(4px);
  }

  &:hover {
    background: linear-gradient(135deg, $primary-light, $primary-color) !important;
    box-shadow: 0 0 20px rgba($primary-color, 0.5), 0 0 40px rgba($neon-cyan, 0.2);
    transform: translateY(-1px);

    &::after {
      opacity: 0.6;
    }
  }
}

// Switch - 霓虹风格
:deep(.el-switch) {
  &.is-checked .el-switch__core {
    background-color: $neon-cyan !important;
    border-color: $neon-cyan !important;
    box-shadow: 0 0 8px rgba($neon-cyan, 0.4);
  }
}

// Radio - 霓虹风格
:deep(.el-radio) {
  .el-radio__input.is-checked .el-radio__inner {
    background-color: $neon-cyan !important;
    border-color: $neon-cyan !important;
    box-shadow: 0 0 6px rgba($neon-cyan, 0.4);
  }

  .el-radio__input.is-checked + .el-radio__label {
    color: $neon-cyan !important;
  }
}

@media (max-width: 1024px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style>
