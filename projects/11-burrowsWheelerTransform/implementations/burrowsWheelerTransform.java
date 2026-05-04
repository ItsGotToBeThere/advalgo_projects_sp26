import java.util.Scanner;

/**
 * The purpose of Burrows-Wheeler Transform (BWT) is to rearrange a string in a format that groups 
 * similar characters together in a reversible manner, in order to help optimize certain 
 * compression algorithms. 
 *
 * This particular implementation is in O(nlogn) time, and works by building a suffix array 
 * using a cyclic shift algorithm. 
 * 
 * See the Python implementation for a summary and explanation of the algorithm. 
 */
public class burrowsWheelerTransform {

    /**
     * Builds the suffix array of s.
     *
     * Sorts suffixes by repeatedly sorting cyclic substrings, with doubling lengths each
     * round until the full length of the string is covered
     * 
     */
    static int[] buildSuffixArray(String s) {
        
        int n = s.length();
        int alphabet = 256;     // number of possible characters

        int[] p = new int[n];   // holds the sorted order of starting indices
        int[] c = new int[n];   // holds the equivalence class for each index
        int[] cnt = new int[alphabet]; // holds the count of each character


        // Simply sort the indices by their characters:
        // First, count occurrences of each character
        for (int i = 0; i < n; i++) {
            cnt[s.charAt(i)]++;
        }
        // Next, convert counts into prefix sums so cnt[i] tells us how many 
        // characters have value <= i
        for (int i = 1; i < alphabet; i++) {
            cnt[i] += cnt[i - 1];
        }
        // Lastly, place each index into its sorted position based on its character
        for (int i = n - 1; i >= 0; i--) {
            p[--cnt[s.charAt(i)]] = i;
        }


        // Assign equivalence classes for single-character substrings:
        // Give the first index in sorted order class 0 and start the class count
        int classes = 1;
        c[p[0]] = 0;
        // Walk p in order; whenever the character changes, open a new class
        for (int i = 1; i < n; i++) {
            if (s.charAt(p[i]) != s.charAt(p[i - 1])) {
                classes++;
            }
            c[p[i]] = classes - 1;
        }


        // Double substring length each round until sorted order is for full suffixes:
        int[] pn = new int[n];   // shifted starts used when sorting by the first half
        int[] cn = new int[n];   // class labels for the new doubled length

        for (int h = 0; (1 << h) < n; h++) {
            int half = 1 << h;

            // Extend each sorted piece by half a character to the left (cyclic)
            // Shift every start in p left by half, wrapping negative indices with n
            for (int i = 0; i < n; i++) {
                pn[i] = p[i] - half;
                if (pn[i] < 0) {
                    pn[i] += n;
                }
            }

            // Counting sort the shifted starts by their first-half class (c[pn[i]]):
            // This is similar to the first step of the algorithm, but we are 
            // now sorting by the first half of the substring rather than by the character
            // Recount the number of starts in each class
            for (int i = 0; i < classes; i++) {
                cnt[i] = 0;
            }
            for (int i = 0; i < n; i++) {
                cnt[c[pn[i]]]++;
            }
            // Convert counts into prefix sums for slot positions
            for (int i = 1; i < classes; i++) {
                cnt[i] += cnt[i - 1];
            }
            // Refill p from right to left so ties on the first half keep prior order
            for (int i = n - 1; i >= 0; i--) {
                p[--cnt[c[pn[i]]]] = pn[i];
            }

            // Recompute classes for substrings twice as long:
            // Reset the leader in p to class 0 and the running class count
            cn[p[0]] = 0;
            classes = 1;
            // Compare neighbors in p by (class at start, class at start + half)
            for (int i = 1; i < n; i++) {
                int curFirst = c[p[i]];
                int curSecond = c[(p[i] + half) % n];
                int prevFirst = c[p[i - 1]];
                int prevSecond = c[(p[i - 1] + half) % n];

                if (curFirst != prevFirst || curSecond != prevSecond) {
                    classes++;
                }
                cn[p[i]] = classes - 1;
            }
            // Swap c and cn so the next round uses the updated classes
            int[] tmp = c;
            c = cn;
            cn = tmp;
        }

        return p;
    }

    /**
     * Returns the Burrows-Wheeler transform of s.
     *
     * Appends '$', builds the suffix array, then reads off the cyclic predecessor of each
     * suffix in sorted order to form the output string.
     */
    static String bwt(String s) {
        if (s.indexOf('$') != -1) {
            throw new IllegalArgumentException("Input string cannot contain '$'");
        }


        // Build the transformed string from the suffix array:
        // Append the sentinel and record the new length
        s += '$';
        int n = s.length();
        int[] suffixArray = buildSuffixArray(s);
        // Collect the character before each suffix start in sorted order (wrap at 0)
        StringBuilder result = new StringBuilder(n);
        for (int start : suffixArray) {
            result.append(s.charAt((start - 1 + n) % n));
        }

        return result.toString();
    }

    public static void main(String[] args) {
        // Read once from stdin and print its BWT
        Scanner scanner = new Scanner(System.in);
        String s = scanner.next();
        System.out.print(bwt(s));
        scanner.close();
    }
}
