package a0

import commons.BitInputStream
import commons.Decoder
import commons.DecoderWriter
import java.io.InputStream
import java.io.OutputStream

object A0Decoder : Decoder {
  override fun decode(input: InputStream, output: OutputStream) {
    A0DecoderWriter(input, output).writeDecoded()
  }
}

private class A0DecoderWriter(
  private val input: InputStream,
  private val output: OutputStream
) : DecoderWriter {

  private val frequencyModel = A0FrequencyModel()

  private val bitInputStream = BitInputStream(input)

  private var low = 0

  private var high = CODE_VALUE_MAX

  private var codeValue = 0

  init {
    repeat(CODE_VALUE_BITS) { moveToTheNextBit() }
  }

  private fun moveToTheNextBit() {
    val nextBit = bitInputStream.read()
    if (nextBit == -1) {
      codeValue = 2 * codeValue
    } else {
      codeValue = 2 * codeValue + nextBit
    }
  }

  override fun writeDecoded() {
    while (true) {
      val symbolIndex = nextSymbolIndex()
      if (symbolIndex == EOF_SYMBOL) break
      val char = frequencyModel.indexToChar(symbolIndex)
      output.write(char)
      frequencyModel.update(symbolIndex)
    }
    close()
  }

  private fun nextSymbolIndex(): Int {
    val range = high - low + 1
    val total = frequencyModel.getTotal()
    val cum = ((codeValue - low + 1) * total - 1) / range

    val symbolIndex = findSymbolIndex(cum)
    val symbolLow = frequencyModel.getLow(symbolIndex)
    val symbolHigh = frequencyModel.getHigh(symbolIndex)
    high = low + range * symbolHigh / total - 1
    low = low + range * symbolLow / total

    while (true) {
      if (high < CODE_VALUE_HALF) {
        // do nothing
      } else if (low >= CODE_VALUE_HALF) {
        codeValue -= CODE_VALUE_HALF
        low -= CODE_VALUE_HALF
        high -= CODE_VALUE_HALF
      } else if (low >= CODE_VALUE_FIRST_QUARTER && high < CODE_VALUE_THIRD_QUARTER) {
        codeValue -= CODE_VALUE_FIRST_QUARTER
        low -= CODE_VALUE_FIRST_QUARTER
        high -= CODE_VALUE_FIRST_QUARTER
      } else break
      low = 2 * low
      high = 2 * high + 1
      moveToTheNextBit()
    }

    return symbolIndex
  }

  private fun findSymbolIndex(cum: Int): Int {
    var symbolIndex = 1
    while (frequencyModel.getLow(symbolIndex) > cum) {
      symbolIndex++
    }
    return symbolIndex
  }

  override fun close() {
    bitInputStream.close()
    output.close()
  }
}