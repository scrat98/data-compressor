import a0.A0Compressor
import bwt.BWTCompressor
import commons.Compressor
import mtf.MTFCompressor
import rle.RLECompressor
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object DataCompressor : Compressor {

  private val compressorChain =
      listOf(RLECompressor, BWTCompressor, MTFCompressor, RLECompressor, A0Compressor)

  override fun encode(input: InputStream, output: OutputStream) {
    processChain(compressorChain, input, output, Compressor::encode)
  }

  override fun decode(input: InputStream, output: OutputStream) {
    processChain(compressorChain.reversed(), input, output, Compressor::decode)
  }

  private fun processChain(
    chain: List<Compressor>,
    input: InputStream,
    output: OutputStream,
    process: Compressor.(input: InputStream, output: OutputStream) -> Unit
  ) {
    val tempFile1 = createTempFile()
    val tempFile2 = createTempFile()

    var currentInputStream = input
    var currentOutputTempFile = tempFile1

    chain.dropLast(1).forEach {
      val tempOutputStream = currentOutputTempFile.outputStream().buffered()
      it.process(currentInputStream, tempOutputStream)

      if (currentOutputTempFile == tempFile1) {
        currentInputStream = tempFile1.inputStream().buffered()
        currentOutputTempFile = tempFile2
      } else {
        currentInputStream = tempFile2.inputStream().buffered()
        currentOutputTempFile = tempFile1
      }
    }
    chain.last().process(currentInputStream, output)

    tempFile1.delete()
    tempFile2.delete()
  }

  private fun createTempFile(): File {
    return File.createTempFile("~data-compressor-${System.currentTimeMillis()}", ".tmp")
  }
}