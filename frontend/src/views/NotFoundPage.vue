<template>
  <div class="not-found-page">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <div class="error-title">页面不存在</div>
      <div class="error-desc">抱歉，您访问的页面不存在或已被移除</div>
      <div class="error-actions">
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
        <el-button @click="$router.back()">返回上页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-abyss;
  text-align: center;
}

.not-found-content {
  animation: float-up 0.6s ease forwards;
}

.error-code {
  font-family: 'Orbitron', sans-serif;
  font-size: 120px;
  font-weight: 900;
  background: linear-gradient(135deg, $neon-cyan, $primary-light, $neon-purple);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  text-shadow: 0 0 40px rgba($neon-cyan, 0.3);
}

.error-title {
  font-size: 24px;
  font-weight: 600;
  color: $text-primary;
  margin-top: 16px;
}

.error-desc {
  font-size: 14px;
  color: $text-secondary;
  margin-top: 8px;
}

.error-actions {
  margin-top: 32px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

@keyframes float-up {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
