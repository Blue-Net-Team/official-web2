## MODIFIED Requirements

### Requirement: DB 层测试基类保留完整迁移与隔离

`DBIntegrationTest` SHALL 为需要真实数据库的测试提供真实 PostgreSQL 实例与完整 Flyway 迁移，并 MUST 保证用例之间数据互不污染。

迁移 MUST 在每个测试类中至少执行一次，且 MUST NOT 在每个测试方法中重复执行。`DBIntegrationTest` MUST NOT 关闭 Flyway，原因是 Application Service 与 RepositoryImpl 测试断言依赖真实 schema 与迁移插入的种子数据。

用例之间的数据隔离 SHALL 通过清空数据实现（`TRUNCATE ... RESTART IDENTITY CASCADE`），MUST NOT 通过逐用例重建 schema 实现——表结构在每个用例中完全相同，drop 并重建 schema 是达成数据隔离最重的方式。

#### Scenario: DB 层测试执行完整迁移
- **WHEN** 运行任意继承 `DBIntegrationTest` 的测试类
- **THEN** 数据库 SHALL 已应用全部 Flyway 迁移脚本
- **THEN** 迁移插入的种子数据 SHALL 存在

#### Scenario: 迁移在测试类内只执行一次
- **WHEN** 运行一个包含多个测试方法的 `DBIntegrationTest` 子类
- **THEN** 该测试类对应的数据库 SHALL 只执行一次 Flyway 迁移
- **THEN** 该测试类日志中的迁移执行次数 SHALL 为 1，而不等于测试方法数

#### Scenario: DB 层测试用例之间数据隔离
- **WHEN** 同一个 `DBIntegrationTest` 子类中先后执行两个会写入数据的用例
- **THEN** 第二个用例开始前 SHALL 无法读取到第一个用例写入的数据

### Requirement: 只读参考表不参与数据清空且受运行时保护

`tb_role` SHALL 被定位为只读参考表：其内容由数据库迁移写入，此后 MUST NOT 被任何运行期代码或测试修改。生产代码 SHALL NOT 提供创建或修改角色的能力。

`DBIntegrationTest` MUST NOT 把 `tb_role` 纳入清空范围。原因是清空后无法在不重复迁移的前提下恢复其内容，而迁移每类只执行一次。

为守住只读性，`DBIntegrationTest` MUST 在迁移完成后快照 `tb_role` 的内容，并在每个测试方法结束时比对快照与当前内容。不一致时 MUST 判定该测试方法失败，并明确提示「测试写入了只读参考表」，而非把污染留到后续用例造成难以定位的失败。

#### Scenario: 清空范围排除只读参考表
- **WHEN** 一个 `DBIntegrationTest` 子类的用例执行完毕并触发数据清空
- **THEN** `tb_role` SHALL NOT 被清空
- **THEN** `tb_role` SHALL 仍包含 SUPER_ADMIN、DIRECTION_ADMIN、MEMBER、CANDIDATE 四个角色

#### Scenario: 只读参考表的内容在迁移后保持稳定
- **WHEN** 依次执行同一个 `DBIntegrationTest` 子类的多个用例
- **THEN** 每个用例开始时 `tb_role` 的内容 SHALL 与迁移完成时快照的内容一致

#### Scenario: 测试写入只读参考表时立即失败
- **WHEN** 某个测试方法向 `tb_role` 插入、更新或删除数据
- **THEN** 该测试方法 SHALL 失败
- **THEN** 失败信息 SHALL 明确指出有测试写入了只读参考表
- **THEN** 该写入 SHALL NOT 泄漏到后续用例

#### Scenario: 角色名查询始终可用
- **WHEN** 任意 `DBIntegrationTest` 子类通过 `RoleFixture.roleId(roleMapper, RoleType.MEMBER)` 获取角色 ID
- **THEN** 查询 SHALL 返回非空结果

### Requirement: 可变种子表通过迁移快照恢复

`tb_direction_learning_step` 在生产中可被创建（`LearningPathAppService.createStep`），属于可变表，因此 MUST 参与数据清空。其种子数据（迁移 V1 写入的 12 行）MUST 在每个用例清空后被恢复。

恢复方式 SHALL 为：在迁移完成后对该表的内容做内存快照，清空后按快照逐行回填，并修正自增序列。种子数据 MUST NOT 在测试代码中硬编码——迁移会改写种子数据（V26 更新标题、V28 写入 `sort_order`），硬编码会产生静默漂移且漂移无告警。

#### Scenario: 可变种子表被清空后恢复
- **WHEN** 一个用例向 `tb_direction_learning_step` 写入数据，随后执行下一个用例
- **THEN** 下一个用例 SHALL 无法读取到前一个用例写入的数据
- **THEN** 该表中 `computer_vision` 方向的行数 SHALL 为 4，且标题与 `sort_order` SHALL 为迁移演进后的最终值

#### Scenario: 依赖种子数据的既有断言保持通过
- **WHEN** `LearningPathAppServiceImplIntegrationTest` 的用例自建 2 行后查询该方向的学习路径
- **THEN** 结果 SHALL 为 6 行（4 行种子 + 2 行自建）

#### Scenario: 回填后自增序列可用
- **WHEN** 一个用例在种子数据回填后向该表插入新行
- **THEN** 插入 SHALL 成功且不产生主键冲突
