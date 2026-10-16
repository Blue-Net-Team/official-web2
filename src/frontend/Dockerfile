# BlueNet Frontend Dockerfile
# 构建上下文应为 src/frontend 目录

# ==================== 构建阶段 ====================
FROM node:20-slim AS builder

# 构建时参数：后端公网地址（用于 SSR 预渲染时访问后端 API）
# 在 CI/CD 中构建时传入，例如：--build-arg BUILD_BACKEND_HOST=api.example.com
ARG CACHE_BUST=default
ARG BUILD_BACKEND_HOST=localhost
ARG BUILD_BACKEND_PORT=8080
ARG BUILD_SSL_ENABLED=false

# 构建时参数：客户端访问后端的地址（NEXT_PUBLIC_* 会被打包进客户端代码）
ARG NEXT_PUBLIC_BACKEND_HOST=localhost
ARG NEXT_PUBLIC_BACKEND_PORT=8080
ARG NEXT_PUBLIC_SSL_ENABLED=false

# 构建时参数：AI 客服服务（独立于主 API，NEXT_PUBLIC_* 同样会被打包进客户端代码）
ARG NEXT_PUBLIC_AI_SERVICE_HOST=localhost
ARG NEXT_PUBLIC_AI_SERVICE_PORT=8000
ARG NEXT_PUBLIC_AI_SERVICE_SSL_ENABLED=false
ARG NEXT_PUBLIC_AI_SERVICE_PREFIX=/ai/v1

# 依赖安装源：默认官方源（CI 不受影响）；国内构建可传
# --build-arg NPM_REGISTRY=https://registry.npmmirror.com 加速。
# pnpm-lock.yaml 不记录 registry 地址，切换源不改变锁文件。
ARG NPM_REGISTRY=https://registry.npmjs.org
ENV NPM_CONFIG_REGISTRY=${NPM_REGISTRY}

# 安装 pnpm（支持国内镜像源加速）
ARG COREPACK_NPM_REGISTRY=https://registry.npmjs.org
ENV COREPACK_NPM_REGISTRY=$COREPACK_NPM_REGISTRY
RUN corepack enable && corepack prepare pnpm@9.15.0 --activate

WORKDIR /app

# 复制 package.json 和 lock 文件
COPY package.json pnpm-lock.yaml ./

# 安装依赖
RUN pnpm install --frozen-lockfile

# 强制 invalidate layer cache：每次 CACHE_BUST 变化时重新复制源代码
ARG CACHE_BUST
RUN echo "Cache bust: ${CACHE_BUST}" > /dev/null

# 复制源代码（宿主机 node_modules 即使被 dockerignore 忽略也做一次清理，确保使用容器内安装的依赖）
COPY ./ ./
RUN rm -rf /app/node_modules && pnpm install --frozen-lockfile

# 自托管 Monaco Editor：把 monaco-editor 的 AMD 产物复制到 public 下随镜像发布，
# 避免运行时从 jsdelivr CDN 拉取（国内网络超时，编辑器无法加载）。
# 对应代码中的 loader.config({ paths: { vs: '/monaco/vs' } })。
# 先 rm 再拷：若目标已存在（例如仓库里误提交过 public/monaco），cp -r 会嵌套成
# public/monaco/vs/vs 导致 404，这里保证幂等。
RUN rm -rf public/monaco \
 && mkdir -p public/monaco \
 && cp -r node_modules/monaco-editor/min/vs public/monaco/vs \
 && test -f public/monaco/vs/loader.js

# 设置构建时的环境变量并构建应用
ENV BACKEND_HOST=${BUILD_BACKEND_HOST}
ENV BACKEND_PORT=${BUILD_BACKEND_PORT}
ENV SSL_ENABLED=${BUILD_SSL_ENABLED}
ENV NEXT_PUBLIC_BACKEND_HOST=${NEXT_PUBLIC_BACKEND_HOST}
ENV NEXT_PUBLIC_BACKEND_PORT=${NEXT_PUBLIC_BACKEND_PORT}
ENV NEXT_PUBLIC_SSL_ENABLED=${NEXT_PUBLIC_SSL_ENABLED}
ENV NEXT_PUBLIC_AI_SERVICE_HOST=${NEXT_PUBLIC_AI_SERVICE_HOST}
ENV NEXT_PUBLIC_AI_SERVICE_PORT=${NEXT_PUBLIC_AI_SERVICE_PORT}
ENV NEXT_PUBLIC_AI_SERVICE_SSL_ENABLED=${NEXT_PUBLIC_AI_SERVICE_SSL_ENABLED}
ENV NEXT_PUBLIC_AI_SERVICE_PREFIX=${NEXT_PUBLIC_AI_SERVICE_PREFIX}
RUN pnpm build

# ==================== 运行阶段 ====================
FROM node:20-slim AS runner
LABEL authors="IVEN"
LABEL description="BlueNet Frontend Service"

WORKDIR /app

# 运行时环境变量
# 注意：这里的 BACKEND_HOST 默认值会被 docker-compose.yml 中的环境变量覆盖
# 在 Docker 网络内，应该使用容器名（如 'api-service'）而不是 localhost 或公网地址
ENV NODE_ENV=production
ENV BACKEND_HOST=localhost
ENV BACKEND_PORT=8080

# 从构建阶段复制必要文件
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

# 创建非 root 用户并设置权限
RUN addgroup --system --gid 1001 nodejs && \
    adduser --system --uid 1001 nextjs && \
    chown -R nextjs:nodejs /app

# 切换到非 root 用户
USER nextjs

# 开放端口
EXPOSE 3000

# 启动服务
ENTRYPOINT ["node", "server.js"]
