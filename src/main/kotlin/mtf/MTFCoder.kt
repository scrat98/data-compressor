package mtf

import commons.Coder
import commons.CoderWriter
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
  override fun writeEncoded() {
    TODO("Not yet implemented")
  }

  override fun close() {
    TODO("Not yet implemented")
  }
}