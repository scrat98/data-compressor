package a0

import commons.Compressor
import java.io.InputStream
import java.io.OutputStream

object A0Compressor : Compressor {
  override fun encode(input: InputStream, output: OutputStream) {
    A0Coder.encode(input, output)
  }

  override fun decode(input: InputStream, output: OutputStream) {
    A0Decoder.decode(input, output)
  }
}