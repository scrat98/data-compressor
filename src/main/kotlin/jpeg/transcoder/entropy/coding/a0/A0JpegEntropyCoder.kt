package jpeg.transcoder.entropy.coding.a0

import a0.A0Compressor
import com.davidjohnburrowes.format.jpeg.JpegData
import com.davidjohnburrowes.format.jpeg.data.EntropyData
import commons.*
import jpeg.transcoder.entropy.coding.JpegEntropyCoder
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

object A0JpegEntropyCoder : JpegEntropyCoder {
  override fun encode(input: InputStream, output: OutputStream) {
    A0JpegEntropyCoderWriter(input, output).writeEncoded()
  }
}

private class A0JpegEntropyCoderWriter(
  private val input: InputStream,
  private val output: OutputStream
) : CoderWriter {

  private val jpegData = JpegData().apply { read(input) }

  private val encodedSymbols = ByteArrayOutputStream()

  private val coefData = ByteArrayOutputStream()

  private val entropyData = jpegData.filterIsInstance<EntropyData>().first()

  private val bitInputStream = BitInputStream(entropyData.data.inputStream())

  private val coefDataBitOutputStream = BitOutputStream(coefData, 1)

  override fun writeEncoded() {
    while (true) {
      val symbolBits = (1..8).map { bitInputStream.read() }
      if (symbolBits.contains(-1)) {
        break
      }

      val symbol = symbolBits.bitsToInt()
      val coefBitsSize = symbol and 15
      encodedSymbols.write(symbol)

      repeat(coefBitsSize) { coefDataBitOutputStream.write(bitInputStream.read()) }
    }
    close()
  }

  override fun close() {
    bitInputStream.close()
    coefDataBitOutputStream.close()
    val symbolsEncoded = A0Compressor.encode(encodedSymbols.toByteArray())
    entropyData.data = symbolsEncoded.size.toBytes() + symbolsEncoded + coefData.toByteArray()
    jpegData.write(output)
    input.close()
    output.close()
  }
}