## ADDED Requirements

### Requirement: 考生考题页面容器级玻璃材质统一为烟色玻璃

考生考题页面（assessment questions 列表页与 QuestionDetail 作答页）的所有容器级面板 SHALL 使用 `SmokedGlassCard` 组件渲染，MUST NOT 使用无底色纯透明玻璃配方（`bg-white/[0.0x]` + 细边框、无深色保底底色的容器壳）。考题页作为 DarkVeil 全屏场景，所有容器面板统一使用 deep（浓烟）档：不透明深色实底可阻挡背景光斑染色，保证任何帧下视觉稳定。

#### Scenario: 作答页容器面板

- **WHEN** 考生打开题目作答页
- **THEN** 题面主卡及页面所有容器面板（队伍、倒计时、提交、答题信息、算法题/选择题 section、编辑器容器、判题结果）均以 `<SmokedGlassCard>`（deep，不挂 `hoverable`）渲染

#### Scenario: backdrop-filter 失效环境

- **WHEN** 页面运行在 backdrop-filter 不生效的环境（如微信 Android XWEB 内核）
- **THEN** 所有面板仍因 SmokedGlassCard 的深色保底底色保持有形可辨识，不出现"融化"成背景的无形区域

#### Scenario: 面板内边距归属

- **WHEN** 容器面板迁移为 SmokedGlassCard
- **THEN** 删除面板内容根部的 Tailwind padding 工具类（`p-5`/`p-7` 等），内边距由组件默认 24px（`--sgc-padding`）统一提供

### Requirement: 面板内层元素分层规则

面板内部的元素按"容器 / 内层格子 / 内联点缀"三级处理：仅容器级元素使用 SmokedGlassCard；内层格子去底留框；内联点缀与 antd 原子组件保持原样。

#### Scenario: 内层格子去底留框

- **WHEN** 面板内存在嵌套的"面板状"内层容器（题目导航格、提交记录格、用例格等）
- **THEN** 移除其 `bg-white/[0.03]`~`bg-white/[0.06]` 透明底色，保留边框并加强至 `border-white/[0.15]`，保证在 DarkVeil 光斑透色的半透明实底上仍可分层

#### Scenario: 内联点缀零改动

- **WHEN** 面板内存在 blockquote 引用条、行内代码、代码块（`bg-black/30`）、彩色状态标签（AC/WA/选中态等）
- **THEN** 这些元素保持原有样式不改，仅随外层容器换底后自然呈现

#### Scenario: antd 原子组件零改动

- **WHEN** 面板内存在 antd 原子组件（Select、Tag、Button、Upload、Collapse、Tooltip、Modal 等）
- **THEN** 组件样式由全局 ConfigProvider（darkAlgorithm）token 驱动，不做任何组件级样式覆盖
