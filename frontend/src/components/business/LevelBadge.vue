<template>
  <div class="level-badge" :style="{ color: levelColor }">
    <span v-if="icon" class="level-icon">{{ icon }}</span>
    <span class="level-name">{{ name }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  level: number
  name?: string
  icon?: string
}

const props = withDefaults(defineProps<Props>(), {
  name: '',
  icon: ''
})

const levelColor = computed(() => {
  const colors = [
    '#999999', // 1 新手
    '#cd7f32', // 2 青铜
    '#c0c0c0', // 3 白银
    '#ffd700', // 4 黄金
    '#e5e4e2', // 5 铂金
    '#b9f2ff', // 6 钻石
    '#ff6b6b', // 7 大师
    '#ff4757', // 8 王者
    '#ffd700', // 9 传奇
    '#ff0000'  // 10 至尊
  ]
  return colors[props.level - 1] || '#999999'
})
</script>

<style scoped lang="scss">
.level-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  position: relative;
  overflow: hidden;

  // 各等级对应不同霓虹色发光效果
  // 1 新手 - 灰色微光
  &[style*="#999999"] {
    background: rgba(153, 153, 153, 0.12);
    border: 1px solid rgba(153, 153, 153, 0.2);
    box-shadow: 0 0 6px rgba(153, 153, 153, 0.15);
  }

  // 2 青铜 - 铜色暖光
  &[style*="#cd7f32"] {
    background: rgba(205, 127, 50, 0.12);
    border: 1px solid rgba(205, 127, 50, 0.25);
    box-shadow: 0 0 8px rgba(205, 127, 50, 0.2);
  }

  // 3 白银 - 银白冷光
  &[style*="#c0c0c0"] {
    background: rgba(192, 192, 192, 0.12);
    border: 1px solid rgba(192, 192, 192, 0.25);
    box-shadow: 0 0 8px rgba(192, 192, 192, 0.2);
  }

  // 4 黄金 - 金色暖光
  &[style*="#ffd700"] {
    background: rgba(255, 215, 0, 0.12);
    border: 1px solid rgba(255, 215, 0, 0.3);
    box-shadow: 0 0 10px rgba(255, 215, 0, 0.25);
  }

  // 5 铂金 - 冰蓝冷光
  &[style*="#e5e4e2"] {
    background: rgba(229, 228, 226, 0.1);
    border: 1px solid rgba(229, 228, 226, 0.25);
    box-shadow: 0 0 10px rgba(229, 228, 226, 0.2);
  }

  // 6 钻石 - 青色霓虹（高等级加强发光）
  &[style*="#b9f2ff"] {
    background: rgba(185, 242, 255, 0.1);
    border: 1px solid rgba(185, 242, 255, 0.35);
    box-shadow:
      0 0 12px rgba(185, 242, 255, 0.35),
      0 0 24px rgba(185, 242, 255, 0.15);
    animation: diamond-glow 2.5s ease-in-out infinite;
  }

  // 7 大师 - 红色霓虹（高等级加强发光）
  &[style*="#ff6b6b"] {
    background: rgba(255, 107, 107, 0.1);
    border: 1px solid rgba(255, 107, 107, 0.35);
    box-shadow:
      0 0 12px rgba(255, 107, 107, 0.35),
      0 0 24px rgba(255, 107, 107, 0.15);
    animation: master-glow 2.5s ease-in-out infinite;
  }

  // 8 王者 - 深红霓虹（高等级加强发光）
  &[style*="#ff4757"] {
    background: rgba(255, 71, 87, 0.12);
    border: 1px solid rgba(255, 71, 87, 0.4);
    box-shadow:
      0 0 14px rgba(255, 71, 87, 0.4),
      0 0 28px rgba(255, 71, 87, 0.2);
    animation: king-glow 2s ease-in-out infinite;
  }

  // 9 传奇 - 金色强霓虹（高等级加强发光）
  &[style*="rgb(255, 215, 0)"] {
    background: rgba(255, 215, 0, 0.12);
    border: 1px solid rgba(255, 215, 0, 0.4);
    box-shadow:
      0 0 14px rgba(255, 215, 0, 0.4),
      0 0 28px rgba(255, 215, 0, 0.2);
    animation: legend-glow 2s ease-in-out infinite;
  }

  // 10 至尊 - 红色最强霓虹（高等级加强发光）
  &[style*="#ff0000"] {
    background: rgba(255, 0, 0, 0.12);
    border: 1px solid rgba(255, 0, 0, 0.45);
    box-shadow:
      0 0 16px rgba(255, 0, 0, 0.45),
      0 0 32px rgba(255, 0, 0, 0.25),
      0 0 48px rgba(255, 0, 0, 0.1);
    animation: supreme-glow 1.8s ease-in-out infinite;
  }
  
  .level-icon {
    font-size: 14px;
  }
}

// 高等级发光动画
@keyframes diamond-glow {
  0%, 100% {
    box-shadow:
      0 0 12px rgba(185, 242, 255, 0.35),
      0 0 24px rgba(185, 242, 255, 0.15);
  }
  50% {
    box-shadow:
      0 0 18px rgba(185, 242, 255, 0.5),
      0 0 36px rgba(185, 242, 255, 0.25);
  }
}

@keyframes master-glow {
  0%, 100% {
    box-shadow:
      0 0 12px rgba(255, 107, 107, 0.35),
      0 0 24px rgba(255, 107, 107, 0.15);
  }
  50% {
    box-shadow:
      0 0 18px rgba(255, 107, 107, 0.5),
      0 0 36px rgba(255, 107, 107, 0.25);
  }
}

@keyframes king-glow {
  0%, 100% {
    box-shadow:
      0 0 14px rgba(255, 71, 87, 0.4),
      0 0 28px rgba(255, 71, 87, 0.2);
  }
  50% {
    box-shadow:
      0 0 20px rgba(255, 71, 87, 0.55),
      0 0 40px rgba(255, 71, 87, 0.3);
  }
}

@keyframes legend-glow {
  0%, 100% {
    box-shadow:
      0 0 14px rgba(255, 215, 0, 0.4),
      0 0 28px rgba(255, 215, 0, 0.2);
  }
  50% {
    box-shadow:
      0 0 20px rgba(255, 215, 0, 0.55),
      0 0 40px rgba(255, 215, 0, 0.3);
  }
}

@keyframes supreme-glow {
  0%, 100% {
    box-shadow:
      0 0 16px rgba(255, 0, 0, 0.45),
      0 0 32px rgba(255, 0, 0, 0.25),
      0 0 48px rgba(255, 0, 0, 0.1);
  }
  50% {
    box-shadow:
      0 0 24px rgba(255, 0, 0, 0.6),
      0 0 48px rgba(255, 0, 0, 0.35),
      0 0 64px rgba(255, 0, 0, 0.15);
  }
}
</style>
