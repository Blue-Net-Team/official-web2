## 1. ISR 配置基础

- [x] 1.1 创建 `src/frontend/src/config/isr.ts`，导出 ISR 常量对象，包含 default、home、competitions、labEnvironment、direction 五个字段，每个字段优先读取对应环境变量，fallback 到 default（默认 3600）
- [x] 1.2 在 `src/frontend/.env.example` 中添加 ISR 相关环境变量的注释说明

## 2. 页面改造

- [x] 2.1 修改 `src/frontend/src/app/(public)/(home)/page.tsx`，导入 ISR 配置，添加 `export const revalidate = ISR.home`
- [x] 2.2 修改 `src/frontend/src/app/(public)/(other)/competitions/page.tsx`，导入 ISR 配置，添加 `export const revalidate = ISR.competitions`
- [x] 2.3 修改 `src/frontend/src/app/(public)/(other)/lab-environment/page.tsx`，导入 ISR 配置，添加 `export const revalidate = ISR.labEnvironment`
- [x] 2.4 修改 `src/frontend/src/app/(public)/(other)/direction/[slug]/page.tsx`，将硬编码 `revalidate = 3600` 替换为 `revalidate = ISR.direction`

## 3. 验证

- [x] 3.1 执行 `pnpm build` 确认构建成功，检查构建输出中各页面的 revalidate 配置是否生效
