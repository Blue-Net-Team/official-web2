# Docker 环境配置说明

## 概述

本项目支持通过环境变量区分开发环境和生产环境的 Docker 卷，避免数据混淆。

## 环境变量配置

### ENV 环境变量

`ENV` 变量用于指定当前运行环境，影响 Docker 卷的命名规则。

| 环境值 | 卷名称前缀 | 说明 |
|--------|-----------|------|
| `dev` | `dev_` | 开发环境（默认） |
| `prod` | `prod_` | 生产环境 |

### Docker 卷命名规则

根据 `ENV` 变量，Docker 卷会自动添加环境前缀：

| 服务 | 开发环境卷名 | 生产环境卷名 |
|------|------------|------------|
| PostgreSQL | `dev_database_data` | `prod_database_data` |
| RabbitMQ | `dev_rabbitmq_data` | `prod_rabbitmq_data` |
| MinIO | `dev_minio_data` | `prod_minio_data` |

## 使用方法

### 1. 配置 .env 文件

在 `docker/` 目录下创建 `.env` 文件：

```bash
cd docker
cp .env.example .env
```

编辑 `.env` 文件，设置 `ENV` 变量：

```bash
# 开发环境
ENV=dev

# 生产环境
ENV=prod
```

### 2. 启动服务

#### 开发环境

```bash
cd docker

# 使用 .env 文件中的配置
docker-compose --env-file .env up -d

# 或者直接指定环境变量
ENV=dev docker-compose up -d
```

#### 生产环境

```bash
cd docker

# 使用 .env 文件中的配置
docker-compose --env-file .env up -d

# 或者直接指定环境变量
ENV=prod docker-compose up -d
```

### 3. 查看当前卷

```bash
# 查看所有卷
docker volume ls

# 查看特定环境的卷
docker volume ls | grep dev_   # 开发环境
docker volume ls | grep prod_  # 生产环境
```

### 4. 清理环境

#### 清理开发环境

```bash
cd docker
ENV=dev docker-compose down -v
```

#### 清理生产环境

```bash
cd docker
ENV=prod docker-compose down -v
```

## 批量拉取镜像脚本

### Linux/Mac

```bash
cd docker
./pull-images.sh
```

脚本会自动加载 `.env` 文件并显示当前环境。

### Windows

```cmd
cd docker
pull-images.bat
```

脚本会自动加载 `.env` 文件并显示当前环境。

## 注意事项

1. **默认环境**：如果不设置 `ENV` 变量，默认使用 `dev` 环境
2. **数据隔离**：不同环境的卷完全独立，数据不会互相影响
3. **卷命名**：卷名称格式为 `{ENV}_{service}_data`
4. **环境切换**：切换环境前，建议先停止当前环境的服务
5. **数据备份**：重要数据建议定期备份

## 示例场景

### 场景 1：本地开发

```bash
# 1. 配置开发环境
cd docker
echo "ENV=dev" >> .env

# 2. 启动开发环境
docker-compose up -d

# 3. 查看卷
docker volume ls
# 输出：
# dev_database_data
# dev_rabbitmq_data
# dev_minio_data
```

### 场景 2：生产部署

```bash
# 1. 配置生产环境
cd docker
echo "ENV=prod" >> .env

# 2. 启动生产环境
docker-compose up -d

# 3. 查看卷
docker volume ls
# 输出：
# prod_database_data
# prod_rabbitmq_data
# prod_minio_data
```

### 场景 3：环境切换

```bash
# 停止开发环境
cd docker
ENV=dev docker-compose down

# 启动生产环境
ENV=prod docker-compose up -d
```

## 故障排查

### 问题 1：卷名称不正确

**症状**：启动服务时出现卷找不到的错误

**解决方案**：
```bash
# 检查 .env 文件中的 ENV 变量
cat docker/.env | grep ENV

# 确认卷名称
docker volume ls
```

### 问题 2：数据混淆

**症状**：开发环境的数据出现在生产环境

**解决方案**：
```bash
# 停止所有服务
docker-compose down

# 删除所有卷（谨慎操作）
docker volume prune

# 重新启动指定环境
ENV=dev docker-compose up -d
```

## 相关文件

- `docker/docker-compose.yml` - Docker Compose 配置
- `docker/.env` - 环境变量配置（需自行创建）
- `docker/.env.example` - 环境变量示例
- `docker/pull-images.sh` - Linux/Mac 批量拉取镜像脚本
- `docker/pull-images.bat` - Windows 批量拉取镜像脚本
