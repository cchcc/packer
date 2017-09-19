package cchcc.kt.packer

interface Packer {
    fun pack(src: ByteArray): ByteArray

    fun pack(src: String): ByteArray = pack(src.toByteArray())

    /**
     * @return if unpacking is failed returns null
     */
    fun unpack(src: ByteArray): ByteArray?

    /**
     * @return if unpacking is failed returns null
     */
    fun unpackToString(src: ByteArray): String? = unpack(src)?.let { String(it) }
}

fun ByteArray.pack(packer: (ByteArray) -> ByteArray): ByteArray = packer.invoke(this)

fun String.pack(packer: (String) -> ByteArray): ByteArray = packer.invoke(this)

fun ByteArray.unpack(unpacker: (ByteArray) -> ByteArray): ByteArray = unpacker.invoke(this)

fun String.unpack(unpacker: (String) -> ByteArray): ByteArray = unpacker.invoke(this)