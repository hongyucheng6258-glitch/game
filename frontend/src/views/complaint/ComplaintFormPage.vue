<template>
  <div class="complaint-form-page">
    <div class="form-container">
      <h2 class="page-title">投诉订单</h2>
      <el-card shadow="never">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="complaint-form">
          <el-form-item label="订单号" prop="orderId">
            <el-input v-model="orderNo" disabled />
          </el-form-item>

          <el-form-item label="投诉类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择投诉类型">
              <el-option
                v-for="(label, value) in COMPLAINT_TYPE_LABELS"
                :key="value"
                :label="label"
                :value="Number(value)"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="投诉内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="6"
              placeholder="请详细描述您遇到的问题（不少于10个字符）"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="证据图片（可选）">
            <div class="upload-tip">
              <el-icon><InfoFilled /></el-icon>
              请上传能证明您问题的图片（如聊天记录截图等）
            </div>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="form-footer">
            <el-button @click="$router.back()">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">提交投诉</el-button>
          </div>
        </template>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { createComplaint } from '@/api/complaint'
import { COMPLAINT_TYPE_LABELS } from '@/utils/constants'

const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const orderNo = ref('')

const form = reactive({
  orderId: 0,
  type: null as number | null,
  content: '',
  evidenceImages: '',
})

const rules: FormRules = {
  type: [{ required: true, message: '请选择投诉类型', trigger: 'change' }],
  content: [
    { required: true, message: '请输入投诉内容', trigger: 'blur' },
    { min: 10, message: '投诉内容不能少于10个字符', trigger: 'blur' },
  ],
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      await createComplaint({
        orderId: form.orderId,
        type: form.type!,
        content: form.content,
        evidenceImages: form.evidenceImages,
      })
      ElMessage.success('投诉提交成功，请等待管理员处理')
      router.push('/user/complaints')
    } catch {
      // handled by interceptor
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  const orderId = route.query.orderId as string
  const orderNoVal = route.query.orderNo as string
  if (orderId) {
    form.orderId = Number(orderId)
  }
  if (orderNoVal) {
    orderNo.value = orderNoVal
  }
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.complaint-form-page {
  padding: $spacing-lg 0;
}

.form-container {
  max-width: 600px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.complaint-form {
  padding: $spacing-md;
}

.upload-tip {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  color: $text-muted;
  font-size: 13px;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
  padding-top: $spacing-md;
  border-top: 1px solid $border-color;
}
</style>
