package bwt

import commons.Decoder
import commons.DecoderWriter
import commons.NUMBER_OF_CHARS
import commons.read4BytesAsInt
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

  private val buffer = ByteArray(BLOCK_SIZE + 1)

  private val BWTReverseVector = Array<Int>(BLOCK_SIZE + 1) { 0 }

  private val count = Array<Int>(NUMBER_OF_CHARS + 1) { 0 }

  private val runningTotal = Array<Int>(NUMBER_OF_CHARS + 1) { 0 }

  private var bufferLength = -1

  private var firstIndex = -1

  private var eofIndex = -1

  override fun writeDecoded() {
    while (true) {
      bufferLength = input.read4BytesAsInt()
      if (bufferLength == -1) break
      input.read(buffer, 0, bufferLength)
      firstIndex = input.read4BytesAsInt()
      eofIndex = input.read4BytesAsInt()
      calculateCharsCount()
      calculateBWTReverseVector()
      decodeWithBWTReverseVector()
    }
    close()
  }

  private fun calculateCharsCount() {
    (0..NUMBER_OF_CHARS).forEach { count[it] = 0 }
    (0 until bufferLength).forEach {
      val byteIndex = getIndexForByte(it)
      count[byteIndex]++
    }
    var sum = 0
    (0..NUMBER_OF_CHARS).forEach {
      runningTotal[it] = sum
      sum += count[it]
      count[it] = 0
    }
  }

  private fun calculateBWTReverseVector() {
    (0 until bufferLength).forEach {
      val byteIndex = getIndexForByte(it)
      BWTReverseVector[count[byteIndex] + runningTotal[byteIndex]] = it
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