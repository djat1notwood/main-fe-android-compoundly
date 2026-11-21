package com.djatslabs.compoundly.domain.model

enum class CompoundingFrequency(val periodsPerYear: Int) {
    MONTHLY(periodsPerYear = 12),
    ANNUALLY(periodsPerYear = 1)
}
