package com.bluenet.web.infrastructure.storage;

public interface JudgeAssetStorage {
    /**
     * 保存判题资产到独立 bucket。
     *
     * @param objectKey
     *            判题 bucket 内的对象键。
     * @param content
     *            要写入的文件字节内容。
     * @param contentType
     *            文件内容类型。
     */
    void put(String objectKey, byte[] content, String contentType);

    /**
     * 删除判题资产对象。
     *
     * @param objectKey
     *            判题 bucket 内的对象键。
     */
    void delete(String objectKey);

    /**
     * 按前缀批量删除判题资产对象。
     *
     * @param prefix
     *            对象键前缀。
     */
    void deleteByPrefix(String prefix);

    /**
     * 读取判题资产内容。
     *
     * @param objectKey
     *            判题 bucket 内的对象键。
     * @return 对象字节内容；不存在时返回空字节数组。
     */
    byte[] get(String objectKey);
}
