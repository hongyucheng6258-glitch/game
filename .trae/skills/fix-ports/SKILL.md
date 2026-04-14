---
name: "fix-ports"
description: "固定前端和后端端口，如果后端端口被占用就强行停止服务并重新运行，如果前端端口被占用则不用重新运行。"
---

# 端口修复工具

## 功能
- 固定前端和后端端口配置
- 检查后端端口占用情况
- 当后端端口被占用时，强行停止占用端口的进程
- 重新启动后端服务
- 前端端口如果被占用，保持当前状态

## 使用场景
当您需要：
- 固定前端和后端服务的端口
- 解决端口冲突问题
- 确保服务在指定端口上运行
- 修改完代码需要重新运行后端服务的时候

## 操作步骤

### 1. 配置端口
修改以下文件：

**后端配置** (`backend/src/main/resources/application.yml`):
```yaml
server:
  port: 8080  # 固定后端端口
```

**前端配置** (`frontend/vite.config.ts`):
```typescript
server: {
  port: 3000,  # 固定前端端口
  strictPort: true,  # 端口被占用时会报错
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  # 指向后端端口
      changeOrigin: true,
    },
    '/ws': {
      target: 'ws://localhost:8080',
      ws: true,
    },
    '/uploads': {
      target: 'http://localhost:8080',
    },
  },
},
```

### 2. 检查并处理后端端口占用
**检查后端端口（8080）是否被占用**：
```bash
netstat -ano | findstr :8080
```

**如果端口被占用，自动停止占用进程**：
```bash
# 找到进程 ID（PID）并停止
for /f "tokens=5" %a in ('netstat -ano ^| findstr :8080') do taskkill /PID %a /F
```

### 3. 重新启动后端服务
```bash
cd backend
mvn spring-boot:run
```

### 4. 前端服务处理
**如果前端端口（3000）被占用**：
- 跳过启动前端服务，保持当前状态
- 前端端口被占用时，Vite 会自动尝试使用其他端口

**如果前端端口未被占用**：
```bash
cd frontend
npm run dev
```

## 注意事项
- 后端端口默认设置为 8080
- 前端端口默认设置为 3000
- 前端端口被占用时，跳过启动前端服务
- 后端端口被占用时，自动停止占用进程后重新启动

## 示例

**场景 1**：后端端口 8080 被占用，前端端口 3000 未被占用

**操作**：
1. 检查后端端口占用：`netstat -ano | findstr :8080`
2. 自动停止占用进程：`for /f "tokens=5" %a in ('netstat -ano ^| findstr :8080') do taskkill /PID %a /F`
3. 重新启动后端服务：`mvn spring-boot:run`
4. 启动前端服务：`npm run dev`

**结果**：
- 后端服务在 8080 端口运行
- 前端服务在 3000 端口运行

**场景 2**：后端端口 8080 未被占用，前端端口 3000 被占用

**操作**：
1. 启动后端服务：`mvn spring-boot:run`
2. 跳过启动前端服务（端口被占用）

**结果**：
- 后端服务在 8080 端口运行
- 前端服务保持当前状态（可能在其他端口运行）