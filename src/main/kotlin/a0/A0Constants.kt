package a0

internal const val NUMBER_OF_CHARS = 256

internal const val EOF_SYMBOL = NUMBER_OF_CHARS + 1

internal const val NUMBER_OF_SYMBOLS = NUMBER_OF_CHARS + 1

internal const val CODE_VALUE_BITS = 16

internal const val CODE_VALUE_MAX = (1 shl CODE_VALUE_BITS) - 1

internal const val CODE_VALUE_FIRST_QUARTER = CODE_VALUE_MAX / 4 + 1

internal const val CODE_VALUE_HALF = 2 * CODE_VALUE_FIRST_QUARTER

internal const val CODE_VALUE_THIRD_QUARTER = 3 * CODE_VALUE_FIRST_QUARTER

internal const val MAX_FREQUENCY = CODE_VALUE_FIRST_QUARTER - 1