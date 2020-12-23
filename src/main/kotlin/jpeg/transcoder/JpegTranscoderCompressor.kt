package jpeg.transcoder

import commons.decode
import commons.encode
import jpeg.JpegCompressor
import jpeg.transcoder.entropy.coding.a0.A0JpegEntropyCompressor
import jpeg.transcoder.entropy.coding.huffman.HuffmanJpegEntropyCompressor
import java.io.InputStream
import java.io.OutputStream

object JpegTranscoderCompressor : JpegCompressor {
  override fun encode(input: InputStream, output: OutputStream) {
    val huffmanDecoded = HuffmanJpegEntropyCompressor.decode(input)
    A0JpegEntropyCompressor.encode(huffmanDecoded, output)
  }

  override fun decode(input: InputStream, output: OutputStream) {
    val a0Decoded = A0JpegEntropyCompressor.decode(input)
    HuffmanJpegEntropyCompressor.encode(a0Decoded, output)
  }
}