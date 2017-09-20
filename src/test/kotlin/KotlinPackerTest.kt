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

    @Test
    fun xor_test() {
        val key = byteArrayOf(-20, -107, -120, -21, -123, -107, -19, -107, -104, -20, -117, -92, -22, -80, -128, 32, -29, -123, -117, -29, -123, -117, -29, -123, -117, 32, 97, 106, 105, 101)
        val v = byteArrayOf(-49, -80, 0, -1, 5, -53, -116, 35, -94, 92, 77, -107, -74, 30, -74, -83, 52)
        val packer = XorPacker(key, v)

        val src = packer.unpackToString(byteArrayOf(-53,-103,68,8,112,-1,-80,93,-52,-123,34,-59,-94,-80,-51,-122,-112,-69,-73,98,97,-69,-29,-29,-124,-29,-65,-76,-32,-79,-79,-79,-104,-79,-25,67,-74,72,15,118,101,104,36,107,111,49,118,105,34,104,115,19,51,10,8,63,-17,6,-89,-22,107,56,99,124,-36))
        println(src)
    }
}