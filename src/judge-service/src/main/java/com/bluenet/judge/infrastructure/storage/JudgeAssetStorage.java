package com.bluenet.judge.infrastructure.storage;

/**
 * 判题资产对象存储接口。
 */
public interface JudgeAssetStorage {
    /**
     * 读取判题资产对象。
     *
     * @param objectKey
     *            判题 bucket 内对象键。
     * @return 文件字节内容。
     */
    byte[] get(String objectKey);

    /**
     * 写入判题资产对象。
     *
     * @param objectKey
     *            判题 bucket 内对象键。
     * @param content
     *            文件字节内容。
     * @param contentType
     *            文件内容类型。
     */
    void put(String objectKey, byte[] content, String contentType);

    /**
     * 删除判题资产对象。
     *
     * @param objectKey
     *            判题 bucket 内对象键。
     */
    void delete(String objectKey);
}
