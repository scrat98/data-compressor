package jpeg

import DataCompressor
import commons.Compressor
import java.io.InputStream
import java.io.OutputStream

interface JpegCompressor : Compressor

object NoneJpegCompressor : JpegCompressor {
  override fun encode(input: InputStream, output: OutputStream) {
    output.write(input.readBytes())
  }

  override fun decode(input: InputStream, output: OutputStream) {
    output.write(input.readBytes())
  }
}

object BaseJpegCompressor : JpegCompressor {
  override fun encode(input: InputStream, output: OutputStream) {
    DataCompressor.encode(input, output)
  }

  override fun decode(input: InputStream, output: OutputStream) {
    DataCompressor.decode(input, output)
  }
}