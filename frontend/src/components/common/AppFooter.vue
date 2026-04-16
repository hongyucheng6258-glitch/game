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
  background:
    linear-gradient(180deg, rgba($bg-dark, 0.6), rgba($bg-abyss, 0.95)),
    repeating-linear-gradient(
      0deg,
      transparent,
      transparent 39px,
      rgba($neon-cyan, 0.03) 39px,
      rgba($neon-cyan, 0.03) 40px
    ),
    repeating-linear-gradient(
      90deg,
      transparent,
      transparent 39px,
      rgba($neon-cyan, 0.03) 39px,
      rgba($neon-cyan, 0.03) 40px
    );
  border-top: none;
  padding: 32px 20px 20px;
  margin-top: auto;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, $neon-cyan, $neon-purple, transparent);
    box-shadow: 0 0 8px rgba($neon-cyan, 0.3), 0 0 8px rgba($neon-purple, 0.2);
  }
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
  border-bottom: 1px solid rgba($neon-cyan, 0.06);
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
  font-family: 'Orbitron', sans-serif;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, $neon-cyan, $neon-purple);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 0 4px rgba($neon-cyan, 0.3));
  letter-spacing: 0.5px;
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
    text-decoration: none;
    transition: all $transition-normal;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 0;
      width: 0;
      height: 1px;
      background: $neon-cyan;
      box-shadow: 0 0 6px rgba($neon-cyan, 0.5);
      transition: width $transition-normal;
    }

    &:hover {
      color: $neon-cyan;
      text-shadow: 0 0 8px rgba($neon-cyan, 0.4);

      &::after {
        width: 100%;
      }
    }
  }
}

.footer-bottom {
  text-align: center;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    bottom: -8px;
    left: 10%;
    right: 10%;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba($neon-cyan, 0.2), rgba($neon-purple, 0.2), transparent);
  }

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
