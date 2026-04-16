import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/storage'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/home/HomePage.vue'), meta: { title: '首页' } },
      { path: 'service', name: 'ServiceList', component: () => import('@/views/service/ServiceListPage.vue'), meta: { title: '服务列表' } },
      { path: 'service/:id', name: 'ServiceDetail', component: () => import('@/views/service/ServiceDetailPage.vue'), meta: { title: '服务详情' } },
      { path: 'ranking', name: 'Ranking', component: () => import('@/views/ranking/RankingPage.vue'), meta: { title: '排行榜' } },
      { path: 'announcement', name: 'Announcement', component: () => import('@/views/announcement/AnnouncementPage.vue'), meta: { title: '系统公告' } },
    ],
  },
  {
    path: '/auth',
    component: () => import('@/layouts/BlankLayout.vue'),
    children: [
      { path: 'login', name: 'Login', component: () => import('@/views/auth/LoginPage.vue'), meta: { title: '登录', guest: true } },
      { path: 'register', name: 'Register', component: () => import('@/views/auth/RegisterPage.vue'), meta: { title: '注册', guest: true } },
    ],
  },
  {
    path: '/user',
    component: () => import('@/layouts/UserCenterLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'UserCenter', component: () => import('@/views/user/UserCenterPage.vue'), meta: { title: '用户中心' } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/user/ProfilePage.vue'), meta: { title: '个人信息' } },
      { path: 'game-accounts', name: 'GameAccounts', component: () => import('@/views/user/GameAccountPage.vue'), meta: { title: '游戏账号' } },
      { path: 'favorites', name: 'Favorites', component: () => import('@/views/user/FavoritePage.vue'), meta: { title: '我的收藏' } },
      { path: 'balance', name: 'Balance', component: () => import('@/views/user/BalancePage.vue'), meta: { title: '余额管理' } },
      { path: 'payment-records', name: 'PaymentRecords', component: () => import('@/views/user/PaymentRecordPage.vue'), meta: { title: '支付记录' } },
      { path: 'reviews', name: 'MyReviews', component: () => import('@/views/review/MyReviewPage.vue'), meta: { title: '我的评价' } },
      { path: 'complaints', name: 'MyComplaints', component: () => import('@/views/complaint/MyComplaintPage.vue'), meta: { title: '我的投诉' } },
      { path: 'level', name: 'LevelCenter', component: () => import('@/views/user/LevelCenterPage.vue'), meta: { title: '等级中心' } },
    ],
  },
  {
    path: '/provider',
    component: () => import('@/layouts/UserCenterLayout.vue'),
    meta: { requiresAuth: true, requiresProvider: true },
    children: [
      { path: '', name: 'ProviderCenter', component: () => import('@/views/provider/ProviderCenterPage.vue'), meta: { title: '服务者中心' } },
      { path: 'services', name: 'MyServices', component: () => import('@/views/provider/MyServicePage.vue'), meta: { title: '我的服务' } },
      { path: 'service/create', name: 'ServiceCreate', component: () => import('@/views/provider/ServiceFormPage.vue'), meta: { title: '发布服务' } },
      { path: 'service/edit/:id', name: 'ServiceEdit', component: () => import('@/views/provider/ServiceFormPage.vue'), meta: { title: '编辑服务' } },
      { path: 'orders', name: 'ProviderOrders', component: () => import('@/views/provider/ProviderOrderPage.vue'), meta: { title: '接单管理' } },
    ],
  },
  {
    path: '/order',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: 'confirm/:serviceId', name: 'OrderConfirm', component: () => import('@/views/order/OrderConfirmPage.vue'), meta: { title: '确认订单' } },
      { path: 'list', name: 'OrderList', component: () => import('@/views/order/OrderListPage.vue'), meta: { title: '我的订单' } },
      { path: ':orderNo', name: 'OrderDetail', component: () => import('@/views/order/OrderDetailPage.vue'), meta: { title: '订单详情' } },
    ],
  },
  {
    path: '/complaint',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: 'create', name: 'ComplaintCreate', component: () => import('@/views/complaint/ComplaintFormPage.vue'), meta: { title: '投诉订单' } },
    ],
  },
  {
    path: '/message',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'MessageCenter', component: () => import('@/views/message/MessageCenterPage.vue'), meta: { title: '消息中心' } },
      { path: 'chat/:userId', name: 'Chat', component: () => import('@/views/message/ChatPage.vue'), meta: { title: '聊天' } },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('@/views/admin/AdminDashboardPage.vue'), meta: { title: '管理后台' } },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/AdminUserPage.vue'), meta: { title: '用户管理' } },
      { path: 'services', name: 'AdminServices', component: () => import('@/views/admin/AdminServicePage.vue'), meta: { title: '服务管理' } },
      { path: 'orders', name: 'AdminOrders', component: () => import('@/views/admin/AdminOrderPage.vue'), meta: { title: '订单管理' } },
      { path: 'payments', name: 'AdminPayments', component: () => import('@/views/admin/AdminPaymentPage.vue'), meta: { title: '财务管理' } },
      { path: 'withdrawals', name: 'AdminWithdrawals', component: () => import('@/views/admin/AdminWithdrawalPage.vue'), meta: { title: '提现记录' } },
      { path: 'reviews', name: 'AdminReviews', component: () => import('@/views/admin/AdminReviewPage.vue'), meta: { title: '评价管理' } },
      { path: 'complaints', name: 'AdminComplaints', component: () => import('@/views/admin/AdminComplaintPage.vue'), meta: { title: '投诉仲裁' } },
      { path: 'announcements', name: 'AdminAnnouncements', component: () => import('@/views/admin/AdminAnnouncementPage.vue'), meta: { title: '公告管理' } },
      { path: 'tags', name: 'AdminTags', component: () => import('@/views/admin/AdminTagPage.vue'), meta: { title: '标签管理' } },
      { path: 'activities', name: 'AdminActivities', component: () => import('@/views/admin/AdminActivityPage.vue'), meta: { title: '活动管理' } },
      { path: 'settings', name: 'AdminSettings', component: () => import('@/views/admin/AdminSettingsPage.vue'), meta: { title: '系统设置' } },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundPage.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || '电竞陪玩平台'} - 电竞陪玩代打平台`

  const token = getToken()

  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.guest && token) {
    next({ name: 'Home' })
  } else if (to.meta.requiresAdmin && token) {
    // Check admin role from localStorage
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      if (user.role !== 2) {
        next({ name: 'Home' })
        return
      }
    }
    next()
  } else if (to.meta.requiresProvider && token) {
    // Check provider role from localStorage
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      if (user.role !== 1 && user.role !== 2) {
        next({ name: 'Home' })
        return
      }
    }
    next()
  } else {
    next()
  }
})

export default router
