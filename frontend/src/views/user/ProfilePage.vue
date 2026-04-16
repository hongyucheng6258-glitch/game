<template>
  <div class="profile-page">
    <h2 class="page-title">个人中心</h2>

    <div class="profile-container">
      <div class="profile-sidebar">
        <el-card shadow="never" class="user-info-card">
          <div class="user-avatar-section">
            <el-avatar :size="100" :src="getAvatarUrl(userInfo?.avatar)" class="user-avatar">
              {{ userInfo?.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <div class="user-basic-info">
              <h3 class="user-name">{{ userInfo?.username }}</h3>
              <el-tag :type="roleTagType" size="small">{{ roleText }}</el-tag>
            </div>
          </div>
          <div class="user-status-section">
            <div class="status-item">
              <span class="status-label">账户状态</span>
              <el-tag :type="statusTagType" size="small">{{ statusText }}</el-tag>
            </div>
            <div class="status-item">
              <span class="status-label">信誉分</span>
              <el-tag :type="creditScoreTagType" size="small">{{ userInfo?.creditScore ?? 100 }}</el-tag>
            </div>
            <div class="status-item">
              <span class="status-label">实名认证</span>
              <el-tag :type="verifiedTagType" size="small">{{ verifiedText }}</el-tag>
            </div>
            <div class="status-item">
              <span class="status-label">注册时间</span>
              <span class="status-value">{{ formatDate(userInfo?.createdAt) }}</span>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="stats-card">
          <h4 class="stats-title">账户统计</h4>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ formatMoney(userInfo?.balance || 0) }}</div>
              <div class="stat-label">账户余额</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.orderCount || 0 }}</div>
              <div class="stat-label">订单数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.serviceCount || 0 }}</div>
              <div class="stat-label">服务数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.reviewCount || 0 }}</div>
              <div class="stat-label">评价数</div>
            </div>
          </div>
        </el-card>
      </div>

      <div class="profile-content">
        <el-tabs v-model="activeTab" class="profile-tabs" type="border-card">
          <el-tab-pane label="基本信息" name="info">
            <el-card shadow="never" class="profile-card">
              <div class="avatar-section">
                <el-avatar :size="80" :src="getAvatarUrl(formData.avatar)">
                  {{ formData.nickname?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <el-upload
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :http-request="handleAvatarUpload"
                  accept="image/*"
                >
                  <el-button size="small" type="primary" plain>更换头像</el-button>
                </el-upload>
              </div>

              <el-form
                ref="formRef"
                :model="formData"
                :rules="formRules"
                label-position="top"
                size="large"
              >
                <el-form-item label="用户名" prop="nickname">
                  <el-input v-model="formData.nickname" placeholder="请输入用户名" clearable />
                </el-form-item>

                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="formData.phone" placeholder="请输入手机号" clearable />
                </el-form-item>

                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="formData.email" placeholder="请输入邮箱" clearable />
                </el-form-item>

                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="formData.realName" placeholder="请输入真实姓名（选填）" clearable />
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" :loading="saving" @click="handleSave">
                    {{ saving ? '保存中...' : '保存修改' }}
                  </el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="账户安全" name="security">
            <el-card shadow="never" class="security-card">
              <div class="security-item">
                <div class="security-info">
                  <el-icon class="security-icon"><Lock /></el-icon>
                  <div class="security-text">
                    <h4>登录密码</h4>
                    <p>定期更换密码，保护账户安全</p>
                  </div>
                </div>
                <el-button type="primary" plain @click="showPasswordDialog = true">修改密码</el-button>
              </div>

              <div class="security-item">
                <div class="security-info">
                  <el-icon class="security-icon"><Phone /></el-icon>
                  <div class="security-text">
                    <h4>绑定手机</h4>
                    <p>{{ formData.phone ? '已绑定' : '未绑定' }} - 用于找回密码和接收通知</p>
                  </div>
                </div>
                <el-button 
                  type="primary" 
                  plain 
                  :disabled="!!formData.phone"
                  @click="handleBindPhone"
                >
                  {{ formData.phone ? '已绑定' : '去绑定' }}
                </el-button>
              </div>

              <div class="security-item">
                <div class="security-info">
                  <el-icon class="security-icon"><Message /></el-icon>
                  <div class="security-text">
                    <h4>绑定邮箱</h4>
                    <p>{{ formData.email ? '已绑定' : '未绑定' }} - 用于接收重要通知</p>
                  </div>
                </div>
                <el-button 
                  type="primary" 
                  plain 
                  :disabled="!!formData.email"
                  @click="handleBindEmail"
                >
                  {{ formData.email ? '已绑定' : '去绑定' }}
                </el-button>
              </div>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="实名认证" name="verify">
            <el-card shadow="never" class="verify-card">
              <div class="verify-content">
                <div v-if="isVerified" class="verify-success">
                  <el-icon class="verify-icon" color="#67C23A"><CircleCheck /></el-icon>
                  <h3>已完成实名认证</h3>
                  <p>您的真实姓名：{{ userInfo?.realName || '---' }}</p>
                  <el-button type="primary" plain @click="showVerifyDetail = true">查看详情</el-button>
                </div>
                <div v-else class="verify-pending">
                  <el-icon class="verify-icon" color="#E6A23C"><Warning /></el-icon>
                  <h3>未完成实名认证</h3>
                  <p>完成实名认证后，即可享受以下特权：</p>
                  <ul class="privilege-list">
                    <li>发布服务，成为服务者</li>
                    <li>享受更高的交易限额</li>
                    <li>获得更安全的账户保障</li>
                  </ul>
                  <el-button type="primary" @click="showVerifyDialog = true">去认证</el-button>
                </div>
              </div>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <el-dialog v-model="showPasswordDialog" title="修改密码" width="450px" destroy-on-close>
      <el-form
        ref="passwordFormRef"
        :model="passwordFormData"
        :rules="passwordFormRules"
        label-position="top"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordFormData.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordFormData.newPassword"
            type="password"
            placeholder="请输入新密码（6-100个字符）"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordFormData.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="handlePasswordUpdate">
          确认修改
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showVerifyDialog" title="实名认证" width="500px" destroy-on-close>
      <el-alert
        title="提示"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      >
        请确保您填写的信息真实有效，虚假信息将影响您的账户使用。
      </el-alert>
      <el-form
        ref="verifyFormRef"
        :model="verifyFormData"
        :rules="verifyFormRules"
        label-position="top"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="verifyFormData.realName"
            placeholder="请输入您的真实姓名"
          />
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input
            v-model="verifyFormData.idCard"
            placeholder="请输入您的身份证号"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="verifyFormData.phone"
            placeholder="请输入您的手机号"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showVerifyDialog = false">取消</el-button>
        <el-button type="primary" :loading="verifyLoading" @click="handleVerify">
          提交认证
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showBindPhoneDialog" title="绑定手机" width="450px" destroy-on-close>
      <el-form
        ref="bindPhoneFormRef"
        :model="bindPhoneFormData"
        :rules="bindPhoneFormRules"
        label-position="top"
      >
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="bindPhoneFormData.phone"
            placeholder="请输入手机号"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBindPhoneDialog = false">取消</el-button>
        <el-button type="primary" :loading="bindPhoneLoading" @click="handleBindPhoneSubmit">
          确认绑定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showBindEmailDialog" title="绑定邮箱" width="450px" destroy-on-close>
      <el-form
        ref="bindEmailFormRef"
        :model="bindEmailFormData"
        :rules="bindEmailFormRules"
        label-position="top"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="bindEmailFormData.email"
            placeholder="请输入邮箱"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBindEmailDialog = false">取消</el-button>
        <el-button type="primary" :loading="bindEmailLoading" @click="handleBindEmailSubmit">
          确认绑定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showVerifyDetail" title="实名认证详情" width="500px" destroy-on-close>
      <div class="verify-detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="真实姓名">
            {{ userInfo?.realName || '---' }}
          </el-descriptions-item>
          <el-descriptions-item label="认证状态">
            <el-tag type="success">已认证</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="认证时间">
            {{ formatDate(userInfo?.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          style="margin-top: 20px;"
        >
          实名认证信息已加密保存，仅用于身份验证和账户安全保障。
        </el-alert>
      </div>
      <template #footer>
        <el-button type="primary" @click="showVerifyDetail = false">
          关闭
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadRequestOptions } from 'element-plus'
import { Lock, Phone, Message, CircleCheck, Warning } from '@element-plus/icons-vue'
import { put, post, get } from '@/api/request'
import { useUserStore } from '@/stores/user'
import { isValidPhone, isValidEmail, isValidIdCard } from '@/utils/validate'
import { getAvatarUrl } from '@/utils/avatar'
import type { PasswordUpdateRequest, User } from '@/types/user'

const userStore = useUserStore()

const activeTab = ref('info')
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const verifyFormRef = ref<FormInstance>()
const bindPhoneFormRef = ref<FormInstance>()
const bindEmailFormRef = ref<FormInstance>()
const saving = ref(false)
const passwordLoading = ref(false)
const verifyLoading = ref(false)
const bindPhoneLoading = ref(false)
const bindEmailLoading = ref(false)
const showPasswordDialog = ref(false)
const showVerifyDialog = ref(false)
const showVerifyDetail = ref(false)
const showBindPhoneDialog = ref(false)
const showBindEmailDialog = ref(false)

const userInfo = computed(() => userStore.userInfo)

const stats = reactive({
  orderCount: 0,
  serviceCount: 0,
  reviewCount: 0,
})

const formData = reactive({
  avatar: '',
  nickname: '',
  phone: '',
  email: '',
  realName: '',
})

const passwordFormData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const verifyFormData = reactive({
  realName: '',
  idCard: '',
  phone: '',
})

const bindPhoneFormData = reactive({
  phone: '',
})

const bindEmailFormData = reactive({
  email: '',
})

const originalData = reactive({ ...formData })

const isVerified = computed(() => {
  const user = userInfo.value
  return !!(user?.realName && user?.realName.length > 0 && user?.idCard && user?.idCard.length > 0)
})
const roleText = computed(() => {
  const role = userInfo.value?.role
  if (role === 2) return '管理员'
  if (role === 1) return '服务者'
  return '普通用户'
})
const roleTagType = computed(() => {
  const role = userInfo.value?.role
  if (role === 2) return 'danger'
  if (role === 1) return 'success'
  return 'info'
})
const statusText = computed(() => {
  const status = userInfo.value?.status
  return status === 0 ? '正常' : '禁用'
})
const statusTagType = computed(() => {
  const status = userInfo.value?.status
  return status === 0 ? 'success' : 'danger'
})
const verifiedText = computed(() => {
  return isVerified.value ? '已认证' : '未认证'
})
const verifiedTagType = computed(() => {
  return isVerified.value ? 'success' : 'warning'
})

const creditScoreTagType = computed(() => {
  const score = userInfo.value?.creditScore ?? 100
  if (score >= 90) return 'success'
  if (score >= 70) return ''
  if (score >= 50) return 'warning'
  return 'danger'
})

const validatePhone = (_rule: any, value: string, callback: any) => {
  if (value && !isValidPhone(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const validateEmail = (_rule: any, value: string, callback: any) => {
  if (value && !isValidEmail(value)) {
    callback(new Error('请输入正确的邮箱'))
  } else {
    callback()
  }
}

const validateIdCard = (_rule: any, value: string, callback: any) => {
  if (value && !isValidIdCard(value)) {
    callback(new Error('请输入正确的身份证号'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== passwordFormData.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const formRules: FormRules = {
  nickname: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度为 2-20 个字符', trigger: 'blur' },
  ],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  email: [{ validator: validateEmail, trigger: 'blur' }],
}

const passwordFormRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度为 6-100 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const verifyFormRules: FormRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度为 2-20 个字符', trigger: 'blur' },
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { validator: validateIdCard, trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: validatePhone, trigger: 'blur' },
  ],
}

const bindPhoneFormRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: validatePhone, trigger: 'blur' },
  ],
}

const bindEmailFormRules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' },
  ],
}

function formatDate(dateStr: string | undefined) {
  if (!dateStr) return '---'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

function formatMoney(amount: number) {
  return '¥' + (amount || 0).toFixed(2)
}

function loadUserData() {
  const info = userStore.userInfo
  if (info) {
    formData.avatar = info.avatar || ''
    formData.nickname = info.username || ''
    formData.phone = info.phone || ''
    formData.email = info.email || ''
    formData.realName = info.realName || ''
    Object.assign(originalData, { ...formData })
  }
}

function beforeAvatarUpload(file: File) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

async function handleAvatarUpload(options: UploadRequestOptions) {
  const formDataUpload = new FormData()
  formDataUpload.append('file', options.file)
  try {
    const res = await post<{ url: string }>('/upload', formDataUpload)
    formData.avatar = res.data.url
    ElMessage.success('头像上传成功')
  } catch {
    // handled by interceptor
  }
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    saving.value = true
    try {
      await put('/users/profile', {
        avatar: formData.avatar,
        username: formData.nickname,
        phone: formData.phone || undefined,
        email: formData.email || undefined,
        realName: formData.realName || undefined,
      })
      ElMessage.success('保存成功')
      await userStore.fetchUserInfo()
      loadUserData()
      Object.assign(originalData, { ...formData })
    } catch {
      // handled by interceptor
    } finally {
      saving.value = false
    }
  })
}

function handleReset() {
  Object.assign(formData, { ...originalData })
  formRef.value?.clearValidate()
}

async function handlePasswordUpdate() {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    passwordLoading.value = true
    try {
      const request: PasswordUpdateRequest = {
        oldPassword: passwordFormData.oldPassword,
        newPassword: passwordFormData.newPassword,
      }
      await put('/users/password', request)
      ElMessage.success('密码修改成功，请重新登录')
      showPasswordDialog.value = false
      userStore.logout()
      window.location.href = '/auth/login'
    } catch {
      // handled by interceptor
    } finally {
      passwordLoading.value = false
    }
  })
}

async function handleVerify() {
  if (!verifyFormRef.value) return
  await verifyFormRef.value.validate(async (valid) => {
    if (!valid) return

    verifyLoading.value = true
    try {
      await put('/users/profile', {
        realName: verifyFormData.realName,
        idCard: verifyFormData.idCard,
        phone: verifyFormData.phone,
      })
      ElMessage.success('实名认证提交成功')
      showVerifyDialog.value = false
      await userStore.fetchUserInfo()
      loadUserData()
    } catch {
      // handled by interceptor
    } finally {
      verifyLoading.value = false
    }
  })
}

function handleBindPhone() {
  bindPhoneFormData.phone = ''
  showBindPhoneDialog.value = true
}

async function handleBindPhoneSubmit() {
  if (!bindPhoneFormRef.value) return
  await bindPhoneFormRef.value.validate(async (valid) => {
    if (!valid) return

    bindPhoneLoading.value = true
    try {
      await put('/users/profile', {
        phone: bindPhoneFormData.phone,
      })
      ElMessage.success('手机绑定成功')
      showBindPhoneDialog.value = false
      await userStore.fetchUserInfo()
      loadUserData()
    } catch {
      // handled by interceptor
    } finally {
      bindPhoneLoading.value = false
    }
  })
}

function handleBindEmail() {
  bindEmailFormData.email = ''
  showBindEmailDialog.value = true
}

async function handleBindEmailSubmit() {
  if (!bindEmailFormRef.value) return
  await bindEmailFormRef.value.validate(async (valid) => {
    if (!valid) return

    bindEmailLoading.value = true
    try {
      await put('/users/profile', {
        email: bindEmailFormData.email,
      })
      ElMessage.success('邮箱绑定成功')
      showBindEmailDialog.value = false
      await userStore.fetchUserInfo()
      loadUserData()
    } catch {
      // handled by interceptor
    } finally {
      bindEmailLoading.value = false
    }
  })
}

async function loadStats() {
  try {
    const [orderRes, reviewRes] = await Promise.all([
      get('/orders?page=1&size=1'),
      get('/reviews/my?page=1&size=1'),
    ])
    stats.orderCount = orderRes.data?.total || 0
    stats.reviewCount = reviewRes.data?.total || 0

    const role = userInfo.value?.role
    if (role === 1) {
      try {
        const serviceRes = await get('/provider/services?page=1&size=1')
        stats.serviceCount = serviceRes.data?.total || 0
      } catch {
        stats.serviceCount = 0
      }
    } else {
      stats.serviceCount = 0
    }
  } catch {
    // ignore
  }
}

onMounted(() => {
  loadUserData()
  loadStats()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.profile-page {
  padding: $spacing-lg 0;
}

// 页面标题 - Orbitron字体 + 渐变文字
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: $spacing-lg;
  font-family: 'Orbitron', monospace;
  background: linear-gradient(135deg, $neon-cyan, $primary-light, $neon-purple);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: none;
  filter: drop-shadow(0 0 8px rgba($neon-cyan, 0.3));
}

.profile-container {
  display: flex;
  gap: $spacing-lg;
  align-items: flex-start;
}

.profile-sidebar {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.profile-content {
  flex: 1;
  min-width: 0;
}

.user-info-card,
.stats-card,
.profile-card,
.security-card,
.verify-card {
  :deep(.el-card__body) {
    padding: $spacing-lg;
  }
}

// 侧边栏卡片 - neon-border 毛玻璃
.user-info-card,
.stats-card {
  :deep(.el-card) {
    background: $glass-bg;
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border: 1px solid $border-glow;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-glow, $shadow-neon-cyan;
    transition: box-shadow $transition-normal, border-color $transition-normal;

    &:hover {
      border-color: rgba($neon-cyan, 0.35);
      box-shadow: 0 0 30px rgba($neon-cyan, 0.2), 0 0 40px rgba($primary-color, 0.15);
    }
  }
}

.user-avatar-section {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding-bottom: $spacing-lg;
  margin-bottom: $spacing-lg;
  border-bottom: 1px solid rgba(148, 163, 184, 0.06);
}

// 头像霓虹光环
.user-avatar {
  background: linear-gradient(135deg, $primary-color, $primary-dark);
  border: 3px solid $neon-cyan;
  box-shadow:
    0 0 12px rgba($neon-cyan, 0.5),
    0 0 24px rgba($neon-cyan, 0.2);
  animation: avatar-glow 3s ease-in-out infinite;
}

@keyframes avatar-glow {
  0%, 100% {
    box-shadow:
      0 0 12px rgba($neon-cyan, 0.5),
      0 0 24px rgba($neon-cyan, 0.2);
  }
  50% {
    box-shadow:
      0 0 18px rgba($neon-cyan, 0.7),
      0 0 36px rgba($neon-cyan, 0.3);
  }
}

.user-basic-info {
  flex: 1;
}

.user-name {
  margin: 0 0 $spacing-xs 0;
  font-size: 18px;
  font-weight: 600;
  color: $text-primary;
}

.user-status-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.status-label {
  color: $text-secondary;
  font-size: 14px;
}

.status-value {
  color: $text-primary;
  font-size: 14px;
}

.stats-title {
  margin: 0 0 $spacing-md 0;
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
}

.stat-item {
  text-align: center;
  padding: $spacing-md;
  background: rgba(15, 23, 42, 0.6);
  border-radius: $border-radius;
  border: 1px solid rgba(148, 163, 184, 0.06);
  transition: all $transition-normal;

  &:hover {
    border-color: rgba($neon-cyan, 0.2);
    box-shadow: 0 0 12px rgba($neon-cyan, 0.15);
    transform: translateY(-2px);
  }
}

// 统计数字 - Orbitron字体
.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: $primary-light;
  margin-bottom: $spacing-xs;
  font-family: 'Orbitron', monospace;
  text-shadow: 0 0 12px rgba($primary-light, 0.4);
}

.stat-label {
  font-size: 12px;
  color: $text-secondary;
}

.profile-tabs {
  :deep(.el-tabs__header) {
    margin: 0;
  }

  :deep(.el-tabs__content) {
    background: $glass-bg;
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border: 1px solid $glass-border;
    border-top: none;
    border-radius: 0 0 $border-radius-lg $border-radius-lg;
  }
}

// 头像上传区域 - 霓虹虚线边框
.avatar-section {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  margin-bottom: $spacing-xl;
  padding-bottom: $spacing-xl;
  border-bottom: 1px solid rgba(148, 163, 184, 0.06);
  padding: $spacing-lg;
  margin-bottom: $spacing-xl;
  border: 2px dashed rgba($neon-cyan, 0.3);
  border-radius: $border-radius-lg;
  background: rgba($neon-cyan, 0.02);
  transition: all $transition-normal;

  &:hover {
    border-color: rgba($neon-cyan, 0.5);
    background: rgba($neon-cyan, 0.04);
    box-shadow: 0 0 20px rgba($neon-cyan, 0.08);
  }

  :deep(.el-avatar) {
    box-shadow:
      0 0 12px rgba($neon-cyan, 0.4),
      0 0 24px rgba($neon-cyan, 0.15);
    border: 2px solid rgba($neon-cyan, 0.4);
  }

  // 更换头像按钮 - 霓虹发光hover
  :deep(.el-button--primary.is-plain) {
    border-color: rgba($neon-cyan, 0.4);
    color: $neon-cyan;
    background: rgba($neon-cyan, 0.05);

    &:hover {
      background: rgba($neon-cyan, 0.15);
      border-color: $neon-cyan;
      box-shadow: 0 0 16px rgba($neon-cyan, 0.3);
      color: $neon-cyan;
    }
  }
}

// 表单输入框 - focus时cyan发光边框
.profile-card {
  :deep(.el-input__wrapper) {
    background: $bg-input;
    border: 1px solid $border-color;
    box-shadow: none;
    transition: all $transition-normal;

    &:hover {
      border-color: rgba($neon-cyan, 0.25);
    }

    &.is-focus {
      border-color: $neon-cyan;
      box-shadow:
        0 0 0 1px rgba($neon-cyan, 0.3),
        0 0 12px rgba($neon-cyan, 0.15);
    }
  }

  // 保存/重置按钮 - 霓虹发光hover
  :deep(.el-button--primary:not(.is-plain)) {
    background: linear-gradient(135deg, $primary-color, $neon-purple);
    border: none;
    box-shadow: 0 4px 12px rgba($primary-color, 0.3);

    &:hover {
      box-shadow:
        0 0 16px rgba($neon-cyan, 0.4),
        0 0 30px rgba($primary-color, 0.3);
      transform: translateY(-1px);
    }
  }
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-lg 0;
  border-bottom: 1px solid rgba(148, 163, 184, 0.06);
  transition: background $transition-fast;
  border-radius: $border-radius;

  &:hover {
    background: rgba($neon-cyan, 0.02);
  }

  &:last-child {
    border-bottom: none;
  }
}

.security-info {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.security-icon {
  font-size: 32px;
  color: $neon-cyan;
  filter: drop-shadow(0 0 10px rgba($neon-cyan, 0.4));
}

.security-text {
  h4 {
    margin: 0 0 $spacing-xs 0;
    font-size: 16px;
    font-weight: 600;
    color: $text-primary;
  }

  p {
    margin: 0;
    font-size: 13px;
    color: $text-secondary;
  }
}

// 安全按钮 - 霓虹发光hover
.security-item {
  :deep(.el-button--primary.is-plain) {
    border-color: rgba($neon-cyan, 0.3);
    color: $neon-cyan;
    background: rgba($neon-cyan, 0.05);

    &:hover:not(:disabled) {
      background: rgba($neon-cyan, 0.15);
      border-color: $neon-cyan;
      box-shadow: 0 0 16px rgba($neon-cyan, 0.3);
      color: $neon-cyan;
    }
  }
}

.verify-content {
  text-align: center;
  padding: $spacing-xl;
}

.verify-icon {
  font-size: 64px;
  margin-bottom: $spacing-lg;
  filter: drop-shadow(0 0 20px rgba($neon-cyan, 0.3));
}

.verify-success,
.verify-pending {
  h3 {
    margin: 0 0 $spacing-md 0;
    font-size: 20px;
    font-weight: 600;
  }

  p {
    margin: 0 0 $spacing-lg 0;
    color: $text-secondary;
  }
}

.privilege-list {
  text-align: left;
  margin: 0 0 $spacing-xl 0;
  padding-left: $spacing-xl;

  li {
    margin-bottom: $spacing-sm;
    color: $text-primary;
    padding-left: $spacing-xs;

    &::marker {
      color: $neon-cyan;
    }
  }
}

// 响应式
@media (max-width: 992px) {
  .profile-container {
    flex-direction: column;
  }

  .profile-sidebar {
    width: 100%;
  }

  .avatar-section {
    flex-direction: column;
    text-align: center;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
