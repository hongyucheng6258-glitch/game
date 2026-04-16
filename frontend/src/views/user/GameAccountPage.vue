<template>
  <div class="game-account-page">
    <div class="page-header">
      <h2 class="page-title">游戏账号管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 添加账号
      </el-button>
    </div>

    <!-- 账号列表 -->
    <div v-loading="loading" class="account-list">
      <template v-if="accounts.length > 0">
        <el-card
          v-for="account in accounts"
          :key="account.id"
          shadow="never"
          class="account-card"
        >
          <div class="account-content">
            <div class="account-icon">
              <el-icon :size="24" color="#6366f1"><Monitor /></el-icon>
            </div>
            <div class="account-info">
              <div class="account-row">
                <span class="game-type">{{ account.gameType }}</span>
                <el-tag size="small" type="info">{{ account.region || '未设置区服' }}</el-tag>
                <el-tag v-if="account.accountLevel" size="small" type="success">等级：{{ account.accountLevel }}</el-tag>
              </div>
              <span class="game-account">账号：{{ account.accountName }}</span>
              <span class="game-password">密码：{{ account.accountPassword ? '********' : '未设置' }}</span>
              <span v-if="account.accountDescription" class="account-desc">备注：{{ account.accountDescription }}</span>
            </div>
            <div class="account-actions">
              <el-button text type="primary" size="small" @click="openDialog(account)">
                编辑
              </el-button>
              <el-button text type="danger" size="small" @click="handleDelete(account.id)">
                删除
              </el-button>
            </div>
          </div>
        </el-card>
      </template>
      <el-empty v-else description="暂无游戏账号，点击上方按钮添加" />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑游戏账号' : '添加游戏账号'"
      width="480px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="游戏类型" prop="gameType">
          <el-select v-model="formData.gameType" placeholder="请选择游戏" style="width: 100%">
            <el-option
              v-for="game in GAME_TYPES"
              :key="game"
              :label="game"
              :value="game"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="游戏账号" prop="accountName">
          <el-input v-model="formData.accountName" placeholder="请输入游戏账号" />
        </el-form-item>
        <el-form-item label="游戏密码" prop="accountPassword">
          <el-input
            v-model="formData.accountPassword"
            type="password"
            placeholder="请输入游戏密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="账号等级" prop="accountLevel">
          <el-input v-model="formData.accountLevel" placeholder="请输入账号等级（选填）" clearable />
        </el-form-item>
        <el-form-item label="区服" prop="region">
          <el-input v-model="formData.region" placeholder="请输入区服（选填）" clearable />
        </el-form-item>
        <el-form-item label="备注" prop="accountDescription">
          <el-input
            v-model="formData.accountDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（选填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '确认添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Monitor } from '@element-plus/icons-vue'
import { get, post, put, del } from '@/api/request'
import { GAME_TYPES } from '@/utils/constants'

interface GameAccount {
  id: number
  gameType: string
  accountName: string
  accountPassword: string
  accountLevel: string | null
  region: string | null
  accountDescription: string | null
}

const loading = ref(false)
const accounts = ref<GameAccount[]>([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive({
  gameType: '',
  accountName: '',
  accountPassword: '',
  accountLevel: '',
  region: '',
  accountDescription: '',
})

const formRules: FormRules = {
  gameType: [{ required: true, message: '请选择游戏类型', trigger: 'change' }],
  accountName: [{ required: true, message: '请输入游戏账号', trigger: 'blur' }],
  accountPassword: [{ required: true, message: '请输入游戏密码', trigger: 'blur' }],
}

function openDialog(account?: GameAccount) {
  if (account) {
    isEdit.value = true
    editId.value = account.id
    formData.gameType = account.gameType
    formData.accountName = account.accountName
    formData.accountPassword = ''
    formData.accountLevel = account.accountLevel || ''
    formData.region = account.region || ''
    formData.accountDescription = account.accountDescription || ''
  } else {
    isEdit.value = false
    editId.value = null
    formData.gameType = ''
    formData.accountName = ''
    formData.accountPassword = ''
    formData.accountLevel = ''
    formData.region = ''
    formData.accountDescription = ''
  }
  dialogVisible.value = true
}

async function fetchAccounts() {
  loading.value = true
  try {
    const res = await get<GameAccount[]>('/game-accounts')
    accounts.value = res.data
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const data = {
        gameType: formData.gameType,
        accountName: formData.accountName,
        accountPassword: formData.accountPassword,
        accountLevel: formData.accountLevel || undefined,
        region: formData.region || undefined,
        accountDescription: formData.accountDescription || undefined,
      }

      if (isEdit.value && editId.value) {
        await put(`/game-accounts/${editId.value}`, data)
        ElMessage.success('修改成功')
      } else {
        await post('/game-accounts', data)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      fetchAccounts()
    } catch {
      // handled by interceptor
    } finally {
      submitting.value = false
    }
  })
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除该游戏账号吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await del(`/game-accounts/${id}`)
    ElMessage.success('删除成功')
    fetchAccounts()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  fetchAccounts()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.game-account-page {
  padding: $spacing-lg 0;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-lg;

  // 添加按钮 - 霓虹发光
  :deep(.el-button--primary) {
    background: linear-gradient(135deg, $neon-cyan, $primary-color);
    border: none;
    box-shadow: 0 4px 12px rgba($neon-cyan, 0.3);
    transition: all $transition-normal;

    &:hover {
      background: linear-gradient(135deg, lighten($neon-cyan, 5%), $primary-light);
      box-shadow:
        0 0 20px rgba($neon-cyan, 0.5),
        0 0 40px rgba($neon-cyan, 0.2);
      transform: translateY(-1px);
    }
  }
}

// 页面标题 - Orbitron字体 + 渐变文字
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  position: relative;
  padding-left: 16px;
  font-family: 'Orbitron', monospace;
  background: linear-gradient(135deg, $neon-cyan, $primary-light, $neon-purple);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 0 8px rgba($neon-cyan, 0.3));

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 24px;
    border-radius: 2px;
    background: linear-gradient(180deg, $neon-cyan, $primary-color);
    box-shadow: 0 0 8px rgba($neon-cyan, 0.5);
  }
}

// 账号列表
.account-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  min-height: 200px;
}

// 游戏账号卡片 - neon-border + 对应游戏类型颜色左边框
.account-card {
  background: $glass-bg;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $border-glow;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-glow;
  transition: transform $transition-normal, box-shadow $transition-normal, border-color $transition-normal;
  border-left: 4px solid $primary-color;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-glow-strong, $shadow-neon-cyan;
    border-color: rgba($neon-cyan, 0.3);
    border-left-color: $neon-cyan;
  }

  :deep(.el-card__body) {
    padding: $spacing-md;
  }
}

.account-content {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.account-icon {
  width: 48px;
  height: 48px;
  border-radius: $border-radius;
  background: linear-gradient(135deg, rgba($neon-cyan, 0.15), rgba($primary-color, 0.05));
  border: 1px solid rgba($neon-cyan, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 0 12px rgba($neon-cyan, 0.1);
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

.game-type {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
}

.game-account,
.game-password,
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

  // 操作按钮 - 霓虹发光hover
  :deep(.el-button--primary.is-text) {
    &:hover {
      text-shadow: 0 0 8px rgba($neon-cyan, 0.5);
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .account-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .account-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
