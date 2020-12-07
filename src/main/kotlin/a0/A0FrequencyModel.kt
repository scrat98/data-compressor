package a0

internal class A0FrequencyModel {

  private val charToIndex = Array<Int>(NUMBER_OF_CHARS) { 0 }

  private val indexToChar = Array<Int>(NUMBER_OF_SYMBOLS + 1) { 0 }

  private val frequencies = Array<Int>(NUMBER_OF_SYMBOLS + 1) { 0 }

  private val cumulative = Array<Int>(NUMBER_OF_SYMBOLS + 1) { 0 }

  init {
    (0 until NUMBER_OF_CHARS).forEach {
      charToIndex[it] = it + 1
      indexToChar[it + 1] = it
    }
    (0..NUMBER_OF_SYMBOLS).forEach {
      frequencies[it] = 1
      cumulative[it] = NUMBER_OF_SYMBOLS - it
    }
    frequencies[0] = 0
  }

  fun getTotal(): Int {
    return cumulative[0]
  }

  fun getLow(symbolIndex: Int): Int {
    return cumulative[symbolIndex]
  }

  fun getHigh(symbolIndex: Int): Int {
    return cumulative[symbolIndex - 1]
  }

  fun indexToChar(symbolIndex: Int): Int {
    return indexToChar[symbolIndex]
  }

  fun charToIndex(char: Int): Int {
    return charToIndex[char]
  }

  fun update(symbolIndex: Int) {
    halveAllFrequenciesIfExceeded()
    val newSymbolIndex = updateSymbolIndexIfNeeded(symbolIndex)
    updateFrequencies(newSymbolIndex)
  }

  private fun halveAllFrequenciesIfExceeded() {
    if (getTotal() == MAX_FREQUENCY) {
      var cum = 0
      (NUMBER_OF_SYMBOLS downTo 0).forEach {
        frequencies[it] = (frequencies[it] + 1) / 2
        cumulative[it] = cum
        cum += frequencies[it]
      }
    }
  }

  private fun updateSymbolIndexIfNeeded(symbolIndex: Int): Int {
    var newSymbolIndex = symbolIndex
    while (frequencies[newSymbolIndex] == frequencies[newSymbolIndex - 1]) {
      newSymbolIndex--
    }

    if (newSymbolIndex < symbolIndex) {
      val newChar = indexToChar[newSymbolIndex]
      val oldChar = indexToChar[symbolIndex]

      indexToChar[newSymbolIndex] = oldChar
      indexToChar[symbolIndex] = newChar

      charToIndex[newChar] = symbolIndex
      charToIndex[oldChar] = newSymbolIndex
    }
    return newSymbolIndex
  }

  private fun updateFrequencies(symbolIndex: Int) {
    frequencies[symbolIndex]++
    var indexToUpdate = symbolIndex
    while (indexToUpdate > 0) {
      indexToUpdate--
      cumulative[indexToUpdate] += 1
    }
  }
}