## ADDED Requirements

### Requirement: UI设计文件宽度修正
UI设计文件 `docs/UI/home/Index.html` 必须占满浏览器视口宽度，容器宽度应为100%而非固定像素值。

#### Scenario: 桌面端视口占满
- **WHEN** 用户在桌面浏览器打开UI设计文件
- **THEN** 页面容器宽度占满浏览器视口
- **AND** 内容区域使用相对单位布局

#### Scenario: 移动端视口占满
- **WHEN** 用户在移动设备浏览器打开UI设计文件
- **THEN** 页面容器宽度占满设备视口
- **AND** 无横向滚动条

### Requirement: 响应式断点支持
主页必须支持三个响应式断点：移动端（<768px）、平板（768px-1024px）、桌面（>1024px）。

#### Scenario: 移动端断点生效
- **WHEN** 视口宽度小于768px
- **THEN** 应用移动端样式规则
- **AND** 布局调整为单列显示

#### Scenario: 平板断点生效
- **WHEN** 视口宽度在768px到1024px之间
- **THEN** 应用平板样式规则
- **AND** 布局调整为双列显示

#### Scenario: 桌面断点生效
- **WHEN** 视口宽度大于1024px
- **THEN** 应用桌面样式规则
- **AND** 布局保持三列显示

### Requirement: TopContent组件响应式适配
TopContent组件必须在所有断点下正确显示，包括标题、副标题和按钮。

#### Scenario: 移动端TopContent显示
- **WHEN** 视口宽度小于768px
- **THEN** 标题字体大小缩放至24px-32px
- **AND** 副标题字体大小缩放至16px-20px
- **AND** 内边距调整为20px
- **AND** 内容宽度占满容器

#### Scenario: 桌面端TopContent显示
- **WHEN** 视口宽度大于1024px
- **THEN** 标题字体大小保持48px
- **AND** 副标题字体大小保持32px
- **AND** 内边距保持147px

### Requirement: 竞赛卡片响应式适配
竞赛卡片必须在所有断点下正确排列，移动端单列，平板双列，桌面三列。

#### Scenario: 移动端竞赛卡片布局
- **WHEN** 视口宽度小于768px
- **THEN** 竞赛卡片单列显示
- **AND** 卡片宽度占满容器
- **AND** 卡片间距调整为15px

#### Scenario: 平板竞赛卡片布局
- **WHEN** 视口宽度在768px到1024px之间
- **THEN** 竞赛卡片双列显示
- **AND** 每个卡片宽度为50%

#### Scenario: 桌面竞赛卡片布局
- **WHEN** 视口宽度大于1024px
- **THEN** 竞赛卡片三列显示
- **AND** 每个卡片宽度为33.33%

### Requirement: 方向介绍组件响应式适配
方向介绍组件必须在所有断点下正确显示，包括标题、描述文本和方向卡片。

#### Scenario: 移动端方向介绍显示
- **WHEN** 视口宽度小于768px
- **THEN** 方向卡片单列垂直排列
- **AND** 标题字体缩放至28px
- **AND** 内边距调整为20px

#### Scenario: 桌面端方向介绍显示
- **WHEN** 视口宽度大于1024px
- **THEN** 方向卡片三列水平排列
- **AND** 标题字体保持43px
- **AND** 内边距保持93px

### Requirement: 招新流程组件响应式适配
招新流程组件必须在所有断点下正确显示流程卡片和连接箭头。

#### Scenario: 移动端招新流程显示
- **WHEN** 视口宽度小于768px
- **THEN** 流程卡片垂直排列
- **AND** 箭头图标旋转90度指向下方
- **AND** 卡片宽度占满容器

#### Scenario: 桌面端招新流程显示
- **WHEN** 视口宽度大于1024px
- **THEN** 流程卡片水平排列
- **AND** 箭头图标保持水平方向
- **AND** 卡片之间保持适当间距

### Requirement: 移动端导航栏适配 ✅ **已完成**
导航栏在移动端必须提供折叠菜单功能，通过汉堡菜单按钮触发。

#### Scenario: 移动端导航栏显示
- **WHEN** 视口宽度小于768px
- **THEN** 隐藏桌面导航链接
- **AND** 显示汉堡菜单按钮
- **AND** 点击汉堡菜单按钮展开抽屉导航

#### Scenario: 抽屉导航交互
- **WHEN** 用户点击汉堡菜单按钮
- **THEN** 从屏幕右侧滑出抽屉导航
- **AND** 显示所有导航链接
- **AND** 点击导航链接或遮罩层关闭抽屉

#### Scenario: 桌面端导航栏显示
- **WHEN** 视口宽度大于1024px
- **THEN** 显示桌面导航链接
- **AND** 隐藏汉堡菜单按钮

### Requirement: 字体响应式缩放
所有文本内容必须在不同设备上保持可读性，使用流体字体或媒体查询实现缩放。

#### Scenario: 标题字体缩放
- **WHEN** 视口宽度从320px增加到1920px
- **THEN** 标题字体大小在24px到48px之间平滑缩放
- **AND** 保持最小可读性要求

#### Scenario: 正文字体缩放
- **WHEN** 视口宽度从320px增加到1920px
- **THEN** 正文字体大小在14px到20px之间平滑缩放
- **AND** 保持行高在1.4到1.6之间

### Requirement: 背景图片响应式适配
背景图片必须在所有设备上正确显示，不出现拉伸或裁剪问题。

#### Scenario: 移动端背景显示
- **WHEN** 视口宽度小于768px
- **THEN** 背景图片位置和大小适配移动设备
- **AND** 背景图片不遮挡内容

#### Scenario: 桌面端背景显示
- **WHEN** 视口宽度大于1024px
- **THEN** 背景图片保持原有位置和大小
- **AND** 背景效果与设计稿一致

### Requirement: 无横向滚动
在所有支持的设备尺寸下，页面不得出现横向滚动条。

#### Scenario: 移动端无横向滚动
- **WHEN** 用户在移动设备上浏览主页
- **THEN** 页面宽度不超过视口宽度
- **AND** 不出现横向滚动条

#### Scenario: 平板无横向滚动
- **WHEN** 用户在平板设备上浏览主页
- **THEN** 页面宽度不超过视口宽度
- **AND** 不出现横向滚动条

### Requirement: 触摸目标大小
移动端所有可交互元素的最小触摸目标尺寸必须不小于44x44像素。

#### Scenario: 按钮触摸目标
- **WHEN** 用户在移动设备上点击按钮
- **THEN** 按钮触摸区域不小于44x44像素
- **AND** 按钮之间保持足够间距

#### Scenario: 链接触摸目标
- **WHEN** 用户在移动设备上点击链接
- **THEN** 链接触摸区域不小于44x44像素
- **AND** 链接之间保持足够间距

### Requirement: Ant Design组件使用ConfigProvider配置
对于Ant Design组件的字体、间距、尺寸等样式调整，必须优先使用ConfigProvider进行主题配置，而非直接修改CSS。

#### Scenario: 使用ConfigProvider配置字体大小
- **WHEN** 需要调整Ant Design组件的字体大小
- **THEN** 必须通过ConfigProvider的theme.token.fontSize配置
- **AND** 不直接修改组件的CSS样式

#### Scenario: 使用ConfigProvider配置组件尺寸
- **WHEN** 需要调整Ant Design组件的尺寸（如Button高度、Menu项高度）
- **THEN** 必须通过ConfigProvider的theme.components配置
- **AND** 确保移动端触摸目标不小于44x44像素

#### Scenario: 使用ConfigProvider配置间距
- **WHEN** 需要调整Ant Design组件的间距
- **THEN** 必须通过ConfigProvider的theme.token.padding和margin配置
- **AND** 保持与设计规范的一致性
