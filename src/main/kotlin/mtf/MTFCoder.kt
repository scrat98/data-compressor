package mtf

import commons.Coder
import commons.CoderWriter
import commons.NUMBER_OF_CHARS
import commons.forEachByte
import java.io.InputStream
import java.io.OutputStream

object MTFCoder : Coder {
  override fun encode(input: InputStream, output: OutputStream) {
    MTFCoderWriter(input, output).writeEncoded()
  }
}

private class MTFCoderWriter(
  private val input: InputStream,
  private val output: OutputStream
) : CoderWriter {

  private val indexToByte = IntArray(NUMBER_OF_CHARS) { it }

  override fun writeEncoded() {
    input.forEachByte { byte ->
      val index = indexToByte.indexOfFirst { it == byte }
      for (i in index downTo 1) {
        indexToByte[i] = indexToByte[i - 1]
      }
      indexToByte[0] = byte
      output.write(index)
    }
    close()
  }

  override fun close() {
    input.close()
    output.close()
  }
}