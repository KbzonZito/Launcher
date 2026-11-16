package dev.kbzon.launcher.search.data

import dev.kbzon.launcher.search.Searchable
import dev.kbzon.launcher.unitconverter.Dimension
import dev.kbzon.launcher.unitconverter.UnitValue

open class UnitConverter(
    val dimension: Dimension,
    val inputValue: UnitValue,
    val values: List<UnitValue>
): Searchable
