package address;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by bqh on 2017/4/7.
 * <p>
 * E-mail:M201672845@hust.edu.cn
 */
public class Sha256Hash {
    private static final long serialVersionUID = 1L;
    public static final int LENGTH = 32; // bytes
    public static final Sha256Hash ZERO_HASH = wrap(new byte[LENGTH]);

    private final byte[] bytes;

    /**
     * Use {@link #wrap(byte[])} instead.
     */
    @Deprecated
    public Sha256Hash(byte[] rawHashBytes) {
        this.bytes = rawHashBytes;
    }

    /**
     * Creates a new instance that wraps the given hash value.
     *
     * @param rawHashBytes
     *            the raw hash bytes to wrap
     * @return a new instance
     * @throws IllegalArgumentException
     *             if the given array length is not exactly 32
     */

    // the future
    public static Sha256Hash wrap(byte[] rawHashBytes) {
        return new Sha256Hash(rawHashBytes);
    }

    /** Use {@link #of(byte[])} instead: this old name is ambiguous. */
    @Deprecated
    public static Sha256Hash create(byte[] contents) {
        return of(contents);
    }

    /**
     * Creates a new instance containing the calculated (one-time) hash of the
     * given bytes.
     *
     * @param contents
     *            the bytes on which the hash value is calculated
     * @return a new instance containing the calculated (one-time) hash
     */
    public static Sha256Hash of(byte[] contents) {
        return wrap(hash(contents));
    }

    /** Use {@link #twiceOf(byte[])} instead: this old name is ambiguous. */
    @Deprecated
    public static Sha256Hash createDouble(byte[] contents) {
        return twiceOf(contents);
    }

    /**
     * Creates a new instance containing the hash of the calculated hash of the
     * given bytes.
     *
     * @param contents
     *            the bytes on which the hash value is calculated
     * @return a new instance containing the calculated (two-time) hash
     */
    public static Sha256Hash twiceOf(byte[] contents) {
        return wrap(hashTwice(contents));
    }

    /**
     * Returns a new SHA-256 MessageDigest instance.
     *
     * This is a convenience method which wraps the checked exception that can
     * never occur with a RuntimeException.
     *
     * @return a new SHA-256 MessageDigest instance
     */
    public static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // Can't happen.
        }
    }

    /**
     * Calculates the SHA-256 hash of the given bytes.
     *
     * @param input
     *            the bytes to hash
     * @return the hash (in big-endian order)
     */
    public static byte[] hash(byte[] input) {
        return hash(input, 0, input.length);
    }

    /**
     * Calculates the SHA-256 hash of the given byte range.
     *
     * @param input
     *            the array containing the bytes to hash
     * @param offset
     *            the offset within the array of the bytes to hash
     * @param length
     *            the number of bytes to hash
     * @return the hash (in big-endian order)
     */
    public static byte[] hash(byte[] input, int offset, int length) {
        MessageDigest digest = newDigest();
        digest.update(input, offset, length);
        return digest.digest();
    }

    /**
     * Calculates the SHA-256 hash of the given bytes, and then hashes the
     * resulting hash again.
     *
     * @param input
     *            the bytes to hash
     * @return the double-hash (in big-endian order)
     */
    public static byte[] hashTwice(byte[] input) {
        return hashTwice(input, 0, input.length);
    }

    /**
     * Calculates the SHA-256 hash of the given byte range, and then hashes the
     * resulting hash again.
     *
     * @param input
     *            the array containing the bytes to hash
     * @param offset
     *            the offset within the array of the bytes to hash
     * @param length
     *            the number of bytes to hash
     * @return the double-hash (in big-endian order)
     */
    public static byte[] hashTwice(byte[] input, int offset, int length) {
        MessageDigest digest = newDigest();
        digest.update(input, offset, length);
        return digest.digest(digest.digest());
    }

    /**
     * Calculates the hash of hash on the given byte ranges. This is equivalent
     * to concatenating the two ranges and then passing the result to
     * {@link #hashTwice(byte[])}.
     */
    public static byte[] hashTwice(byte[] input1, int offset1, int length1, byte[] input2, int offset2, int length2) {
        MessageDigest digest = newDigest();
        digest.update(input1, offset1, length1);
        digest.update(input2, offset2, length2);
        return digest.digest(digest.digest());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return Arrays.equals(bytes, ((Sha256Hash) o).bytes);
    }

    /**
     * Returns the bytes interpreted as a positive integer.
     */
    public BigInteger toBigInteger() {
        return new BigInteger(1, bytes);
    }

    /**
     * Returns the internal byte array, without defensively copying. Therefore
     * do NOT modify the returned array.
     */
    public byte[] getBytes() {
        return bytes;
    }

    public int compareTo(final Sha256Hash other) {
        for (int i = LENGTH - 1; i >= 0; i--) {
            final int thisByte = this.bytes[i] & 0xff;
            final int otherByte = other.bytes[i] & 0xff;
            if (thisByte > otherByte)
                return 1;
            if (thisByte < otherByte)
                return -1;
        }
        return 0;
    }
}
