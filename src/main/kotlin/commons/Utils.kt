package commons

import java.io.InputStream

fun InputStream.forEachByte(action: (byte: Int) -> Unit) {
  while (true) {
    val byte = this.read()
    if (byte == -1) break
    action(byte)
  }
}