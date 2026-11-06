# 压测结果目录

每次实验使用独立 `runId` 子目录，保存容器/JVM 原始时序、k6 摘要、实验元数据和汇总报告。

本目录中的实际结果默认被 Git 忽略。不得提交密码、Cookie、JWT、CSRF Token、预签名 URL、环境密钥或完整业务文件。

实验阶段统一使用：`idle`、`preflight`、`warmup`、`ramp`、`steady`、`drain`、`recovery`；失败或提前中止时保留部分结果并设置 `complete=false`。
