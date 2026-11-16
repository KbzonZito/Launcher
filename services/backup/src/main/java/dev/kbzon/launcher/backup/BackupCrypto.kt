package dev.kbzon.launcher.backup

import java.io.BufferedInputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * [fork] Cifrado opcional de backups con contraseña.
 *
 * Formato: "LNCHBK01" (8 bytes) | sal (16) | IV (12) | AES-256-GCM(zip)
 * Clave: PBKDF2-HMAC-SHA256, 210 000 iteraciones.
 */
object BackupCrypto {
    private val MAGIC = "LNCHBK01".toByteArray(Charsets.US_ASCII)
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12
    private const val ITERATIONS = 210_000
    private const val KEY_BITS = 256
    private const val TAG_BITS = 128

    class WrongPasswordException : Exception("Wrong password or corrupted backup")

    /** Devuelve un stream que cifra lo que se escribe en él; hay que cerrarlo para terminar. */
    fun encrypting(out: OutputStream, password: CharArray): OutputStream {
        val random = SecureRandom()
        val salt = ByteArray(SALT_SIZE).also(random::nextBytes)
        val iv = ByteArray(IV_SIZE).also(random::nextBytes)
        out.write(MAGIC)
        out.write(salt)
        out.write(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(TAG_BITS, iv))
        }
        return CipherOutputStream(out, cipher)
    }

    /** true si el stream empieza con la cabecera de backup cifrado (no consume bytes si es mark-able). */
    fun isEncrypted(input: InputStream): Boolean {
        val header = ByteArray(MAGIC.size)
        var read = 0
        while (read < header.size) {
            val n = input.read(header, read, header.size - read)
            if (n < 0) return false
            read += n
        }
        return header.contentEquals(MAGIC)
    }

    /**
     * Descifra un backup completo. El GCM verifica la integridad: con una contraseña incorrecta
     * lanza [WrongPasswordException].
     */
    fun decrypting(input: InputStream, password: CharArray): InputStream {
        val buffered = BufferedInputStream(input)
        if (!isEncrypted(buffered)) throw IllegalArgumentException("Not an encrypted backup")
        val salt = buffered.readExactly(SALT_SIZE)
        val iv = buffered.readExactly(IV_SIZE)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(TAG_BITS, iv))
        }
        // Se descifra todo en memoria para verificar la etiqueta antes de usar nada (los backups son chicos).
        val plain = try {
            cipher.doFinal(buffered.readBytes())
        } catch (e: javax.crypto.AEADBadTagException) {
            throw WrongPasswordException()
        }
        return plain.inputStream()
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_BITS)
        val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(key, "AES")
    }

    private fun InputStream.readExactly(n: Int): ByteArray {
        val buf = ByteArray(n)
        var read = 0
        while (read < n) {
            val r = read(buf, read, n - read)
            if (r < 0) throw IllegalArgumentException("Truncated backup")
            read += r
        }
        return buf
    }
}
