<template>
  <footer class="app-footer">
    <div class="footer-inner">
      <div class="footer-top">
        <div class="footer-brand">
          <div class="footer-logo">
            <el-icon :size="18" color="#a78bfa"><Trophy /></el-icon>
            <span class="footer-brand-name">锐竞电竞</span>
          </div>
          <p class="footer-desc">专业电竞陪玩代打平台</p>
        </div>
        <div class="footer-links">
          <a href="#">关于我们</a>
          <a href="#">服务条款</a>
          <a href="#">隐私政策</a>
          <a href="#">联系我们</a>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2026 锐竞电竞 - All Rights Reserved</p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { Trophy } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.app-footer {
  background: linear-gradient(180deg, rgba(30, 41, 59, 0.5), rgba(15, 23, 42, 0.8));
  border-top: 1px solid rgba(148, 163, 184, 0.06);
  padding: 32px 20px 20px;
  margin-top: auto;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.footer-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.06);
  margin-bottom: 16px;
}

.footer-brand {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.footer-brand-name {
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #818cf8, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.footer-desc {
  color: $text-muted;
  font-size: 13px;
  margin: 0;
}

.footer-links {
  display: flex;
  gap: 24px;

  a {
    color: $text-muted;
    font-size: 13px;
    transition: color $transition-fast;

    &:hover {
      color: $primary-light;
    }
  }
}

.footer-bottom {
  text-align: center;

  p {
    color: $text-muted;
    font-size: 12px;
    margin: 0;
  }
}

@media (max-width: 768px) {
  .footer-top {
    flex-direction: column;
    gap: 16px;
  }

  .footer-links {
    flex-wrap: wrap;
    gap: 16px;
  }
}
</style>
