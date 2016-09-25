import cchcc.kt.packer.AesPacker
import cchcc.kt.packer.Packer
import cchcc.kt.packer.XorPacker
import org.junit.Assert
import org.junit.Test
import java.util.*

class KotlinPackerTest {

    val random = Random()

    fun packAndUnpack(packer: Packer) = with(packer) {
        val srcBytes = ByteArray(100).apply { random.nextBytes(this) }
        val encBytes = pack(srcBytes)
        val decBytes = unpack(encBytes)

        Assert.assertArrayEquals(srcBytes, decBytes)

        val srcString = """{"key":"value"}"""
        val encrypted = pack(srcString)
        val decString = unpackToString(encrypted)

        Assert.assertEquals(srcString, decString)
    }

    @Test
    fun aes_packAndUnpack() {
        AesPacker(ByteArray(16).apply { random.nextBytes(this) }
                , ByteArray(16).apply { random.nextBytes(this) })
            .let { packAndUnpack(it) }
    }

    @Test
    fun xor_packAndUnpack() {
        XorPacker(ByteArray(16).apply { random.nextBytes(this) }
                , ByteArray(16).apply { random.nextBytes(this) })
            .let { packAndUnpack(it) }
    }
}