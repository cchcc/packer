package cchcc.java.packer;

import java.io.UnsupportedEncodingException;
import java.util.Random;

public class XorPacker implements Packer {
    private static final int MAX_DUMMY_SIZE = 40;
    private static final int MAX_DYNAMIC_KEY_SIZE = 8;
    private static final int MAX_PADDING_SIZE = 8;

    private static Random random = new Random();

    private byte[] seedkey, verification;
    private int maxDummySize = MAX_DUMMY_SIZE;
    private int maxDynamicKeySize = MAX_DYNAMIC_KEY_SIZE;
    private int maxPaddingSize = MAX_PADDING_SIZE;
    private boolean dynamicLength = true;

    public XorPacker(byte[] key, byte[] verification
            , int maxDummySize, int maxDynamicKeySize, int maxPaddingSize
            , boolean dynamicLength) {
        this.seedkey = key;
        this.verification = verification;
        this.maxDummySize = maxDummySize;
        this.maxDynamicKeySize = maxDynamicKeySize;
        this.maxPaddingSize = maxPaddingSize;
        this.dynamicLength = dynamicLength;
    }

    public XorPacker(byte[] key, byte[] verification) {
        this(key,verification,MAX_DUMMY_SIZE,MAX_DYNAMIC_KEY_SIZE,MAX_PADDING_SIZE,true);
    }

    private static byte[] encrypt(byte[] key, byte[] src){
        byte result[] = new byte[src.length];
        for(int pos = 0; pos < src.length; ) {
            for(byte k : key) {
                result[pos] = (byte) (src[pos++] ^ k);
                if(pos >= src.length)
                    break;
            }
        }

        return result;
    }

    private byte[] generateKey(byte dynamicKey[]){
        byte[] key = new byte[seedkey.length + dynamicKey.length];
        System.arraycopy(dynamicKey,0,key,0,dynamicKey.length);
        System.arraycopy(seedkey,0,key,dynamicKey.length, seedkey.length);
        return key;
    }

    public byte[] pack(byte src[]){
        byte dynamicKeySize = (byte) (dynamicLength?random.nextInt(maxDynamicKeySize) + 2:maxDynamicKeySize);
        byte dummySize = (byte) (dynamicLength?random.nextInt(maxDummySize)+ 2:maxDummySize);

        byte padding[] = new byte[maxPaddingSize];
        random.nextBytes(padding);
        byte dynamicKey[] = new byte[dynamicKeySize];
        random.nextBytes(dynamicKey);
        byte dummy[] = new byte[dummySize];
        random.nextBytes(dummy);

        byte key[] = generateKey(dynamicKey);

        byte encrypted[] = encrypt(key,src);
        byte[] result = new byte[padding.length + 1 + dynamicKey.length + 1 + verification.length + encrypted.length + dummySize];
        System.arraycopy(padding,0,result,0,padding.length);
        result[padding.length] = dynamicKeySize;
        System.arraycopy(dynamicKey,0,result,padding.length + 1,dynamicKey.length);
        result[padding.length + 1 + dynamicKey.length] = dummySize;

        result[padding.length] = (byte) (result[padding.length] ^ result[0]);
        result[padding.length + 1 + dynamicKey.length] = (byte) (result[padding.length + 1 + dynamicKey.length] ^ result[1]);

        for(int pos = 0; pos< verification.length ; ++pos)
            result[padding.length + 1 + dynamicKey.length + 1 + pos] = src[Math.abs(verification[pos] % src.length)];

        System.arraycopy(encrypted,0,result,padding.length + 1 + dynamicKey.length + 1 + verification.length,encrypted.length);
        System.arraycopy(dummy,0,result,padding.length + 1 + dynamicKey.length + 1 + verification.length + encrypted.length,dummy.length);

        return result;
    }

    public byte[] unpack(byte src[]){
        try {
            byte dynamicKeySize = src[maxPaddingSize];

            dynamicKeySize = (byte) (dynamicKeySize ^ src[0]);
            if(dynamicKeySize < 0)
                return null;
            byte dynamicKey[] = new byte[dynamicKeySize];

            System.arraycopy(src, maxPaddingSize + 1, dynamicKey, 0, dynamicKeySize);

            byte dummySize = src[maxPaddingSize + 1 + dynamicKeySize];

            dummySize = (byte) (dummySize ^ src[1]);
            if(dummySize < 0)
                return null;

            byte vf[] = new byte[verification.length];
            System.arraycopy(src, maxPaddingSize + 1 + dynamicKeySize + 1, vf, 0, verification.length);

            int encryptedSize = src.length - (maxPaddingSize + 1 + dynamicKeySize + 1 + verification.length + dummySize);
            if(encryptedSize < 0)
                return null;

            byte encrypted[] = new byte[encryptedSize];
            System.arraycopy(src, maxPaddingSize + 1 + dynamicKeySize + 1 + verification.length, encrypted, 0, encrypted.length);

            byte key[] = generateKey(dynamicKey);
            byte decrypted[] = encrypt(key, encrypted);

            for (int pos = 0; pos < verification.length; ++pos)
                if (vf[pos] != decrypted[Math.abs(verification[pos] % decrypted.length)])
                    return null;

            return decrypted;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }


    public byte[] pack(String src){
        try {
            return pack(src.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }


    public String unpackToString(byte src[]) {
        byte unpacked[] = unpack(src);
        if (unpacked == null)
            return null;

        try {
            return new String(unpacked, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}