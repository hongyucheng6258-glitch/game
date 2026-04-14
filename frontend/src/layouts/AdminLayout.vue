<template>
  <div class="admin-layout">
    <div class="admin-header">
      <div class="admin-logo">
        <el-icon :size="24"><Setting /></el-icon>
        <span>管理后台</span>
      </div>
      <div class="admin-user">
        <el-dropdown>
          <span class="admin-username">
            {{ userStore.userInfo?.username }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/')">返回前台</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    <div class="admin-body">
      <AppSidebar :menus="adminMenus" />
      <main class="admin-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Setting, ArrowDown } from '@element-plus/icons-vue'
import AppSidebar from '@/components/common/AppSidebar.vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const adminMenus = [
  { title: '仪表盘', icon: 'DataAnalysis', path: '/admin' },
  { title: '用户管理', icon: 'User', path: '/admin/users' },
  { title: '服务管理', icon: 'Goods', path: '/admin/services' },
  { title: '订单管理', icon: 'Document', path: '/admin/orders' },
  { title: '财务管理', icon: 'Money', path: '/admin/payments' },
  { title: '提现记录', icon: 'CreditCard', path: '/admin/withdrawals' },
  { title: '评价管理', icon: 'ChatDotRound', path: '/admin/reviews' },
  { title: '投诉仲裁', icon: 'Warning', path: '/admin/complaints' },
  { title: '公告管理', icon: 'Bell', path: '/admin/announcements' },
  { title: '活动管理', icon: 'Present', path: '/admin/activities' },
  { title: '标签管理', icon: 'PriceTag', path: '/admin/tags' },
  { title: '系统设置', icon: 'Setting', path: '/admin/settings' },
]

function handleLogout() {
  userStore.logout()
  router.push('/auth/login')
}
</script>

<style scoped lang="scss">
@import '@/assets/styles/admin-table.scss';

.admin-layout {
  min-height: 100vh;
  background: #0f172a;
}
.admin-header {
  height: 60px;
  background: #1e293b;
  border-bottom: 1px solid #334155;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.admin-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #f1f5f9;
}
.admin-username {
  color: #94a3b8;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
.admin-body {
  display: flex;
  height: calc(100vh - 60px);
}
.admin-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  .el-table {
    --el-table-bg-color: #0f172a;
    --el-table-header-bg-color: #1e293b;
    --el-table-row-hover-bg-color: #334155;
    --el-table-border-color: #334155;
    --el-table-text-color: #f1f5f9;
    --el-table-header-text-color: #94a3b8;
  }
  .el-table__row {
    background-color: #0f172a !important;
  }
  .el-table__row:nth-child(even) {
    background-color: #1e293b !important;
  }
}
</style>
