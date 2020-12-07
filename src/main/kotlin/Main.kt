import a0.A0Coder
import java.io.FileInputStream
import java.io.FileOutputStream

fun main() {
  val input = FileInputStream("/home/ct/projects/data-compressor/lecture4.pdf").buffered()
  val output = FileOutputStream("/home/ct/projects/data-compressor/encoded.pdf").buffered()
  A0Coder.encode(input, output)
}