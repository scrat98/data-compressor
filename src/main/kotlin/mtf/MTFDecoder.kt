package mtf

import commons.Decoder
import commons.DecoderWriter
import commons.NUMBER_OF_CHARS
import commons.forEachByte
import java.io.InputStream
import java.io.OutputStream

object MTFDecoder : Decoder {
  override fun decode(input: InputStream, output: OutputStream) {
    MTFDecoderWriter(input, output).writeDecoded()
  }
}

private class MTFDecoderWriter(
  private val input: InputStream,
  private val output: OutputStream
) : DecoderWriter {

  private val indexToByte = IntArray(NUMBER_OF_CHARS) { it }

  override fun writeDecoded() {
    input.forEachByte { index ->
      val byte = indexToByte[index]
      for (i in index downTo 1) {
        indexToByte[i] = indexToByte[i - 1]
      }
      indexToByte[0] = byte
      output.write(byte)
    }
    close()
  }

  override fun close() {
    input.close()
    output.close()
  }
}