package rle

import commons.Coder
import commons.CoderWriter
import commons.forEachByte
import java.io.InputStream
import java.io.OutputStream

object RLECoder : Coder {
  override fun encode(input: InputStream, output: OutputStream) {
    RLECoderWriter(input, output).writeEncoded()
  }
}

private class RLECoderWriter(
  private val input: InputStream,
  private val output: OutputStream
) : CoderWriter {

  private val maxSequenceSize = Byte.MAX_VALUE.toInt()

  private var currentSequence = ByteArray(maxSequenceSize)

  private var currentSequenceSize = 0

  private val previousByte
    get() = currentSequence[currentSequenceSize - 1]

  private val currentSequenceIsRepeated
    get() = currentSequence[0] == currentSequence[1]

  override fun writeEncoded() {
    input.forEachByte { encodeNext(it.toByte()) }
    close()
  }

  private fun encodeNext(byte: Byte) {
    if (currentSequenceSize == 0 || currentSequenceSize == 1) {
      currentSequence[currentSequenceSize++] = byte
      return
    }

    if (previousByte == byte) {
      if (currentSequenceIsRepeated) {
        // just extend existed repeated sequence
        currentSequence[currentSequenceSize++] = byte
      } else {
        // found the repeated sequence
        currentSequenceSize--
        flushCurrentSequence()
        currentSequenceSize = 2
        currentSequence[0] = byte
        currentSequence[1] = byte
      }
    } else {
      if (currentSequenceIsRepeated) {
        // end of repeated sequence
        flushCurrentSequence()
        currentSequence[currentSequenceSize++] = byte
      } else {
        // just extend existed non-repeated sequence
        currentSequence[currentSequenceSize++] = byte
      }
    }

    if (currentSequenceSize == maxSequenceSize) {
      flushCurrentSequence()
    }
  }

  private fun flushCurrentSequence() {
    if (currentSequenceSize == 0) return
    if (currentSequenceIsRepeated) {
      val byteOfLength = currentSequenceSize
      val repeatedByte = previousByte
      output.write(byteArrayOf(byteOfLength.toByte(), repeatedByte), 0, 2)
    } else {
      val byteOfLength = -currentSequenceSize
      output.write(byteArrayOf(byteOfLength.toByte()), 0, 1)
      output.write(currentSequence, 0, currentSequenceSize)
    }
    currentSequenceSize = 0
  }

  override fun close() {
    input.close()
    flushCurrentSequence()
    output.close()
  }

}