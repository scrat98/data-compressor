package jpeg.simple

import DataCompressor
import a0.A0Compressor
import bwt.BWTCompressor
import com.davidjohnburrowes.format.jpeg.JpegData
import com.davidjohnburrowes.format.jpeg.data.EntropyData
import jpeg.JpegCompressor
import mtf.MTFCompressor
import rle.RLECompressor
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

object SimpleJpegCompressor : JpegCompressor {

  private val bestCompressorChain =
      listOf(BWTCompressor, MTFCompressor, RLECompressor, A0Compressor)

  override fun encode(input: InputStream, output: OutputStream) {
    val jpegData = JpegData()
    jpegData.read(input)
    val compressedData = jpegData.filterIsInstance<EntropyData>()
    compressedData.forEach {
      val encoded = ByteArrayOutputStream()
      DataCompressor.encode(bestCompressorChain, it.data.inputStream(), encoded)
      it.data = encoded.toByteArray()
    }
    jpegData.write(output)
    input.close()
    output.close()
  }

  override fun decode(input: InputStream, output: OutputStream) {
    val jpegData = JpegData()
    jpegData.read(input)
    val compressedData = jpegData.filterIsInstance<EntropyData>()
    compressedData.forEach {
      val decoded = ByteArrayOutputStream()
      DataCompressor.decode(bestCompressorChain, it.data.inputStream(), decoded)
      it.data = decoded.toByteArray()
    }
    jpegData.write(output)
    input.close()
    output.close()
  }
}