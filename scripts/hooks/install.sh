#!/bin/sh
#
# Git Hooks 安装脚本
# 将项目共享的 git hooks 复制到 .git/hooks 目录
#

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
HOOKS_DIR="$SCRIPT_DIR"
GIT_HOOKS_DIR="$(git rev-parse --git-dir)/hooks"

echo "📦 安装 Git Hooks..."
echo ""

# 检查 .git/hooks 目录是否存在
if [ ! -d "$GIT_HOOKS_DIR" ]; then
  echo "❌ 未找到 .git/hooks 目录，请确保在 Git 仓库根目录运行此脚本"
  exit 1
fi

# 复制所有 hook 文件
for hook_file in "$HOOKS_DIR"/*; do
  if [ -f "$hook_file" ]; then
    hook_name=$(basename "$hook_file")
    
    # 跳过安装脚本本身
    if [ "$hook_name" = "install.sh" ]; then
      continue
    fi
    
    # 复制文件
    cp "$hook_file" "$GIT_HOOKS_DIR/$hook_name"
    
    # 设置可执行权限
    chmod +x "$GIT_HOOKS_DIR/$hook_name"
    
    echo "✅ 已安装: $hook_name"
  fi
done

echo ""
echo "🎉 Git Hooks 安装完成！"
