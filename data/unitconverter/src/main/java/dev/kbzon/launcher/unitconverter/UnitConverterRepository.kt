package dev.kbzon.launcher.unitconverter

import android.content.Context
import dev.kbzon.launcher.currencies.CurrencyRepository
import dev.kbzon.launcher.preferences.search.UnitConverterSettings
import dev.kbzon.launcher.search.data.UnitConverter
import dev.kbzon.launcher.unitconverter.converters.AreaConverter
import dev.kbzon.launcher.unitconverter.converters.Converter
import dev.kbzon.launcher.unitconverter.converters.CurrencyConverter
import dev.kbzon.launcher.unitconverter.converters.DataConverter
import dev.kbzon.launcher.unitconverter.converters.LengthConverter
import dev.kbzon.launcher.unitconverter.converters.MassConverter
import dev.kbzon.launcher.unitconverter.converters.TemperatureConverter
import dev.kbzon.launcher.unitconverter.converters.TimeConverter
import dev.kbzon.launcher.unitconverter.converters.VelocityConverter
import dev.kbzon.launcher.unitconverter.converters.VolumeConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

interface UnitConverterRepository {
    fun search(query: String): Flow<UnitConverter?>
    suspend fun getAvailableConverters(includeCurrencies: Boolean): List<Converter>
}

internal class UnitConverterRepositoryImpl(
    private val context: Context,
    private val currencyRepository: CurrencyRepository,
    private val settings: UnitConverterSettings,
) : UnitConverterRepository, KoinComponent {

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    init {
        scope.launch {
            settings.map { it.enabled && it.currencies }
                .distinctUntilChanged().collectLatest {
                    if (it) currencyRepository.enableCurrencyUpdateWorker()
                    else currencyRepository.disableCurrencyUpdateWorker()
                }
        }
    }

    override fun search(query: String): Flow<UnitConverter?> {
        if (query.isBlank()) return flowOf(null)
        return settings.distinctUntilChanged().map {
            if (!it.enabled) null
            else queryUnitConverter(query, it.currencies)
        }
    }

    override suspend fun getAvailableConverters(includeCurrencies: Boolean): List<Converter> {
        val converters = mutableListOf(
            MassConverter(context),
            LengthConverter(context),
            DataConverter(context),
            TimeConverter(context),
            VelocityConverter(context),
            AreaConverter(context),
            TemperatureConverter(context),
            VolumeConverter(context),
        )
        if (includeCurrencies) converters.add(CurrencyConverter(currencyRepository, settings.preferredCurrencies.first()))

        return converters
    }

    private suspend fun queryUnitConverter(
        query: String,
        includeCurrencies: Boolean
    ): UnitConverter? {
        val regex = Regex("""([+\-]?[\d+\-e,.]+|[^\d>\-]+)""")

        val matches = regex.findAll(query)

        var inputStr: String? = null
        var inputValue: Double? = null
        var inputUnit: String? = null
        var outputUnit: String? = null

        for ((i, match) in matches.withIndex()) {
            when (i) {
                0 -> {
                    inputStr = match.value.trim()
                    inputValue = inputStr.toDoubleOrNull()
                        ?: inputStr.replace(',', '.').toDoubleOrNull()
                                ?: return null
                }
                1 -> inputUnit = match.value.trim()
                2 -> {
                    if (!match.value.contains("-") && !match.value.contains(">")) {
                        outputUnit = match.value.trim()
                    }
                }
                3 -> {
                    if (outputUnit == null) {
                        outputUnit = match.value.trim()
                        break
                    } else {
                        return null
                    }
                }
                else -> return null
            }
        }

        if (inputValue == null || inputUnit == null) {
            return null
        }

        val converters = getAvailableConverters(includeCurrencies)

        for (converter in converters) {
            if (!converter.isValidUnit(inputUnit)) continue
            if (outputUnit != null && !converter.isValidUnit(outputUnit)) continue
            return converter.convert(context, inputUnit, inputValue, outputUnit)
        }
        return null
    }
}