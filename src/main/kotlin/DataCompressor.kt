import commons.Compressor
import java.io.InputStream
import java.io.OutputStream

object DataCompressor : Compressor {

  private val compressorChain = listOf<Compressor>()

  override fun encode(input: InputStream, output: OutputStream) {
    TODO("Not yet implemented")
  }

  override fun decode(input: InputStream, output: OutputStream) {
    TODO("Not yet implemented")
  }
}