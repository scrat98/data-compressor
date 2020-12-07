package commons

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

interface Coder {
  fun encode(input: InputStream, output: OutputStream)
}

fun Coder.encode(input: ByteArray): ByteArray {
  val inputStream = input.inputStream()
  val outputStream = ByteArrayOutputStream()
  this.encode(inputStream, outputStream)
  return outputStream.toByteArray()
}