package a0

import java.io.Closeable
import java.io.InputStream

internal class BitInputStream(
  private val input: InputStream
) : Closeable {

  private var currentByte = 0

  private var bitsToGo = 0

  fun read(): Int {
    if (currentByte == -1) return -1
    if (bitsToGo == 0) {
      currentByte = input.read()
      if (currentByte == -1) {
        close()
        return -1
      }
      bitsToGo = 8
    }
    bitsToGo--
    return (currentByte ushr bitsToGo) and 1
  }

  override fun close() {
    input.close()
    currentByte = -1
    bitsToGo = 0
  }
}