package ecdsa;

/**
 * A variable-length encoded unsigned integer using Satoshi's encoding (a.k.a. "CompactSize").
 */
public class VarInt {
    public final long value;
    private final int originallyEncodedSize;

    /**
     * Constructs a new VarInt with the given unsigned long value.
     *
     * @param value the unsigned long value (beware widening conversion of negatives!)
     */
    public VarInt(long value) {
        this.value = value;
        originallyEncodedSize = getSizeInBytes();
    }

    /**
     * Constructs a new VarInt with the value parsed from the specified offset of the given buffer.
     *
     * @param buf the buffer containing the value
     * @param offset the offset of the value
     */
    public VarInt(byte[] buf, int offset) {
        int first = 0xFF & buf[offset];
        if (first < 253) {
            value = first;
            originallyEncodedSize = 1; // 1 data byte (8 bits)
        } else if (first == 253) {
            value = (0xFF & buf[offset + 1]) | ((0xFF & buf[offset + 2]) << 8);
            originallyEncodedSize = 3; // 1 marker + 2 data bytes (16 bits)
        } else if (first == 254) {
            value = readUint32(buf, offset + 1);
            originallyEncodedSize = 5; // 1 marker + 4 data bytes (32 bits)
        } else {
            value = readInt64(buf, offset + 1);
            originallyEncodedSize = 9; // 1 marker + 8 data bytes (64 bits)
        }
    }

    /**
     * Returns the original number of bytes used to encode the value if it was
     * deserialized from a byte array, or the minimum encoded size if it was not.
     */
    public int getOriginalSizeInBytes() {
        return originallyEncodedSize;
    }

    /**
     * Returns the minimum encoded size of the value.
     */
    public final int getSizeInBytes() {
        return sizeOf(value);
    }

    /**
     * Returns the minimum encoded size of the given unsigned long value.
     *
     * @param value the unsigned long value (beware widening conversion of negatives!)
     */
    public static int sizeOf(long value) {
        // if negative, it's actually a very large unsigned long value
        if (value < 0) return 9; // 1 marker + 8 data bytes
        if (value < 253) return 1; // 1 data byte
        if (value <= 0xFFFFL) return 3; // 1 marker + 2 data bytes
        if (value <= 0xFFFFFFFFL) return 5; // 1 marker + 4 data bytes
        return 9; // 1 marker + 8 data bytes
    }

    /**
     * Encodes the value into its minimal representation.
     *
     * @return the minimal encoded bytes of the value
     */
    public byte[] encode() {
        byte[] bytes;
        switch (sizeOf(value)) {
            case 1:
                return new byte[]{(byte) value};
            case 3:
                return new byte[]{(byte) 253, (byte) (value), (byte) (value >> 8)};
            case 5:
                bytes = new byte[5];
                bytes[0] = (byte) 254;
                uint32ToByteArrayLE(value, bytes, 1);
                return bytes;
            default:
                bytes = new byte[9];
                bytes[0] = (byte) 255;
                uint64ToByteArrayLE(value, bytes, 1);
                return bytes;
        }
    }
    
    /** Parse 4 bytes from the byte array (starting at the offset) as unsigned 32-bit integer in little endian format. */
    public static long readUint32(byte[] bytes, int offset) {
        return (bytes[offset] & 0xffl) |
                ((bytes[offset + 1] & 0xffl) << 8) |
                ((bytes[offset + 2] & 0xffl) << 16) |
                ((bytes[offset + 3] & 0xffl) << 24);
    }

    /** Parse 8 bytes from the byte array (starting at the offset) as signed 64-bit integer in little endian format. */
    public static long readInt64(byte[] bytes, int offset) {
        return (bytes[offset] & 0xffl) |
               ((bytes[offset + 1] & 0xffl) << 8) |
               ((bytes[offset + 2] & 0xffl) << 16) |
               ((bytes[offset + 3] & 0xffl) << 24) |
               ((bytes[offset + 4] & 0xffl) << 32) |
               ((bytes[offset + 5] & 0xffl) << 40) |
               ((bytes[offset + 6] & 0xffl) << 48) |
               ((bytes[offset + 7] & 0xffl) << 56);
    }
    
    public static void uint32ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
    }
    
    public static void uint64ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
        out[offset + 4] = (byte) (0xFF & (val >> 32));
        out[offset + 5] = (byte) (0xFF & (val >> 40));
        out[offset + 6] = (byte) (0xFF & (val >> 48));
        out[offset + 7] = (byte) (0xFF & (val >> 56));
    }
}
