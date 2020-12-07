package a0

import commons.Coder
import commons.CoderWriter
import commons.forEachByte
import java.io.InputStream
import java.io.OutputStream

object A0Coder : Coder {
  override fun encode(input: InputStream, output: OutputStream) {
    A0CoderWriter(input, output).writeEncoded()
  }
}

private class A0CoderWriter(
  private val input: InputStream,
  private val output: OutputStream
) : CoderWriter {

  private val frequencyModel = A0FrequencyModel()

  private val bitOutputStream = BitOutputStream(output)

  private var low = 0

  private var high = CODE_VALUE_MAX

  private var bitsToFollow = 0

  override fun writeEncoded() {
    input.forEachByte { byte -> write(byte) }
    close()
  }

  private fun write(char: Int) {
    val charIndex = frequencyModel.charToIndex(char)
    encodeNext(charIndex)
    frequencyModel.update(charIndex)
  }

  private fun encodeNext(charIndex: Int) {
    val range = high - low + 1
    val total = frequencyModel.getTotal()
    val charLow = frequencyModel.getLow(charIndex)
    val charHigh = frequencyModel.getHigh(charIndex)
    high = low + range * charHigh / total - 1
    low = low + range * charLow / total

    while (true) {
      if (high < CODE_VALUE_HALF) {
        writeBitWithFollow(0)
      } else if (low >= CODE_VALUE_HALF) {
        writeBitWithFollow(1)
        low -= CODE_VALUE_HALF
        high -= CODE_VALUE_HALF
      } else if (low >= CODE_VALUE_FIRST_QUARTER && high < CODE_VALUE_THIRD_QUARTER) {
        bitsToFollow++
        low -= CODE_VALUE_FIRST_QUARTER
        high -= CODE_VALUE_FIRST_QUARTER
      } else break
      low = 2 * low
      high = 2 * high + 1
    }
  }

  private fun writeBitWithFollow(bit: Int) {
    bitOutputStream.write(bit)
    val oppositeBit = if (bit == 0) 1 else 0
    while (bitsToFollow > 0) {
      bitOutputStream.write(oppositeBit)
      bitsToFollow--
    }
  }

  override fun close() {
    input.close()
    writeEOFAndFlush()
    bitOutputStream.close()
  }

  private fun writeEOFAndFlush() {
    encodeNext(EOF_SYMBOL)
    flush()
  }

  private fun flush() {
    bitsToFollow++
    if (low < CODE_VALUE_FIRST_QUARTER) writeBitWithFollow(0)
    else writeBitWithFollow(1)
  }
}