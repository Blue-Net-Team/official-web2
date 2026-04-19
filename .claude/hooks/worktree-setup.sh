#!/bin/bash
# Claude Code Worktree 环境初始化脚本
# 所有输出到 stderr，不干扰 Claude Code

WORKTREE_DIR="${CLAUDE_WORKTREE_DIR:-$(pwd)}"
PROJECT_DIR="${CLAUDE_PROJECT_DIR:-$(git rev-parse --show-toplevel 2>/dev/null || echo "..")}"

{
  echo "=== Worktree 环境初始化 ==="
  echo "Worktree: $WORKTREE_DIR"
  echo "Project:  $PROJECT_DIR"

  # 1. 安装前端依赖
  echo "[1/3] 安装前端依赖..."
  cd "$WORKTREE_DIR/src/frontend" && pnpm install

  # 2. 复制后端环境变量
  echo "[2/3] 复制后端 .env..."
  if [ -f "$PROJECT_DIR/src/backend/.env" ]; then
    cp "$PROJECT_DIR/src/backend/.env" "$WORKTREE_DIR/src/backend/.env"
    echo "  已从源仓库复制 .env"
  elif [ -f "$PROJECT_DIR/src/backend/.env.example" ]; then
    cp "$PROJECT_DIR/src/backend/.env.example" "$WORKTREE_DIR/src/backend/.env"
    echo "  已从 .env.example 复制（请检查配置）"
  else
    echo "  ⚠ 未找到 .env 或 .env.example，跳过"
  fi

  # 3. 安装 Git Hooks
  echo "[3/3] 安装 Git Hooks..."
  cd "$WORKTREE_DIR" && sh "$WORKTREE_DIR/scripts/hooks/install.sh"

  echo "=== 初始化完成 ==="
} >&2
