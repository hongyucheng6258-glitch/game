<template>
  <div class="test-page">
    <h1 class="test-title">测试页面</h1>
    <p class="test-text">这是一个测试页面，用于验证前端是否正常工作</p>
    <el-button type="primary" class="test-button">测试按钮</el-button>
    <div class="test-card glass-effect">
      <h3>测试卡片</h3>
      <p>这是一个测试卡片，用于验证样式是否正常</p>
    </div>
  </div>
</template>

<script setup lang="ts">
// 测试页面
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.test-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-xl;
  padding: $spacing-xl;
  background: $bg-gradient;
}

.test-title {
  font-size: 32px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  font-family: $font-family-heading;
  background: linear-gradient(135deg, $primary-color, $secondary-color);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.test-text {
  font-size: 18px;
  color: $text-secondary;
  text-align: center;
}

.test-button {
  padding: 12px 32px;
  font-size: 16px;
  font-weight: $font-weight-bold;
}

.test-card {
  padding: $spacing-xl;
  border-radius: $border-radius-lg;
  text-align: center;
  max-width: 400px;
  width: 100%;
  h3 {
    color: $text-primary;
    margin-bottom: $spacing-md;
    font-family: $font-family-heading;
  }
  p {
    color: $text-secondary;
  }
}
</style>