package cchcc.java.packer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AesPacker implements Packer {

    private Cipher cipherDec;
    private Cipher cipherEnc;

    private byte[] verification;

    // key : 128 or 256 bits, iv : 128 bits
    public AesPacker(byte[] key, byte[] iv) throws InvalidAlgorithmParameterException, InvalidKeyException {

        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key,"AES");

        try {
            cipherEnc = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipherEnc.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

            cipherDec = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipherDec.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            verification = new byte[key.length / 5 + 1];
            for(int i = 0;i<verification.length; ++i){
                verification[i] = key[i];
            }

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
        }
    }

    private byte[] encrypt(byte[] src){
        try {
            return cipherEnc.doFinal(src);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
        }
        return null;
    }

    private byte[] decrypt(byte[] src){
        try {
            return cipherDec.doFinal(src);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
        }
        return null;
    }


    public byte[] pack(byte[] src){

        byte hash[] = new byte[verification.length];
        for(int i = 0; i<hash.length; ++i)
            hash[i] = src[Math.abs(verification[i] % src.length)];

        byte encrypted[] = encrypt(src);
        if(encrypted == null)
            return null;

        byte packed[] = new byte[hash.length + encrypted.length];

        System.arraycopy(hash,0,packed,0,hash.length);
        System.arraycopy(encrypted,0,packed,hash.length,encrypted.length);

        return packed;
    }

    public byte[] unpack(byte[] src){

        byte hash[] = new byte[verification.length];
        System.arraycopy(src,0,hash,0,hash.length);

        byte encrypted[] = new byte[src.length - hash.length];
        System.arraycopy(src,hash.length,encrypted,0,encrypted.length);

        byte decrypted[] = decrypt(encrypted);
        if(decrypted == null)
            return null;

        try {

            for (int i = 0; i < hash.length; ++i) {
                if (hash[i] != decrypted[Math.abs(verification[i] % decrypted.length)])
                    return null;
            }
        }catch (ArrayIndexOutOfBoundsException e){
            return null;
        }

        return decrypted;
    }

    public byte[] pack(String src){
        try {
            return pack(src.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
           return null;
        }
    }

    public String unpackToString(byte[] src){
        byte[] decrypt = unpack(src);
        if(decrypt == null)
            return null;

        try {
            return new String(decrypt,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

}
