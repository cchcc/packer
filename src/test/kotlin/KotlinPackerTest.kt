import cchcc.kt.packer.AesPacker
import cchcc.kt.packer.Packer
import cchcc.kt.packer.XorPacker
import org.junit.Assert
import org.junit.Test
import java.util.*
import kotlin.system.measureTimeMillis

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


    fun checkPerformance(packer: Packer) = with(packer) {
        val src = ByteArray(1024 * 1024).apply { random.nextBytes(this) }

        // warm up memory
        pack(src)
        unpack(src)
        unpack(pack(src))
        unpack(pack(src))
        unpack(pack(src))


        val spentTime_ms = measureTimeMillis {
            (1..100).forEach { unpack(pack(src)) }
        }
        println("${javaClass.name} - pack and unpack 100MB : $spentTime_ms milliseconds")
    }

    @Test
    fun aes_performance() {
        AesPacker(ByteArray(16).apply { random.nextBytes(this) }
                , ByteArray(16).apply { random.nextBytes(this) })
                .let { checkPerformance(it) }
    }

    @Test
    fun xor_performance() {
        XorPacker(ByteArray(16).apply { random.nextBytes(this) }
                , ByteArray(16).apply { random.nextBytes(this) })
                .let { checkPerformance(it) }
    }
}