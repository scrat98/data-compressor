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

  private val MAX_SEQUENCE_SIZE = 255 + 2

  private var currentSequenceSize = 0

  private var previousByte = -1

  override fun writeEncoded() {
    input.forEachByte { encodeNext(it) }
    close()
  }

  private fun encodeNext(byte: Int) {
    if (byte == previousByte) {
      currentSequenceSize++
    } else {
      flushCurrentSequence()
      output.write(byte)
      currentSequenceSize = 1
    }
    if (currentSequenceSize == MAX_SEQUENCE_SIZE) {
      flushCurrentSequence()
      currentSequenceSize = 1
    }
    previousByte = byte
  }

  private fun flushCurrentSequence() {
    if (currentSequenceSize < 2) return
    val repeatedByte = previousByte.toByte()
    val repeatedSequenceSize = (currentSequenceSize - 2).toByte()
    output.write(byteArrayOf(repeatedByte, repeatedSequenceSize), 0, 2)
    currentSequenceSize = 0
  }

  override fun close() {
    input.close()
    flushCurrentSequence()
    output.close()
  }

}