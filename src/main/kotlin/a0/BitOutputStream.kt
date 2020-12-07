package a0

import java.io.Closeable
import java.io.OutputStream

internal class BitOutputStream(
  private val output: OutputStream
) : Closeable {

  private var currentByte = 0

  private var numBitsFilled = 0

  fun write(bit: Int) {
    require(bit == 0 || bit == 1) { "Argument must be 0 or 1" }
    currentByte = (currentByte shl 1) or bit
    numBitsFilled++
    if (numBitsFilled == Byte.SIZE_BITS) {
      output.write(currentByte)
      currentByte = 0
      numBitsFilled = 0
    }
  }

  override fun close() {
    while (numBitsFilled != 0) write(0)
    output.close()
  }
}