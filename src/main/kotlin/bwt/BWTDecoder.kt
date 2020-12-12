package bwt

import commons.Decoder
import commons.DecoderWriter
import commons.NUMBER_OF_CHARS
import commons.toInt
import java.io.InputStream
import java.io.OutputStream

object BWTDecoder : Decoder {
  override fun decode(input: InputStream, output: OutputStream) {
    BWTDecoderWriter(input, output).writeDecoded()
  }
}

private class BWTDecoderWriter(
  private val input: InputStream,
  private val output: OutputStream
) : DecoderWriter {

  private val buffer = ByteArray(BLOCK_SIZE + 1 + 2 * Int.SIZE_BYTES)

  private val BWTReverseVector = IntArray(BLOCK_SIZE + 1) { 0 }

  private val count = IntArray(NUMBER_OF_CHARS + 1) { 0 }

  private var bufferLength = -1

  private var firstIndex = -1

  private var eofIndex = -1

  override fun writeDecoded() {
    while (true) {
      bufferLength = input.read(buffer)
      if (bufferLength == -1) break
      eofIndex = readLast4BytesAsInt(); bufferLength -= Int.SIZE_BYTES
      firstIndex = readLast4BytesAsInt(); bufferLength -= Int.SIZE_BYTES
      calculateCharsCount()
      calculateBWTReverseVector()
      decodeWithBWTReverseVector()
    }
    close()
  }

  private fun readLast4BytesAsInt(): Int =
      buffer.copyOfRange(bufferLength - Int.SIZE_BYTES, bufferLength).toInt()

  private fun calculateCharsCount() {
    (0..NUMBER_OF_CHARS).forEach { count[it] = 0 }
    (0 until bufferLength).forEach {
      val byteIndex = getIndexForByte(it)
      count[byteIndex]++
    }
    var sum = 0
    (0..NUMBER_OF_CHARS).forEach {
      sum += count[it]
      count[it] = sum - count[it]
    }
  }

  private fun calculateBWTReverseVector() {
    (0 until bufferLength).forEach {
      val byteIndex = getIndexForByte(it)
      BWTReverseVector[count[byteIndex]] = it
      count[byteIndex]++
    }
  }

  private fun decodeWithBWTReverseVector() {
    var pos = firstIndex
    repeat(bufferLength - 1) {
      output.write(buffer[pos].toInt())
      pos = BWTReverseVector[pos]
    }
  }

  private fun getIndexForByte(byteIndex: Int): Int {
    return if (byteIndex == eofIndex) {
      NUMBER_OF_CHARS
    } else {
      buffer[byteIndex].toInt() - Byte.MIN_VALUE
    }
  }

  override fun close() {
    input.close()
    output.close()
  }
}