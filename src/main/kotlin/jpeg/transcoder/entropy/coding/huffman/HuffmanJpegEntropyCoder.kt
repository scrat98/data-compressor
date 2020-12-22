package jpeg.transcoder.entropy.coding.huffman

import com.davidjohnburrowes.format.jpeg.JpegData
import com.davidjohnburrowes.format.jpeg.data.EntropyData
import commons.*
import jpeg.transcoder.entropy.coding.JpegEntropyCoder
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

object HuffmanJpegEntropyCoder : JpegEntropyCoder {
  override fun encode(input: InputStream, output: OutputStream) {
    HuffmanJpegEntropyCoderWriter(input, output).writeEncoded()
  }
}

private class HuffmanJpegEntropyCoderWriter(
  private val input: InputStream,
  private val output: OutputStream
) : CoderWriter {

  private val jpegData = JpegData().apply { read(input) }

  private val jpegMeta = HuffmanJpegMeta.fromJpeg(jpegData)

  private val encoded = ByteArrayOutputStream()

  private val entropyData = jpegData.filterIsInstance<EntropyData>().first()

  private val bitInputStream = BitInputStream(entropyData.data.inputStream())

  private val bitOutputStream = BitOutputStream(encoded, 1)

  override fun writeEncoded() {
    (1..jpegMeta.mcuHeight).forEach { hMcu ->
      (1..jpegMeta.mcuWidth).forEach { vMcu ->
        jpegMeta.components.forEach { component ->
          (1..component.frameInfo.verticalScaling).forEach { vScale ->
            (1..component.frameInfo.horizontalScaling).forEach { hScale ->
              var coef = 0
              while (coef < 64) {
                val huffmanTable = if (coef == 0) component.dcTable else component.acTable

                val symbolBits = (1..8).map { bitInputStream.read() }
                val symbol = symbolBits.bitsToInt()
                val zerosSize = symbol shr 4
                val coefBitsSize = symbol and 15

                val code = huffmanTable.valueToCode.getValue(symbol.toByte())
                val codeBits = code.asBits()
                codeBits.forEach { bitOutputStream.write(it) }

                if (coef != 0 && symbol == 0) {
                  coef = 64
                  break
                }

                coef += zerosSize + 1
                (1..coefBitsSize).forEach { bitOutputStream.write(bitInputStream.read()) }
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
    bitOutputStream.close()
    entropyData.data = encoded.toByteArray()
    jpegData.write(output)
  }
}