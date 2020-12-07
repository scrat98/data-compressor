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
  val inputStream = input.inputStream()
  val outputStream = ByteArrayOutputStream()
  this.decode(inputStream, outputStream)
  return outputStream.toByteArray()
}