<template>
  <div class="favorite-page">
    <h1 class="page-title">我的收藏</h1>
    
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>
    
    <div v-else-if="favorites.length === 0" class="empty-state">
      <el-icon class="empty-icon"><Star /></el-icon>
      <p>暂无收藏的服务</p>
      <el-button type="primary" @click="$router.push('/')">去浏览服务</el-button>
    </div>
    
    <div v-else class="favorite-list">
      <div v-for="favorite in favorites" :key="favorite.id" class="favorite-item">
        <div class="favorite-content">
          <div class="service-info">
            <h3 class="service-title">{{ favorite.service.name }}</h3>
            <p class="service-description">{{ favorite.service.description }}</p>
            <div class="service-meta">
              <span class="price">¥{{ favorite.service.price }}</span>
              <span class="provider">{{ favorite.service.provider.username }}</span>
            </div>
          </div>
          <div class="action-buttons">
            <el-button type="primary" @click="viewService(favorite.service.id)">
              查看详情
            </el-button>
            <el-button type="danger" @click="removeFavorite(favorite.service.id)">
              <el-icon><Delete /></el-icon>
              取消收藏
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="!loading && favorites.length > 0" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFavorites, removeFavorite as removeFavoriteApi } from '@/api/favorite'
import { ElMessage } from 'element-plus'
import { Star, Delete } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const favorites = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const loadFavorites = async () => {
  loading.value = true
  try {
    const response = await getFavorites({
      page: currentPage.value,
      size: pageSize.value
    })
    if (response.code === 200) {
      favorites.value = response.data.items.map((item: any) => ({
        ...item,
        service: {
          id: item.serviceId,
          name: item.service?.name || '服务已删除',
          description: item.service?.description || '服务描述已不可用',
          price: item.service?.price || 0,
          provider: {
            username: item.service?.provider?.username || '未知提供者'
          }
        }
      }))
      total.value = response.data.total
    } else {
      ElMessage.error('获取收藏列表失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

const removeFavorite = async (serviceId: number) => {
  try {
    const response = await removeFavoriteApi(serviceId)
    if (response.code === 200) {
      ElMessage.success('取消收藏成功')
      loadFavorites()
    } else {
      ElMessage.error('取消收藏失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
  }
}

const viewService = (serviceId: number) => {
  router.push(`/service/${serviceId}`)
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadFavorites()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadFavorites()
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorite-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 30px;
  color: #333;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  color: #dcdfe6;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: #909399;
  margin-bottom: 24px;
}

.favorite-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.favorite-item {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  transition: all 0.3s ease;
}

.favorite-item:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.favorite-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.service-info {
  flex: 1;
  min-width: 300px;
}

.service-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
}

.service-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.service-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 14px;
}

.price {
  font-weight: bold;
  color: #f56c6c;
}

.provider {
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .favorite-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .action-buttons {
    align-self: flex-end;
  }
}
</style>