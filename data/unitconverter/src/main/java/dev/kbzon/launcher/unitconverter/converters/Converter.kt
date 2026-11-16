package dev.kbzon.launcher.unitconverter.converters

import android.content.Context
import dev.kbzon.launcher.search.data.UnitConverter
import dev.kbzon.launcher.unitconverter.Dimension
import dev.kbzon.launcher.unitconverter.MeasureUnit

interface Converter {
    val dimension: Dimension

    suspend fun isValidUnit(symbol: String): Boolean

    suspend fun convert(
        context: Context,
        fromUnit: String,
        value: Double,
        toUnit: String?
    ): UnitConverter

    suspend fun getSupportedUnits(): List<MeasureUnit>
}

