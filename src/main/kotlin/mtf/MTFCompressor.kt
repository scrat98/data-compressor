package mtf

import commons.Compressor
import java.io.InputStream
import java.io.OutputStream

object MTFCompressor : Compressor {
  override fun encode(input: InputStream, output: OutputStream) {
    MTFCoder.encode(input, output)
  }

  override fun decode(input: InputStream, output: OutputStream) {
    MTFDecoder.decode(input, output)
  }
}