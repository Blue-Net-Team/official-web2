# Nginx 配置建议（大文件上传场景）

## 背景

改造后，文件上传流量不再经过后端代理，而是走预签名 URL 直传 OSS（MinIO / 阿里云 OSS）。因此 Nginx 的 `client_max_body_size` 等限制主要影响：

1. **旧接口过渡期**：`POST /api/v1/file/upload` 仍可能走 Nginx → 后端
2. **批量下载接口**：`GET /api/v1/file/download/batch` 流式输出 ZIP 时，Nginx 作为反向代理缓冲响应
3. **预签名流程控制接口**：`POST /api/v1/file/prepare-upload` 和 `/confirm-upload` 请求体极小，不受影响

## 推荐配置

```nginx
server {
    listen 80;
    server_name api.bluenet.example;

    # 旧接口过渡期仍可能接收大文件，保留 500MB
    # 改造完成后可收紧至 1MB（仅控制接口请求体）
    client_max_body_size 500M;

    # 批量下载 ZIP 流式输出时，避免 Nginx 长时间等待后端产生数据
    proxy_read_timeout 300s;
    proxy_send_timeout 300s;

    # 禁用 Nginx 对后端流式响应的缓冲，直接透传，降低内存占用
    proxy_buffering off;
    proxy_cache off;

    # 长连接保持，减少频繁建立 TCP 连接的开销
    proxy_http_version 1.1;
    proxy_set_header Connection "";

    location /api/v1/file/upload {
        proxy_pass http://backend;
        # 旧上传接口可单独放宽，改造完成后删除此 location
    }

    location /api/v1/file/download/batch {
        proxy_pass http://backend;
        # 确保流式输出不被缓冲
        proxy_buffering off;
    }

    location /api/v1/ {
        proxy_pass http://backend;
    }
}
```

## 关键参数说明

| 参数 | 建议值 | 说明 |
|------|--------|------|
| `client_max_body_size` | `500M` → 后续收紧至 `1M` | 控制接口请求体极小，旧上传接口完成后可大幅收紧 |
| `proxy_read_timeout` | `300s` | 批量下载大文件 ZIP 时，后端流式生成数据可能较慢 |
| `proxy_send_timeout` | `300s` | 向后端发送请求的超时 |
| `proxy_buffering` | `off` | 关闭代理缓冲，流式响应直接透传，降低 Nginx 内存占用 |
| `proxy_cache` | `off` | 文件下载不应被缓存，避免敏感数据泄漏或存储浪费 |

## 后续优化

- **旧接口下线后**：将 `client_max_body_size` 从 `500M` 收紧到 `1M`，所有大文件流量已走直传 OSS，Nginx 不再承担文件传输压力。
- **HTTPS 强制**：生产环境建议配置 `listen 443 ssl` 并启用 HSTS，防止预签名 URL 在传输过程中被劫持。
- **OSS 域名独立**：若使用阿里云 OSS，建议将 OSS 域名（如 `oss-cn-hangzhou.aliyuncs.com`）通过 CNAME 绑定独立子域名（如 `cdn.bluenet.example`），前端直接访问该域名，不经过 Nginx。
