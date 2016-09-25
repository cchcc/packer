package cchcc.java.packer;

public interface Packer {

    byte[] pack(byte[] src);
    byte[] pack(String src);

    /**
     * @return if unpacking is failed returns null
     */
    byte[] unpack(byte[] src);

    /**
     * @return if unpacking is failed returns null
     */
    String unpackToString(byte[] src);
}
