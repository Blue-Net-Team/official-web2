## Context

### 当前系统状态

AI Service 的文档入库流水线位于 `src/ai-service/pipeline/load2db_pipeline.py`，负责将 `docs/ai-knowledge-base/` 下的 Markdown 文档读取、分段、生成标签、Embedding、存入 PgVector。

当前标签生成链路：

```
读取文档 → 语义分段(chunk) → LLM生成标签 → normalize_tags()硬编码归一化 → embedding → 入库
```

归一化依赖 `tag_normalization.py` 中硬编码的 `_TAG_SYNONYMS` 映射表：

```python
_TAG_SYNONYMS = {
    "电控方向": ["电控", "嵌入式", "嵌入式编程", "STM32", ...],
    "结构方向": ["结构", "机械设计", "SolidWorks", ...],
    ...
}
```

**问题**：映射表覆盖有限，LLM 生成的新变体（如"32位单片机"、"Keil开发"）不在表中，直接作为新标签入库，形成"标签孤岛"——入库了但检索时通过同义词扩展无法召回。

### 知识库文档特点

`docs/ai-knowledge-base/` 包含 17 个 Markdown 文件，内容结构清晰：

| 文档 | 内容主题 | 典型标签 |
|------|----------|----------|
| 01-团队简介.md | 团队概况、历史、规模 | 蓝网科技、团队简介 |
| 02-技术方向介绍.md | 三方向概述 | 结构方向、电控方向、视觉方向 |
| 04-结构方向详解.md | 结构方向详细说明、岗位、学习路径 | 结构方向、SolidWorks、机械设计、就业 |
| 12-FAQ-基础问题.md | 零基础、专业、性别等FAQ | 招新、FAQ、零基础 |

同一概念在多个文档中反复出现（如"三个方向"在 01、02、04、05、06、07 中都有），LLM 容易生成多种字面表述。

---

## Goals / Non-Goals

**Goals:**
- 删除硬编码同义词映射表，消除人工维护成本
- 入库阶段防止同义标签重复入库，从源头减少标签膨胀
- 检索层简化逻辑，不再依赖同义词字面扩展
- 保持检索行为不变（语义搜索能力不受影响）

**Non-Goals:**
- 不修改数据库 Schema
- 不修改检索 API 接口和返回格式
- 不引入新的外部依赖（复用已有的 Embedding 和 Reranker）
- 不要求 100% 消除所有同义标签（Reranker 阈值留有余量）
- 不修改语义分段(chunking)逻辑

---

## Decisions

### Decision 1: Prompt 工程主导 + Reranker 兜底

**选择**: 用改进的 Prompt 引导 LLM 生成更一致的标签，再用 Reranker 做入库时的动态归并兜底。

**理由**:
- 相比纯 Reranker 归并，Prompt 引导能在生成阶段就减少变体
- Prompt 中注入全局已有标签列表，让 LLM 知道应该复用什么
- Reranker 作为兜底，处理 LLM 仍产生的边缘 case
- 避免了纯 Prompt 方案"无法完全消除同义"的问题

**替代方案分析**:

| 方案 | 优点 | 缺点 |
|------|------|------|
| 纯 Reranker 归并（不做 Prompt 改进） | 兜底全面 | 成本高（每次入库都要对大量标签做 Reranker），LLM 持续生成低质量标签 |
| 纯 Prompt 改进（不做 Reranker） | 成本低 | 无法完全消除同义，LLM 仍会生成边缘变体 |
| **Prompt + Reranker（选定）** | 生成阶段减少变体 + 兜底处理边缘 case | 实现复杂度中等 |

### Decision 2: Reranker 阈值 0.90

**选择**: 归并阈值设为 0.90。

**理由**:
- 0.90 是经验值，能区分"同义"和"相关但不等同"
- "嵌入式" vs "电控方向" → Reranker 约 0.95（归并）
- "SolidWorks" vs "结构方向" → Reranker 约 0.75（不归并）
- 阈值偏严格，宁可漏归并也不误判（保持标签粒度）

**阈值选择的边界分析**:

| 标签对 | 预期 Reranker Score | 0.90 阈值判断 | 说明 |
|--------|---------------------|---------------|------|
| "嵌入式" vs "电控方向" | ~0.95 | 归并 | 同义 |
| "招新" vs "报名" | ~0.92 | 归并 | 同义（上下文均为团队招新） |
| "STM32" vs "电控方向" | ~0.80 | 不归并 | 包含关系，非同义 |
| "SolidWorks" vs "结构方向" | ~0.75 | 不归并 | 工具 vs 方向 |
| "蓝网" vs "蓝网科技" | ~0.98 | 归并 | 同义 |
| "比赛" vs "竞赛" | ~0.96 | 归并 | 同义 |
| "Python" vs "视觉方向" | ~0.60 | 不归并 | 不同概念 |

**替代方案**: 0.85 → 可能把"STM32"误判为"电控方向"；0.95 → 过于严格，可能漏掉"招新"vs"报名"。

### Decision 3: Embedding 粗筛 Top-5 + Reranker 精排

**选择**: 先用 embedding 余弦相似度找 Top-5 候选，再用 Reranker 对这 5 个候选做精排。

**理由**:
- 对所有已有标签都做 Reranker 调用成本太高（已有标签可能几十上百个）
- Embedding 粗筛能快速排除明显不相关的标签
- Top-5 足够覆盖潜在的同义候选

**性能估算**:

假设已有标签库 50 个：
- 无粗筛：每个新标签需要 50 次 Reranker 调用
- Top-5 粗筛：每个新标签只需要 5 次 Reranker 调用 + 50 次 embedding 余弦相似度计算（本地计算，零 API 成本）

### Decision 4: 标签分层结构写入 Prompt

**选择**: 在 Prompt 中定义三层标签结构（方向/主题/技术），引导 LLM 生成一致粒度的标签。

**理由**:
- 知识库文档内容结构清晰，天然适合分层
- 方向标签作为"锚点"，保证每个 chunk 都有粗粒度标签兜底
- 检索时搜"结构方向"能召回所有结构相关 chunk，即使技术标签不同

**三层结构定义**:

| 层级 | 粒度 | 示例 | 是否必选 |
|------|------|------|----------|
| 方向标签 | 粗 | 结构方向、电控方向、视觉方向 | 必选其一 |
| 主题标签 | 中 | 招新、考核、比赛、FAQ、学习资源 | 必选 |
| 技术标签 | 细 | SolidWorks、STM32、Python、OpenCV | 可选 |

---

## Implementation Details

### 1. 新 Prompt 设计

```python
TAG_GENERATION_PROMPT = """\
请为以下文本生成2-3个标签，用于信息检索分类。

【标签层级规范】
每个 chunk 的标签必须按以下层级选择，优先复用"已有标签"列表中的标签：

1. 方向标签（必选，选其一）：
   结构方向 | 电控方向 | 视觉方向
   → 判断文本主要讲哪个方向的技术内容

2. 主题标签（必选，从已有标签中选择或新建）：
   团队简介 | 招新 | 考核 | 比赛 | FAQ | 学习资源 | 就业 | 设备要求
   → 判断文本的核心主题是什么

3. 技术标签（可选，有具体技术工具时加）：
   SolidWorks | STM32 | Python | OpenCV | Keil | C语言
   → 文本中提到的具体技术或工具

【已有标签】（优先复用，不要创造同义词）：
{existing_tags}

【生成规则】
- 只输出标签，用英文逗号分隔
- 不要输出层级名称，只输出标签本身
- 如果文本主题与已有标签匹配，直接复用已有标签名
- 不要生成过于宽泛的标签（如"技术"、"知识"、"团队"）

文本: {text}"""
```

### 2. Reranker 动态归并实现

```python
import math

SIMILARITY_THRESHOLD = 0.88  # Embedding 粗筛阈值
RERANKER_THRESHOLD = 0.90    # Reranker 精排阈值


def _cosine_similarity(a: list[float], b: list[float]) -> float:
    """计算两个向量的余弦相似度。"""
    dot = sum(x * y for x, y in zip(a, b))
    norm_a = math.sqrt(sum(x * x for x in a))
    norm_b = math.sqrt(sum(x * x for x in b))
    if norm_a == 0 or norm_b == 0:
        return 0.0
    return dot / (norm_a * norm_b)


def _resolve_tag_with_reranker(
    new_tag: str,
    existing_tags: list[TagRecord],
    embedding,
    reranker,
) -> str:
    """判断新标签应归并到哪个已有标签，或保持独立。

    流程：
    1. 精确匹配检查
    2. Embedding 粗筛 Top-5
    3. Reranker 精排
    4. 阈值判断

    Returns:
        最终应使用的标签名（归并后的已有标签名，或保持原样的新标签名）
    """
    # 1. 快速路径：精确匹配
    existing_names = {tag.tag_name for tag in existing_tags}
    if new_tag in existing_names:
        return new_tag

    # 2. 粗筛：计算新标签与所有已有标签的 embedding 余弦相似度
    new_vec = embedding.embed_texts([new_tag])[0]
    scored = []
    for tag in existing_tags:
        sim = _cosine_similarity(new_vec, tag.tag_vector)
        scored.append((tag.tag_name, sim))

    # 按相似度排序，取 Top-5
    scored.sort(key=lambda x: x[1], reverse=True)
    top5 = [name for name, sim in scored[:5] if sim >= SIMILARITY_THRESHOLD]

    if not top5:
        _log.debug(f"标签粗筛无候选: '{new_tag}' (best_sim={scored[0][1]:.4f})")
        return new_tag

    # 3. 精排：Reranker 判断新标签与 Top-5 候选的同义程度
    rerank_results = reranker.rerank(new_tag, top5, top_k=1)

    # 4. 阈值判断
    if rerank_results and rerank_results[0].relevance_score >= RERANKER_THRESHOLD:
        matched = top5[rerank_results[0].index]
        _log.info(
            f"标签归并: '{new_tag}' → '{matched}' "
            f"(reranker={rerank_results[0].relevance_score:.4f}, "
            f"emb_sim={scored[0][1]:.4f})"
        )
        return matched

    _log.debug(
        f"标签保持独立: '{new_tag}' "
        f"(best_reranker={rerank_results[0].relevance_score:.4f if rerank_results else 'N/A'}, "
        f"best_emb={scored[0][1]:.4f})"
    )
    return new_tag
```

### 3. 修改后的入库流水线流程

```
阶段一: 并发读取所有文档并语义分段
    ├── 读取 17 个 .md 文件
    ├── SemanticChunker.split() 分段
    └── 返回 chunk 列表

阶段二: 查询数据库已有标签
    ├── store.get_all_tags()
    └── 返回 existing_tags（含 tag_vector）

阶段三: 生成 chunk 标签（改进 Prompt）
    ├── 对每个 chunk：
    │   ├── 注入已有标签列表到 Prompt
    │   ├── LLM 生成原始标签
    │   └── 返回 [tag1, tag2, ...]
    └── 收集所有 chunk_tag_map

阶段四: 【新增】动态归并
    ├── 收集所有新标签（不在已有标签中）
    ├── 批量 embedding 新标签
    ├── 对每个新标签：
    │   ├── Embedding 粗筛 Top-5 已有标签
    │   ├── Reranker 精排
    │   └── 阈值判断：归并 or 保持独立
    ├── 更新 chunk_tag_map（用归并后的标签替换）
    └── 重新计算需要入库的新标签集合

阶段五: 插入新标签
    ├── 对新标签批量 embedding
    └── store.insert_tags()

阶段六: 插入所有 chunks
    ├── 对每个 chunk：
    │   ├── embedding
    │   └── store.insert_chunks()

阶段七: 统计并更新标签引用次数
    └── SQL 更新 tb_rag_tags.chunks_count
```

### 4. 归并结果处理

```python
# 归并映射示例
merge_map = {
    "嵌入式": "电控方向",      # Reranker 0.95 → 归并
    "招新": "报名",           # Reranker 0.92 → 归并
    "SolidWorks": "SolidWorks", # Reranker 0.75 → 保持独立
    "新标签A": "新标签A",      # 无候选 → 保持独立
}

# 更新 chunk_tag_map
for chunk, tags in chunk_tag_map.items():
    chunk_tag_map[chunk] = [merge_map.get(t, t) for t in tags]

# 最终需要入库的新标签
all_tag_names = set()
for tags in chunk_tag_map.values():
    all_tag_names.update(tags)
new_tag_names = list(all_tag_names - existing_names)
```

---

## Risks / Trade-offs

| Risk | 影响 | Mitigation |
|------|------|-----------|
| Reranker API 调用增加入库耗时 | 中 | 粗筛后只 Top-5 做 Reranker；流水线为离线批处理，耗时不是首要问题 |
| Reranker 阈值 0.90 漏掉部分同义词 | 低 | 可接受——宁可保守也不误判，误判会导致不同概念被错误归并 |
| Prompt 注入大量标签超出上下文窗口 | 低 | 初期 < 50 个标签，远小于 LLM 上下文窗口（通常 8K+） |
| 删除 `expand_tags` 后精确匹配召回率下降 | 低 | 向量搜索已覆盖大部分语义匹配；如后续发现问题，可改用标签 embedding 做第二路搜索 |
| 新标签被误判归并到不相关的已有标签 | 低 | 两层过滤（Embedding 粗筛 + Reranker 精排）降低误判概率；阈值 0.90 偏严格 |

---

## Migration Plan

1. **开发测试**
   - 修改代码后在本地运行 `load2db_pipeline.py`
   - 观察日志中的归并记录数量和质量
   - 人工抽查几个边界 case

2. **数据迁移（如需）**
   - 清空现有标签和 chunk 数据：`store.drop_collection()`
   - 重新运行入库流水线
   - 对比新旧标签表大小

3. **回滚策略**
   - 代码回滚：`git checkout` 恢复 `tag_normalization.py` 和 `load2db_pipeline.py`
   - 数据回滚：重新运行旧版入库流水线（如有备份）

---

## Open Questions

1. Reranker 阈值 0.90 是否需要根据实际入库日志中的归并记录调优？
2. 当标签库增长到 200+ 时，Prompt 中注入全部标签是否会超出上下文窗口或影响 LLM 生成质量？
3. 是否需要定期运行标签聚类任务，发现入库后遗漏的同义标签对？
