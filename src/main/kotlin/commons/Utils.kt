package commons

import java.io.InputStream
import java.io.OutputStream

internal const val NUMBER_OF_CHARS = 256

internal fun InputStream.forEachByte(action: (byte: Int) -> Unit) {
  while (true) {
    val byte = this.read()
    if (byte == -1) break
    action(byte)
  }
}

internal fun InputStream.read4BytesAsInt(): Int {
  val bytes = (1..4).map { this.read() }
  if (bytes[0] == -1) return -1
  return (
      (0xff and bytes[0] shl 24) or
          (0xff and bytes[1] shl 16) or
          (0xff and bytes[2] shl 8) or
          (0xff and bytes[3] shl 0)
      )
}

internal fun OutputStream.writeIntAs4Bytes(number: Int) {
  val bytes = byteArrayOf(
      (number shr 24 and 0xff).toByte(),
      (number shr 16 and 0xff).toByte(),
      (number shr 8 and 0xff).toByte(),
      (number shr 0 and 0xff).toByte()
  )
  this.write(bytes)
}