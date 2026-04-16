<template>
  <div class="register-page">
    <!-- 左侧内容区 -->
    <div class="relative hidden lg:flex flex-col justify-between bg-gradient-to-br from-primary/90 via-primary to-primary/80 p-12 text-white">
      <div class="relative z-20">
        <div class="flex items-center gap-2 text-lg font-semibold">
          <div class="size-8 rounded-lg bg-white/10 backdrop-blur-sm flex items-center justify-center">
            <span class="text-white">🎮</span>
          </div>
          <span>{{ systemStore.settings.siteName }}</span>
        </div>
      </div>

      <div class="relative z-20 flex items-end justify-center h-[500px]">
        <!-- 卡通角色 -->
        <div class="relative" style="width: 550px; height: 400px">
          <!-- 紫色角色 -->
          <div 
            ref="purpleRef"
            class="absolute bottom-0 transition-all duration-700 ease-in-out"
            :style="{
              left: '70px',
              width: '180px',
              height: '400px',
              backgroundColor: '#6C3FF5',
              borderRadius: '10px 10px 0 0',
              zIndex: 1,
              transform: 'skewX(' + purplePos.bodySkew + 'deg)',
              transformOrigin: 'bottom center'
            }"
          >
            <!-- 眼睛 -->
            <div 
              class="absolute flex gap-8 transition-transform duration-300"
              :style="{
                left: '45px',
                top: '40px',
                transform: 'translateX(' + (purplePos.faceX * 0.8) + 'px) translateY(' + (purplePos.faceY * 0.8) + 'px)'
              }"
            >
              <div 
                class="rounded-full transition-all duration-150"
                :class="{ 'blinking': isPurpleBlinking }"
                style="width: 18px; height: 18px; background-color: white; display: flex; align-items: center; justify-content: center"
              >
                <div 
                  class="rounded-full transition-all duration-300"
                  :style="{
                    width: isPurplePeeking ? '10px' : '7px',
                    height: isPurplePeeking ? '4px' : '7px',
                    backgroundColor: '#2D2D2D',
                    transform: `translateX(${isPurplePeeking ? '2px' : '0px'})`
                  }"
                />
              </div>
              <div 
                class="rounded-full transition-all duration-150"
                :class="{ 'blinking': isPurpleBlinking }"
                style="width: 18px; height: 18px; background-color: white; display: flex; align-items: center; justify-content: center"
              >
                <div 
                  class="rounded-full transition-all duration-300"
                  :style="{
                    width: isPurplePeeking ? '10px' : '7px',
                    height: isPurplePeeking ? '4px' : '7px',
                    backgroundColor: '#2D2D2D',
                    transform: `translateX(${isPurplePeeking ? '2px' : '0px'})`
                  }"
                />
              </div>
            </div>
          </div>

          <!-- 黑色角色 -->
          <div 
            ref="blackRef"
            class="absolute bottom-0 transition-all duration-700 ease-in-out"
            :style="{
              left: '240px',
              width: '120px',
              height: '310px',
              backgroundColor: '#2D2D2D',
              borderRadius: '8px 8px 0 0',
              zIndex: 2,
              transform: 'skewX(' + blackPos.bodySkew + 'deg)',
              transformOrigin: 'bottom center'
            }"
          >
            <!-- 眼睛 -->
            <div 
              class="absolute flex gap-6 transition-transform duration-300"
              :style="{
                left: '26px',
                top: '32px',
                transform: 'translateX(' + (blackPos.faceX * 0.8) + 'px) translateY(' + (blackPos.faceY * 0.8) + 'px)'
              }"
            >
              <div 
                class="rounded-full transition-all duration-150"
                :class="{ 'blinking': isBlackBlinking }"
                style="width: 16px; height: 16px; background-color: white; display: flex; align-items: center; justify-content: center"
              >
                <div 
                  class="rounded-full transition-all duration-300"
                  :style="{
                    width: '6px',
                    height: '6px',
                    backgroundColor: '#2D2D2D',
                    transform: `translateX(${isLookingAtEachOther ? '2px' : '0px'})`
                  }"
                />
              </div>
              <div 
                class="rounded-full transition-all duration-150"
                :class="{ 'blinking': isBlackBlinking }"
                style="width: 16px; height: 16px; background-color: white; display: flex; align-items: center; justify-content: center"
              >
                <div 
                  class="rounded-full transition-all duration-300"
                  :style="{
                    width: '6px',
                    height: '6px',
                    backgroundColor: '#2D2D2D',
                    transform: `translateX(${isLookingAtEachOther ? '2px' : '0px'})`
                  }"
                />
              </div>
            </div>
          </div>

          <!-- 橙色角色 -->
          <div 
            ref="orangeRef"
            class="absolute bottom-0 transition-all duration-700 ease-in-out"
            :style="{
              left: '0px',
              width: '240px',
              height: '200px',
              zIndex: 3,
              backgroundColor: '#FF9B6B',
              borderRadius: '120px 120px 0 0',
              transform: 'skewX(' + orangePos.bodySkew + 'deg)',
              transformOrigin: 'bottom center'
            }"
          >
            <!-- 眼睛 -->
            <div 
              class="absolute flex gap-8 transition-transform duration-300"
              :style="{
                left: '82px',
                top: '90px',
                transform: 'translateX(' + (orangePos.faceX * 0.6) + 'px) translateY(' + (orangePos.faceY * 0.6) + 'px)'
              }"
            >
              <div class="rounded-full" style="width: 12px; height: 12px; background-color: #2D2D2D" />
              <div class="rounded-full" style="width: 12px; height: 12px; background-color: #2D2D2D" />
            </div>
          </div>

          <!-- 黄色角色 -->
          <div 
            ref="yellowRef"
            class="absolute bottom-0 transition-all duration-700 ease-in-out"
            :style="{
              left: '310px',
              width: '140px',
              height: '230px',
              backgroundColor: '#E8D754',
              borderRadius: '70px 70px 0 0',
              zIndex: 4,
              transform: 'skewX(' + yellowPos.bodySkew + 'deg)',
              transformOrigin: 'bottom center'
            }"
          >
            <!-- 眼睛 -->
            <div 
              class="absolute flex gap-6 transition-transform duration-300"
              :style="{
                left: '52px',
                top: '40px',
                transform: 'translateX(' + (yellowPos.faceX * 0.6) + 'px) translateY(' + (yellowPos.faceY * 0.6) + 'px)'
              }"
            >
              <div class="rounded-full" style="width: 12px; height: 12px; background-color: #2D2D2D" />
              <div class="rounded-full" style="width: 12px; height: 12px; background-color: #2D2D2D" />
            </div>
            <!-- 嘴巴 -->
            <div 
              class="absolute w-20 h-[4px] bg-[#2D2D2D] rounded-full transition-all duration-300"
              :style="{
                left: '40px',
                top: '88px',
                transform: 'scaleX(' + (isTyping ? 0.8 : 1) + ')'
              }"
            />
          </div>
        </div>
      </div>

      <div class="relative z-20 flex items-center gap-8 text-sm text-white/60">
        <a href="#" class="hover:text-white transition-colors">
          隐私政策
        </a>
        <a href="#" class="hover:text-white transition-colors">
          服务条款
        </a>
        <a href="#" class="hover:text-white transition-colors">
          联系我们
        </a>
      </div>

      <!-- 装饰元素 -->
      <div class="absolute inset-0 bg-grid-white/[0.05] bg-[size:20px_20px]" />
      <div class="absolute top-1/4 right-1/4 size-64 bg-white/10 rounded-full blur-3xl" />
      <div class="absolute bottom-1/4 left-1/4 size-96 bg-white/5 rounded-full blur-3xl" />
    </div>

    <!-- 右侧注册表单 -->
    <div class="flex items-center justify-center p-8 bg-background">
      <div class="w-full max-w-[420px]">
        <!-- 移动端Logo -->
        <div class="lg:hidden flex items-center justify-center gap-2 text-lg font-semibold mb-12">
          <div class="size-8 rounded-lg bg-primary/10 flex items-center justify-center">
            <span class="text-primary">🎮</span>
          </div>
          <span>{{ systemStore.settings.siteName }}</span>
        </div>

        <!-- 标题 -->
        <div class="text-center mb-10">
          <h1 class="text-3xl font-bold tracking-tight mb-2">创建新账号</h1>
          <p class="text-muted-foreground text-sm">请填写以下信息完成注册</p>
        </div>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
        size="large"
        @submit.prevent="handleRegister"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入用户名（2-20个字符）"
            prefix-icon="User"
            clearable
            @focus="handleInputFocus"
            @blur="handleInputBlur"
          />
          <div v-if="formData.username.length >= 2" class="input-feedback">
            <el-icon v-if="usernameChecking" class="icon-loading"><Loading /></el-icon>
            <el-icon v-else-if="!usernameExists" class="icon-success"><Check /></el-icon>
            <el-icon v-else class="icon-error"><Close /></el-icon>
            <span v-if="usernameChecking" class="feedback-text">检查中...</span>
            <span v-else-if="!usernameExists" class="feedback-text success">用户名可用</span>
            <span v-else class="feedback-text error">用户名已存在</span>
          </div>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            prefix-icon="Lock"
            show-password
            @focus="handleInputFocus"
            @blur="handleInputBlur"
          />
          <div v-if="formData.password" class="password-strength">
            <div class="strength-bar">
              <div 
                class="strength-fill" 
                :style="{ 
                  width: (passwordStrength * 20) + '%',
                  backgroundColor: getPasswordStrengthColor(passwordStrength)
                }"
              ></div>
            </div>
            <div class="strength-label" :style="{ color: getPasswordStrengthColor(passwordStrength) }">
              {{ getPasswordStrengthLabel(passwordStrength) }}
            </div>
          </div>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="formData.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            show-password
            @focus="handleInputFocus"
            @blur="handleInputBlur"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入手机号（选填）"
            prefix-icon="Phone"
            clearable
            @focus="handleInputFocus"
            @blur="handleInputBlur"
          />
          <div v-if="formData.phone" class="input-feedback">
            <el-icon v-if="isValidPhone(formData.phone)" class="icon-success"><Check /></el-icon>
            <el-icon v-else class="icon-error"><Close /></el-icon>
            <span v-if="isValidPhone(formData.phone)" class="feedback-text success">手机号格式正确</span>
            <span v-else class="feedback-text error">手机号格式不正确</span>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="register-btn"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span class="footer-text">已有账号？</span>
        <router-link to="/auth/login" class="footer-link">去登录</router-link>
      </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules, ElIcon } from 'element-plus'
import { Check, Close, Loading } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'
import { isValidPhone } from '@/utils/validate'
import { calculatePasswordStrength, getPasswordStrengthColor, getPasswordStrengthLabel } from '@/utils/password'

const router = useRouter()
const userStore = useUserStore()
const systemStore = useSystemStore()

onMounted(() => {
  systemStore.fetchSettings()
  window.addEventListener('mousemove', handleMouseMove)
  const cleanupBlinking = setupBlinking()
  
  return () => {
    window.removeEventListener('mousemove', handleMouseMove)
    cleanupBlinking()
  }
})

const formRef = ref<FormInstance>()
const loading = ref(false)
const passwordStrength = ref(0)
const usernameChecking = ref(false)
const usernameExists = ref(false)
const showPassword = ref(false)

// 互动动画相关
const mouseX = ref(0)
const mouseY = ref(0)
const isTyping = ref(false)
const isPurpleBlinking = ref(false)
const isBlackBlinking = ref(false)
const isLookingAtEachOther = ref(false)
const isPurplePeeking = ref(false)

const purpleRef = ref<HTMLElement | null>(null)
const blackRef = ref<HTMLElement | null>(null)
const yellowRef = ref<HTMLElement | null>(null)
const orangeRef = ref<HTMLElement | null>(null)

const formData = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
})

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== formData.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (_rule: any, value: string, callback: any) => {
  if (value && !isValidPhone(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度为 2-20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度为 6-100 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' },
  ],
}

// 监听密码输入，更新强度
watch(
  () => formData.password,
  (newPassword) => {
    passwordStrength.value = calculatePasswordStrength(newPassword)
  }
)

// 监听用户名输入，检查是否已存在
watch(
  () => formData.username,
  async (newUsername) => {
    if (newUsername.length >= 2) {
      usernameChecking.value = true
      try {
        usernameExists.value = await userStore.checkUsername(newUsername)
      } catch {
        usernameExists.value = false
      } finally {
        usernameChecking.value = false
      }
    } else {
      usernameExists.value = false
    }
  }
)

// 计算角色位置
const calculatePosition = (ref: any) => {
  if (!ref.value) return { faceX: 0, faceY: 0, bodySkew: 0 }

  const rect = ref.value.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 3

  const deltaX = mouseX.value - centerX
  const deltaY = mouseY.value - centerY

  const faceX = Math.max(-15, Math.min(15, deltaX / 20))
  const faceY = Math.max(-10, Math.min(10, deltaY / 30))
  const bodySkew = Math.max(-6, Math.min(6, -deltaX / 120))

  return { faceX, faceY, bodySkew }
}

const purplePos = computed(() => calculatePosition(purpleRef))
const blackPos = computed(() => calculatePosition(blackRef))
const yellowPos = computed(() => calculatePosition(yellowRef))
const orangePos = computed(() => calculatePosition(orangeRef))

// 鼠标移动事件
const handleMouseMove = (e: MouseEvent) => {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}

// 眨眼效果
const setupBlinking = () => {
  // 紫色角色眨眼
  const schedulePurpleBlink = () => {
    const blinkTimeout = setTimeout(() => {
      isPurpleBlinking.value = true
      setTimeout(() => {
        isPurpleBlinking.value = false
        schedulePurpleBlink()
      }, 150)
    }, Math.random() * 4000 + 3000)
    return blinkTimeout
  }

  // 黑色角色眨眼
  const scheduleBlackBlink = () => {
    const blinkTimeout = setTimeout(() => {
      isBlackBlinking.value = true
      setTimeout(() => {
        isBlackBlinking.value = false
        scheduleBlackBlink()
      }, 150)
    }, Math.random() * 4000 + 3000)
    return blinkTimeout
  }

  const purpleBlinkTimeout = schedulePurpleBlink()
  const blackBlinkTimeout = scheduleBlackBlink()

  return () => {
    clearTimeout(purpleBlinkTimeout)
    clearTimeout(blackBlinkTimeout)
  }
}

// 输入时的互动
const handleInputFocus = () => {
  isTyping.value = true
  isLookingAtEachOther.value = true
  setTimeout(() => {
    isLookingAtEachOther.value = false
  }, 800)
}

const handleInputBlur = () => {
  isTyping.value = false
}

// 查看密码时的偷看效果
const handlePasswordVisibilityChange = () => {
  showPassword.value = !showPassword.value
  if (formData.password.length > 0 && showPassword.value) {
    setTimeout(() => {
      isPurplePeeking.value = true
      setTimeout(() => {
        isPurplePeeking.value = false
      }, 800)
    }, Math.random() * 3000 + 2000)
  } else {
    isPurplePeeking.value = false
  }
}

async function handleRegister() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await userStore.register({
        username: formData.username,
        password: formData.password,
        phone: formData.phone || undefined,
      })
      ElMessage.success('注册成功，请登录')
      router.push('/auth/login')
    } catch {
      // error handled by interceptor
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;500;600;700;800;900&display=swap');

.register-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr;

  @media (min-width: 1024px) {
    grid-template-columns: 1fr 1fr;
  }

  :deep(.el-form-item__label) {
    color: $text-secondary;
    font-weight: 500;
    font-size: 14px;
    margin-bottom: 6px;
    letter-spacing: 0.3px;
  }

  :deep(.el-input__wrapper) {
    background-color: rgba(6, 8, 15, 0.9) !important;
    border: 1px solid $border-color !important;
    border-radius: 12px !important;
    box-shadow: none !important;
    padding: 8px 16px !important;
    transition: all 0.3s ease;

    &:hover {
      border-color: rgba(0, 240, 255, 0.3) !important;
    }

    &.is-focus {
      border-color: $neon-cyan !important;
      box-shadow: 0 0 0 3px rgba(0, 240, 255, 0.1), 0 0 15px rgba(0, 240, 255, 0.08) !important;
    }
  }

  :deep(.el-input__inner) {
    color: $text-primary;
    font-size: 15px;

    &::placeholder {
      color: $text-muted;
    }
  }
}

// 左侧面板 - 深色渐变 + 赛博网格纹理
.bg-gradient-to-br {
  background: linear-gradient(160deg, $bg-dark 0%, $bg-abyss 100%);
  position: relative;
  overflow: hidden;

  // 赛博网格纹理
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image:
      linear-gradient(rgba(0, 240, 255, 0.06) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 240, 255, 0.06) 1px, transparent 1px);
    background-size: 40px 40px;
    z-index: 1;
    animation: gridPulse 8s ease-in-out infinite;
  }

  // 顶部扫描线效果
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: repeating-linear-gradient(
      0deg,
      transparent,
      transparent 2px,
      rgba(0, 240, 255, 0.015) 2px,
      rgba(0, 240, 255, 0.015) 4px
    );
    z-index: 1;
    pointer-events: none;
  }
}

@keyframes gridPulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

// 赛博网格纹理层
.bg-grid-white\/\[0\.05\] {
  background-image:
    linear-gradient(to right, rgba(0, 240, 255, 0.08) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(0, 240, 255, 0.08) 1px, transparent 1px);
  background-size: 40px 40px;
  z-index: 2;
}

.size-8 {
  width: 32px;
  height: 32px;
}

// 装饰光晕 - cyan 和 purple 色
.size-64 {
  width: 256px;
  height: 256px;
  background: radial-gradient(circle, rgba(0, 240, 255, 0.25) 0%, transparent 70%) !important;
  filter: blur(40px);
  animation: glowFloat1 6s ease-in-out infinite;
}

.size-96 {
  width: 384px;
  height: 384px;
  background: radial-gradient(circle, rgba(191, 90, 242, 0.2) 0%, transparent 70%) !important;
  filter: blur(50px);
  animation: glowFloat2 8s ease-in-out infinite;
}

@keyframes glowFloat1 {
  0%, 100% { transform: translate(0, 0); opacity: 0.8; }
  50% { transform: translate(20px, -15px); opacity: 1; }
}

@keyframes glowFloat2 {
  0%, 100% { transform: translate(0, 0); opacity: 0.7; }
  50% { transform: translate(-15px, 20px); opacity: 1; }
}

.backdrop-blur-sm {
  backdrop-filter: blur(4px);
}

.blur-3xl {
  filter: blur(24px);
}

.z-20 {
  z-index: 20;
}

.h-\[500px\] {
  height: 500px;
}

// 右侧表单区域 - 更深背景
.bg-background {
  background-color: $bg-abyss;
  background-image:
    radial-gradient(ellipse at 20% 50%, rgba(0, 240, 255, 0.03) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(191, 90, 242, 0.03) 0%, transparent 50%);
}

.p-8 {
  padding: 32px;
}

.p-12 {
  padding: 48px;
}

.text-white {
  color: white;
}

.text-white\/60 {
  color: rgba(255, 255, 255, 0.6);
}

.text-white\/10 {
  background-color: rgba(255, 255, 255, 0.1);
}

.text-white\/5 {
  background-color: rgba(255, 255, 255, 0.05);
}

.hover\:text-white:hover {
  color: white;
}

.transition-colors {
  transition: color 0.3s ease;
}

.relative {
  position: relative;
}

.absolute {
  position: absolute;
}

.bottom-0 {
  bottom: 0;
}

.top-1\/4 {
  top: 25%;
}

.right-1\/4 {
  right: 25%;
}

.left-1\/4 {
  left: 25%;
}

.bottom-1\/4 {
  bottom: 25%;
}

.rounded-lg {
  border-radius: $border-radius;
}

.rounded-full {
  border-radius: 9999px;
}

.flex {
  display: flex;
}

.flex-col {
  flex-direction: column;
}

.items-center {
  align-items: center;
}

.items-end {
  align-items: flex-end;
}

.justify-center {
  justify-content: center;
}

.justify-between {
  justify-content: space-between;
}

.gap-2 {
  gap: 8px;
}

.gap-8 {
  gap: 32px;
}

.text-center {
  text-align: center;
}

.text-lg {
  font-size: 1.125rem;
}

.text-sm {
  font-size: 0.875rem;
}

.font-semibold {
  font-weight: 600;
}

.mb-12 {
  margin-bottom: 48px;
}

.mb-10 {
  margin-bottom: 40px;
}

// 标题 - Orbitron 字体 + 霓虹发光
.text-3xl {
  font-size: 1.875rem;
  font-family: 'Orbitron', sans-serif;
}

.font-bold {
  font-weight: 700;
}

.tracking-tight {
  letter-spacing: -0.025em;
}

.text-muted-foreground {
  color: $text-secondary;
}

.text-foreground {
  color: $text-primary;
}

.hover\:text-foreground:hover {
  color: $text-primary;
}

.transition-all {
  transition: all 0.3s ease;
}

.duration-700 {
  transition-duration: 700ms;
}

.duration-300 {
  transition-duration: 300ms;
}

.duration-150 {
  transition-duration: 150ms;
}

.ease-in-out {
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
}

.w-full {
  width: 100%;
}

.max-w-\[420px\] {
  max-width: 420px;
}

.lg\:hidden {
  display: flex;

  @media (min-width: 1024px) {
    display: none;
  }
}

.lg\:flex {
  display: none;

  @media (min-width: 1024px) {
    display: flex;
  }
}

.relative {
  position: relative;
}

.right-3 {
  right: 12px;
}

.top-1\/2 {
  top: 50%;
}

.-translate-y-1\/2 {
  transform: translateY(-50%);
}

// 注册容器样式 - 霓虹边框发光效果
.register-container {
  width: 100%;
  max-width: 440px;
  padding: 40px 36px;
  background: linear-gradient(145deg, rgba(11, 15, 26, 0.95), rgba(6, 8, 15, 0.98));
  border-radius: 20px;
  border: 1px solid rgba(0, 240, 255, 0.12);
  box-shadow:
    0 0 30px rgba(0, 240, 255, 0.08),
    0 0 60px rgba(191, 90, 242, 0.05),
    0 25px 50px -12px rgba(0, 0, 0, 0.6),
    inset 0 1px 0 rgba(0, 240, 255, 0.06);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;

  // 顶部霓虹渐变高光线
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(0, 240, 255, 0.6), rgba(191, 90, 242, 0.6), transparent);
  }

  // 底部霓虹渐变高光线
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 10%;
    right: 10%;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(0, 240, 255, 0.2), rgba(191, 90, 242, 0.2), transparent);
  }
}

.register-header {
  text-align: center;
  margin-bottom: 36px;
  position: relative;
  z-index: 1;
}

// 标题 - Orbitron 字体 + 霓虹发光
.register-title {
  font-size: 32px;
  font-weight: 800;
  font-family: 'Orbitron', sans-serif;
  background: linear-gradient(135deg, $neon-cyan, #a5b4fc, $neon-purple);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 10px;
  letter-spacing: 1px;
  text-shadow: none;
  filter: drop-shadow(0 0 20px rgba(0, 240, 255, 0.3));
}

.register-subtitle {
  color: $text-secondary;
  font-size: 15px;
  font-weight: 400;
  letter-spacing: 0.2px;
}

// 注册按钮 - 增强发光效果
.register-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 700;
  font-family: 'Orbitron', sans-serif;
  letter-spacing: 2px;
  border-radius: 12px;
  background: linear-gradient(135deg, $neon-cyan, $primary-color);
  border: none;
  color: $bg-abyss;
  box-shadow:
    0 0 20px rgba(0, 240, 255, 0.3),
    0 0 40px rgba(0, 240, 255, 0.1),
    0 4px 15px rgba(0, 0, 0, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transition: left 0.5s ease;
  }

  // 顶部高光线
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 10%;
    right: 10%;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.6), transparent);
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow:
      0 0 30px rgba(0, 240, 255, 0.5),
      0 0 60px rgba(0, 240, 255, 0.2),
      0 8px 25px rgba(0, 0, 0, 0.4);

    &::before {
      left: 100%;
    }
  }

  &:active {
    transform: translateY(0);
    box-shadow:
      0 0 15px rgba(0, 240, 255, 0.4),
      0 2px 10px rgba(0, 0, 0, 0.3);
  }
}

.register-footer {
  text-align: center;
  margin-top: $spacing-lg;
}

.footer-text {
  color: $text-secondary;
  font-size: 14px;
}

// 底部链接"去登录" - $neon-cyan 色
.footer-link {
  color: $neon-cyan;
  font-size: 14px;
  font-weight: 500;
  margin-left: $spacing-xs;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.3);
  transition: all 0.3s ease;

  &:hover {
    color: lighten($neon-cyan, 15%);
    text-shadow: 0 0 15px rgba(0, 240, 255, 0.6), 0 0 30px rgba(0, 240, 255, 0.3);
  }
}

.input-feedback {
  display: flex;
  align-items: center;
  margin-top: 8px;
  font-size: 12px;

  .icon-loading {
    color: $neon-cyan;
    margin-right: 6px;
    animation: neonPulse 1s ease-in-out infinite;
  }

  .icon-success {
    color: $neon-green;
    margin-right: 6px;
    text-shadow: 0 0 6px rgba(57, 255, 20, 0.4);
  }

  .icon-error {
    color: $neon-pink;
    margin-right: 6px;
    text-shadow: 0 0 6px rgba(255, 45, 120, 0.4);
  }

  .feedback-text {
    &.success {
      color: $neon-green;
    }
    &.error {
      color: $neon-pink;
    }
  }
}

@keyframes neonPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.password-strength {
  margin-top: 12px;

  .strength-bar {
    height: 4px;
    background-color: rgba(0, 240, 255, 0.08);
    border-radius: 2px;
    overflow: hidden;
    margin-bottom: 6px;

    .strength-fill {
      height: 100%;
      transition: all 0.3s ease;
      border-radius: 2px;
      box-shadow: 0 0 8px currentColor;
    }
  }

  .strength-label {
    font-size: 12px;
    text-align: right;
  }
}

// 眨眼动画
.blinking {
  animation: blink 0.15s ease-in-out;
}

@keyframes blink {
  0%, 100% {
    transform: scaleY(1);
  }
  50% {
    transform: scaleY(0.1);
  }
}
</style>