package commons

import java.io.InputStream
import java.io.OutputStream

interface Coder {
  fun encode(input: InputStream, output: OutputStream)
}