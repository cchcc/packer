package cchcc.kt.packer

import java.util.*

class XorPacker @JvmOverloads constructor(private val seedkey: ByteArray
                                          , private val verification: ByteArray
                                          , val maxDummySize: Int = XorPacker.MAX_DUMMY_SIZE
                                          , val maxDynamicKeySize: Int = XorPacker.MAX_DYNAMIC_KEY_SIZE
                                          , val maxPaddingSize: Int = XorPacker.MAX_PADDING_SIZE
                                          , val dynamicLength: Boolean = true) : Packer {

    infix fun Byte.xor(b: Byte): Byte = (this.toInt() xor b.toInt()).toByte()

    private fun encrypt(key: ByteArray, src: ByteArray): ByteArray {
        val result = ByteArray(src.size)
        var pos = 0
        while (pos < src.size) {
            for (k in key) {
                result[pos] = (src[pos++] xor k).toByte()
                if (pos >= src.size)
                    break
            }
        }

        return result
    }

    private fun generateKey(dynamicKey: ByteArray): ByteArray {
        val key = ByteArray(seedkey.size + dynamicKey.size)
        System.arraycopy(dynamicKey, 0, key, 0, dynamicKey.size)
        System.arraycopy(seedkey, 0, key, dynamicKey.size, seedkey.size)
        return key
    }

    override fun pack(src: ByteArray): ByteArray {
        val dynamicKeySize = if (dynamicLength) random.nextInt(maxDynamicKeySize) + 2 else maxDynamicKeySize
        val dummySize = if (dynamicLength) random.nextInt(maxDummySize) + 2 else maxDummySize

        val padding = ByteArray(maxPaddingSize)
        random.nextBytes(padding)
        val dynamicKey = ByteArray(dynamicKeySize)
        random.nextBytes(dynamicKey)
        val dummy = ByteArray(dummySize)
        random.nextBytes(dummy)

        val key = generateKey(dynamicKey)

        val encrypted = encrypt(key, src)
        val result = ByteArray(padding.size + 1 + dynamicKey.size + 1 + verification.size + encrypted.size + dummySize.toInt())
        System.arraycopy(padding, 0, result, 0, padding.size)
        result[padding.size] = dynamicKeySize.toByte()
        System.arraycopy(dynamicKey, 0, result, padding.size + 1, dynamicKey.size)
        result[padding.size + 1 + dynamicKey.size] = dummySize.toByte()

        result[padding.size] = (result[padding.size] xor result[0]).toByte()
        result[padding.size + 1 + dynamicKey.size] = (result[padding.size + 1 + dynamicKey.size] xor result[1]).toByte()

        for (pos in verification.indices)
            result[padding.size + 1 + dynamicKey.size + 1 + pos] = src[Math.abs(verification[pos] % src.size)]

        System.arraycopy(encrypted, 0, result, padding.size + 1 + dynamicKey.size + 1 + verification.size, encrypted.size)
        System.arraycopy(dummy, 0, result, padding.size + 1 + dynamicKey.size + 1 + verification.size + encrypted.size, dummy.size)

        return result
    }

    override fun unpack(src: ByteArray): ByteArray? {

        try {
            var dynamicKeySize = src[maxPaddingSize].toInt()

            dynamicKeySize = (dynamicKeySize.toByte() xor src[0]).toInt()
            if (dynamicKeySize < 0)
                return null
            val dynamicKey = ByteArray(dynamicKeySize)

            System.arraycopy(src, maxPaddingSize + 1, dynamicKey, 0, dynamicKeySize.toInt())

            var dummySize = src[maxPaddingSize + 1 + dynamicKeySize.toInt()]

            dummySize = (dummySize xor src[1]).toByte()
            if (dummySize < 0)
                return null

            val vf = ByteArray(verification.size)
            System.arraycopy(src, maxPaddingSize + 1 + dynamicKeySize.toInt() + 1, vf, 0, verification.size)

            val encryptedSize = src.size - (maxPaddingSize + 1 + dynamicKeySize.toInt() + 1 + verification.size + dummySize.toInt())
            if (encryptedSize < 0)
                return null

            val encrypted = ByteArray(encryptedSize)
            System.arraycopy(src, maxPaddingSize + 1 + dynamicKeySize.toInt() + 1 + verification.size, encrypted, 0, encrypted.size)

            val key = generateKey(dynamicKey)
            val decrypted = encrypt(key, encrypted)

            for (pos in verification.indices)
                if (vf[pos] != decrypted[Math.abs(verification[pos] % decrypted.size)])
                    return null

            return decrypted
        } catch (e: ArrayIndexOutOfBoundsException) {
            return null
        }

    }

    companion object {
        private val MAX_DUMMY_SIZE = 40
        private val MAX_DYNAMIC_KEY_SIZE = 8
        private val MAX_PADDING_SIZE = 8
        private val random = Random()
    }
}