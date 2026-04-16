#!/bin/bash
# ============================================================
# 锐竞电竞点单系统 - 一键启动脚本
# ============================================================

# 加载环境变量
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export MAVEN_HOME=/opt/maven
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"

# 颜色
GREEN='\033[0;32m'
CYAN='\033[0;36m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${CYAN}=========================================="
echo "  锐竞电竞点单系统 - 一键启动"
echo -e "==========================================${NC}"

# 检查 Java
if ! command -v java &>/dev/null; then
    echo -e "${RED}错误: Java 未安装，请先运行: bash /workspace/game/setup.sh${NC}"
    exit 1
fi

# 确定 Maven 命令
if command -v mvn &>/dev/null; then
    MVN_CMD="mvn"
elif [ -f /workspace/game/backend/mvnw ]; then
    MVN_CMD="bash /workspace/game/backend/mvnw"
else
    echo -e "${RED}错误: Maven 未安装，请先运行: bash /workspace/game/setup.sh${NC}"
    exit 1
fi

# 检查 Node
if ! command -v node &>/dev/null; then
    echo -e "${RED}错误: Node.js 未安装，请先运行: bash /workspace/game/setup.sh${NC}"
    exit 1
fi

# 杀掉旧进程
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
lsof -ti:3000 | xargs kill -9 2>/dev/null || true

# 启动后端
echo -e "${GREEN}>>> 启动后端 (端口 8080)...${NC}"
cd /workspace/game/backend
nohup $MVN_CMD spring-boot:run \
    -Dspring-boot.run.jvmArguments="-Duser.dir=/workspace/game/backend" \
    > /tmp/backend.log 2>&1 &
echo "  后端 PID: $!"

# 启动前端
echo -e "${GREEN}>>> 启动前端 (端口 3000)...${NC}"
cd /workspace/game/frontend
nohup npx vite --host 0.0.0.0 > /tmp/frontend.log 2>&1 &
echo "  前端 PID: $!"

# 等待启动
echo ""
echo ">>> 等待服务启动..."
for i in $(seq 1 40); do
    sleep 2
    BE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/public/settings 2>/dev/null || echo "000")
    FE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000 2>/dev/null || echo "000")
    [ "$BE" = "200" ] && [ "$FE" = "200" ] && break
    printf "."
done
echo ""

echo -e "${CYAN}=========================================="
echo -e "  ✅ 启动完成！"
echo -e "==========================================${NC}"
echo ""
echo -e "  ${GREEN}前端:${NC} http://localhost:3000"
echo -e "  ${GREEN}后端:${NC} http://localhost:8080"
echo ""
echo "  日志:  tail -f /tmp/backend.log"
echo "        tail -f /tmp/frontend.log"
echo "  停止:  bash /workspace/game/stop.sh"
