import a0.A0Compressor
import bwt.BWTCompressor
import commons.Compressor
import mtf.MTFCompressor
import org.junit.jupiter.api.Assertions.assertArrayEquals
import rle.RLECompressor
import java.io.ByteArrayOutputStream

fun main() {
  DataCompressorPerformanceTest().start()
}

data class CompressionResult(
  val fileName: String,
  val rawSize: Int,
  val compressedSize: Int,
  val bitsPerByte: Double,
  val elapsedTimeMillis: Int
)

class DataCompressorPerformanceTest {

  private val compressorChainsToTest = listOf(
      listOf(A0Compressor),
      listOf(BWTCompressor, MTFCompressor, A0Compressor),
      listOf(RLECompressor, BWTCompressor, MTFCompressor, A0Compressor),
      listOf(BWTCompressor, MTFCompressor, RLECompressor, A0Compressor),
      listOf(RLECompressor, BWTCompressor, MTFCompressor, RLECompressor, A0Compressor)
  )

  private val calgaryFilesData = getAllCorpusData()
      .getValue("calgary")
      .getValue("standard")
      .map { it.name to it.readBytes() }

  fun start() {
    val results = compressorChainsToTest.associate { chain ->
      val chainName = chain.joinToString(" <-> ")
      { it::class.simpleName.toString().removeSuffix("Compressor") }
      println("Start testing compression with chain: $chainName")
      val results = calgaryFilesData.map { (fileName, data) ->
        println("Testing file $fileName")
        val start = System.currentTimeMillis()
        val encoded = DataCompressor.encode(chain, data)
        val decoded = DataCompressor.decode(chain, encoded)
        val end = System.currentTimeMillis()
        val elapsedTimeMillis = end - start
        assertArrayEquals(data, decoded)
        CompressionResult(
            fileName = fileName,
            rawSize = data.size,
            compressedSize = encoded.size,
            bitsPerByte = encoded.size.toDouble() / data.size * Byte.SIZE_BITS,
            elapsedTimeMillis = elapsedTimeMillis.toInt()
        )
      }
      printSeparationLine()
      chainName to results.sortedBy { it.fileName }
    }
    println(buildMarkdownResult(results).exportAsText())
  }

  private fun buildMarkdownResult(results: Map<String, List<CompressionResult>>): MDDocument {
    val totalResults = results.map { (chainName, results) ->
      chainName to CompressionResult(
          fileName = "total",
          rawSize = results.sumBy { it.rawSize },
          compressedSize = results.sumBy { it.compressedSize },
          bitsPerByte = results.sumByDouble { it.bitsPerByte },
          elapsedTimeMillis = results.sumBy { it.elapsedTimeMillis }
      )
    }.toMap()
    return document {
      h1 { "Performance test results" }; br()
      t { "For tests we are going to use [Calgary group dataset](http://www.data-compression.info/Corpora/CalgaryCorpus/)" }
      br(); br()

      h2 { "Entropy for files" }
      table {
        th { }
      }

      val resultFields = arrayOf(
          "File name" to CompressionResult::fileName,
          "Raw size(bytes)" to CompressionResult::rawSize,
          "Compressed size(bytes)" to CompressionResult::compressedSize,
          "Bits per byte" to CompressionResult::bitsPerByte,
          "Elapsed time(ms)" to CompressionResult::elapsedTimeMillis
      )
      results.forEach { (chainName, results) ->
        h2 { chainName }
        table {
          th { resultFields.map { item { it.first } } }
          val rowResults = results.plus(totalResults.getValue(chainName))
          rowResults.forEach { result ->
            tr {
              resultFields.map {
                val res = it.second.get(result)
                if (res is Double) {
                  return@map "%.3f".format(res)
                }
                res.toString()
              }.map { item { it } }
            }
          }
        }
        br()
      }

      h2 { "Overall result" }
      table {
        th {
          item { "Type" }; item { "Total compressed(bytes)" }; item { "Total bits per byte" }
          item { "Total elapsed time(ms)" }; item { "(raw - compressed)/elapsed time ratio" }
        }
        totalResults.forEach { (chainName, totalResult) ->
          tr {
            item { chainName }; item { "${totalResult.compressedSize}" }
            item { "%.3f".format(totalResult.bitsPerByte) }; item { "${totalResult.elapsedTimeMillis}" }
            item { "${(totalResult.rawSize - totalResult.compressedSize) / totalResult.elapsedTimeMillis}" }
          }
        }
      }
    }
  }

  private fun DataCompressor.encode(chain: List<Compressor>, input: ByteArray): ByteArray {
    val inputStream = input.inputStream()
    val outputStream = ByteArrayOutputStream()
    this.encode(chain, inputStream, outputStream)
    return outputStream.toByteArray()
  }

  private fun DataCompressor.decode(chain: List<Compressor>, input: ByteArray): ByteArray {
    val inputStream = input.inputStream()
    val outputStream = ByteArrayOutputStream()
    this.decode(chain, inputStream, outputStream)
    return outputStream.toByteArray()
  }

  private fun printSeparationLine() {
    println((0..256).joinToString("") { "-" })
  }
}