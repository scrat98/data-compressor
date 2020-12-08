package rle

import commons.Decoder
import commons.DecoderWriter
import java.io.InputStream
import java.io.OutputStream

object RLEDecoder : Decoder {
  override fun decode(input: InputStream, output: OutputStream) {
    RLEDecoderWriter(input, output).writeDecoded()
  }
}

private class RLEDecoderWriter(
  private val input: InputStream,
  private val output: OutputStream
) : DecoderWriter {

  override fun writeDecoded() {
    while (true) {
      val byte = input.read()
      if (byte == -1) break
      val sequenceLength = byte.toByte().toInt()
      if (sequenceLength < 0) {
        repeat(-sequenceLength) {
          output.write(input.read())
        }
      } else {
        val repeatedByte = input.read()
        repeat(sequenceLength) {
          output.write(repeatedByte)
        }
      }
    }
    close()
  }

  override fun close() {
    input.close()
    output.close()
  }

}