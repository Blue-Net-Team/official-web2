import os
from chunking import SemanticChunker
from llm_providers import EmbeddingFactory, LLMFactory
from retrieval import PgVectorStore, TagRecord
from loguru import logger
from retrieval.base import ChunkRecord
_log = logger.bind(module="load2db_pipeline")

DOC_FOLDER_PATH = "docs\\ai-knowledge-base"
# 语言模型，用于标签生成
llm = LLMFactory.create(provider="deepseek")
# 嵌入模型
embedding = EmbeddingFactory.create(provider="siliconflow")
# 分段器
chunker = SemanticChunker(llm)
# 标签生成提示
TAG_GENERATION_PROMPT = """\
请为以下文本生成2-3个简短标签（中文标签），用于信息检索分类。
已有可用标签: {existing_tags}

如果以下文本的主题与已有标签匹配，请直接输出对应的已有标签名；
如果已有标签都不合适，再生成新的简短标签。
只输出标签，用英文逗号分隔，不要输出任何其他内容。

文本: {text}"""


# 加载文档路径
docs = []
for root, _, files in os.walk(DOC_FOLDER_PATH):
    for f in files:
        if f.lower().endswith(".md"):
            docs.append(os.path.join(root, f))

# 遍历路径
for doc in docs:
    with open(doc, "r", encoding="utf-8") as f:
        # 分片文档，语义分割
        raw_text = f.read()
        chunks = chunker.split(raw_text)
    _log.info(f"文档 {doc} 分片为 {len(chunks)} 个分段")

for chunk in chunks:
    # 读取已有的tag
    existing_tags = []
    # 从db读取已有的tag
    
    with PgVectorStore() as store:
        existing_tag_records = store.get_all_tags()
        existing_tags = [tag.tag_name for tag in existing_tag_records]
    
    # 对chunk打标，复用已有的标签
    prompt = TAG_GENERATION_PROMPT.format(
        existing_tags=existing_tags, text=chunk
    )
    response = llm.invoke([{"role": "user", "content": prompt}])
    tags = response.strip().split(",")
    _log.info(f"生成标签: {tags}")
    
    # 记录复用标签数
    num_reused_tags = sum(tag in existing_tags for tag in tags)
    _log.info(f"复用 {num_reused_tags} 个标签\t新标签数: {len(tags) - num_reused_tags}")
    
    # 取出新标签
    new_tag = [tag.strip() for tag in tags if tag not in existing_tags]

    # 对新tag向量化
    new_tag_embeddings = embedding.embed_texts(new_tag)

    # 构造tagRecord
    tag_records = []
    for tag_name, tag_vector in zip(new_tag, new_tag_embeddings):
        tag_records.append(TagRecord(tag_name=tag_name, tag_vector=tag_vector))
    
    # 存储tag到db
    with PgVectorStore() as store:
        store.insert_tags(tag_records)
        _log.info(f"存储 {len(tag_records)} 个新标签")

    # 对chunk向量化
    chunk_embedding = embedding.embed_texts([chunk])

    # 存储chunk到db
    chunk_record = ChunkRecord(content=chunk, chunk_vector=chunk_embedding[0], tags=tags)
    with PgVectorStore() as store:
        store.insert_chunks([chunk_record])
        _log.info(f"存储 {chunk_record.content} 到 chunks")

