<template>
  <div class="admin-user-page">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
    </div>

    <!-- 搜索筛选 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-input v-model="searchKeyword" placeholder="搜索用户名/手机号" clearable style="width: 250px" @clear="fetchUsers" @keyup.enter="fetchUsers">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="filterRole" placeholder="角色筛选" clearable style="width: 150px" @change="fetchUsers">
          <el-option label="普通用户" :value="0" />
          <el-option label="服务者" :value="1" />
          <el-option label="管理员" :value="2" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 150px" @change="fetchUsers">
          <el-option label="正常" :value="0" />
          <el-option label="禁用" :value="1" />
        </el-select>
        <el-button type="primary" @click="fetchUsers">搜索</el-button>
      </div>
    </el-card>

    <!-- 用户表格 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="users" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户" min-width="150">
          <template #default="{ row }">
            <div class="user-cell">
              <UserAvatar :avatar="row.avatar" :username="row.username" :size="32" />
              <span>{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">{{ row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="180">
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)" size="small">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="100">
          <template #default="{ row }">
            <span style="color: #f59e0b">¥{{ row.balance?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="creditScore" label="信誉分" width="100">
          <template #default="{ row }">
            <el-tag :type="creditScoreTagType(row.creditScore)" size="small">
              {{ row.creditScore ?? 100 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="success" @click="handleResetPassword(row)">重置密码</el-button>
            <el-button size="small" type="warning" @click="handleAdjustBalance(row)">余额</el-button>
            <el-dropdown trigger="click" @command="(cmd) => handleMoreAction(cmd, row)">
              <el-button size="small">更多<el-icon class="el-icon--right"><arrow-down /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="toggleStatus">
                    {{ row.status === 0 ? '禁用' : '启用' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="toggleRole">
                    {{ row.role === 1 ? '取消服务者' : '设为服务者' }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="fetchUsers"
          @current-change="fetchUsers"
        />
      </div>
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="用户详情" width="600px">
      <div v-loading="detailLoading" class="detail-content">
        <el-descriptions :column="2" border v-if="selectedUser">
          <el-descriptions-item label="ID">{{ selectedUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ selectedUser.username }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ selectedUser.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedUser.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ selectedUser.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ selectedUser.idCard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="roleTagType(selectedUser.role)" size="small">{{ roleLabel(selectedUser.role) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedUser.status === 0 ? 'success' : 'danger'" size="small">
              {{ selectedUser.status === 0 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="余额" :span="2">
            <span style="color: #f59e0b; font-size: 18px; font-weight: 600">¥{{ selectedUser.balance?.toFixed(2) || '0.00' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="信誉分" :span="2">
            <el-tag :type="creditScoreTagType(selectedUser.creditScore)" size="large">
              {{ selectedUser.creditScore ?? 100 }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间" :span="2">{{ formatDate(selectedUser.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">{{ formatDate(selectedUser.updatedAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑用户" width="500px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="editForm.idCard" placeholder="请输入身份证号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="showResetPasswordDialog" title="重置密码" width="400px">
      <el-form ref="resetPasswordFormRef" :model="resetPasswordForm" :rules="resetPasswordRules" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPasswordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPasswordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showResetPasswordDialog = false">取消</el-button>
        <el-button type="primary" :loading="resetPasswordLoading" @click="handleSaveResetPassword">确认</el-button>
      </template>
    </el-dialog>

    <!-- 调整余额对话框 -->
    <el-dialog v-model="showAdjustBalanceDialog" title="调整余额" width="400px">
      <el-form ref="adjustBalanceFormRef" :model="adjustBalanceForm" :rules="adjustBalanceRules" label-width="100px">
        <el-form-item label="当前余额">
          <span style="color: #f59e0b; font-weight: 600">¥{{ selectedUser?.balance?.toFixed(2) || '0.00' }}</span>
        </el-form-item>
        <el-form-item label="调整金额" prop="amount">
          <el-input-number v-model="adjustBalanceForm.amount" :precision="2" :step="10" :min="-999999" :max="999999" style="width: 100%" placeholder="正数增加，负数减少" />
        </el-form-item>
        <el-form-item label="调整后余额">
          <span :style="{ color: (selectedUser?.balance || 0) + adjustBalanceForm.amount >= 0 ? '#22c55e' : '#ef4444', fontWeight: 600 }">
            ¥{{ ((selectedUser?.balance || 0) + adjustBalanceForm.amount).toFixed(2) }}
          </span>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="adjustBalanceForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdjustBalanceDialog = false">取消</el-button>
        <el-button type="primary" :loading="adjustBalanceLoading" @click="handleSaveAdjustBalance">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import { getAdminUsers, updateUserStatus, updateUserRole, getAdminUser, updateAdminUser, resetUserPassword, adjustUserBalance } from '@/api/admin'
import type { User } from '@/types/user'
import { formatDate } from '@/utils/format'
import UserAvatar from '@/components/business/UserAvatar.vue'

const loading = ref(false)
const users = ref<User[]>([])
const total = ref(0)
const searchKeyword = ref('')
const filterRole = ref<number | undefined>()
const filterStatus = ref<number | undefined>()
const pagination = reactive({ page: 1, size: 10 })

const selectedUser = ref<User | null>(null)
const detailLoading = ref(false)
const showDetailDialog = ref(false)

const showEditDialog = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive({
  username: '',
  phone: '',
  email: '',
  realName: '',
  idCard: ''
})
const editRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

const showResetPasswordDialog = ref(false)
const resetPasswordLoading = ref(false)
const resetPasswordFormRef = ref<FormInstance>()
const resetPasswordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== resetPasswordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}
const resetPasswordRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const showAdjustBalanceDialog = ref(false)
const adjustBalanceLoading = ref(false)
const adjustBalanceFormRef = ref<FormInstance>()
const adjustBalanceForm = reactive({
  amount: 0,
  remark: ''
})
const adjustBalanceRules: FormRules = {
  amount: [{ required: true, message: '请输入调整金额', trigger: 'blur' }]
}

function roleLabel(role: number) {
  const map: Record<number, string> = { 0: '普通用户', 1: '服务者', 2: '管理员' }
  return map[role] || '未知'
}

function roleTagType(role: number) {
  const map: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'danger' }
  return map[role] || 'info'
}

function creditScoreTagType(score: number | undefined | null) {
  const s = score ?? 100
  if (s >= 90) return 'success'
  if (s >= 70) return ''
  if (s >= 50) return 'warning'
  return 'danger'
}

async function fetchUsers() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: pagination.page, size: pagination.size }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterRole.value !== undefined) params.role = filterRole.value
    if (filterStatus.value !== undefined) params.status = filterStatus.value
    const res = await getAdminUsers(params)
    users.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

async function handleToggleStatus(user: User) {
  const action = user.status === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户 "${user.username}" 吗？`, '提示', { type: 'warning' })
    await updateUserStatus(user.id, user.status === 0 ? 1 : 0)
    ElMessage.success(`${action}成功`)
    fetchUsers()
  } catch {
    // cancelled
  }
}

async function handleToggleRole(user: User) {
  const action = user.role === 1 ? '取消服务者' : '设为服务者'
  try {
    await ElMessageBox.confirm(`确定要将用户 "${user.username}" ${action}吗？`, '提示', { type: 'warning' })
    await updateUserRole(user.id, user.role === 1 ? 0 : 1)
    ElMessage.success('操作成功')
    fetchUsers()
  } catch {
    // cancelled
  }
}

async function handleViewDetail(user: User) {
  selectedUser.value = user
  showDetailDialog.value = true
  detailLoading.value = true
  try {
    const res = await getAdminUser(user.id)
    selectedUser.value = res.data
  } catch {
    // ignore
  } finally {
    detailLoading.value = false
  }
}

function handleEdit(user: User) {
  selectedUser.value = user
  editForm.username = user.username || ''
  editForm.phone = user.phone || ''
  editForm.email = user.email || ''
  editForm.realName = user.realName || ''
  editForm.idCard = user.idCard || ''
  showEditDialog.value = true
}

async function handleSaveEdit() {
  if (!editFormRef.value || !selectedUser.value) return
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    editLoading.value = true
    try {
      await updateAdminUser(selectedUser.value!.id, editForm)
      ElMessage.success('更新成功')
      showEditDialog.value = false
      fetchUsers()
    } catch {
      // ignore
    } finally {
      editLoading.value = false
    }
  })
}

function handleResetPassword(user: User) {
  selectedUser.value = user
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  showResetPasswordDialog.value = true
}

async function handleSaveResetPassword() {
  if (!resetPasswordFormRef.value || !selectedUser.value) return
  await resetPasswordFormRef.value.validate(async (valid) => {
    if (!valid) return
    resetPasswordLoading.value = true
    try {
      await resetUserPassword(selectedUser.value!.id, resetPasswordForm.newPassword)
      ElMessage.success('密码重置成功')
      showResetPasswordDialog.value = false
    } catch {
      // ignore
    } finally {
      resetPasswordLoading.value = false
    }
  })
}

function handleAdjustBalance(user: User) {
  selectedUser.value = user
  adjustBalanceForm.amount = 0
  adjustBalanceForm.remark = ''
  showAdjustBalanceDialog.value = true
}

async function handleSaveAdjustBalance() {
  if (!adjustBalanceFormRef.value || !selectedUser.value) return
  await adjustBalanceFormRef.value.validate(async (valid) => {
    if (!valid) return
    adjustBalanceLoading.value = true
    try {
      await adjustUserBalance(selectedUser.value!.id, adjustBalanceForm.amount, adjustBalanceForm.remark)
      ElMessage.success('余额调整成功')
      showAdjustBalanceDialog.value = false
      fetchUsers()
    } catch {
      // ignore
    } finally {
      adjustBalanceLoading.value = false
    }
  })
}

async function handleMoreAction(command: string, user: User) {
  if (command === 'toggleStatus') {
    await handleToggleStatus(user)
  } else if (command === 'toggleRole') {
    await handleToggleRole(user)
  }
}

onMounted(() => fetchUsers())
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.admin-user-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  margin: 0;
}

.filter-card {
  :deep(.el-card__body) {
    padding: $spacing-md;
  }
}

.filter-row {
  display: flex;
  gap: $spacing-sm;
  align-items: center;
  flex-wrap: wrap;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-md;
}

.detail-content {
  min-height: 100px;
}

:deep(.el-table) {
  --el-table-bg-color: #0f172a;
  --el-table-header-bg-color: #1e293b;
  --el-table-row-hover-bg-color: #334155;
  --el-table-border-color: #334155;
  --el-table-text-color: #f1f5f9;
  --el-table-header-text-color: #94a3b8;
}

:deep(.el-table__row) {
  background-color: #0f172a !important;
}
</style>
