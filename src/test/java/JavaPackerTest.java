import cchcc.java.packer.AesPacker;
import cchcc.java.packer.XorPacker;
import cchcc.java.packer.Packer;
import org.junit.Assert;
import org.junit.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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
        byte key[] = new byte[16], iv[] = new byte[16];;
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

}
