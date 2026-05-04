import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class hyperLogLog {
    // Match the C++ implementation seed so hashes are comparable across languages.
    private static final int SEED = 0xADC83B19;

    // Lightweight token scanner: reads whitespace-delimited stream elements.
    private static final class FastScanner {
        private final InputStream in;
        private final byte[] buffer = new byte[1 << 16];
        private int ptr;
        private int len;

        FastScanner(InputStream in) {
            this.in = in;
        }

        private int read() throws IOException {
            if (ptr >= len) {
                len = in.read(buffer);
                ptr = 0;
                if (len <= 0) {
                    return -1;
                }
            }
            return buffer[ptr++];
        }

        String next() throws IOException {
            int c;
            do {
                c = read();
                if (c == -1) {
                    return null;
                }
            } while (Character.isWhitespace(c));

            StringBuilder sb = new StringBuilder();
            while (c != -1 && !Character.isWhitespace(c)) {
                sb.append((char) c);
                c = read();
            }
            return sb.toString();
        }
    }

    // HyperLogLog counter with precision b and m = 2^b registers.
    private static final class HyperLogLog {
        private final int b;
        private final int m;
        private final byte[] registers;
        private final double alphaM;

        HyperLogLog(int precision) {
            this.b = precision;
            this.m = 1 << b;
            this.registers = new byte[m];
            // Standard bias-correction constants from the HLL paper.
            if (m == 16) {
                this.alphaM = 0.673;
            } else if (m == 32) {
                this.alphaM = 0.697;
            } else if (m == 64) {
                this.alphaM = 0.709;
            } else {
                this.alphaM = 0.7213 / (1.0 + 1.079 / m);
            }
        }

        private static long rotl64(long x, int r) {
            return (x << r) | (x >>> (64 - r));
        }

        private static long fmix64(long k) {
            // Final avalanche stage used by MurmurHash3.
            k ^= k >>> 33;
            k *= 0xff51afd7ed558ccdl;
            k ^= k >>> 33;
            k *= 0xc4ceb9fe1a85ec53l;
            k ^= k >>> 33;
            return k;
        }

        private static long getBlock64LE(byte[] data, int offset) {
            // MurmurHash3 x64 variant consumes little-endian 64-bit words.
            return ((long) data[offset] & 0xffL)
                    | (((long) data[offset + 1] & 0xffL) << 8)
                    | (((long) data[offset + 2] & 0xffL) << 16)
                    | (((long) data[offset + 3] & 0xffL) << 24)
                    | (((long) data[offset + 4] & 0xffL) << 32)
                    | (((long) data[offset + 5] & 0xffL) << 40)
                    | (((long) data[offset + 6] & 0xffL) << 48)
                    | (((long) data[offset + 7] & 0xffL) << 56);
        }

        private static long murmurHash3x64_128First64(byte[] key, int seed) {
            int len = key.length;
            int nblocks = len / 16;

            long h1 = seed & 0xffffffffL;
            long h2 = seed & 0xffffffffL;
            long c1 = 0x87c37b91114253d5L;
            long c2 = 0x4cf5ad432745937fL;

            // Body: process full 16-byte blocks.
            for (int i = 0; i < nblocks; i++) {
                int blockOffset = i * 16;
                long k1 = getBlock64LE(key, blockOffset);
                long k2 = getBlock64LE(key, blockOffset + 8);

                k1 *= c1;
                k1 = rotl64(k1, 31);
                k1 *= c2;
                h1 ^= k1;

                h1 = rotl64(h1, 27);
                h1 += h2;
                h1 = h1 * 5 + 0x52dce729L;

                k2 *= c2;
                k2 = rotl64(k2, 33);
                k2 *= c1;
                h2 ^= k2;

                h2 = rotl64(h2, 31);
                h2 += h1;
                h2 = h2 * 5 + 0x38495ab5L;
            }

            // Tail: mix remaining bytes (0..15) using switch fallthrough.
            long k1 = 0L;
            long k2 = 0L;
            int tailStart = nblocks * 16;
            switch (len & 15) {
                case 15:
                    k2 ^= ((long) key[tailStart + 14] & 0xffL) << 48;
                case 14:
                    k2 ^= ((long) key[tailStart + 13] & 0xffL) << 40;
                case 13:
                    k2 ^= ((long) key[tailStart + 12] & 0xffL) << 32;
                case 12:
                    k2 ^= ((long) key[tailStart + 11] & 0xffL) << 24;
                case 11:
                    k2 ^= ((long) key[tailStart + 10] & 0xffL) << 16;
                case 10:
                    k2 ^= ((long) key[tailStart + 9] & 0xffL) << 8;
                case 9:
                    k2 ^= ((long) key[tailStart + 8] & 0xffL);
                    k2 *= c2;
                    k2 = rotl64(k2, 33);
                    k2 *= c1;
                    h2 ^= k2;
                case 8:
                    k1 ^= ((long) key[tailStart + 7] & 0xffL) << 56;
                case 7:
                    k1 ^= ((long) key[tailStart + 6] & 0xffL) << 48;
                case 6:
                    k1 ^= ((long) key[tailStart + 5] & 0xffL) << 40;
                case 5:
                    k1 ^= ((long) key[tailStart + 4] & 0xffL) << 32;
                case 4:
                    k1 ^= ((long) key[tailStart + 3] & 0xffL) << 24;
                case 3:
                    k1 ^= ((long) key[tailStart + 2] & 0xffL) << 16;
                case 2:
                    k1 ^= ((long) key[tailStart + 1] & 0xffL) << 8;
                case 1:
                    k1 ^= ((long) key[tailStart] & 0xffL);
                    k1 *= c1;
                    k1 = rotl64(k1, 31);
                    k1 *= c2;
                    h1 ^= k1;
                default:
                    break;
            }

            // Finalization: length mix + fmix avalanche.
            h1 ^= len;
            h2 ^= len;
            h1 += h2;
            h2 += h1;
            h1 = fmix64(h1);
            h2 = fmix64(h2);
            h1 += h2;
            return h1;
        }

        private long hash(String item) {
            byte[] bytes = item.getBytes(StandardCharsets.UTF_8);
            // Keep only the first 64 bits, just like the C++ code.
            return murmurHash3x64_128First64(bytes, SEED);
        }

        private int countLeadingZeros(long x) {
            if (x == 0L) {
                return 64 - b + 1;
            }
            return Long.numberOfLeadingZeros(x) + 1;
        }

        void add(String item) {
            long x = hash(item);
            // Top b bits choose the register index.
            int idx = (int) (x >>> (64 - b));
            // Remaining bits encode rho = position of first 1.
            long remainingBits = x << b;
            int rho = countLeadingZeros(remainingBits);
            // Register stores the max rho seen for that bucket.
            if (rho > (registers[idx] & 0xff)) {
                registers[idx] = (byte) rho;
            }
        }

        void merge(HyperLogLog other) {
            if (this.b != other.b) {
                throw new IllegalArgumentException("Cannot merge HLLs with different precision.");
            }
            for (int j = 0; j < m; j++) {
                if ((other.registers[j] & 0xff) > (this.registers[j] & 0xff)) {
                    this.registers[j] = other.registers[j];
                }
            }
        }

        long count() {
            double sum = 0.0;
            int emptyRegisters = 0;

            for (byte v : registers) {
                int val = v & 0xff;
                sum += 1.0 / (1L << val);
                if (val == 0) {
                    emptyRegisters++;
                }
            }

            // Raw HLL estimate via harmonic mean.
            double estimate = alphaM * m * m / sum;

            // Small-range correction (linear counting).
            if (estimate <= 2.5 * m) {
                if (emptyRegisters > 0) {
                    estimate = m * Math.log((double) m / emptyRegisters);
                }
            // Large-range correction (same threshold/form as C++ file).
            } else if (estimate > 4294967296.0 / 30.0) {
                estimate = -4294967296.0 * Math.log(1.0 - (estimate / 4294967296.0));
            }

            return (long) estimate;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java hyperLogLog <input_file>");
            return;
        }

        try (InputStream in = new BufferedInputStream(new FileInputStream(args[0]))) {
            // Same default precision used by the C++ sample.
            HyperLogLog hll = new HyperLogLog(14);
            FastScanner scanner = new FastScanner(in);

            String item;
            while ((item = scanner.next()) != null) {
                hll.add(item);
            }

            long estimatedVal = hll.count();
            System.out.println("Estimated Cardinality: " + estimatedVal);
        }
    }
}
