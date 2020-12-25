package jpeg.transcoder.entropy.coding.a0

import a0.A0Compressor
import com.davidjohnburrowes.format.jpeg.JpegData
import com.davidjohnburrowes.format.jpeg.data.EntropyData
import commons.*
import jpeg.transcoder.entropy.coding.JpegEntropyCoder
import jpeg.transcoder.entropy.coding.huffman.HuffmanJpegMeta
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

  private val jpegMeta = HuffmanJpegMeta.fromJpeg(jpegData)

  private val entropyData = jpegData.filterIsInstance<EntropyData>().first()

  private val bitInputStream = BitInputStream(entropyData.data.inputStream())

  private val componentSymbolsData =
      mutableMapOf<Int, Pair<ByteArrayOutputStream, ByteArrayOutputStream>>()

  private val coefData = ByteArrayOutputStream()

  private val coefDataBitOutputStream = BitOutputStream(coefData, 1)

  override fun writeEncoded() {
    (1..jpegMeta.mcuHeight).forEach { hMcu ->
      (1..jpegMeta.mcuWidth).forEach { vMcu ->
        jpegMeta.components.forEach { component ->
          val componentId = component.frameInfo.componentId
          val componentSymbolData = componentSymbolsData
              .getOrPut(componentId) { ByteArrayOutputStream() to ByteArrayOutputStream() }
          (1..component.frameInfo.verticalScaling).forEach { vScale ->
            (1..component.frameInfo.horizontalScaling).forEach { hScale ->
              var coef = 0
              while (coef < 64) {
                val coefByteArray = if (coef == 0) {
                  componentSymbolData.first
                } else componentSymbolData.second
                val symbolBits = (1..8).map { bitInputStream.read() }
                val symbol = symbolBits.bitsToInt()
                val zerosSize = symbol shr 4
                val coefBitsSize = symbol and 15

                coefByteArray.write(symbol)

                if (coef != 0 && symbol == 0) {
                  coef = 64
                  break
                }

                coef += zerosSize + 1
                repeat(coefBitsSize) { coefDataBitOutputStream.write(bitInputStream.read()) }
              }
            }
          }
        }
      }
    }
    close()
  }

  override fun close() {
    bitInputStream.close()
    coefDataBitOutputStream.close()

    val coefsByComponentsAndTable = componentSymbolsData.map {
      val dcData = A0Compressor.encode(it.value.first.toByteArray())
      val acData = A0Compressor.encode(it.value.second.toByteArray())
      dcData.size.toBytes() + dcData + acData.size.toBytes() + acData
    }.reduce { acc, bytes -> acc + bytes }

    entropyData.data = coefsByComponentsAndTable + coefData.toByteArray()

    jpegData.write(output)
    input.close()
    output.close()
  }
}