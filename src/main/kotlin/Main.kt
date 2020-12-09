import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.system.exitProcess

fun main(args: Array<String>) {
  if (args.size != 3) {
    invalidArgsError()
  }
  val type = args[0]
  val input = FileInputStream(args[1]).buffered()
  val output = FileOutputStream(args[2]).buffered()
  when (type) {
    "encode" -> DataCompressor.encode(input, output)
    "decode" -> DataCompressor.decode(input, output)
    else -> invalidArgsError()
  }
}

private fun invalidArgsError() {
  println("Usage: java -jar data-compressor.jar <encode | decode> <input file> <output file>")
  exitProcess(1)
}