## 1. 本地 Values

- [x] 1.1 以当前生产配置为基线新增七个本地 values，保留拓扑、探针、资源和持久化配置
- [x] 1.2 替换本地 Registry、local 镜像标签、域名及 OSS bucket 占位符，确认敏感项仍引用 Secret

## 2. 静态验证

- [x] 2.1 对 Chart 执行 helm lint，并对七份本地 values 分别执行 helm template
- [x] 2.2 检查渲染结果中的副本、Service 类型、节点调度、local-path、镜像标签和本地环境参数
- [x] 2.3 确认生产 values、Chart 模板、业务代码及 GitHub Actions 未被修改
