package dev.kbzon.launcher.currencies

import android.content.Context
import dev.kbzon.launcher.preferences.privacy.PrivacySettings
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.kbzon.launcher.crashreporter.CrashReporter
import dev.kbzon.launcher.database.AppDatabase
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import org.w3c.dom.Element
import java.text.SimpleDateFormat
import javax.xml.parsers.DocumentBuilderFactory

class ExchangeRateWorker(val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params), KoinComponent {
    private val privacySettings: PrivacySettings by inject() // [fork]

    override suspend fun doWork(): Result {
        // [fork] Modo offline: no descargar el tipo de cambio.
        if (privacySettings.offlineMode.first()) return Result.success()
        Log.d("MM20", "Updating currency exchange rates")
        val httpClient = HttpClient()
        try {
            val response = httpClient.get {
                url("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml")
            }
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(response.bodyAsChannel().toInputStream())
            val cubes = document.getElementsByTagName("Cube")
            val values = mutableListOf<Pair<String, Double>>()
            var timestamp = System.currentTimeMillis()
            values += "EUR" to 1.0
            for (i in 0 until cubes.length) {
                val cube = cubes.item(i) as? Element ?: continue
                if (cube.hasAttribute("currency")) {
                    val symbol = cube.getAttribute("currency")
                    val value = cube.getAttribute("rate").toDoubleOrNull() ?: continue
                    values += symbol to value
                } else if (cube.hasAttribute("time")) {
                    val date = cube.getAttribute("time")
                    timestamp = SimpleDateFormat("yyyy-MM-dd").parse(date).time
                }
            }
            val currencies = values.map {
                Currency(
                    symbol = it.first,
                    value = it.second,
                    lastUpdate = timestamp
                ).toDatabaseEntity()
            }
            AppDatabase.getInstance(context).currencyDao().insertAll(currencies)
            return Result.success()
        } catch (e: Exception) {
            CrashReporter.logException(e)
            return Result.retry()
        } finally {
            httpClient.close()
        }
    }
}