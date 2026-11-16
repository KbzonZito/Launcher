package dev.kbzon.launcher.search.data

import dev.kbzon.launcher.unitconverter.Dimension
import dev.kbzon.launcher.unitconverter.UnitValue

class CurrencyUnitConverter(dimension: Dimension, inputValue: UnitValue, values: List<UnitValue>, val updateTimestamp: Long)
    : UnitConverter(dimension, inputValue, values)