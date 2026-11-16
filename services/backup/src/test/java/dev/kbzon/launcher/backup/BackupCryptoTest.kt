package dev.kbzon.launcher.backup

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

// [fork]
class BackupCryptoTest {
    private val data = ByteArray(200_000) { (it * 31 % 251).toByte() }

    private fun encrypt(password: String): ByteArray {
        val out = ByteArrayOutputStream()
        BackupCrypto.encrypting(out, password.toCharArray()).use { it.write(data) }
        return out.toByteArray()
    }

    @Test
    fun roundTrip() {
        val enc = encrypt("correct horse")
        val dec = BackupCrypto.decrypting(enc.inputStream(), "correct horse".toCharArray()).readBytes()
        assertArrayEquals(data, dec)
    }

    @Test(expected = BackupCrypto.WrongPasswordException::class)
    fun wrongPasswordFails() {
        val enc = encrypt("correct horse")
        BackupCrypto.decrypting(enc.inputStream(), "battery staple".toCharArray()).readBytes()
    }

    @Test(expected = BackupCrypto.WrongPasswordException::class)
    fun tamperingIsDetected() {
        val enc = encrypt("pw")
        enc[enc.size / 2] = (enc[enc.size / 2].toInt() xor 1).toByte()
        BackupCrypto.decrypting(enc.inputStream(), "pw".toCharArray()).readBytes()
    }

    @Test
    fun detectsHeader() {
        assertTrue(BackupCrypto.isEncrypted(encrypt("pw").inputStream()))
        assertFalse(BackupCrypto.isEncrypted(byteArrayOf(0x50, 0x4b, 3, 4, 0, 0, 0, 0, 0).inputStream())) // zip
        assertFalse(BackupCrypto.isEncrypted(ByteArray(3).inputStream()))
    }

    @Test
    fun sameInputDifferentCiphertext() {
        // sal e IV aleatorios: dos cifrados del mismo contenido no coinciden
        assertFalse(encrypt("pw").contentEquals(encrypt("pw")))
    }
}
