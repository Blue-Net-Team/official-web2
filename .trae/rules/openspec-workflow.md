---
alwaysApply: false
description: 创建新功能、管理变更时手动引用
---

# OpenSpec 工作流

## 目录结构

```
openspec/
├── changes/archive/   # 已归档变更
└── specs/             # 规范文件
```

## 常用命令

| 命令 | 用途 |
|------|------|
| `/opsx:propose` | 创建新功能提案 |
| `/opsx:apply <name>` | 实现任务 |
| `/opsx:continue <name>` | 继续变更 |
| `/opsx:verify <name>` | 验证实现 |
| `/opsx:archive <name>` | 归档变更 |

## 引用方式

```
#openspec-workflow 我想添加用户积分系统
```

## 最佳实践

- 一个变更一个功能
- 任务粒度适中（1-2 小时）
- 验证后再归档
