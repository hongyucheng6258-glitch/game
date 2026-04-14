# 锐竞电竞平台

一个专业的电竞服务平台，提供游戏陪玩、代练等服务。

## 项目简介

锐竞电竞平台是一个完整的电竞服务系统，包含前端和后端两部分。

- **前端**：使用 Vue 3 + TypeScript + Vite + Element Plus
- **后端**：使用 Spring Boot + MySQL + Redis

## 功能特性

- 用户登录注册
- 服务浏览与搜索
- 订单管理
- 聊天功能
- 评价系统
- 钱包与支付
- 后台管理系统

## 如何推送到 GitHub

本项目已初始化 Git 仓库并完成首次提交，如需推送到 GitHub，请按以下步骤操作：

### 方法一：使用命令行

1. 在 GitHub 上创建新仓库（命名为 `锐竞电竞平台`）
2. 添加远程仓库地址：
```bash
git remote add origin https://github.com/你的用户名/锐竞电竞平台.git
```
3. 推送到 GitHub：
```bash
git branch -M main
git push -u origin main
```

### 方法二：使用 GitHub Desktop

1. 下载并安装 [GitHub Desktop](https://desktop.github.com/)
2. 打开 GitHub Desktop，选择 "File" -> "Add Local Repository"
3. 选择本项目文件夹
4. 点击 "Publish repository" 按钮
5. 填写仓库信息并发布

## 项目结构

```
锐竞电竞点单系统/
├── backend/          # 后端项目
│   ├── src/
│   └── pom.xml
├── frontend/         # 前端项目
│   ├── src/
│   └── package.json
└── README.md
```

## 开发说明

### 前端开发

```bash
cd frontend
npm install
npm run dev
```

### 后端开发

```bash
cd backend
# 使用 Maven 或 IDE 运行
```

## 许可证

MIT License
