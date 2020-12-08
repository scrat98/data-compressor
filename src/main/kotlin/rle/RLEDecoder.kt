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

  private var previousByte = -1

  override fun writeDecoded() {
    while (true) {
      val byte = input.read()
      if (byte == -1) break
      output.write(byte)
      if (byte == previousByte) {
        val repeatedSequenceSize = input.read()
        repeat(repeatedSequenceSize) {
          output.write(previousByte)
        }
      }
      previousByte = byte
    }
    close()
  }

  override fun close() {
    input.close()
    output.close()
  }

}