package jpeg

import DataCompressor
import a0.A0Compressor
import bwt.BWTCompressor
import commons.Compressor
import mtf.MTFCompressor
import rle.RLECompressor
import java.io.InputStream
import java.io.OutputStream

interface JpegCompressor : Compressor

object NoneJpegCompressor : JpegCompressor {
  override fun encode(input: InputStream, output: OutputStream) {
    output.write(input.readBytes())
    input.close()
    output.close()
  }

  override fun decode(input: InputStream, output: OutputStream) {
    output.write(input.readBytes())
    input.close()
    output.close()
  }
}

object BaseJpegCompressor : JpegCompressor {

  private val bestCompressorChain =
      listOf(BWTCompressor, MTFCompressor, RLECompressor, A0Compressor)

  override fun encode(input: InputStream, output: OutputStream) {
    DataCompressor.encode(bestCompressorChain, input, output)
  }

  override fun decode(input: InputStream, output: OutputStream) {
    DataCompressor.decode(bestCompressorChain, input, output)
  }
}