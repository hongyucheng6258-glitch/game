import { get, post, put, del } from './request'

// Admin Users
export function getAdminUsers(params?: any) { return get('/admin/users', params) }
export function getAdminUser(id: number) { return get(`/admin/users/${id}`) }
export function updateUserStatus(id: number, status: number) { return put(`/admin/users/${id}/status`, { status }) }
export function updateUserRole(id: number, role: number) { return put(`/admin/users/${id}/role`, { role }) }
export function updateAdminUser(id: number, data: any) { return put(`/admin/users/${id}`, data) }
export function resetUserPassword(id: number, newPassword: string) { return put(`/admin/users/${id}/reset-password`, { newPassword }) }
export function adjustUserBalance(id: number, amount: number, remark: string) { return put(`/admin/users/${id}/balance`, { amount, remark }) }

// Admin Services
export function getAdminServices(params?: any) { return get('/admin/services', params) }
export function adminToggleService(id: number) { return put(`/admin/services/${id}/status`) }
export function adminDeleteService(id: number) { return del(`/admin/services/${id}`) }
export function approveService(id: number) { return put(`/admin/services/${id}/approve`) }
export function rejectService(id: number) { return put(`/admin/services/${id}/reject`) }

// Admin Orders
export function getAdminOrders(params?: any) { return get('/admin/orders', params) }
export function getAdminOrder(orderNo: string) { return get(`/admin/orders/${orderNo}`) }

// Admin Payments
export function getAdminPayments(params?: any) { return get('/admin/payments', params) }
export function getAdminPaymentStats() { return get('/admin/payments/statistics') }

// Admin Withdrawals
export function getAdminWithdrawals(params?: any) { return get('/admin/withdrawals', params) }
export function auditWithdrawal(id: number, data: any) { return put(`/admin/withdrawals/${id}/audit`, data) }

// Admin Reviews
export function getAdminReviews(params?: any) { return get('/admin/reviews', params) }
export function adminDeleteReview(id: number) { return del(`/admin/reviews/${id}`) }

// Admin Announcements
export function getAdminAnnouncements(params?: any) { return get('/admin/announcements', params) }
export function createAnnouncement(data: any) { return post('/admin/announcements', data) }
export function updateAnnouncement(id: number, data: any) { return put(`/admin/announcements/${id}`, data) }
export function publishAnnouncement(id: number) { return post(`/admin/announcements/${id}/publish`) }
export function toggleAnnouncementTop(id: number, top: boolean) { return post(`/admin/announcements/${id}/top?top=${top}`) }
export function deleteAnnouncement(id: number) { return del(`/admin/announcements/${id}`) }

// Admin Tags
export function getAdminTags() { return get('/admin/tags') }
export function createTag(data: any) { return post('/admin/tags', data) }
export function updateTag(id: number, data: any) { return put(`/admin/tags/${id}`, data) }
export function deleteTag(id: number) { return del(`/admin/tags/${id}`) }

// Admin Activities
export function getAdminActivities(params?: any) { return get('/admin/activities', params) }
export function getAdminActivity(id: number) { return get(`/admin/activities/${id}`) }
export function createActivity(data: any) { return post('/admin/activities', data) }
export function updateActivity(id: number, data: any) { return put(`/admin/activities/${id}`, data) }
export function publishActivity(id: number) { return post(`/admin/activities/${id}/publish`) }
export function endActivity(id: number) { return post(`/admin/activities/${id}/end`) }
export function deleteActivity(id: number) { return del(`/admin/activities/${id}`) }
export function updateActivityStatuses() { return post('/admin/activities/update-statuses') }

// Admin Statistics
export function getDashboardStats() { return get('/admin/statistics/dashboard') }
