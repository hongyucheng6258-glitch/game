#!/bin/bash
# ============================================================
# 锐竞电竞点单系统 - 一键环境配置脚本
# 适用于 Ubuntu 22.04+ 云服务器
# ============================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}=========================================="
echo "  锐竞电竞点单系统 - 一键环境配置"
echo -e "==========================================${NC}"

# ---------- 1. 系统依赖 ----------
echo ""
echo -e "${GREEN}>>> [1/5] 安装系统依赖...${NC}"
DEBIAN_FRONTEND=noninteractive apt-get update -qq 2>/dev/null
DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    curl wget unzip git build-essential \
    openjdk-17-jdk-headless \
    2>&1 | tail -3

# ---------- 2. Java ----------
echo ""
echo -e "${GREEN}>>> [2/5] 配置 Java 17...${NC}"
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
echo "  Java: $(java -version 2>&1 | head -1)"

# ---------- 3. Maven (离线优先) ----------
echo ""
echo -e "${GREEN}>>> [3/5] 配置 Maven...${NC}"
MAVEN_HOME=/opt/maven

# 方式1: 系统已有 mvn
if command -v mvn &>/dev/null; then
    MAVEN_HOME=$(dirname $(dirname $(which mvn)))
    echo "  使用系统 Maven: $MAVEN_HOME"
# 方式2: /opt/maven 已存在
elif [ -f "$MAVEN_HOME/bin/mvn" ]; then
    echo "  使用已安装 Maven: $MAVEN_HOME"
else
    # 方式3: Maven Wrapper
    echo "  系统无 Maven，配置 Maven Wrapper..."
    cd /workspace/game/backend
    mkdir -p .mvn/wrapper
    
    # 下载 wrapper jar
    if [ ! -f ".mvn/wrapper/maven-wrapper.jar" ]; then
        curl -sL "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar" \
            -o .mvn/wrapper/maven-wrapper.jar 2>/dev/null || true
    fi
    
    # 配置镜像
    mkdir -p ~/.m2
    if [ ! -f ~/.m2/settings.xml ]; then
        cat > ~/.m2/settings.xml << 'MAVENEOF'
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <url>https://maven.aliyun.com/repository/central</url>
    </mirror>
  </mirrors>
</settings>
MAVENEOF
    fi
    
    # 创建 mvnw
    if [ ! -f "mvnw" ]; then
        cat > mvnw << 'MVNWEOF'
#!/bin/sh
MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")" && pwd)"
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
if [ -f "$MAVEN_WRAPPER_PROPERTIES" ]; then
    MAVEN_DIST_URL=$(grep "distributionUrl" "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
fi
MAVEN_HOME="${HOME}/.m2/wrapper/dists/apache-maven-3.9.6"
if [ ! -f "$MAVEN_HOME/bin/mvn" ]; then
    mkdir -p "$MAVEN_HOME"
    echo "Downloading Maven..."
    TMPFILE="$(mktemp)"
    curl -sL "$MAVEN_DIST_URL" -o "$TMPFILE"
    unzip -q -o "$TMPFILE" -d "${HOME}/.m2/wrapper/dists/"
    rm -f "$TMPFILE"
fi
exec "$MAVEN_HOME/bin/mvn" "$@"
MVNWEOF
        chmod +x mvnw
    fi
    
    # wrapper properties
    if [ ! -f ".mvn/wrapper/maven-wrapper.properties" ]; then
        cat > .mvn/wrapper/maven-wrapper.properties << 'PROPEOF'
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip
PROPEOF
    fi
    
    MAVEN_CMD="bash /workspace/game/backend/mvnw"
    echo "  Maven Wrapper 已配置"
fi

export PATH="$MAVEN_HOME/bin:$PATH"

# ---------- 4. Node.js ----------
echo ""
echo -e "${GREEN}>>> [4/5] 配置 Node.js...${NC}"
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
if ! command -v node &>/dev/null; then
    curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
    . "$NVM_DIR/nvm.sh"
    nvm install 22 --no-progress 2>&1 | tail -2
    nvm alias default 22
fi
echo "  Node: $(node -v)"
echo "  NPM:  $(npm -v)"

# ---------- 5. 前端依赖 ----------
echo ""
echo -e "${GREEN}>>> [5/5] 安装前端依赖...${NC}"
cd /workspace/game/frontend
if [ ! -d "node_modules" ]; then
    npm install --no-audit --no-fund 2>&1 | tail -3
else
    echo "  node_modules 已存在，跳过"
fi

# ---------- 写入环境变量 ----------
grep -q "JAVA_HOME" ~/.bashrc 2>/dev/null || cat >> ~/.bashrc << 'BASHEOF'

# === 锐竞电竞点单系统 ===
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export MAVEN_HOME=/opt/maven
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"
BASHEOF

echo ""
echo -e "${CYAN}=========================================="
echo -e "  ✅ 环境配置完成！"
echo -e "==========================================${NC}"
echo ""
echo "  启动命令:  bash /workspace/game/start.sh"
echo "  停止命令:  bash /workspace/game/stop.sh"
echo ""
