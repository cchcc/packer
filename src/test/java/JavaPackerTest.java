import cchcc.java.packer.AesPacker;
import cchcc.java.packer.XorPacker;
import cchcc.java.packer.Packer;
import org.junit.Assert;
import org.junit.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Random;

public class JavaPackerTest {
    Random random = new Random();

    void packAndUnpack(Packer packer) {
        byte srcBytes[] = new byte[100];
        random.nextBytes(srcBytes);
        byte encBytes[] = packer.pack(srcBytes);
        byte decBytes[] = packer.unpack(encBytes);

        Assert.assertArrayEquals(srcBytes, decBytes);
    }

    @Test
    public void aes_packAndUnpack() throws InvalidAlgorithmParameterException, InvalidKeyException {
        byte key[] = new byte[16], iv[] = new byte[16];
        random.nextBytes(key);
        random.nextBytes(iv);

        packAndUnpack(new AesPacker(key, iv));
    }

    @Test
    public void xor_packAndUnpack() {
        byte key[] = new byte[16], iv[] = new byte[16];
        random.nextBytes(key);
        random.nextBytes(iv);

        packAndUnpack(new XorPacker(key, iv));
    }


    void checkPerformance(Packer packer) {
        byte[] src = new byte[1024 * 1024];
        random.nextBytes(src);

        // warm up memory
        packer.pack(src);
        packer.unpack(src);
        packer.unpack(packer.pack(src));
        packer.unpack(packer.pack(src));
        packer.unpack(packer.pack(src));

        long start = System.currentTimeMillis();
        for(int i = 0; i< 100; i++)
            packer.unpack(packer.pack(src));

        long spentTime = System.currentTimeMillis() - start;

        System.out.println(String.format("%s - pack and unpack of 100MB : %d milliseconds"
                           , packer.getClass().getName(), spentTime));
    }

    @Test
    public void aes_performance() throws InvalidAlgorithmParameterException, InvalidKeyException {
        byte key[] = new byte[16], iv[] = new byte[16];
        random.nextBytes(key);
        random.nextBytes(iv);

        checkPerformance(new AesPacker(key, iv));
    }

    @Test
    public void xor_performance() {
        byte key[] = new byte[16], iv[] = new byte[16];
        random.nextBytes(key);
        random.nextBytes(iv);

        checkPerformance(new XorPacker(key, iv));
    }

    @Test
    public void xor_test() {
        byte key[] = new byte[]{-20, -107, -120, -21, -123, -107, -19, -107, -104, -20, -117, -92, -22, -80, -128, 32, -29, -123, -117, -29, -123, -117, -29, -123, -117, 32, 97, 106, 105, 101};
        byte v[] = new byte[]{-49, -80, 0, -1, 5, -53, -116, 35, -94, 92, 77, -107, -74, 30, -74, -83, 52};

        XorPacker packer = new XorPacker(key, v);
        System.out.println(Arrays.toString(packer.pack("bas꿻ㄱㄴㄷ😗฿")));
    }
}
