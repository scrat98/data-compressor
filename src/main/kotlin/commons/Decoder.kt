package commons

import java.io.InputStream
import java.io.OutputStream

interface Decoder {
  fun decode(input: InputStream, output: OutputStream)
}