package jpeg.entropy.coding

import commons.decode
import commons.encode
import jpeg.getAllJpegs
import jpeg.transcoder.entropy.coding.JpegEntropyCompressor
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream

abstract class AbstractJpegEntropyCodingTest(
  private val entropyCompressor: JpegEntropyCompressor,
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
    val decoded = entropyCompressor.decode(input)
    val encoded = entropyCompressor.encode(decoded)
    assertArrayEquals(input, encoded)
    println("Original: ${input.size}. Decoded: ${decoded.size}.")
  }
}