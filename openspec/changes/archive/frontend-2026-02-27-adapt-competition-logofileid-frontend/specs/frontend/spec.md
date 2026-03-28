## Purpose

前端适配后端 API 变更，使用 `logoFileId` 字段通过文件下载接口获取竞赛Logo图片。

## Requirements

### Requirement: 更新类型定义

#### Scenario: CompetitionBriefDTO 接口更新
- **WHEN** 定义竞赛简要信息接口
- **THEN** 接口 SHALL 包含 `logoFileId: number | null` 字段
- **THEN** `logoUrl` 字段 SHALL 保留但标记为废弃（注释说明）

### Requirement: 更新竞赛卡片组件

#### Scenario: 显示竞赛Logo
- **WHEN** 渲染竞赛卡片组件
- **THEN** 组件 SHALL 使用 `logoFileId` 构建图片URL
- **THEN** 图片URL SHALL 为 `/api/v1/file/download/${logoFileId}`
- **THEN** 当 `logoFileId` 为 null 时，SHALL 显示默认图标

#### Scenario: 处理图片加载
- **WHEN** 图片加载失败
- **THEN** SHALL 显示默认图标（FireOutlined）

## TypeScript Interface Changes

### CompetitionBriefDTO

```typescript
/**
 * 竞赛简介
 * 对应后端 CompetitionBriefDTO.java
 */
export interface CompetitionBriefDTO {
    id: number;
    name: string;
    shortName: string;
    /** @deprecated 请使用 logoFileId */
    logoUrl: string | null;
    /** Logo文件ID，用于调用下载接口 */
    logoFileId: number | null;
    summary: string;
}
```

## Component Changes

### CompetitionCard

```tsx
const CompetitionCard = ({ competition }: { competition: CompetitionBriefDTO }) => {
    // 构建图片URL
    const logoImageUrl = competition.logoFileId 
        ? `/api/v1/file/download/${competition.logoFileId}`
        : null;

    return (
        <Card className={styles.card}>
            <Flex align="start" gap={10}>
                <div className={styles.logoWrapper}>
                    {logoImageUrl ? (
                        <Image
                            src={logoImageUrl}
                            alt={competition.name}
                            width={64}
                            height={44}
                            className={styles.logo}
                        />
                    ) : (
                        <FireOutlined style={{ fontSize: 28, color: 'white' }} />
                    )}
                </div>
                {/* ... */}
            </Flex>
        </Card>
    );
};
```

## Migration Guide

### 前端代码迁移

**变更前：**
```tsx
{competition.logoUrl ? <Image
    src={competition.logoUrl}
    alt={competition.name}
    width={64}
    height={44}
/> : <FireOutlined />}
```

**变更后：**
```tsx
{competition.logoFileId ? <Image
    src={`/api/v1/file/download/${competition.logoFileId}`}
    alt={competition.name}
    width={64}
    height={44}
/> : <FireOutlined />}
```

## Testing Checklist

- [ ] 类型定义正确，无TypeScript错误
- [ ] 竞赛列表正常显示
- [ ] 有Logo的竞赛显示正确图片
- [ ] 无Logo的竞赛显示默认图标
- [ ] 图片加载失败时显示默认图标
