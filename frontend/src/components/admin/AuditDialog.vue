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
