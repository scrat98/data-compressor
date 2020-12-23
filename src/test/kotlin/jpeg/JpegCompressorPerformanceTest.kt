package jpeg

import MDDocument
import br
import commons.decode
import commons.encode
import document
import h1
import h2
import item
import jpeg.simple.SimpleJpegCompressor
import jpeg.transcoder.JpegTranscoderCompressor
import org.junit.jupiter.api.Assertions.assertArrayEquals
import table
import th
import tr

fun main() {
  JpegCompressorPerformanceTest().start()
}

data class CompressionResult(
  val fileName: String,
  val rawSize: Int,
  val compressedSize: Int
) {
  val ratio = 100.0 - compressedSize.toDouble() / rawSize.toDouble() * 100.0
}

class JpegCompressorPerformanceTest {

  private val compressorsToTest = listOf(
      BaseJpegCompressor,
      SimpleJpegCompressor,
      JpegTranscoderCompressor,
      GenericJpegCompressor
  )

  private val jpegsToTest = getAllJpegs()
      .getValue("30")
      .map { it.name to it.readBytes() }

  fun start() {
    val results = compressorsToTest.associate { compressor ->
      val compressorName = compressor::class.simpleName.toString()
      println("Start testing compression with: $compressorName")
      val resultForCompressor = calculateResultForCompressor(compressor)
      compressorName to resultForCompressor
    }
    println(buildMarkdownResult(results).exportAsText())
  }

  private fun calculateResultForCompressor(compressor: JpegCompressor): List<CompressionResult> {
    val jpegCompressionResults = jpegsToTest.map { (fileName, jpegData) ->
      println("Testing file $fileName")
      val encoded = compressor.encode(jpegData)
      val decoded = compressor.decode(encoded)
      assertArrayEquals(jpegData, decoded)
      CompressionResult(
          fileName = fileName,
          rawSize = jpegData.size,
          compressedSize = encoded.size
      )
    }
    val totalRawBytes = jpegCompressionResults.sumBy { it.rawSize }
    val totalCompressedSize = jpegCompressionResults.sumBy { it.compressedSize }
    val totalResults = CompressionResult(
        fileName = "total",
        rawSize = totalRawBytes,
        compressedSize = totalCompressedSize
    )
    return jpegCompressionResults + totalResults
  }

  private fun buildMarkdownResult(results: Map<String, List<CompressionResult>>): MDDocument {
    val resultFields = arrayOf(
        "File name" to CompressionResult::fileName,
        "Raw size(bytes)" to CompressionResult::rawSize,
        "Compressed size(bytes)" to CompressionResult::compressedSize,
        "Ratio (%)" to CompressionResult::ratio,
    )

    return document {
      h1 { "Algorithms comparison" }; br()
      results.forEach { (algorithm, results) ->
        h2 { algorithm }
        table {
          th { resultFields.map { item { it.first } } }
          results.forEach { result ->
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
    }
  }
}