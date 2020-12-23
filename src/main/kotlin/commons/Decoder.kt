package commons

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream

internal interface DecoderWriter : Closeable {
  fun writeDecoded()
}

interface Decoder {
  fun decode(input: InputStream, output: OutputStream)
}

fun Decoder.decode(input: ByteArray): ByteArray {
  return decode(input.inputStream())
}

fun Decoder.decode(input: InputStream): ByteArray {
  val outputStream = ByteArrayOutputStream()
  this.decode(input, outputStream)
  return outputStream.toByteArray()
}

fun Decoder.decode(input: ByteArray, output: OutputStream) {
  this.decode(input.inputStream(), output)
}