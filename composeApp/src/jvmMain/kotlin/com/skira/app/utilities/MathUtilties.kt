package com.skira.app.utilities

/**
 * Normalize an integer value within a specified range to a float between 0 and 1
 *
 * @param start The start integer value of the range
 * @param end The end integer value of the range
 *
 * @return A float value between 0 and 1 representing the normalized position of the integer
 */
fun Int.normalizeValueToFloat(start: Int, end: Int): Float {
    return (this - start).toFloat() / (end - start)
}

/**
 * Convert a normalized value to an integer within a specified range
 *
 * @param start The start integer value of the range
 * @param end The end integer value of the range
 *
 * @return An integer value within the specified range corresponding to the normalized float
 */
fun Float.denormalizeToInt(start: Int, end: Int): Int {
    return (this * (end - start) + start).toInt()
}