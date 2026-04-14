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
    background: linear-gradient(180deg, $primary-color, $primary-dark);
    border-radius: 2px;
  }
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
    transform: translateY(-2px);
  }

  .el-card__header {
    border-bottom: 1px solid rgba(148, 163, 184, 0.06) !important;
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

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  background: rgba(15, 23, 42, 0.6) !important;
  border-color: rgba(148, 163, 184, 0.06) !important;
  color: $text-primary;
  box-shadow: none !important;
  border-radius: $border-radius;
  transition: all $transition-fast;

  &:focus,
  &:focus-within {
    border-color: rgba($primary-color, 0.4) !important;
    box-shadow: 0 0 0 2px rgba($primary-color, 0.1) !important;
  }
}

:deep(.el-input-number) {
  .el-input__wrapper {
    background: rgba(15, 23, 42, 0.6) !important;
  }
}

// 按钮渐变效果
:deep(.el-button--primary) {
  background: linear-gradient(135deg, $primary-color, $primary-dark) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba($primary-color, 0.3);
  transition: all $transition-normal;

  &:hover {
    background: linear-gradient(135deg, $primary-light, $primary-color) !important;
    box-shadow: 0 4px 16px rgba($primary-color, 0.4);
    transform: translateY(-1px);
  }
}

// Switch 美化
:deep(.el-switch) {
  &.is-checked .el-switch__core {
    background-color: $primary-color !important;
    border-color: $primary-color !important;
  }
}

// Radio 美化
:deep(.el-radio) {
  .el-radio__input.is-checked .el-radio__inner {
    background-color: $primary-color !important;
    border-color: $primary-color !important;
  }

  .el-radio__input.is-checked + .el-radio__label {
    color: $primary-light !important;
  }
}

@media (max-width: 1024px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style>
