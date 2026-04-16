#!/bin/bash
# 停止前后端服务
lsof -ti:8080 | xargs kill -9 2>/dev/null && echo "后端已停止" || echo "后端未运行"
lsof -ti:3000 | xargs kill -9 2>/dev/null && echo "前端已停止" || echo "前端未运行"
