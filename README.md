# 锐竞电竞点单系统 (Dianjing Esports Platform)

> 赛博朋克电竞风格的电竞陪玩代打服务平台

## 🎮 项目简介

锐竞电竞点单系统是一个前后端分离的电竞服务交易平台，为用户提供游戏陪玩、代练等服务的在线交易功能。系统包含**用户端**、**服务商端**和**管理端**三个角色，支持服务发布、订单管理、在线支付、即时通讯、评价系统等核心功能。

## ✨ 特色

- 🎨 **赛博朋克电竞风 UI** — 霓虹色彩系统、Orbitron 科技字体、赛博网格纹理、扫描线动画
- 🔐 **安全可靠** — JWT 认证、BCrypt 加密、Spring Security、限流防护
- ⚡ **实时通讯** — WebSocket + STOMP 即时聊天，支持价格协商
- 📊 **数据可视化** — ECharts 图表、管理后台数据仪表盘
- 🏆 **等级体系** — 10 级等级系统，经验值升级，会员折扣

## 🛠 技术栈

| 层次 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.5 + Java 17 |
| 前端框架 | Vue 3.5 + TypeScript + Vite 8 |
| UI 组件库 | Element Plus 2.13 |
| 数据库 | SQLite + Spring Data JPA |
| 缓存 | Redis |
| 实时通讯 | WebSocket + STOMP |
| 安全认证 | Spring Security + JWT |
| 图表 | ECharts 6 + vue-echarts |
| 状态管理 | Pinia 3 |
| 样式 | Sass (赛博朋克主题) |

## 🚀 快速启动

### 环境要求
- Java 17+
- Node.js 18+
- Redis

### 后端启动
```bash
cd backend
mvn spring-boot:run
# 访问 http://localhost:8080
# API 文档 http://localhost:8080/swagger-ui.html
```

### 前端启动
```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:3000
```

### 测试账号
| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 普通用户 | testuser2 | 123456 |
| 服务商 | 橙子 | 123456 |

## 📁 项目结构

```
game/
├── backend/          # Spring Boot 后端
│   └── src/main/java/com/dianjing/
│       ├── controller/    # 控制器 (30+)
│       ├── service/       # 业务逻辑 (20+)
│       ├── entity/        # 实体类 (18)
│       ├── mapper/        # 数据访问 (18)
│       ├── dto/           # 数据传输对象 (30+)
│       ├── config/        # 配置类 (6)
│       ├── security/      # 安全认证
│       └── websocket/     # WebSocket
├── frontend/         # Vue 3 前端
│   └── src/
│       ├── views/         # 页面组件 (40+)
│       ├── components/    # 公共组件 (20+)
│       ├── api/           # API 接口 (18)
│       ├── stores/        # 状态管理 (7)
│       ├── layouts/       # 布局组件 (4)
│       └── assets/styles/ # 赛博朋克主题样式
└── 项目清单.md        # 详细项目文档
```

## 📋 功能模块

- **用户系统** — 注册/登录、个人信息、等级体系、信用分
- **服务交易** — 服务发布/浏览/搜索/收藏/推荐
- **订单管理** — 创建/支付/议价/状态跟踪/超时处理
- **即时通讯** — 一对一聊天、WebSocket 实时推送、价格协商
- **评价系统** — 订单评价、评分、匿名评价、服务商回复
- **支付系统** — 余额充值、订单支付、提现申请
- **投诉仲裁** — 投诉提交、举证、仲裁处理
- **营销活动** — 活动配置、折扣计算、服务关联
- **排行榜** — 服务商排行、用户排行
- **管理后台** — 用户/服务/订单/评价/投诉/财务/内容/设置管理

## 📝 更新日志

### v1.1.0 (2026-04-16)
- 🎨 全局赛博朋克电竞风 UI 美化（40+ 页面）
- 🐛 修复 SecurityConfig 白名单、路由缺失、字段不匹配等问题
- ✨ 新增公告置顶功能

详见 [项目清单.md](./项目清单.md)
