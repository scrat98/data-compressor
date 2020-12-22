package jpeg.transcoder.entropy.coding.huffman

import com.davidjohnburrowes.format.jpeg.JpegData
import com.davidjohnburrowes.format.jpeg.data.EntropyData
import commons.*
import jpeg.transcoder.entropy.coding.JpegEntropyDecoder
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

object HuffmanJpegEntropyDecoder : JpegEntropyDecoder {
  override fun decode(input: InputStream, output: OutputStream) {
    HuffmanJpegEntropyDecoderWriter(input, output).writeDecoded()
  }
}

private class HuffmanJpegEntropyDecoderWriter(
  private val input: InputStream,
  private val output: OutputStream
) : DecoderWriter {

  private val jpegData = JpegData().apply { read(input) }

  private val jpegMeta = HuffmanJpegMeta.fromJpeg(jpegData)

  private val decoded = ByteArrayOutputStream()

  private val entropyData = jpegData.filterIsInstance<EntropyData>().first()

  private val bitInputStream = BitInputStream(entropyData.data.inputStream())

  private val bitOutputStream = BitOutputStream(decoded, 1)

  override fun writeDecoded() {
    (1..jpegMeta.mcuHeight).forEach { hMcu ->
      (1..jpegMeta.mcuWidth).forEach { vMcu ->
        jpegMeta.components.forEach { component ->
          (1..component.frameInfo.verticalScaling).forEach { vScale ->
            (1..component.frameInfo.horizontalScaling).forEach { hScale ->
              var coef = 0
              while (coef < 64) {
                val huffmanTable = if (coef == 0) component.dcTable else component.acTable
                var huffmanCode = ""
                var codeValue: Byte = 0
                while (true) {
                  val nextBit = bitInputStream.read()
                  huffmanCode += nextBit
                  val foundValue = huffmanTable.codeToValue.get(huffmanCode)
                  if (foundValue != null) {
                    codeValue = foundValue
                    break
                  }
                }

                val codeValueInt = codeValue.toInt() and 0xFF
                val codeValueBits = codeValueInt.asBitsString(8).asBits()
                codeValueBits.forEach { bitOutputStream.write(it) }

                if (coef != 0 && codeValueInt == 0) {
                  coef = 64
                  break
                }

                val zerosSize = codeValueInt shr 4
                val coefBitsSize = codeValueInt and 15
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
    entropyData.data = decoded.toByteArray()
    jpegData.write(output)
  }
}