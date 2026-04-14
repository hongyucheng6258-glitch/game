# 电竞陪玩代打点单平台 - 实施计划

## 摘要

基于规范文档，构建一个完整的电竞陪玩代打点单平台。项目采用前后端分离架构，后端 Spring Boot 3.2.x + Java 17 + SQLite + Redis，前端 Vue 3 + TypeScript + Element Plus + Vite。实现全部功能模块，采用模拟支付和 WebSocket 实时聊天。

## 规范文档改进建议

在实施前，对原始规范文档提出以下改进：

### 1. 数据库设计改进
- **user 表增加 `balance` 字段**：原规范缺少余额字段，但业务涉及余额支付、充值、提现
- **order 表 `id` 统一为 `Long` 类型**：原规范 order 表用 VARCHAR(32) 作主键，与其他表不一致，改为 Long 自增，`order_no` 作为业务订单号
- **新增 `conversation` 会话表**：原 message 表缺少会话概念，聊天查询效率低。新增 `id, user1_id, user2_id, last_message, last_message_time, updated_at`
- **message 表增加 `conversation_id` 字段**：关联会话表，便于按会话查询

### 2. 架构设计改进
- **删除架构图中的 API 网关层**：单体应用不需要独立网关，简化为 前端 → Spring Boot → 数据层
- **WebSocket + JWT 集成方案**：浏览器 WebSocket 不支持自定义 Header，通过 URL Query Parameter 传递 Token（`ws://localhost:8080/ws?token=xxx`）

### 3. 功能范围调整
- **第三方登录（微信/QQ/微博）**：仅预留接口，不实际实现（需开发者账号和 OAuth 配置）
- **文件上传**：使用本地文件存储（`backend/uploads/`），通过静态资源映射访问
- **数据初始化**：使用 `data.sql` 初始化管理员账号、服务标签、游戏类型等基础数据

### 4. 技术细节调整
- **SQLite 方言**：Hibernate 6.x 不再内置 SQLite 方言，需引入 `hibernate-community-dialects` 依赖
- **SQLite WAL 模式**：开启 WAL 模式提升并发性能
- **SQLite DECIMAL 处理**：SQLite 不原生支持 DECIMAL，使用 REAL 类型存储，业务层处理精度

---

## 当前状态分析

项目为全新项目，`/workspace` 目录下仅有规范文档，无任何代码文件。需要从零搭建前后端项目。

---

## 实施计划

### 阶段一：基础架构搭建

> 目标：搭建可运行的项目骨架，前后端能正常启动和通信

#### 后端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 1.1 | `backend/pom.xml` | 创建 Maven 项目，配置所有依赖（Spring Boot Web/Security/Validation/WebSocket/Data Redis/Data JPA、SQLite JDBC、hibernate-community-dialects、JWT jjwt 0.12.5、SpringDoc OpenAPI、Lombok） |
| 1.2 | `backend/src/main/java/com/dianjing/DianjingApplication.java` | 启动类 |
| 1.3 | `backend/src/main/resources/application.yml` | 主配置（端口8080、SQLite路径、JPA配置、Redis、JWT密钥、文件上传限制、SpringDoc路径） |
| 1.4 | `backend/src/main/resources/application-dev.yml` | 开发环境配置 |
| 1.5 | `backend/src/main/java/com/dianjing/common/Result.java` | 统一响应体 `{code, message, data}` |
| 1.6 | `backend/src/main/java/com/dianjing/common/PageResult.java` | 分页响应体 `{total, pages, current, records}` |
| 1.7 | `backend/src/main/java/com/dianjing/common/BusinessException.java` | 自定义业务异常 |
| 1.8 | `backend/src/main/java/com/dianjing/common/GlobalExceptionHandler.java` | 全局异常处理 `@RestControllerAdvice` |
| 1.9 | `backend/src/main/java/com/dianjing/common/Constants.java` | 常量定义（角色、订单状态、消息类型等） |
| 1.10 | `backend/src/main/java/com/dianjing/config/CorsConfig.java` | 跨域配置 |
| 1.11 | `backend/src/main/java/com/dianjing/config/SecurityConfig.java` | Spring Security 配置，放行注册/登录/Swagger/WebSocket路径 |
| 1.12 | `backend/src/main/java/com/dianjing/config/OpenApiConfig.java` | SpringDoc 配置 |
| 1.13 | `backend/src/main/java/com/dianjing/config/WebMvcConfig.java` | 静态资源映射 `/uploads/**` |
| 1.14 | `backend/src/main/java/com/dianjing/security/JwtTokenProvider.java` | JWT 生成、解析、验证 |
| 1.15 | `backend/src/main/java/com/dianjing/security/JwtAuthenticationFilter.java` | JWT 认证过滤器 |
| 1.16 | `backend/src/main/java/com/dianjing/security/CustomUserDetailsService.java` | 用户详情服务 |
| 1.17 | `backend/src/main/java/com/dianjing/entity/*.java` (13个) | 所有实体类：User(含balance)、GameAccount、Service、Order(Long id)、Review、Message(含conversation_id)、Conversation(新增)、ServiceTag、ServiceTagRelation、ServiceFavorite、PaymentRecord、WithdrawalApplication、SystemAnnouncement |
| 1.18 | `backend/src/main/resources/data.sql` | 数据初始化（管理员账号admin/admin123、预置标签、游戏类型） |

#### 前端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 1.19 | `frontend/` 项目初始化 | `npm create vite@latest frontend -- --template vue-ts`，安装依赖（element-plus、pinia、vue-router@4、axios、sass、@stomp/stompjs、sockjs-client、echarts、vue-echarts） |
| 1.20 | `frontend/vite.config.ts` | 配置路径别名、开发代理（/api→8080、/ws→ws://8080、/uploads→8080）、Element Plus自动导入 |
| 1.21 | `frontend/src/assets/styles/variables.scss` | SCSS 主题变量 |
| 1.22 | `frontend/src/assets/styles/global.scss` | 全局样式重置 |
| 1.23 | `frontend/src/assets/styles/element-override.scss` | Element Plus 主题色覆盖 |
| 1.24 | `frontend/src/api/request.ts` | Axios 实例、请求/响应拦截器、JWT自动附加、错误统一处理 |
| 1.25 | `frontend/src/types/*.d.ts` (7个) | TypeScript 类型定义：common、user、service、order、review、message、payment |
| 1.26 | `frontend/src/router/index.ts` | 路由配置（懒加载、路由守卫、权限检查、布局嵌套） |
| 1.27 | `frontend/src/stores/user.ts` | 用户状态管理（token、userInfo、login/logout） |
| 1.28 | `frontend/src/stores/system.ts` | 系统状态管理（标签、公告） |
| 1.29 | `frontend/src/layouts/DefaultLayout.vue` | 默认布局（Header + router-view + Footer） |
| 1.30 | `frontend/src/layouts/UserCenterLayout.vue` | 用户中心布局（Header + Sidebar + router-view） |
| 1.31 | `frontend/src/layouts/AdminLayout.vue` | 后台管理布局（Admin Header + Sidebar + router-view） |
| 1.32 | `frontend/src/layouts/BlankLayout.vue` | 空白布局（登录注册页） |
| 1.33 | `frontend/src/components/common/AppHeader.vue` | 顶部导航栏（Logo、导航、搜索、用户信息、消息角标） |
| 1.34 | `frontend/src/components/common/AppFooter.vue` | 底部信息栏 |
| 1.35 | `frontend/src/components/common/AppSidebar.vue` | 可配置侧边栏 |
| 1.36 | `frontend/src/components/common/SearchBar.vue` | 搜索组件 |
| 1.37 | `frontend/src/components/common/EmptyState.vue` | 空状态组件 |
| 1.38 | `frontend/src/components/common/Pagination.vue` | 分页组件 |
| 1.39 | `frontend/src/utils/format.ts` | 格式化工具（日期、金额） |
| 1.40 | `frontend/src/utils/validate.ts` | 校验工具 |
| 1.41 | `frontend/src/utils/storage.ts` | localStorage 封装 |
| 1.42 | `frontend/src/utils/constants.ts` | 常量定义 |

---

### 阶段二：用户系统

> 目标：实现注册、登录、用户信息管理、游戏账号管理、文件上传

#### 后端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 2.1 | `backend/.../dto/request/RegisterRequest.java` | 注册请求 DTO |
| 2.2 | `backend/.../dto/request/LoginRequest.java` | 登录请求 DTO |
| 2.3 | `backend/.../dto/request/UserUpdateRequest.java` | 用户更新请求 DTO |
| 2.4 | `backend/.../dto/response/LoginResponse.java` | 登录响应 DTO |
| 2.5 | `backend/.../dto/response/UserInfoResponse.java` | 用户信息响应 DTO |
| 2.6 | `backend/.../mapper/UserMapper.java` | 用户 Mapper（JpaRepository + 自定义查询） |
| 2.7 | `backend/.../mapper/GameAccountMapper.java` | 游戏账号 Mapper |
| 2.8 | `backend/.../service/UserService.java` + `impl/UserServiceImpl.java` | 用户服务（注册、登录、信息CRUD、密码修改、头像上传、余额查询、角色/状态管理） |
| 2.9 | `backend/.../service/GameAccountService.java` + `impl/GameAccountServiceImpl.java` | 游戏账号服务（CRUD） |
| 2.10 | `backend/.../controller/AuthController.java` | 认证API：POST register、POST login、POST logout |
| 2.11 | `backend/.../controller/UserController.java` | 用户API：GET info、PUT info、PUT password、POST avatar、GET balance |
| 2.12 | `backend/.../controller/GameAccountController.java` | 游戏账号API：POST、GET、PUT、DELETE |
| 2.13 | `backend/.../controller/UploadController.java` | 文件上传API：POST /api/v1/upload/image |
| 2.14 | `backend/.../util/FileUtil.java` | 文件工具类 |

#### 前端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 2.15 | `frontend/src/api/auth.ts` | 认证 API 封装 |
| 2.16 | `frontend/src/api/user.ts` | 用户 API 封装 |
| 2.17 | `frontend/src/api/upload.ts` | 上传 API 封装 |
| 2.18 | `frontend/src/views/auth/LoginPage.vue` | 登录页 |
| 2.19 | `frontend/src/views/auth/RegisterPage.vue` | 注册页 |
| 2.20 | `frontend/src/views/user/UserCenterPage.vue` | 用户中心首页 |
| 2.21 | `frontend/src/views/user/ProfilePage.vue` | 个人信息编辑 |
| 2.22 | `frontend/src/views/user/GameAccountPage.vue` | 游戏账号管理 |
| 2.23 | `frontend/src/components/common/ImageUpload.vue` | 图片上传组件 |
| 2.24 | `frontend/src/components/business/UserAvatar.vue` | 用户头像组件 |

---

### 阶段三：服务管理

> 目标：实现服务发布、搜索、筛选、详情、收藏、标签管理

#### 后端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 3.1 | `backend/.../dto/request/ServiceCreateRequest.java` | 服务创建 DTO |
| 3.2 | `backend/.../dto/request/ServiceUpdateRequest.java` | 服务更新 DTO |
| 3.3 | `backend/.../dto/response/ServiceDetailResponse.java` | 服务详情响应 DTO |
| 3.4 | `backend/.../mapper/ServiceMapper.java` | 服务 Mapper（多条件筛选、分页、排序） |
| 3.5 | `backend/.../mapper/ServiceTagMapper.java` | 标签 Mapper |
| 3.6 | `backend/.../mapper/ServiceTagRelationMapper.java` | 标签关联 Mapper |
| 3.7 | `backend/.../mapper/ServiceFavoriteMapper.java` | 收藏 Mapper |
| 3.8 | `backend/.../service/ServiceService.java` + `impl/ServiceServiceImpl.java` | 服务服务（CRUD、上下架、搜索、推荐、筛选排序） |
| 3.9 | `backend/.../service/ServiceTagService.java` + `impl/ServiceTagServiceImpl.java` | 标签服务 |
| 3.10 | `backend/.../service/ServiceFavoriteService.java` + `impl/ServiceFavoriteServiceImpl.java` | 收藏服务 |
| 3.11 | `backend/.../controller/ServiceController.java` | 服务API：GET list、GET detail、POST create、PUT update、PUT status、GET recommend |
| 3.12 | `backend/.../controller/ServiceTagController.java` | 标签API：GET list |
| 3.13 | `backend/.../controller/ServiceFavoriteController.java` | 收藏API：POST、DELETE、GET list、GET check |

#### 前端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 3.14 | `frontend/src/api/service.ts` | 服务 API 封装 |
| 3.15 | `frontend/src/api/tag.ts` | 标签 API 封装 |
| 3.16 | `frontend/src/api/favorite.ts` | 收藏 API 封装 |
| 3.17 | `frontend/src/stores/service.ts` | 服务状态管理 |
| 3.18 | `frontend/src/views/home/HomePage.vue` | 首页（轮播图、游戏分类、推荐服务、搜索） |
| 3.19 | `frontend/src/views/service/ServiceListPage.vue` | 服务列表页（筛选、排序、分页） |
| 3.20 | `frontend/src/views/service/ServiceDetailPage.vue` | 服务详情页（信息、服务者、评价、下单按钮） |
| 3.21 | `frontend/src/views/user/FavoritePage.vue` | 我的收藏 |
| 3.22 | `frontend/src/views/provider/ProviderCenterPage.vue` | 服务者中心首页 |
| 3.23 | `frontend/src/views/provider/MyServicePage.vue` | 我的服务管理 |
| 3.24 | `frontend/src/views/provider/ServiceFormPage.vue` | 服务发布/编辑表单 |
| 3.25 | `frontend/src/components/business/ServiceCard.vue` | 服务卡片组件 |
| 3.26 | `frontend/src/components/business/ServiceTag.vue` | 标签组件 |
| 3.27 | `frontend/src/components/business/RatingStar.vue` | 评分星级组件 |

---

### 阶段四：订单系统

> 目标：实现下单、订单状态流转、订单管理

#### 后端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 4.1 | `backend/.../dto/request/OrderCreateRequest.java` | 订单创建 DTO |
| 4.2 | `backend/.../dto/response/OrderDetailResponse.java` | 订单详情响应 DTO |
| 4.3 | `backend/.../mapper/OrderMapper.java` | 订单 Mapper |
| 4.4 | `backend/.../util/OrderNoGenerator.java` | 订单号生成器（时间戳+随机数） |
| 4.5 | `backend/.../service/OrderService.java` + `impl/OrderServiceImpl.java` | 订单服务（创建、支付、取消、开始服务、完成服务、确认完成、列表、详情、统计） |
| 4.6 | `backend/.../controller/OrderController.java` | 订单API：POST create、GET list、GET detail、PUT cancel/start/complete/confirm |

#### 前端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 4.7 | `frontend/src/api/order.ts` | 订单 API 封装 |
| 4.8 | `frontend/src/stores/order.ts` | 订单状态管理 |
| 4.9 | `frontend/src/views/order/OrderConfirmPage.vue` | 订单确认页 |
| 4.10 | `frontend/src/views/order/OrderListPage.vue` | 订单列表页（Tab切换状态） |
| 4.11 | `frontend/src/views/order/OrderDetailPage.vue` | 订单详情页（状态流程、操作按钮） |
| 4.12 | `frontend/src/views/provider/ProviderOrderPage.vue` | 服务者接单管理 |
| 4.13 | `frontend/src/components/business/OrderCard.vue` | 订单卡片组件 |
| 4.14 | `frontend/src/components/business/OrderStatusBadge.vue` | 订单状态标签 |

---

### 阶段五：支付与财务系统

> 目标：实现模拟支付、充值、提现

#### 后端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 5.1 | `backend/.../dto/request/PaymentRequest.java` | 支付请求 DTO |
| 5.2 | `backend/.../dto/request/WithdrawalRequest.java` | 提现请求 DTO |
| 5.3 | `backend/.../mapper/PaymentRecordMapper.java` | 支付记录 Mapper |
| 5.4 | `backend/.../mapper/WithdrawalApplicationMapper.java` | 提现申请 Mapper |
| 5.5 | `backend/.../service/PaymentService.java` + `impl/PaymentServiceImpl.java` | 支付服务（模拟支付、充值、支付记录、统计） |
| 5.6 | `backend/.../service/WithdrawalService.java` + `impl/WithdrawalServiceImpl.java` | 提现服务（申请、列表、审核） |
| 5.7 | `backend/.../controller/PaymentController.java` | 支付API：POST pay、POST recharge、GET records、GET statistics |
| 5.8 | `backend/.../controller/WithdrawalController.java` | 提现API：POST apply、GET list |

#### 前端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 5.9 | `frontend/src/api/payment.ts` | 支付 API 封装 |
| 5.10 | `frontend/src/api/withdrawal.ts` | 提现 API 封装 |
| 5.11 | `frontend/src/stores/payment.ts` | 支付状态管理 |
| 5.12 | `frontend/src/views/user/BalancePage.vue` | 余额/充值/提现页 |
| 5.13 | `frontend/src/views/user/PaymentRecordPage.vue` | 支付记录页 |
| 5.14 | `frontend/src/components/business/PaymentMethodSelector.vue` | 支付方式选择组件 |

---

### 阶段六：评价系统

> 目标：实现服务评价、评价回复、评价统计

#### 后端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 6.1 | `backend/.../dto/request/ReviewCreateRequest.java` | 评价创建 DTO |
| 6.2 | `backend/.../mapper/ReviewMapper.java` | 评价 Mapper（按服务/服务者查询、评分统计） |
| 6.3 | `backend/.../service/ReviewService.java` + `impl/ReviewServiceImpl.java` | 评价服务（创建、列表、回复、统计） |
| 6.4 | `backend/.../controller/ReviewController.java` | 评价API：POST create、GET by service、GET by provider、PUT reply、GET stats |

#### 前端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 6.5 | `frontend/src/api/review.ts` | 评价 API 封装 |
| 6.6 | `frontend/src/components/business/ReviewItem.vue` | 评价项组件 |
| 6.7 | `frontend/src/views/review/MyReviewPage.vue` | 我的评价页 |
| 6.8 | 修改 `ServiceDetailPage.vue` | 集成评价列表和评分统计 |
| 6.9 | 修改 `OrderDetailPage.vue` | 集成评价表单（待评价状态时显示） |

---

### 阶段七：消息与实时聊天系统

> 目标：实现 WebSocket 实时聊天、系统消息、消息管理

#### 后端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 7.1 | `backend/.../config/WebSocketConfig.java` | WebSocket 配置（STOMP、SockJS、消息代理） |
| 7.2 | `backend/.../websocket/WebSocketHandshakeInterceptor.java` | 握手拦截器（从Query提取JWT） |
| 7.3 | `backend/.../websocket/ChatWebSocketHandler.java` | 聊天消息处理器（@MessageMapping） |
| 7.4 | `backend/.../websocket/ChatMessage.java` | 聊天消息 DTO |
| 7.5 | `backend/.../mapper/MessageMapper.java` | 消息 Mapper |
| 7.6 | `backend/.../mapper/ConversationMapper.java` | 会话 Mapper |
| 7.7 | `backend/.../service/MessageService.java` + `impl/MessageServiceImpl.java` | 消息服务（系统消息、列表、已读、未读数） |
| 7.8 | `backend/.../service/ChatService.java` + `impl/ChatServiceImpl.java` | 聊天服务（会话列表、聊天记录、发送消息） |
| 7.9 | `backend/.../controller/MessageController.java` | 消息API：GET list、PUT read、PUT read-all、GET unread-count |
| 7.10 | `backend/.../controller/ChatController.java` | 聊天API：GET conversations、GET history、POST send |

#### 前端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 7.11 | `frontend/src/composables/useWebSocket.ts` | WebSocket 组合式函数（连接、订阅、发送、断线重连） |
| 7.12 | `frontend/src/api/message.ts` | 消息 API 封装 |
| 7.13 | `frontend/src/api/chat.ts` | 聊天 API 封装 |
| 7.14 | `frontend/src/stores/chat.ts` | 聊天状态管理（会话、消息、未读数） |
| 7.15 | `frontend/src/stores/message.ts` | 消息状态管理 |
| 7.16 | `frontend/src/views/message/MessageCenterPage.vue` | 消息中心页 |
| 7.17 | `frontend/src/views/message/ChatPage.vue` | 聊天详情页 |
| 7.18 | `frontend/src/components/business/ChatBox.vue` | 聊天界面组件 |
| 7.19 | `frontend/src/components/business/ChatMessageItem.vue` | 聊天消息项组件 |
| 7.20 | 修改 `AppHeader.vue` | 集成消息通知角标 |

---

### 阶段八：系统公告与排行榜

> 目标：实现公告管理、服务者排行榜

#### 后端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 8.1 | `backend/.../mapper/SystemAnnouncementMapper.java` | 公告 Mapper |
| 8.2 | `backend/.../service/SystemAnnouncementService.java` + `impl/` | 公告服务（创建、发布、列表） |
| 8.3 | `backend/.../service/StatisticsService.java` + `impl/` | 统计服务（排行榜、平台统计） |
| 8.4 | `backend/.../controller/SystemAnnouncementController.java` | 公告API：GET list、GET detail |
| 8.5 | `backend/.../controller/StatisticsController.java` | 统计API：GET ranking、GET platform |

#### 前端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 8.6 | `frontend/src/api/announcement.ts` | 公告 API 封装 |
| 8.7 | `frontend/src/views/announcement/AnnouncementPage.vue` | 系统公告页 |
| 8.8 | `frontend/src/components/business/RankingList.vue` | 排行榜组件 |
| 8.9 | `frontend/src/views/ranking/RankingPage.vue` | 排行榜页（评分榜/销量榜/人气榜） |

---

### 阶段九：后台管理系统

> 目标：实现完整的管理后台

#### 后端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 9.1 | `backend/.../controller/admin/AdminUserController.java` | 管理员用户API（列表、详情、启禁用、改角色、统计） |
| 9.2 | `backend/.../controller/admin/AdminServiceController.java` | 管理员服务API（列表、下架、删除） |
| 9.3 | `backend/.../controller/admin/AdminOrderController.java` | 管理员订单API（列表、详情、改状态、统计） |
| 9.4 | `backend/.../controller/admin/AdminPaymentController.java` | 管理员支付API（记录、统计） |
| 9.5 | `backend/.../controller/admin/AdminWithdrawalController.java` | 管理员提现API（列表、审核） |
| 9.6 | `backend/.../controller/admin/AdminReviewController.java` | 管理员评价API（列表、删除） |
| 9.7 | `backend/.../controller/admin/AdminAnnouncementController.java` | 管理员公告API（CRUD、发布） |
| 9.8 | `backend/.../controller/admin/AdminStatisticsController.java` | 管理员统计API（仪表盘、趋势） |

#### 前端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 9.9 | `frontend/src/api/admin.ts` | 后台管理 API 封装 |
| 9.10 | `frontend/src/components/admin/StatCard.vue` | 统计卡片组件 |
| 9.11 | `frontend/src/components/admin/DataChart.vue` | ECharts 图表组件 |
| 9.12 | `frontend/src/components/admin/AuditDialog.vue` | 审核对话框组件 |
| 9.13 | `frontend/src/views/admin/AdminDashboardPage.vue` | 管理后台仪表盘 |
| 9.14 | `frontend/src/views/admin/AdminUserPage.vue` | 用户管理页 |
| 9.15 | `frontend/src/views/admin/AdminServicePage.vue` | 服务管理页 |
| 9.16 | `frontend/src/views/admin/AdminOrderPage.vue` | 订单管理页 |
| 9.17 | `frontend/src/views/admin/AdminPaymentPage.vue` | 财务管理页 |
| 9.18 | `frontend/src/views/admin/AdminWithdrawalPage.vue` | 提现审核页 |
| 9.19 | `frontend/src/views/admin/AdminReviewPage.vue` | 评价审核页 |
| 9.20 | `frontend/src/views/admin/AdminAnnouncementPage.vue` | 公告管理页 |
| 9.21 | `frontend/src/views/admin/AdminTagPage.vue` | 标签管理页 |
| 9.22 | `frontend/src/views/admin/AdminSettingsPage.vue` | 系统设置页 |

---

### 阶段十：Redis 缓存与性能优化

> 目标：集成 Redis 缓存、接口限流、前端优化

#### 后端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 10.1 | `backend/.../util/RedisUtil.java` | Redis 工具类 |
| 10.2 | 修改各 ServiceImpl | 集成 Redis 缓存（用户信息、推荐服务、服务详情、标签列表、公告列表） |
| 10.3 | 修改 `JwtAuthenticationFilter.java` | JWT 黑名单检查（Redis） |
| 10.4 | 新增限流拦截器 | 登录接口限流（IP维度10次/分钟）、消息接口限流（用户维度30次/分钟） |

#### 前端

| 步骤 | 文件 | 说明 |
|------|------|------|
| 10.5 | `frontend/src/composables/useCountdown.ts` | 倒计时组合式函数 |
| 10.6 | `frontend/src/utils/helpers.ts` | 防抖/节流工具 |
| 10.7 | 优化各页面 | 图片懒加载、搜索防抖、骨架屏/loading状态 |

---

### 阶段十一：联调测试与完善

> 目标：全功能联调、边界处理、数据初始化、安全检查

| 步骤 | 说明 |
|------|------|
| 11.1 | 前后端全功能联调，确保所有 API 对接正确 |
| 11.2 | WebSocket 聊天功能端到端测试 |
| 11.3 | 文件上传功能测试 |
| 11.4 | 完善 `data.sql` 种子数据（测试用户、服务、订单） |
| 11.5 | 边界情况处理（网络异常、并发操作、空状态、加载状态） |
| 11.6 | 安全检查（权限校验、SQL注入防护、XSS防护、敏感操作二次确认） |
| 11.7 | 响应式适配（不同屏幕尺寸、移动端适配） |

---

## 假设与决策

1. **SQLite 保留**：作为演示/Demo项目数据库，开启 WAL 模式提升并发性能
2. **模拟支付**：不接入真实支付 SDK，通过 `Thread.sleep` 模拟支付延迟
3. **WebSocket + STOMP/SockJS**：使用 Spring 内置的 STOMP 消息代理，Token 通过 URL Query 传递
4. **本地文件存储**：上传文件存储在 `backend/uploads/`，通过 WebMvcConfig 静态资源映射访问
5. **JPA + SQLite**：使用 Spring Data JPA + hibernate-community-dialects 方言
6. **前端设计风格**：电竞主题暗色系设计，使用 Element Plus 组件库，自定义主题色
7. **第三方登录**：仅预留接口，不实际接入
8. **管理员初始账号**：admin / admin123

## 验证步骤

1. **后端启动验证**：`mvn spring-boot:run` 启动成功，访问 `http://localhost:8080/swagger-ui.html` 查看 API 文档
2. **前端启动验证**：`npm run dev` 启动成功，访问 `http://localhost:3000` 查看页面
3. **注册登录验证**：注册新用户、登录获取 Token、访问需认证的 API
4. **核心业务流程验证**：发布服务 → 搜索服务 → 下单 → 模拟支付 → 开始服务 → 完成服务 → 评价
5. **聊天功能验证**：两个用户之间 WebSocket 实时消息收发
6. **后台管理验证**：管理员登录后台，查看统计数据，管理用户/服务/订单
7. **Redis 缓存验证**：检查缓存命中率，验证缓存更新策略
