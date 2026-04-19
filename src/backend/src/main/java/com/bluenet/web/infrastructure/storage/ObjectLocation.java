package com.bluenet.web.infrastructure.storage;

/**
 * 对象存储位置。
 *
 * @param bucket
 *            存储桶名称
 * @param objectKey
 *            bucket 内的对象路径
 */
public record ObjectLocation(String bucket, String objectKey) {
}
