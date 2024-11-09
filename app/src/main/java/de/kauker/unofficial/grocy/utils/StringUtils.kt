package de.kauker.unofficial.grocy.utils

import java.util.Locale

fun String.distanceTo(c: String): Double {
    return similarity(this, c)
}

fun similarity(s1: String, s2: String): Double {
    var longer = s1
    var shorter = s2
    if(s1.length < s2.length) { // longer should always have greater length
        longer = s2
        shorter = s1
    }
    val longerLength = longer.length
    return if(longerLength == 0) {
        1.0 /* both strings are zero length */
    } else (longerLength - editDistance(longer, shorter)) / longerLength.toDouble()
}

fun editDistance(is1: String, is2: String): Int {
    var s1 = is1
    var s2 = is2
    s1 = s1.lowercase(Locale.getDefault())
    s2 = s2.lowercase(Locale.getDefault())
    val costs = IntArray(s2.length + 1)
    for(i in 0..s1.length) {
        var lastValue = i
        for(j in 0..s2.length) {
            if(i == 0) costs[j] = j else {
                if(j > 0) {
                    var newValue = costs[j - 1]
                    if(s1[i - 1] != s2[j - 1]) newValue = Math.min(
                        Math.min(newValue, lastValue),
                        costs[j]
                    ) + 1
                    costs[j - 1] = lastValue
                    lastValue = newValue
                }
            }
        }
        if(i > 0) costs[s2.length] = lastValue
    }
    return costs[s2.length]
}
