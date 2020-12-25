package jpeg.transcoder.entropy.coding.a0

import a0.A0Compressor
import com.davidjohnburrowes.format.jpeg.JpegData
import com.davidjohnburrowes.format.jpeg.data.EntropyData
import commons.*
import jpeg.transcoder.entropy.coding.JpegEntropyDecoder
import jpeg.transcoder.entropy.coding.huffman.HuffmanJpegMeta
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

  private val jpegMeta = HuffmanJpegMeta.fromJpeg(jpegData)

  private val entropyData = jpegData.filterIsInstance<EntropyData>().first()

  private val decoded = ByteArrayOutputStream()

  private val bitOutputStream = BitOutputStream(decoded, 1)

  override fun writeDecoded() {
    var currentStreamPos = 0
    val componentSymbolsData = jpegMeta.components.associate {
      val componentId = it.frameInfo.componentId
      val dcSize = entropyData.data.copyOfRange(currentStreamPos, currentStreamPos + 4).toInt()
      currentStreamPos += 4
      val dcSymbolsDataStream = entropyData.data.inputStream(currentStreamPos, dcSize)
      val dcSymbolsData = A0Compressor.decode(dcSymbolsDataStream)
      currentStreamPos += dcSize

      val acSize = entropyData.data.copyOfRange(currentStreamPos, currentStreamPos + 4).toInt()
      currentStreamPos += 4
      val acSymbolsDataStream = entropyData.data.inputStream(currentStreamPos, acSize)
      val acSymbolsData = A0Compressor.decode(acSymbolsDataStream)
      currentStreamPos += acSize

      componentId to (dcSymbolsData.inputStream() to acSymbolsData.inputStream())
    }
    val coefData = entropyData.data
        .inputStream(currentStreamPos, entropyData.data.size - currentStreamPos)
    val coefDataBitInputStream = BitInputStream(coefData)

    (1..jpegMeta.mcuHeight).forEach { hMcu ->
      (1..jpegMeta.mcuWidth).forEach { vMcu ->
        jpegMeta.components.forEach { component ->
          val componentId = component.frameInfo.componentId
          val componentSymbolData = componentSymbolsData.getValue(componentId)
          (1..component.frameInfo.verticalScaling).forEach { vScale ->
            (1..component.frameInfo.horizontalScaling).forEach { hScale ->
              var coef = 0
              while (coef < 64) {
                val coefByteArray = if (coef == 0) {
                  componentSymbolData.first
                } else componentSymbolData.second
                val symbol = coefByteArray.read()
                val zerosSize = symbol shr 4
                val coefBitsSize = symbol and 15

                val symbolBits = symbol.asBitsString(8).asBits()
                symbolBits.forEach { bitOutputStream.write(it) }

                if (coef != 0 && symbol == 0) {
                  coef = 64
                  break
                }

                coef += zerosSize + 1
                repeat(coefBitsSize) { bitOutputStream.write(coefDataBitInputStream.read()) }
              }
            }
          }
        }
      }
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