package jpeg.transcoder.entropy.coding.huffman

import jpeg.transcoder.entropy.coding.JpegEntropyCompressor
import java.io.InputStream
import java.io.OutputStream

object HuffmanJpegEntropyCompressor : JpegEntropyCompressor {
  override fun encode(input: InputStream, output: OutputStream) {
    HuffmanJpegEntropyCoder.encode(input, output)
  }

  override fun decode(input: InputStream, output: OutputStream) {
    HuffmanJpegEntropyDecoder.decode(input, output)
  }
}