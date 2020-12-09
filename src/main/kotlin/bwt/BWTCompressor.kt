package bwt

import commons.Compressor
import java.io.InputStream
import java.io.OutputStream

object BWTCompressor : Compressor {
  override fun encode(input: InputStream, output: OutputStream) {
    BWTCoder.encode(input, output)
  }

  override fun decode(input: InputStream, output: OutputStream) {
    BWTDecoder.decode(input, output)
  }
}