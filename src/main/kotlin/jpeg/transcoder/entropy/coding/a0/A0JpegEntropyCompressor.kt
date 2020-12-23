package jpeg.transcoder.entropy.coding.a0

import jpeg.transcoder.entropy.coding.JpegEntropyCompressor
import java.io.InputStream
import java.io.OutputStream

object A0JpegEntropyCompressor : JpegEntropyCompressor {
  override fun encode(input: InputStream, output: OutputStream) {
    A0JpegEntropyCoder.encode(input, output)
  }

  override fun decode(input: InputStream, output: OutputStream) {
    A0JpegEntropyDecoder.decode(input, output)
  }
}