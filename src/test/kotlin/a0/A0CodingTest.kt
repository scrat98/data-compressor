package a0

import commons.decode
import commons.encode
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.io.FileInputStream
import kotlin.random.Random

class A0CodingTest {

  companion object {
    @JvmStatic
    fun calgaryFiles(): List<Arguments> {
      val calgaryFolderResource = Thread.currentThread().contextClassLoader.getResource("calgary")
      val calgaryFolder = File(calgaryFolderResource.file)
      return calgaryFolder.listFiles().map {
        Arguments.of(it.inputStream(), it.name)
      }
    }
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("calgaryFiles")
  fun `ensure that all files from calgary corpus can encoded and decoded`(
    input: FileInputStream,
    fileName: String
  ) {
    testInputStream(input.readBytes())
  }

  @Test
  fun `test with empty input`() {
    testInputStream(ByteArray(0))
  }

  @Test
  fun `test with one symbol`() {
    testInputStream(ByteArray(1))
  }

  @RepeatedTest(5)
  fun `test random input with all possible chars`() {
    val input = ByteArray(NUMBER_OF_CHARS + 100_000)
    (0..NUMBER_OF_CHARS).forEach {
      input[it] = it.toByte()
    }
    Random.nextBytes(input, NUMBER_OF_CHARS, input.size)
    testInputStream(input)
  }

  private fun testInputStream(input: ByteArray) {
    val encoded = A0Coder.encode(input)
    val decoded = A0Decoder.decode(encoded)
    assertArrayEquals(input, decoded)
  }
}