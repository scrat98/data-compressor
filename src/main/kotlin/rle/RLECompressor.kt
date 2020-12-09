package rle

import commons.Compressor
import java.io.InputStream
import java.io.OutputStream

object RLECompressor : Compressor {
  override fun encode(input: InputStream, output: OutputStream) {
    RLECoder.encode(input, output)
  }

  override fun decode(input: InputStream, output: OutputStream) {
    RLEDecoder.decode(input, output)
  }
}