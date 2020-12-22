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

internal fun Int.toBytes(): ByteArray = byteArrayOf(
    (this shr 24 and 0xff).toByte(),
    (this shr 16 and 0xff).toByte(),
    (this shr 8 and 0xff).toByte(),
    (this shr 0 and 0xff).toByte()
)

internal fun ByteArray.toInt(): Int = this.map { it.toInt() }.bytesToInt()

internal fun List<Int>.bytesToInt(): Int {
  require(this.size == 4) { "Byte array should contain 4 bytes only" }
  return (
      (0xff and this[0] shl 24) or
          (0xff and this[1] shl 16) or
          (0xff and this[2] shl 8) or
          (0xff and this[3] shl 0)
      )
}

internal fun InputStream.read4BytesAsInt(): Int {
  val bytes = (1..4).map { this.read() }
  if (bytes[0] == -1) return -1
  return bytes.bytesToInt()
}

internal fun OutputStream.writeIntAs4Bytes(number: Int) {
  this.write(number.toBytes())
}

internal fun Int.asBitsString(length: Int): String = this.toString(2).padStart(length, '0')

internal fun String.asBits(): List<Int> = this.map { if (it == '0') 0 else 1 }

internal fun List<Int>.bitsToInt() =
    this.map { if (it == 0) '0' else '1' }.joinToString("").toInt(2)