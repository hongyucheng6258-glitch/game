<template>
  <div class="favorite-page">
    <h2 class="page-title">我的收藏</h2>

    <div v-loading="loading" class="favorite-list">
      <template v-if="services.length > 0">
        <div class="service-grid">
          <div v-for="item in services" :key="item.id" class="service-card-wrapper">
            <ServiceCard :service="item" />
          </div>
        </div>
      </template>
      <el-empty v-else description="暂无收藏的服务">
        <el-button type="primary" @click="$router.push('/service')">去发现好服务</el-button>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        :page-sizes="[12, 24, 36]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="fetchFavorites"
        @current-change="fetchFavorites"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getFavorites } from '@/api/favorite'
import type { Service } from '@/types/service'
import type { PageData } from '@/types/common'
import ServiceCard from '@/components/business/ServiceCard.vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const services = ref<Service[]>([])
const total = ref(0)

const pagination = reactive({
  page: 1,
  size: 12,
})

async function fetchFavorites() {
  loading.value = true
  try {
    const res = await getFavorites({
      page: pagination.page,
      size: pagination.size,
    })
    if (res.code === 200) {
      // 这里需要根据实际的响应结构调整
      services.value = (res.data.records || []).map(item => item.service || {})
      total.value = res.data.total || 0
    } else {
      ElMessage.error('获取收藏列表失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.favorite-page {
  padding: $spacing-lg 0;
}

// 页面标题 - Orbitron字体 + 渐变文字
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: $spacing-lg;
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

.favorite-list {
  min-height: 300px;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: $spacing-md;
}

// 收藏卡片 - hover发光效果
.service-card-wrapper {
  min-height: 0;
  transition: transform $transition-normal;
  border-radius: $border-radius-lg;

  &:hover {
    transform: translateY(-4px);

    :deep(.el-card) {
      border-color: rgba($neon-cyan, 0.3);
      box-shadow:
        $shadow-neon-cyan,
        0 0 30px rgba($neon-cyan, 0.12);
    }
  }

  :deep(.el-card) {
    background: $glass-bg;
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
    border: 1px solid $border-glow;
    border-radius: $border-radius-lg;
    transition: border-color $transition-normal, box-shadow $transition-normal;
  }
}

// 空状态优化
.favorite-list {
  :deep(.el-empty) {
    padding: $spacing-3xl 0;

    .el-empty__description p {
      color: $text-muted;
      font-size: 15px;
    }

    .el-button--primary {
      background: linear-gradient(135deg, $neon-cyan, $primary-color);
      border: none;
      box-shadow: 0 4px 12px rgba($neon-cyan, 0.3);

      &:hover {
        box-shadow:
          0 0 20px rgba($neon-cyan, 0.5),
          0 0 40px rgba($neon-cyan, 0.2);
        transform: translateY(-1px);
      }
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-xl;
}

// 响应式
@media (max-width: 768px) {
  .service-grid {
    grid-template-columns: 1fr;
  }
}
</style>
