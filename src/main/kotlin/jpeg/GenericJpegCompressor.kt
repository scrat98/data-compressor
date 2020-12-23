package jpeg

import commons.encode
import jpeg.simple.SimpleJpegCompressor
import jpeg.transcoder.JpegTranscoderCompressor
import java.io.InputStream
import java.io.OutputStream

object GenericJpegCompressor : JpegCompressor {

  private enum class CompressStrategy(val compressor: JpegCompressor) {
    NONE(NoneJpegCompressor),
    BASE(BaseJpegCompressor),
    SIMPLE(SimpleJpegCompressor),
    TRANSCODER(JpegTranscoderCompressor)
  }

  override fun encode(input: InputStream, output: OutputStream) {
    val inputBytes = input.readBytes()
    val strategyResults = CompressStrategy.values()
        .associateWith { it.compressor.encode(inputBytes) }
    val (bestStrategy, encodedData) = strategyResults.minByOrNull { it.value.size }!!
    output.write(bestStrategy.ordinal)
    output.write(encodedData)
    input.close()
    output.close()
  }

  override fun decode(input: InputStream, output: OutputStream) {
    val strategyTypeOrdinal = input.read()
    val strategyType = CompressStrategy.values()[strategyTypeOrdinal]
    strategyType.compressor.decode(input, output)
    input.close()
    output.close()
  }
}