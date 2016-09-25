package cchcc.kt.packer

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * packer using AES
 * @param key : 128 or 256 bits
 * @param iv : 128 bits
 */
class AesPacker(key: ByteArray, iv: ByteArray) : Packer {

    private val cipherEnc: Cipher by lazy {
        Cipher.getInstance("AES/CBC/PKCS5PADDING").apply {
            init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        }
    }
    private val cipherDec: Cipher by lazy {
        Cipher.getInstance("AES/CBC/PKCS5PADDING").apply {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        }
    }

    private val verification: ByteArray by lazy {
        ByteArray(key.size / 5 + 1).apply {
            for (i in indices)
                this[i] = key[i]
        }
    }

    override fun pack(src: ByteArray): ByteArray {

        val hash = ByteArray(verification.size)
        for (i in hash.indices)
            hash[i] = src[Math.abs(verification[i] % src.size)]

        val encrypted = cipherEnc.doFinal(src)

        val packed = ByteArray(hash.size + encrypted.size)

        System.arraycopy(hash, 0, packed, 0, hash.size)
        System.arraycopy(encrypted, 0, packed, hash.size, encrypted.size)

        return packed
    }

    override fun unpack(src: ByteArray): ByteArray? {

        val hash = ByteArray(verification.size)
        System.arraycopy(src, 0, hash, 0, hash.size)

        val encrypted = ByteArray(src.size - hash.size)
        System.arraycopy(src, hash.size, encrypted, 0, encrypted.size)

        val decrypted = cipherDec.doFinal(encrypted)

        try {
            for (i in hash.indices) {
                if (hash[i] != decrypted[Math.abs(verification[i] % decrypted.size)])
                    return null
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            return null
        }

        return decrypted
    }
}
