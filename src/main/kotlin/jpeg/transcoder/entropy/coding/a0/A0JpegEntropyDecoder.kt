package jpeg.transcoder.entropy.coding.a0

import a0.A0Compressor
import com.davidjohnburrowes.format.jpeg.JpegData
import com.davidjohnburrowes.format.jpeg.data.EntropyData
import commons.*
import jpeg.transcoder.entropy.coding.JpegEntropyDecoder
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

object A0JpegEntropyDecoder : JpegEntropyDecoder {
  override fun decode(input: InputStream, output: OutputStream) {
    A0JpegEntropyDecoderWriter(input, output).writeDecoded()
  }
}

private class A0JpegEntropyDecoderWriter(
  private val input: InputStream,
  private val output: OutputStream
) : DecoderWriter {

  private val jpegData = JpegData().apply { read(input) }

  private val entropyData = jpegData.filterIsInstance<EntropyData>().first()

  private val decoded = ByteArrayOutputStream()

  private val bitOutputStream = BitOutputStream(decoded, 1)

  override fun writeDecoded() {
    val symbolsSize = entropyData.data.copyOfRange(0, 4).toInt()
    val encodedSymbols = entropyData.data.inputStream(4, symbolsSize)
    val coefData = entropyData.data.inputStream(4 + symbolsSize, entropyData.data.size - 4 - symbolsSize)
    val coefDataBitInputStream = BitInputStream(coefData)

    val decodedSymbols = A0Compressor.decode(encodedSymbols)
    decodedSymbols.forEach { symbol ->
      val symbolInt = symbol.toInt() and 0xFF
      val coefBitsSize = symbolInt and 15
      val symbolBits = symbolInt.asBitsString(8).asBits()

      symbolBits.forEach { bitOutputStream.write(it) }
      (1..coefBitsSize).forEach { bitOutputStream.write(coefDataBitInputStream.read()) }
    }
    close()
  }

  override fun close() {
    bitOutputStream.close()
    entropyData.data = decoded.toByteArray()
    jpegData.write(output)
    input.close()
    output.close()
  }
}