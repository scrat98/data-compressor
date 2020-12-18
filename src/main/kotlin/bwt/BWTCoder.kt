package bwt

import commons.Coder
import commons.CoderWriter
import commons.NUMBER_OF_CHARS
import commons.writeIntAs4Bytes
import java.io.InputStream
import java.io.OutputStream

/*
  It uses EOF(character, which is lexicographically bigger than any other character in the input stream) for O(n) time complexity
  Data format:
  ...data... | first_index | eof_index
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

  // suffix array related
  // hold this variables here for better performance
  private var currentSuffixLength = 0

  private val suffixArray = IntArray(BLOCK_SIZE + 1)

  private var rank = IntArray(BLOCK_SIZE + 1)

  private var tempArray = IntArray(BLOCK_SIZE + 1)

  private var lastRank = 0

  private var count = IntArray(BLOCK_SIZE + 1)

  override fun writeEncoded() {
    while (true) {
      bufferLength = input.read(buffer)
      if (bufferLength == -1) break

      rebuildSuffixArray()

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

  private fun rebuildSuffixArray() {
    initSuffixArray()
    val n = bufferLength + 1
    while (currentSuffixLength < n) {
      sortSuffixArray()
      updateSuffixRanks()
      if (lastRank == bufferLength) break
      currentSuffixLength *= 2
    }
  }

  private fun initSuffixArray() {
    currentSuffixLength = 1
    lastRank = NUMBER_OF_CHARS
    (0..bufferLength).forEach {
      suffixArray[it] = it
      if (it == bufferLength) {
        rank[it] = NUMBER_OF_CHARS
      } else {
        rank[it] = buffer[it].toInt() - Byte.MIN_VALUE
      }
    }
  }

  private fun sortSuffixArray() {
    (0..lastRank).forEach { count[it] = 0 }
    (0..bufferLength).forEach {
      tempArray[it] = suffixArray[it] - currentSuffixLength / 2
      if (tempArray[it] < 0) {
        tempArray[it] += bufferLength + 1
      }
    }
    (0..bufferLength).forEach { count[rank[tempArray[it]]]++ }
    (1..lastRank).forEach { count[it] += count[it - 1] }
    (bufferLength downTo 0).forEach {
      suffixArray[--count[rank[tempArray[it]]]] = tempArray[it]
    }
  }

  private fun updateSuffixRanks() {
    var currentRank = 0
    tempArray[suffixArray[0]] = currentRank
    (1..bufferLength).forEach {
      val (iPrefixRank, iPostfixRank) = getPrefixAndPostfixRanksForSuffix(suffixArray[it])
      val (jPrefixRank, jPostfixRank) = getPrefixAndPostfixRanksForSuffix(suffixArray[it - 1])
      if (iPrefixRank != jPrefixRank || iPostfixRank != jPostfixRank) {
        currentRank++
      }
      tempArray[suffixArray[it]] = currentRank
    }
    rank = tempArray.also { tempArray = rank }
    lastRank = currentRank
  }

  private fun getPrefixAndPostfixRanksForSuffix(i: Int): Pair<Int, Int> {
    val postfixIndex = (i + currentSuffixLength / 2) % (bufferLength + 1)
    return (i to postfixIndex).let { rank[it.first] to rank[it.second] }
  }

  override fun close() {
    input.close()
    output.close()
  }
}