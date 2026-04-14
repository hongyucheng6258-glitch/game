<template>
  <div class="level-center-page">
    <div class="page-header">
      <h2>等级中心</h2>
      <p>提升等级，享受更多特权</p>
    </div>

    <el-card class="my-level-card" v-loading="loading">
      <div class="my-level-content" v-if="levelInfo">
        <div class="level-display">
          <div class="level-icon">{{ levelInfo.currentLevelIcon }}</div>
          <div class="level-info">
            <div class="level-name">{{ levelInfo.currentLevelName }}</div>
            <div class="level-number">Lv.{{ levelInfo.currentLevel }}</div>
          </div>
        </div>
        <div class="exp-section">
          <ExperienceBar 
            :current-exp="levelInfo.experience"
            :current-level-exp="levelInfo.currentLevelRequiredExp"
            :next-level-exp="levelInfo.nextLevelRequiredExp"
          />
        </div>
        <div class="total-expense">
          <span class="label">累计消费</span>
          <span class="value">¥{{ levelInfo.totalExpense.toFixed(2) }}</span>
        </div>
        <!-- 折扣提示 -->
        <el-alert 
          v-if="levelInfo.discountRate < 1" 
          :title="'当前享受 ' + (levelInfo.discountRate * 100).toFixed(0) + ' 折优惠'" 
          type="success" 
          :closable="false"
          show-icon
          style="margin-top: 16px; width: 100%;"
        >
          <template #default>
            购买服务时可自动享受折扣，省钱更实惠！
          </template>
        </el-alert>
      </div>
    </el-card>

    <el-card class="privileges-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>当前特权</span>
          <el-tag type="success" size="small">Lv.{{ levelInfo?.currentLevel }} {{ levelInfo?.currentLevelName }}</el-tag>
        </div>
      </template>
      <div class="privileges-list" v-if="levelInfo">
        <div class="privilege-item active">
          <div class="privilege-icon">💰</div>
          <div class="privilege-info">
            <div class="privilege-name">服务折扣</div>
            <div class="privilege-value">{{ (levelInfo.discountRate * 100).toFixed(0) }}折</div>
            <div class="privilege-desc">购买所有服务时自动应用</div>
          </div>
          <div class="privilege-badge">已激活</div>
        </div>
        <div class="privilege-item active">
          <div class="privilege-icon">💳</div>
          <div class="privilege-info">
            <div class="privilege-name">提现手续费折扣</div>
            <div class="privilege-value">{{ (levelInfo.withdrawalFeeDiscount * 100).toFixed(0) }}折</div>
            <div class="privilege-desc">提现时手续费减免至{{ (levelInfo.withdrawalFeeDiscount * 5).toFixed(2) }}%</div>
          </div>
          <div class="privilege-badge">已激活</div>
        </div>
        <div class="privilege-item active">
          <div class="privilege-icon">🔄</div>
          <div class="privilege-info">
            <div class="privilege-name">每日提现次数</div>
            <div class="privilege-value">{{ levelInfo.dailyWithdrawalLimit }}次/日</div>
            <div class="privilege-desc">每天最多可提现次数</div>
          </div>
          <div class="privilege-badge">已激活</div>
        </div>
        <div class="privilege-item active">
          <div class="privilege-icon">💎</div>
          <div class="privilege-info">
            <div class="privilege-name">最高服务价格</div>
            <div class="privilege-value">¥{{ levelInfo.maxServicePrice.toLocaleString() }}</div>
            <div class="privilege-desc">作为服务商可发布的最高单价</div>
          </div>
          <div class="privilege-badge">已激活</div>
        </div>
      </div>
    </el-card>

    <el-card class="all-levels-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>等级列表</span>
          <el-tag type="info" size="small">查看升级条件</el-tag>
        </div>
      </template>
      <div class="levels-list">
        <div 
          v-for="level in allLevels" 
          :key="level.id" 
          class="level-item"
          :class="{ 'current': level.level === levelInfo?.currentLevel, 'unlocked': level.level <= (levelInfo?.currentLevel || 1) }"
        >
          <div class="level-icon">{{ level.icon }}</div>
          <div class="level-main">
            <div class="level-header">
              <span class="level-name">{{ level.name }}</span>
              <span class="level-number">Lv.{{ level.level }}</span>
            </div>
            <div class="level-requirement">
              所需经验: {{ level.requiredExp }}
              <span v-if="level.requiredExp > (levelInfo?.experience || 0)" class="exp-gap">
                (还需 {{ level.requiredExp - (levelInfo?.experience || 0) }})
              </span>
              <span v-else class="exp-reached">✓ 已达成</span>
            </div>
            <div class="level-privileges">
              <span class="privilege-tag" :class="{ 'highlight': (level.discountRate * 100) < 100 }">
                🛒 {{ (level.discountRate * 100).toFixed(0) }}折
              </span>
              <span class="privilege-tag" :class="{ 'highlight': (level.withdrawalFeeDiscount * 100) < 100 }">
                💸 提现{{ (level.withdrawalFeeDiscount * 100).toFixed(0) }}折
              </span>
              <span class="privilege-tag" :class="{ 'highlight': level.dailyWithdrawalLimit > 3 }">
                🔄 {{ level.dailyWithdrawalLimit }}次/日
              </span>
              <span class="privilege-tag" :class="{ 'highlight': level.maxServicePrice > 1000 }">
                💎 ¥{{ level.maxServicePrice.toLocaleString() }}
              </span>
            </div>
          </div>
          <el-tag v-if="level.level === levelInfo?.currentLevel" type="primary" size="small">当前</el-tag>
          <el-tag v-else-if="level.level < (levelInfo?.currentLevel || 1)" type="success" size="small" effect="plain">已解锁</el-tag>
          <el-tag v-else type="info" size="small" effect="plain">{{ level.level === (levelInfo?.currentLevel || 1) + 1 ? '下一级' : '' }}</el-tag>
        </div>
      </div>
    </el-card>

    <el-card class="tips-card">
      <template #header>
        <div class="card-header">
          <span>💡 升级攻略</span>
        </div>
      </template>
      <div class="tips-content">
        <div class="tip-item">
          <span class="tip-number">1</span>
          <div class="tip-text">
            <strong>下单消费</strong>
            <p>每消费1元获得1经验值，同时享受等级折扣优惠</p>
          </div>
        </div>
        <div class="tip-item">
          <span class="tip-number">2</span>
          <div class="tip-text">
            <strong>完成订单（服务商）</strong>
            <p>每获得1元收入获得2经验值，升级更快！</p>
          </div>
        </div>
        <div class="tip-item">
          <span class="tip-number">3</span>
          <div class="tip-text">
            <strong>持续活跃</strong>
            <p>保持良好的信用评分，解锁更多高级功能</p>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { levelApi, type UserLevelInfo, type Level } from '@/api/level'
import LevelBadge from '@/components/business/LevelBadge.vue'
import ExperienceBar from '@/components/business/ExperienceBar.vue'

const loading = ref(false)
const levelInfo = ref<UserLevelInfo | null>(null)
const allLevels = ref<Level[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const [levelRes, allLevelsRes] = await Promise.all([
      levelApi.getMyLevelInfo(),
      levelApi.getAllLevels()
    ])
    levelInfo.value = levelRes.data
    allLevels.value = allLevelsRes.data
  } catch (error) {
    ElMessage.error('获取等级信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.level-center-page {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
  
  .page-header {
    text-align: center;
    margin-bottom: 30px;
    
    h2 {
      font-size: 28px;
      color: #333;
      margin-bottom: 8px;
    }
    
    p {
      color: #999;
      font-size: 14px;
    }
  }
  
  .my-level-card {
    margin-bottom: 20px;
    
    .my-level-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .level-display {
        display: flex;
        align-items: center;
        gap: 20px;
        margin-bottom: 24px;
        
        .level-icon {
          font-size: 64px;
        }
        
        .level-info {
          text-align: left;
          
          .level-name {
            font-size: 32px;
            font-weight: 700;
            background: linear-gradient(135deg, #409eff, #66b1ff);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin-bottom: 4px;
          }
          
          .level-number {
            font-size: 16px;
            color: #999;
          }
        }
      }
      
      .exp-section {
        width: 100%;
        max-width: 500px;
        margin-bottom: 16px;
      }
      
      .total-expense {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .label {
          color: #999;
          font-size: 14px;
        }
        
        .value {
          font-size: 20px;
          font-weight: 600;
          color: #f56c6c;
        }
      }
    }
  }
  
  .privileges-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-weight: 600;
    }
    
    .privileges-list {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
      
      .privilege-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px;
        background: #f5f7fa;
        border-radius: 8px;
        border-left: 4px solid transparent;
        
        &.active {
          border-left-color: #67c23a;
          background: linear-gradient(135deg, rgba(103, 194, 58, 0.05), rgba(144, 238, 144, 0.08));
          
          .privilege-badge {
            background: #67c23a;
            color: white;
            padding: 2px 8px;
            border-radius: 10px;
            font-size: 11px;
            font-weight: 600;
          }
        }
        
        .privilege-icon {
          font-size: 28px;
        }
        
        .privilege-info {
          flex: 1;
          
          .privilege-name {
            font-size: 14px;
            font-weight: 600;
            color: #333;
            margin-bottom: 4px;
          }
          
          .privilege-value {
            font-size: 18px;
            font-weight: 700;
            color: #409eff;
            margin-bottom: 4px;
          }
          
          .privilege-desc {
            font-size: 12px;
            color: #999;
          }
        }
      }
    }
  }
  
  .all-levels-card {
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-weight: 600;
    }
    
    .levels-list {
      .level-item {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 16px;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        &.current {
          background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(102, 177, 255, 0.1));
          border-radius: 8px;
        }
        
        &.unlocked:not(.current) {
          opacity: 0.8;
        }
        
        .level-icon {
          font-size: 36px;
        }
        
        .level-main {
          flex: 1;
          
          .level-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 6px;
            
            .level-name {
              font-size: 16px;
              font-weight: 600;
              color: #333;
            }
            
            .level-number {
              font-size: 12px;
              color: #999;
            }
          }
          
          .level-requirement {
            font-size: 12px;
            color: #999;
            margin-bottom: 8px;
            
            .exp-gap {
              color: #f56c6c;
              margin-left: 8px;
            }
            
            .exp-reached {
              color: #67c23a;
              margin-left: 8px;
              font-weight: 600;
            }
          }
          
          .level-privileges {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
            
            .privilege-tag {
              font-size: 11px;
              padding: 2px 8px;
              background: #f0f0f0;
              border-radius: 10px;
              color: #666;
              
              &.highlight {
                background: linear-gradient(135deg, #fff7e6, #ffe7ba);
                color: #e6a23c;
                font-weight: 600;
              }
            }
          }
        }
      }
    }
  }
  
  .tips-card {
    .card-header {
      font-weight: 600;
    }
    
    .tips-content {
      .tip-item {
        display: flex;
        gap: 16px;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .tip-number {
          width: 28px;
          height: 28px;
          border-radius: 50%;
          background: linear-gradient(135deg, #409eff, #66b1ff);
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 700;
          font-size: 14px;
          flex-shrink: 0;
        }
        
        .tip-text {
          strong {
            display: block;
            color: #333;
            margin-bottom: 4px;
          }
          
          p {
            font-size: 13px;
            color: #999;
            margin: 0;
          }
        }
      }
    }
  }
}
</style>
