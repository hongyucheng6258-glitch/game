<template>
  <div class="order-confirm-page">
    <div v-loading="loading" class="confirm-container">
      <template v-if="service">
        <h2 class="page-title">确认订单</h2>

        <!-- 服务信息 -->
        <section class="service-section">
          <el-card shadow="never">
            <template #header>
              <span class="card-header-title">服务信息</span>
            </template>
            <div class="service-info-row">
              <div class="service-detail">
                <h3 class="service-name">{{ service.title }}</h3>
                <div class="service-meta">
                  <el-tag type="primary" size="small">{{ service.gameType }}</el-tag>
                  <el-tag :type="service.serviceType === 0 ? 'success' : 'warning'" size="small">
                    {{ service.serviceType === 0 ? '陪玩' : '代打' }}
                  </el-tag>
                  <span class="duration-text">{{ formatDuration(service.duration) }}</span>
                </div>
              </div>
              <div class="service-price">
                <template v-if="negotiatedPrice">
                  <span class="original-price-small">{{ formatMoney(service.price) }}</span>
                  <span class="negotiated-price">{{ formatMoney(negotiatedPrice) }}</span>
                </template>
                <template v-else-if="service.activityId && service.activityPrice">
                  <span class="original-price-small">{{ formatMoney(service.price) }}</span>
                  <span class="negotiated-price">{{ formatMoney(service.activityPrice) }}</span>
                  <el-tag type="danger" size="small" effect="dark">{{ service.activityTitle }}</el-tag>
                </template>
                <template v-else-if="discountedPrice">
                  <span class="original-price-small">{{ formatMoney(service.price) }}</span>
                  <span class="negotiated-price">{{ formatMoney(discountedPrice) }}</span>
                  <el-tag type="success" size="small">{{ Math.round(levelInfo!.discountRate * 100) }}折</el-tag>
                </template>
                <span v-else>{{ formatMoney(service.price) }}</span>
              </div>
            </div>
          </el-card>
        </section>

        <!-- 服务要求 -->
        <section class="requirements-section">
          <el-card shadow="never">
            <template #header>
              <span class="card-header-title">服务要求</span>
            </template>
            <el-input
              v-model="formData.requirements"
              type="textarea"
              :rows="4"
              placeholder="请输入你的特殊要求，例如：希望使用语音沟通、指定英雄等"
              maxlength="500"
              show-word-limit
            />
          </el-card>
        </section>

        <!-- 游戏账号选择（代打时显示） -->
        <section v-if="service.serviceType === 1" class="account-section">
          <el-card shadow="never">
            <template #header>
              <div class="card-header-row">
                <span class="card-header-title">游戏账号</span>
                <el-button text type="primary" @click="openAccountDialog()">
                  <el-icon><Plus /></el-icon> 添加账号
                </el-button>
              </div>
            </template>
            <div v-loading="accountLoading" class="account-list">
              <template v-if="gameAccounts.length > 0">
                <el-radio-group v-model="formData.gameAccountId" class="account-radio-group">
                  <div
                    v-for="account in gameAccounts"
                    :key="account.id"
                    class="account-item"
                    :class="{ active: formData.gameAccountId === account.id }"
                    @click="formData.gameAccountId = account.id"
                  >
                    <el-radio :value="account.id" class="account-radio">
                      <div class="account-info">
                        <div class="account-row">
                          <span class="account-game">{{ account.gameType }}</span>
                          <el-tag size="small" type="info">{{ account.region || '未设置区服' }}</el-tag>
                          <el-tag v-if="account.accountLevel" size="small" type="success">等级：{{ account.accountLevel }}</el-tag>
                        </div>
                        <span class="account-name">账号：{{ account.accountName }}</span>
                        <span class="account-password">密码：{{ account.accountPassword ? '********' : '未设置' }}</span>
                        <span v-if="account.accountDescription" class="account-desc">备注：{{ account.accountDescription }}</span>
                      </div>
                    </el-radio>
                    <div class="account-actions">
                      <el-button text type="primary" size="small" @click.stop="openAccountDialog(account)">编辑</el-button>
                      <el-button text type="danger" size="small" @click.stop="handleDeleteAccount(account.id)">删除</el-button>
                    </div>
                  </div>
                </el-radio-group>
              </template>
              <el-empty v-else description="暂无游戏账号，请先添加" />
            </div>
          </el-card>
        </section>

        <!-- 金额展示 -->
        <section class="amount-section">
          <el-card shadow="never">
            <div class="amount-row">
              <span class="amount-label">服务金额</span>
              <span class="amount-value">
                <template v-if="negotiatedPrice || (service.activityId && service.activityPrice)">
                  <span class="original-price">{{ formatMoney(service.price) }}</span>
                </template>
                <template v-else-if="discountedPrice">
                  <span class="original-price">{{ formatMoney(service.price) }}</span>
                </template>
                <span v-else>{{ formatMoney(service.price) }}</span>
              </span>
            </div>
            <template v-if="service.activityId && service.activityPrice && !negotiatedPrice">
              <div class="amount-row">
                <span class="amount-label">活动优惠（{{ service.activityTitle }}）</span>
                <span class="amount-value discount">
                  -{{ formatMoney(service.price - service.activityPrice) }}
                </span>
              </div>
            </template>
            <template v-if="discountedPrice && !negotiatedPrice">
              <div class="amount-row">
                <span class="amount-label">等级折扣（{{ levelInfo!.currentLevelName }}）</span>
                <span class="amount-value discount">
                  -{{ formatMoney((service.activityId && service.activityPrice ? service.activityPrice : service.price) - discountedPrice) }}
                </span>
              </div>
            </template>
            <el-divider />
            <div class="amount-row total">
              <span class="amount-label">应付金额</span>
              <span class="amount-total">{{ formatMoney(discountedPrice || negotiatedPrice || (service.activityId && service.activityPrice ? service.activityPrice : service.price)) }}</span>
            </div>
          </el-card>
        </section>

        <!-- 提交按钮 -->
        <div class="submit-section">
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            class="submit-btn"
            @click="handleSubmit"
          >
            {{ submitting ? '提交中...' : '提交订单' }}
          </el-button>
        </div>
      </template>

      <el-empty v-else-if="!loading" description="服务不存在">
        <el-button type="primary" @click="$router.push('/service')">浏览服务</el-button>
      </el-empty>
    </div>

    <!-- 添加/编辑游戏账号对话框 -->
    <el-dialog
      v-model="showAddAccount"
      :title="isEditAccount ? '编辑游戏账号' : '添加游戏账号'"
      width="480px"
      destroy-on-close
    >
      <el-form
        ref="accountFormRef"
        :model="accountForm"
        :rules="accountRules"
        label-position="top"
      >
        <el-form-item label="游戏类型" prop="gameType">
          <el-select v-model="accountForm.gameType" placeholder="请选择游戏" style="width: 100%">
            <el-option
              v-for="game in GAME_TYPES"
              :key="game"
              :label="game"
              :value="game"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="游戏账号" prop="accountName">
          <el-input v-model="accountForm.accountName" placeholder="请输入游戏账号" />
        </el-form-item>
        <el-form-item label="游戏密码" prop="accountPassword">
          <el-input
            v-model="accountForm.accountPassword"
            type="password"
            placeholder="请输入游戏密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="账号等级" prop="accountLevel">
          <el-input v-model="accountForm.accountLevel" placeholder="请输入账号等级（选填）" clearable />
        </el-form-item>
        <el-form-item label="区服" prop="region">
          <el-input v-model="accountForm.region" placeholder="请输入区服（选填）" clearable />
        </el-form-item>
        <el-form-item label="备注" prop="accountDescription">
          <el-input
            v-model="accountForm.accountDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（选填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddAccount = false">取消</el-button>
        <el-button type="primary" :loading="addingAccount" @click="handleAddAccount">
          {{ isEditAccount ? '保存修改' : '确认添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Monitor } from '@element-plus/icons-vue'
import { get, post, put, del } from '@/api/request'
import { getMyLevelInfo } from '@/api/level'
import type { UserLevelInfo } from '@/api/level'
import type { Service } from '@/types/service'
import type { OrderCreateRequest } from '@/types/order'
import { GAME_TYPES } from '@/utils/constants'
import { formatMoney, formatDuration } from '@/utils/format'

interface GameAccount {
  id: number
  gameType: string
  accountName: string
  accountPassword: string
  accountLevel: string | null
  region: string | null
  accountDescription: string | null
}

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const service = ref<Service | null>(null)
const levelInfo = ref<UserLevelInfo | null>(null)

const formData = reactive({
  requirements: '',
  gameAccountId: undefined as number | undefined,
})

const negotiatedPrice = ref<number | null>(null)
const priceNegotiationMessageId = ref<number | null>(null)

const discountedPrice = computed(() => {
  if (!service.value || !levelInfo.value) return null
  const basePrice = negotiatedPrice.value || (service.value.activityId && service.value.activityPrice ? service.value.activityPrice : service.value.price)
  const discountRate = levelInfo.value.discountRate
  if (discountRate === 1) return null
  return Math.round(basePrice * discountRate * 100) / 100
})

// 游戏账号
const accountLoading = ref(false)
const gameAccounts = ref<GameAccount[]>([])
const showAddAccount = ref(false)
const isEditAccount = ref(false)
const editAccountId = ref<number | null>(null)
const addingAccount = ref(false)
const accountFormRef = ref<FormInstance>()
const accountForm = reactive({
  gameType: '',
  accountName: '',
  accountPassword: '',
  accountLevel: '',
  region: '',
  accountDescription: '',
})
const accountRules: FormRules = {
  gameType: [{ required: true, message: '请选择游戏类型', trigger: 'change' }],
  accountName: [{ required: true, message: '请输入游戏账号', trigger: 'blur' }],
  accountPassword: [{ required: true, message: '请输入游戏密码', trigger: 'blur' }],
}

async function fetchService() {
  const serviceId = route.params.serviceId as string
  if (!serviceId) return

  loading.value = true
  try {
    const res = await get<Service>(`/services/${serviceId}`)
    service.value = res.data
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
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

async function fetchGameAccounts() {
  accountLoading.value = true
  try {
    const res = await get<GameAccount[]>('/game-accounts')
    gameAccounts.value = res.data
  } catch {
    // ignore
  } finally {
    accountLoading.value = false
  }
}

function openAccountDialog(account?: GameAccount) {
  if (account) {
    isEditAccount.value = true
    editAccountId.value = account.id
    accountForm.gameType = account.gameType
    accountForm.accountName = account.accountName
    accountForm.accountPassword = ''
    accountForm.accountLevel = account.accountLevel || ''
    accountForm.region = account.region || ''
    accountForm.accountDescription = account.accountDescription || ''
  } else {
    isEditAccount.value = false
    editAccountId.value = null
    accountForm.gameType = ''
    accountForm.accountName = ''
    accountForm.accountPassword = ''
    accountForm.accountLevel = ''
    accountForm.region = ''
    accountForm.accountDescription = ''
  }
  showAddAccount.value = true
}

async function handleAddAccount() {
  if (!accountFormRef.value) return
  await accountFormRef.value.validate(async (valid) => {
    if (!valid) return

    addingAccount.value = true
    try {
      const data = {
        gameType: accountForm.gameType,
        accountName: accountForm.accountName,
        accountPassword: accountForm.accountPassword,
        accountLevel: accountForm.accountLevel || undefined,
        region: accountForm.region || undefined,
        accountDescription: accountForm.accountDescription || undefined,
      }

      if (isEditAccount.value && editAccountId.value) {
        await put(`/game-accounts/${editAccountId.value}`, data)
        ElMessage.success('修改成功')
      } else {
        await post('/game-accounts', data)
        ElMessage.success('添加成功')
      }
      showAddAccount.value = false
      fetchGameAccounts()
    } catch {
      // handled by interceptor
    } finally {
      addingAccount.value = false
    }
  })
}

async function handleDeleteAccount(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除该游戏账号吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await del(`/game-accounts/${id}`)
    ElMessage.success('删除成功')
    if (formData.gameAccountId === id) {
      formData.gameAccountId = undefined
    }
    fetchGameAccounts()
  } catch {
    // cancelled
  }
}

async function handleSubmit() {
  if (service.value?.serviceType === 1 && !formData.gameAccountId) {
    ElMessage.warning('代打服务请选择游戏账号')
    return
  }

  submitting.value = true
  try {
    const data: OrderCreateRequest = {
      serviceId: Number(route.params.serviceId),
      requirements: formData.requirements || undefined,
      gameAccountId: formData.gameAccountId,
      negotiatedPrice: negotiatedPrice.value || undefined,
      priceNegotiationMessageId: priceNegotiationMessageId.value || undefined,
    }
    const res = await post<{ orderNo: string }>('/orders', data)
    ElMessage.success('订单创建成功')
    router.push(`/order/${res.data.orderNo}`)
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // 从路由查询参数获取价格协商信息
  if (route.query.negotiatedPrice) {
    negotiatedPrice.value = Number(route.query.negotiatedPrice)
  }
  if (route.query.priceNegotiationMessageId) {
    priceNegotiationMessageId.value = Number(route.query.priceNegotiationMessageId)
  }
  fetchService()
  fetchGameAccounts()
  fetchUserLevelInfo()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.order-confirm-page {
  padding: $spacing-lg 0;
}

.confirm-container {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;

  :deep(.el-card) {
    background: $glass-bg;
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border: 1px solid $glass-border;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-glow;
    transition: box-shadow $transition-normal;

    &:hover {
      box-shadow: 0 0 30px rgba(99, 102, 241, 0.2);
    }
  }
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  background: linear-gradient(135deg, $text-primary, $primary-light);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-header-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
}

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

// 服务信息
.service-info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-md;
}

.service-detail {
  flex: 1;
}

.service-name {
  font-size: 18px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.service-meta {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.duration-text {
  color: $text-secondary;
  font-size: 14px;
}

.service-price {
  font-size: 24px;
  font-weight: 700;
  color: $danger-color;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  text-shadow: 0 0 16px rgba(239, 68, 68, 0.25);

  .original-price-small {
    font-size: 14px;
    text-decoration: line-through;
    color: $text-muted;
    font-weight: 400;
  }

  .negotiated-price {
    font-size: 24px;
    font-weight: 700;
    color: $danger-color;
  }
}

// 游戏账号
.account-list {
  min-height: 60px;
}

.account-radio-group {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  width: 100%;
}

.account-item {
  padding: $spacing-md;
  border: 1px solid rgba(148, 163, 184, 0.06);
  border-radius: $border-radius;
  cursor: pointer;
  transition: all $transition-fast;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  background: rgba(148, 163, 184, 0.02);

  &:hover,
  &.active {
    border-color: $primary-color;
    background: rgba($primary-color, 0.05);
    box-shadow: 0 0 12px rgba(99, 102, 241, 0.1);
  }
}

.account-radio {
  width: 100%;
  margin-right: 0;
}

.account-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.account-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.account-game {
  color: $primary-light;
  font-weight: 500;
  font-size: 14px;
}

.account-name,
.account-password,
.account-desc {
  font-size: 13px;
  color: $text-secondary;
}

.account-desc {
  margin-top: 4px;
  font-style: italic;
}

.account-actions {
  display: flex;
  gap: $spacing-sm;
  flex-shrink: 0;
}

// 金额
.amount-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-xs 0;
}

.amount-label {
  color: $text-secondary;
  font-size: 14px;
}

.amount-value {
  color: $text-primary;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: $spacing-sm;

  .original-price {
    text-decoration: line-through;
    color: $text-muted;
  }

  &.discount {
    color: $success-color;
    font-weight: 600;
  }
}

.amount-row.total {
  .amount-label {
    font-size: 16px;
    font-weight: 600;
    color: $text-primary;
  }
}

.amount-total {
  font-size: 28px;
  font-weight: 700;
  color: $danger-color;
  text-shadow: 0 0 20px rgba(239, 68, 68, 0.3);
}

// 提交
.submit-section {
  padding: $spacing-lg 0;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: $border-radius-lg;
  background: linear-gradient(135deg, $primary-color, $primary-dark);
  border: none;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.35);
  transition: all $transition-normal;
  letter-spacing: 1px;

  &:hover {
    background: linear-gradient(135deg, $primary-light, $primary-color);
    box-shadow: 0 6px 30px rgba(99, 102, 241, 0.45);
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(0);
  }
}

// 响应式
@media (max-width: 768px) {
  .service-info-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
