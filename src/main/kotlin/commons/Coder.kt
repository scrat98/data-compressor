package commons

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream

internal interface CoderWriter : Closeable {
  fun writeEncoded()
}

interface Coder {
  fun encode(input: InputStream, output: OutputStream)
}

fun Coder.encode(input: ByteArray): ByteArray {
  return encode(input.inputStream())
}

fun Coder.encode(input: InputStream): ByteArray {
  val outputStream = ByteArrayOutputStream()
  this.encode(input, outputStream)
  return outputStream.toByteArray()
}

fun Coder.encode(input: ByteArray, output: OutputStream) {
  this.encode(input.inputStream(), output)
}