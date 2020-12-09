package mtf

import commons.Decoder
import commons.DecoderWriter
import java.io.InputStream
import java.io.OutputStream

object MTFDecoder : Decoder {
  override fun decode(input: InputStream, output: OutputStream) {
    MTFDecoderWriter(input, output).writeDecoded()
  }
}

private class MTFDecoderWriter(
  private val input: InputStream,
  private val output: OutputStream
) : DecoderWriter {
  override fun writeDecoded() {
    TODO("Not yet implemented")
  }

  override fun close() {
    TODO("Not yet implemented")
  }
}