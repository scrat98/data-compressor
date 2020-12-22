package jpeg.simple

import DataCompressor
import com.davidjohnburrowes.format.jpeg.JpegData
import com.davidjohnburrowes.format.jpeg.data.EntropyData
import commons.decode
import commons.encode
import jpeg.JpegCompressor
import java.io.InputStream
import java.io.OutputStream

object SimpleJpegCompressor : JpegCompressor {
  override fun encode(input: InputStream, output: OutputStream) {
    val jpegData = JpegData()
    jpegData.read(input)
    val compressedData = jpegData.filterIsInstance<EntropyData>()
    compressedData.forEach {
      val encoded = DataCompressor.encode(it.data)
      it.data = encoded
    }
    jpegData.write(output)
  }

  override fun decode(input: InputStream, output: OutputStream) {
    val jpegData = JpegData()
    jpegData.read(input)
    val compressedData = jpegData.filterIsInstance<EntropyData>()
    compressedData.forEach {
      val decoded = DataCompressor.decode(it.data)
      it.data = decoded
    }
    jpegData.write(output)
  }
}