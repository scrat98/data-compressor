import a0.A0Compressor
import bwt.BWTCompressor
import commons.Compressor
import mtf.MTFCompressor
import rle.RLECompressor
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object DataCompressor : Compressor {

  private val DEFAULT_COMPRESSOR_CHAIN =
      listOf(RLECompressor, BWTCompressor, MTFCompressor, RLECompressor, A0Compressor)

  override fun encode(input: InputStream, output: OutputStream) {
    encode(DEFAULT_COMPRESSOR_CHAIN, input, output)
  }

  override fun decode(input: InputStream, output: OutputStream) {
    decode(DEFAULT_COMPRESSOR_CHAIN, input, output)
  }

  internal fun encode(chain: List<Compressor>, input: InputStream, output: OutputStream) {
    processChain(chain, input, output, Compressor::encode)
  }

  internal fun decode(chain: List<Compressor>, input: InputStream, output: OutputStream) {
    processChain(chain.reversed(), input, output, Compressor::decode)
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
      it.also { println("Processing ${it::class.simpleName}...") }
          .process(currentInputStream, tempOutputStream)

      if (currentOutputTempFile == tempFile1) {
        currentInputStream = tempFile1.inputStream().buffered()
        currentOutputTempFile = tempFile2
      } else {
        currentInputStream = tempFile2.inputStream().buffered()
        currentOutputTempFile = tempFile1
      }
    }
    chain.last()
        .also { println("Processing ${it::class.simpleName}...") }
        .process(currentInputStream, output)

    tempFile1.delete()
    tempFile2.delete()
  }

  private fun createTempFile(): File {
    return File.createTempFile("~data-compressor-${System.currentTimeMillis()}", ".tmp")
        .also { it.deleteOnExit() }
  }
}