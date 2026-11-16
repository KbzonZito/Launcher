package dev.kbzon.launcher.baselineprofile

import android.content.ComponentName
import android.content.Intent
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

const val PACKAGE = "dev.kbzon.launcher"
private const val HOME = "dev.kbzon.launcher.ui.launcher.LauncherActivity"
private const val ASSISTANT = "dev.kbzon.launcher.ui.assistant.AssistantActivity"

fun MacrobenchmarkScope.startHome() {
    startActivityAndWait(
        Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .setComponent(ComponentName(PACKAGE, HOME))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
    // La home está lista cuando aparece la estrella del riel.
    device.wait(Until.hasObject(By.text("★")), 5_000)
}

/** Recorre lo que más se usa a diario: riel, lista de una letra, búsqueda por el botón lateral. */
fun MacrobenchmarkScope.dailyJourney() {
    val star = device.findObject(By.text("★")) ?: return
    val rail = star.parent ?: return
    // Arrastrar por todo el riel (ola + cambio de lista por letra).
    val b = rail.visibleBounds
    device.swipe(b.centerX(), b.top + 10, b.centerX(), b.bottom - 10, 60)
    device.waitForIdle()
    // Soltar en una letra del medio y desplazar su lista.
    device.click(b.centerX(), b.centerY())
    device.waitForIdle()
    device.findObject(By.scrollable(true))?.fling(Direction.DOWN)
    device.pressBack()
    device.waitForIdle()

    // Búsqueda como desde el botón lateral.
    startActivityAndWait(
        Intent(Intent.ACTION_ASSIST)
            .setComponent(ComponentName(PACKAGE, ASSISTANT))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
    device.waitForIdle()
    device.executeShellCommand("input text c")
    device.waitForIdle()
    device.executeShellCommand("input text h")
    device.waitForIdle()
    device.pressBack()
    device.pressBack()
    device.waitForIdle()
}
