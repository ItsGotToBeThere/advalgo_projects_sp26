import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class BloomFilter<T> {
    private final BitSet bits; // stores the bits
    private final int size; // length of the BitSet
    private final int k; // number of rounds of hashing

    // constructor with bounds for validity
    public BloomFilter(int size, int k) {
        if (size > 0) {
            this.size = size;
        } else {
            this.size = 1;
        }

        if (k > 0) {
            this.k = k;
        } else {
            this.k = 1;
        }

        this.bits = new BitSet(this.size);
    }


    // common method of hashing in Bloom Filter is to just simulate k hashes by running this function k times with different i values
    // Instead of using k independent hash functions, we derive them from:
    // h_i(x) = h1(x) + i * h2(x)
    // standard for Bloom filters and avoids the cost of multiple hashes
    private int hash(T item, int i) {
        int h1 = item.hashCode();
        int h2 = Integer.rotateRight(h1, 16); // mix bits for second hash

        long combined = (long) h1 + (long) i * h2;

        // floorMod ensures a non-negative index even if hash is negative
        return (int) Math.floorMod(combined, size);
    }


    // inserts an item into the filter.
    // Sets k different bit positions determined by the hash function.
    public void insert(T item) {
        for (int i = 0; i < k; i++) {
            int index = hash(item, i); // find the hash then set the index accordingly
            bits.set(index);
        }
    }


    // check if an item is either possibly present or definitely not present
    // you can have false positives if other elements coincidentally set all bits for another element
    // but never false negatives
    public boolean query(T item) {
        for (int i = 0; i < k; i++) {
            int index = hash(item, i); // find the hash
            if (!bits.get(index)) {
                return false; // definitely not present
            }
        }
        return true; // possibly present
    }

}




class Main {

    public static void main(String[] args) throws FileNotFoundException {
        Scanner sc;

        if (args.length > 0) {
            sc = new Scanner(new File(args[0]));
        } else {
            sc = new Scanner(System.in);
        }

        int testCases = Integer.parseInt(sc.nextLine().trim());

        for (int t = 0; t < testCases; t++) {
            String line = sc.nextLine().trim();
            while (line.isEmpty()) {
                line = sc.nextLine().trim();
            }

            String[] parts = line.split("\\s+");
            int n = Integer.parseInt(parts[0]); // expected distinct elements for sizing
            double p = Double.parseDouble(parts[1]); // target false positive rate
            int insertCount = Integer.parseInt(parts[2]);
            int q = Integer.parseInt(parts[3]);

            int m = computeM(n, p);
            int k = computeK(n, m);

            BloomFilter<String> bf = new BloomFilter<>(m, k);
            Set<String> inserted = new HashSet<>();

            for (int i = 0; i < insertCount; i++) {
                String word = sc.nextLine().trim();
                bf.insert(word);
                inserted.add(word);
            }

            String separator = sc.nextLine().trim(); // "---"

            System.out.println("m=" + m + " k=" + k + " n=" + n + " p=" +
                    String.format(Locale.US, "%.2f", p));

            for (int i = 0; i < q; i++) {
                String query = sc.nextLine().trim();
                String result;

                if (inserted.contains(query)) {
                    result = "member";
                } else if (bf.query(query)) {
                    result = "false_positive";
                } else {
                    result = "absent";
                }

                System.out.println(query + "\t" + result);
            }

            if (t < testCases - 1) {
                System.out.println();
            }
        }

        sc.close();
    }

    // m = floor( -(n * ln(p)) / (ln(2)^2) )
    private static int computeM(int n, double p) {
        if (n <= 0 || p <= 0 || p >= 1) {
            return 1;
        }

        double value = -(n * Math.log(p)) / (Math.pow(Math.log(2), 2));
        return Math.max(1, (int) value);
    }

    // k = round((m / n) * ln(2))
    private static int computeK(int n, int m) {
        if (n <= 0) {
            return 1;
        }

        int k = (int) ((m / (double) n) * Math.log(2));
        return Math.max(1, k);
    }
}
