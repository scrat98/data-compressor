import commons.Compressor
import commons.NUMBER_OF_CHARS
import commons.decode
import commons.encode
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import java.io.File
import java.util.stream.Stream
import kotlin.random.Random

abstract class AbstractCodingTest(
  private val compressor: Compressor,
) {

  @TestFactory
  fun `ensure that all data sets from 'Corpora group' can be encoded and decoded`(): Stream<DynamicContainer> {
    return getAllCorpusData().map { (corporaGroupName, dataSets) ->
      val dataSetsContainers = dataSets.map { (dataSetName, files) ->
        val tests = files.map {
          DynamicTest.dynamicTest(it.name) { testInputStream(it.inputStream().readBytes()) }
        }
        DynamicContainer.dynamicContainer(dataSetName, tests)
      }
      DynamicContainer.dynamicContainer(corporaGroupName, dataSetsContainers)
    }.stream()
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
    val encoded = compressor.encode(input)
    val decoded = compressor.decode(encoded)
    assertArrayEquals(input, decoded)
    println("RawSize: ${input.size}. CompressedSize: ${encoded.size}. Compressed ratio: ${input.size.toDouble() / encoded.size}")
  }
}

internal fun getAllCorpusData(): Map<String, Map<String, List<File>>> {
  val corpusDataFolderResource =
      Thread.currentThread().contextClassLoader.getResource("corpora-sets")
  val corpusDataFolder = File(corpusDataFolderResource.file)
  return corpusDataFolder.listFiles()
      .filter { it.isDirectory }
      .associate {
        val corporaGroupName = it.name
        val corporaDataSets = it.listFiles()
            .filter { it.isDirectory }
            .associate { it.name to it.listFiles().toList() }
        corporaGroupName to corporaDataSets
      }
}