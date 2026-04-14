<template>
  <div class="order-detail-page">
    <div v-loading="loading" class="detail-container">
      <template v-if="order">
        <h2 class="page-title">订单详情</h2>

        <!-- 订单状态步骤条 -->
        <section class="status-section">
          <el-card shadow="never">
            <div class="custom-steps">
              <div class="step-item" :class="{ 'step-finished': isStepFinished(0), 'step-active': isStepActive(0) }">
                <div class="step-icon">
                  <el-icon v-if="isStepFinished(0)" class="check-icon"><Check /></el-icon>
                  <span v-else>1</span>
                </div>
                <div class="step-content">
                  <div class="step-title">提交订单</div>
                  <div class="step-desc">{{ formatDate(order.createdAt) }}</div>
                </div>
              </div>
              <div class="step-line" :class="{ 'line-finished': isStepFinished(1) }"></div>
              <div class="step-item" :class="{ 'step-finished': isStepFinished(1), 'step-active': isStepActive(1) }">
                <div class="step-icon">
                  <el-icon v-if="isStepFinished(1)" class="check-icon"><Check /></el-icon>
                  <span v-else>2</span>
                </div>
                <div class="step-content">
                  <div class="step-title">支付完成</div>
                  <div class="step-desc">{{ order.paymentTime ? formatDate(order.paymentTime) : '' }}</div>
                </div>
              </div>
              <div class="step-line" :class="{ 'line-finished': isStepFinished(2) }"></div>
              <div class="step-item" :class="{ 'step-finished': isStepFinished(2), 'step-active': isStepActive(2) }">
                <div class="step-icon">
                  <el-icon v-if="isStepFinished(2)" class="check-icon"><Check /></el-icon>
                  <span v-else>3</span>
                </div>
                <div class="step-content">
                  <div class="step-title">开始服务</div>
                  <div class="step-desc">{{ order.startTime ? formatDate(order.startTime) : '' }}</div>
                </div>
              </div>
              <div class="step-line" :class="{ 'line-finished': isStepFinished(3) }"></div>
              <div class="step-item" :class="{ 'step-finished': isStepFinished(3), 'step-active': isStepActive(3) }">
                <div class="step-icon">
                  <el-icon v-if="isStepFinished(3)" class="check-icon"><Check /></el-icon>
                  <span v-else>4</span>
                </div>
                <div class="step-content">
                  <div class="step-title">服务完成</div>
                  <div class="step-desc">{{ order.endTime ? formatDate(order.endTime) : '' }}</div>
                </div>
              </div>
              <div class="step-line" :class="{ 'line-finished': isStepFinished(4) }"></div>
              <div class="step-item" :class="{ 'step-finished': isStepFinished(4), 'step-active': isStepActive(4) }">
                <div class="step-icon">
                  <el-icon v-if="isStepFinished(4)" class="check-icon"><Check /></el-icon>
                  <span v-else>5</span>
                </div>
                <div class="step-content">
                  <div class="step-title">评价完成</div>
                  <div class="step-desc"></div>
                </div>
              </div>
            </div>
          </el-card>
        </section>

        <!-- 订单信息 -->
        <section class="info-section">
          <el-card shadow="never">
            <template #header>
              <span class="card-header-title">订单信息</span>
            </template>
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">订单号</span>
                <span class="info-value">{{ order.orderNo }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">订单状态</span>
                <el-tag :type="ORDER_STATUS_TYPES[order.status]">
                  {{ ORDER_STATUS_LABELS[order.status] }}
                </el-tag>
              </div>
              <div class="info-item">
                <span class="info-label">服务名称</span>
                <span class="info-value">{{ order.serviceTitle || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">订单金额</span>
                <span class="info-value amount">{{ formatMoney(order.totalAmount) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">下单时间</span>
                <span class="info-value">{{ formatDate(order.createdAt) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">支付时间</span>
                <span class="info-value">{{ order.paymentTime ? formatDate(order.paymentTime) : '-' }}</span>
              </div>
              <div v-if="order.paymentMethod" class="info-item">
                <span class="info-label">支付方式</span>
                <span class="info-value">{{ getPaymentMethodLabel(order.paymentMethod) }}</span>
              </div>
              <div v-if="order.requirements" class="info-item full">
                <span class="info-label">服务要求</span>
                <span class="info-value">{{ order.requirements }}</span>
              </div>
            </div>
          </el-card>
        </section>

        <!-- 服务者/用户信息 -->
        <section class="user-section">
          <el-card shadow="never">
            <template #header>
              <span class="card-header-title">{{ isOrderProvider ? '下单用户信息' : '服务者信息' }}</span>
            </template>
            <div class="user-info">
              <span class="user-name">{{ (isOrderProvider ? order.userName : order.providerName) || '-' }}</span>
              <span class="user-id">ID: {{ isOrderProvider ? order.userId : order.providerId }}</span>
              <el-button type="primary" plain size="small" @click="handleChat">
                <el-icon><ChatDotRound /></el-icon> 联系TA
              </el-button>
            </div>
          </el-card>
        </section>

        <!-- 操作按钮 -->
        <section class="action-section">
          <div class="action-buttons">
            <!-- 用户操作 -->
            <template v-if="isOrderUser">
              <!-- 待支付 -->
              <template v-if="order.status === 0">
                <el-button @click="handleCancel">取消订单</el-button>
                <el-button type="primary" :loading="paying" @click="showPayDialog = true">
                  立即支付
                </el-button>
              </template>

              <!-- 待服务 -->
              <template v-else-if="order.status === 1">
                <el-button @click="handleCancel">取消订单</el-button>
              </template>

              <!-- 服务中 -->
              <template v-else-if="order.status === 2">
                <el-button disabled>等待服务者完成</el-button>
              </template>

              <!-- 待评价 -->
              <template v-else-if="order.status === 3">
                <el-button type="primary" @click="showReviewDialog = true">
                  去评价
                </el-button>
              </template>

              <!-- 已完成 -->
              <template v-else-if="order.status === 4">
                <el-button @click="$router.push('/order/list')">返回订单列表</el-button>
                <el-button type="warning" @click="handleCreateComplaint">投诉订单</el-button>
              </template>

              <!-- 已取消 -->
              <template v-else-if="order.status === 5">
                <el-button @click="$router.push('/service')">重新下单</el-button>
              </template>
            </template>

            <!-- 服务商操作 -->
            <template v-else-if="isOrderProvider">
              <!-- 待服务 -->
              <template v-if="order.status === 1">
                <el-button type="primary" @click="handleStartService">
                  开始服务
                </el-button>
              </template>

              <!-- 服务中 -->
              <template v-else-if="order.status === 2">
                <el-button type="success" @click="handleCompleteService">
                  完成服务
                </el-button>
              </template>

              <!-- 待评价 -->
              <template v-else-if="order.status === 3">
                <el-button disabled>等待用户确认</el-button>
              </template>

              <!-- 已完成 -->
              <template v-else-if="order.status === 4">
                <el-button @click="$router.push('/provider/orders')">返回接单列表</el-button>
              </template>

              <!-- 已取消 -->
              <template v-else-if="order.status === 5">
                <el-button @click="$router.push('/provider/orders')">返回接单列表</el-button>
              </template>

              <!-- 其他状态 -->
              <template v-else>
                <el-button @click="$router.push('/provider/orders')">返回接单列表</el-button>
              </template>
            </template>

            <!-- 既不是用户也不是服务商 -->
            <template v-else>
              <el-button @click="$router.back()">返回</el-button>
            </template>
          </div>
        </section>
      </template>

      <el-empty v-else-if="!loading" description="订单不存在">
        <el-button type="primary" @click="$router.push('/order/list')">返回订单列表</el-button>
      </el-empty>
    </div>

    <!-- 评价对话框 -->
    <el-dialog
      v-model="showReviewDialog"
      title="评价订单"
      width="480px"
      destroy-on-close
    >
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-position="top">
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="reviewForm.rating" :texts="['很差', '较差', '一般', '满意', '非常满意']" show-text />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入你的评价"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button type="primary" :loading="submittingReview" @click="handleSubmitReview">提交评价</el-button>
      </template>
    </el-dialog>

    <!-- 支付方式选择对话框 -->
    <el-dialog
      v-model="showPayDialog"
      title="选择支付方式"
      width="400px"
      destroy-on-close
    >
      <el-radio-group v-model="selectedPaymentMethod" class="payment-method-list">
        <el-radio
          v-for="method in PAYMENT_METHODS"
          :key="method.value"
          :value="method.value"
          class="payment-method-item"
        >
          <div class="payment-method-info">
            <span class="payment-method-label">{{ method.label }}</span>
          </div>
        </el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="showPayDialog = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="handleConfirmPay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { ChatDotRound, Check } from '@element-plus/icons-vue'
import { get, put, post } from '@/api/request'
import { useUserStore } from '@/stores/user'
import type { Order } from '@/types/order'
import { ORDER_STATUS, ORDER_STATUS_LABELS, ORDER_STATUS_TYPES, PAYMENT_METHODS } from '@/utils/constants'
import { formatMoney, formatDate } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const order = ref<Order | null>(null)
const paying = ref(false)

// 支付方式选择
const showPayDialog = ref(false)
const selectedPaymentMethod = ref<string>('balance')

const isProvider = computed(() => userStore.isProvider)
const isOrderProvider = computed(() => order.value && userStore.userInfo?.id === order.value.providerId)
const isOrderUser = computed(() => order.value && userStore.userInfo?.id === order.value.userId)

const showReviewDialog = ref(false)
const submittingReview = ref(false)
const reviewFormRef = ref<FormInstance>()
const reviewForm = reactive({
  rating: 5,
  content: '',
})
const reviewRules: FormRules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
  content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }],
}

function getPaymentMethodLabel(method?: string) {
  const paymentMethod = PAYMENT_METHODS.find(m => m.value === method)
  return paymentMethod?.label || method || '-'
}

const currentStep = computed(() => {
  if (!order.value) return 0
  const status = order.value.status
  if (status === ORDER_STATUS.PENDING_PAYMENT) return 0
  if (status === ORDER_STATUS.PENDING_SERVICE) return 1
  if (status === ORDER_STATUS.IN_SERVICE) return 2
  if (status === ORDER_STATUS.PENDING_REVIEW) return 3
  if (status === ORDER_STATUS.COMPLETED) return 4
  return 0
})

function isStepFinished(stepIndex: number) {
  if (!order.value) return false
  const status = order.value.status
  if (status === ORDER_STATUS.PENDING_PAYMENT) return stepIndex === 0
  if (status === ORDER_STATUS.PENDING_SERVICE) return stepIndex <= 1
  if (status === ORDER_STATUS.IN_SERVICE) return stepIndex <= 2
  if (status === ORDER_STATUS.PENDING_REVIEW) return stepIndex <= 3
  if (status === ORDER_STATUS.COMPLETED) return stepIndex <= 4
  return false
}

function isStepActive(stepIndex: number) {
  if (!order.value) return false
  const status = order.value.status
  if (status === ORDER_STATUS.COMPLETED) {
    return false
  }
  return stepIndex === currentStep.value
}

async function fetchOrder() {
  const orderNo = route.params.orderNo as string
  if (!orderNo) return

  loading.value = true
  try {
    const res = await get<Order>(`/orders/${orderNo}`)
    order.value = res.data
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function handleConfirmPay() {
  if (!order.value) return
  paying.value = true
  try {
    await put(`/orders/${order.value.orderNo}/pay?paymentMethod=${selectedPaymentMethod.value}`)
    ElMessage.success('支付成功')
    showPayDialog.value = false
    fetchOrder()
  } catch {
    // handled by interceptor
  } finally {
    paying.value = false
  }
}

async function handleCancel() {
  if (!order.value) return
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await put(`/orders/${order.value.orderNo}/cancel`)
    ElMessage.success('订单已取消')
    fetchOrder()
  } catch {
    // cancelled
  }
}

async function handleStartService() {
  if (!order.value) return
  try {
    await ElMessageBox.confirm('确定要开始服务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
    await put(`/orders/${order.value.orderNo}/start`)
    ElMessage.success('服务已开始')
    fetchOrder()
  } catch {
    // cancelled
  }
}

async function handleCompleteService() {
  if (!order.value) return
  try {
    await ElMessageBox.confirm('确认服务已完成吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
    await put(`/orders/${order.value.orderNo}/complete`)
    ElMessage.success('服务已完成')
    fetchOrder()
  } catch {
    // cancelled
  }
}

async function handleSubmitReview() {
  if (!reviewFormRef.value || !order.value) return
  await reviewFormRef.value.validate(async (valid) => {
    if (!valid) return

    submittingReview.value = true
    try {
      await post('/reviews', {
        orderId: order.value!.id,
        rating: reviewForm.rating,
        content: reviewForm.content,
      })
      ElMessage.success('评价成功')
      showReviewDialog.value = false
      fetchOrder()
    } catch {
      // handled by interceptor
    } finally {
      submittingReview.value = false
    }
  })
}

function handleChat() {
  if (order.value) {
    const targetUserId = isOrderProvider.value ? order.value.userId : order.value.providerId
    router.push(`/message/chat/${targetUserId}`)
  }
}

function handleCreateComplaint() {
  if (order.value) {
    router.push({
      name: 'ComplaintCreate',
      query: { orderId: order.value.id, orderNo: order.value.orderNo }
    })
  }
}

onMounted(() => {
  fetchOrder()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.order-detail-page {
  padding: $spacing-lg 0;
}

.detail-container {
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

// 自定义步骤条
.custom-steps {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0;
  position: relative;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  z-index: 1;
  position: relative;
}

.step-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  background: rgba(51, 65, 85, 0.6);
  color: #94a3b8;
  border: 2px solid rgba(71, 85, 105, 0.5);
  transition: all 0.3s;
  backdrop-filter: blur(4px);

  .check-icon {
    font-size: 20px;
  }
}

.step-item.step-finished .step-icon {
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: white;
  border-color: #22c55e;
  box-shadow: 0 0 16px rgba(34, 197, 94, 0.35);
}

.step-item.step-active .step-icon {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: white;
  border-color: #6366f1;
  animation: pulse 1.5s infinite;
  box-shadow: 0 0 20px rgba(99, 102, 241, 0.4);
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(99, 102, 241, 0.4);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(99, 102, 241, 0);
  }
}

.step-content {
  text-align: center;
}

.step-title {
  font-size: 14px;
  font-weight: 500;
  color: #94a3b8;
  margin-bottom: 4px;
  transition: color $transition-normal;
}

.step-item.step-finished .step-title,
.step-item.step-active .step-title {
  color: #f1f5f9;
  font-weight: 600;
}

.step-desc {
  font-size: 12px;
  color: #64748b;
}

.step-line {
  flex: 1;
  height: 3px;
  background: rgba(51, 65, 85, 0.6);
  margin: 0 -10px;
  margin-top: -45px;
  transition: background 0.3s;
  border-radius: 2px;
}

.step-line.line-finished {
  background: linear-gradient(90deg, #22c55e, #16a34a);
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.3);
}

// 信息网格
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  padding: $spacing-sm;
  border-radius: $border-radius;
  transition: background $transition-fast;

  &:hover {
    background: rgba(148, 163, 184, 0.04);
  }

  &.full {
    grid-column: 1 / -1;
  }
}

.info-label {
  color: $text-muted;
  font-size: 13px;
}

.info-value {
  color: $text-primary;
  font-size: 14px;

  &.amount {
    color: $danger-color;
    font-size: 18px;
    font-weight: 700;
    text-shadow: 0 0 16px rgba(239, 68, 68, 0.25);
  }
}

// 用户信息
.user-info {
  display: flex;
  align-items: center;
  gap: $spacing-md;

  :deep(.el-avatar) {
    box-shadow: 0 0 12px rgba(99, 102, 241, 0.2);
    border: 2px solid rgba(148, 163, 184, 0.1);
  }
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
}

.user-id {
  color: $text-muted;
  font-size: 13px;
}

// 操作按钮
.action-section {
  padding: $spacing-md 0;

  :deep(.el-button--primary) {
    background: linear-gradient(135deg, $primary-color, $primary-dark);
    border: none;
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.25);
    transition: all $transition-normal;

    &:hover {
      background: linear-gradient(135deg, $primary-light, $primary-color);
      box-shadow: 0 6px 20px rgba(99, 102, 241, 0.35);
      transform: translateY(-1px);
    }
  }
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}

// 响应式
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;

    .el-button {
      width: 100%;
    }
  }
}

// 支付方式选择
.payment-method-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  width: 100%;
}

.payment-method-item {
  padding: $spacing-md;
  border: 1px solid rgba(148, 163, 184, 0.06);
  border-radius: $border-radius;
  cursor: pointer;
  transition: all $transition-fast;
  background: rgba(148, 163, 184, 0.02);

  &:hover {
    border-color: $primary-color;
    background: rgba($primary-color, 0.05);
    box-shadow: 0 0 12px rgba(99, 102, 241, 0.1);
  }

  :deep(.el-radio__label) {
    width: 100%;
  }
}

.payment-method-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.payment-method-label {
  color: $text-primary;
  font-size: 14px;
  font-weight: 500;
}
</style>
