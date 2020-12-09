package bwt

import commons.Coder
import commons.CoderWriter
import commons.writeIntAs4Bytes
import java.io.InputStream
import java.io.OutputStream
import java.lang.Integer.min

/*
  It uses EOF(character, which is lexicographically bigger than any other character in the input stream) for O(n) time complexity
  Data format:
  bytes_size | ...data... | first_index | eof_index
 */

object BWTCoder : Coder {
  override fun encode(input: InputStream, output: OutputStream) {
    BWTCoderWriter(input, output).writeEncoded()
  }
}

private class BWTCoderWriter(
  private val input: InputStream,
  private val output: OutputStream
) : CoderWriter {

  private val buffer = ByteArray(BLOCK_SIZE)

  private var bufferLength = -1

  private val suffixArray = Array<Int>(BLOCK_SIZE + 1) { 0 }

  private val suffixComparator = Comparator<Int> { i1, i2 ->
    val l1 = bufferLength - i1
    val l2 = bufferLength - i2
    val suffixCmpResult = compareSuffixes(i1, i2, min(l1, l2))
    if (suffixCmpResult == 0) {
      l2 - l1
    }
    suffixCmpResult
  }

  private fun compareSuffixes(suffixInd1: Int, suffixInd2: Int, length: Int): Int {
    var result = 0
    for (i in 0 until length) {
      val byte1 = buffer[suffixInd1 + i]
      val byte2 = buffer[suffixInd2 + i]
      if (byte1 != byte2) {
        result = byte1 - byte2
        break
      }
    }
    return result
  }

  override fun writeEncoded() {
    while (true) {
      bufferLength = input.read(buffer)
      if (bufferLength == -1) break
      output.writeIntAs4Bytes(bufferLength + 1)

      (0..bufferLength).forEach { suffixArray[it] = it }
      suffixArray.sortWith(suffixComparator, 0, bufferLength + 1)

      var firstIndex = -1
      var eofIndex = -1
      (0..bufferLength).forEach {
        if (suffixArray[it] == 1) {
          firstIndex = it
        }
        if (suffixArray[it] == 0) {
          eofIndex = it
          output.write(EOF_SYMBOL)
        } else {
          output.write(buffer[suffixArray[it] - 1].toInt())
        }
      }
      output.writeIntAs4Bytes(firstIndex)
      output.writeIntAs4Bytes(eofIndex)
    }
    close()
  }

  override fun close() {
    input.close()
    output.close()
  }
}