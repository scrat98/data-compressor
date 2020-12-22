package jpeg

import commons.decode
import commons.encode
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import java.util.stream.Stream

abstract class AbstractJpegCompressorTest(
  private val jpegCompressor: JpegCompressor
) {

  @TestFactory
  fun `all jpeg files can be encoded and decoded`(): Stream<DynamicContainer> {
    return getAllJpegs().map { (quality, jpegs) ->
      val tests = jpegs.map { jpeg ->
        DynamicTest.dynamicTest(jpeg.name) { testJpeg(jpeg.readBytes()) }
      }
      DynamicContainer.dynamicContainer(quality, tests)
    }.stream()
  }

  private fun testJpeg(input: ByteArray) {
    val encoded = jpegCompressor.encode(input)
    val decoded = jpegCompressor.decode(encoded)
    assertArrayEquals(input, decoded)
    println("RawSize: ${input.size}. CompressedSize: ${encoded.size}. Compressed ratio: ${input.size.toDouble() / encoded.size}")
  }
}

internal fun getAllJpegs(): Map<String, List<File>> {
  val jpegFolderResource =
      Thread.currentThread().contextClassLoader.getResource("jpeg-images")
  val qualityFolder = File(jpegFolderResource.file)
  return qualityFolder.listFiles()
      .filter { it.isDirectory }
      .associate { it.name to it.listFiles().toList() }
}