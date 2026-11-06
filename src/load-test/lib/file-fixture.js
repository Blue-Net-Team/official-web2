/**
 * 创建固定大小、内容确定的二进制 fixture。
 * PK 03 04 文件头用于通过后端 ZIP 魔数校验，剩余字节填零以固定上传大小。
 */
export function createZipFixture(sizeBytes) {
  if (!Number.isInteger(sizeBytes) || sizeBytes < 22) {
    throw new Error('ZIP fixture size must be an integer of at least 22 bytes');
  }
  const bytes = new Uint8Array(sizeBytes);
  bytes.set([0x50, 0x4b, 0x03, 0x04], 0);
  return bytes.buffer;
}
