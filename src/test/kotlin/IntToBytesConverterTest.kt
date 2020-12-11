import commons.toBytes
import commons.toInt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IntToBytesConverterTest {

  @Test
  fun `each number from -1000 to 1000 can be converted`() {
    (-1000..1000).forEach {
      val bytes = it.toBytes()
      val intFromBytes = bytes.toInt()
      assertEquals(it, intFromBytes)
    }
  }
}