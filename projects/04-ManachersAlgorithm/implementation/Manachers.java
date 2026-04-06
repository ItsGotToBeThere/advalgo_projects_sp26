import java.util.Scanner;

public class Manachers {

    static class Manacher {
        String s;
        int[] palindromes;

        // prepare the string for Manacher's algorithm
        // add @ to beginning and $ to end
        // add # before all chars and after last char so all palindromes become odd-length
        // create array of size s to store palindrome radius from each center
        void init(String inS) {
            StringBuilder sb = new StringBuilder();
            sb.append("@");

            for (char c : inS.toCharArray()) {
                sb.append("#");
                sb.append(c);
            }

            sb.append("#$");
            s = sb.toString();

            palindromes = new int[s.length()];
        }

        // run Manacher's algorithm
        // returns longest palindromic substring in O(n) time
        String runManacher() {

            // l and r store the bounds of the right-most extending palindrome found so far
            int l = 0;
            int r = 0;

            int n = s.length();
            int longest = -1;

            for (int i = 1; i < n - 1; i++) {

                // mirror of i around the current palindrome center
                int mirror = l + r - i;

                // if i is inside the current right-most palindrome,
                // initialize its radius using the mirrored value or distance to r
                if (i < r) {
                    palindromes[i] = Math.min(r - i, palindromes[mirror]);
                }

                // while the palindrome centered at i can still expand, increase its radius
                while (s.charAt(i + 1 + palindromes[i]) == s.charAt(i - 1 - palindromes[i])) {
                    palindromes[i]++;
                }

                // if this palindrome extends past the previous right boundary,
                // update l and r to represent this new right-most palindrome
                if (i + palindromes[i] > r) {
                    l = i - palindromes[i];
                    r = i + palindromes[i];
                }

                // if the current palindrome is longer than the previous longest one, update longest
                if (longest == -1 || palindromes[i] > palindromes[longest]) {
                    longest = i;
                }
            }

            // the longest palindrome is centered at "longest"
            // it begins at longest - palindromes[longest]
            // and ends at longest + palindromes[longest]
            // then remove filler chars #, @, and $
            StringBuilder toRet = new StringBuilder();

            for (int j = longest - palindromes[longest]; j < longest + palindromes[longest] + 1; j++) {
                char c = s.charAt(j);
                if (c != '#' && c != '@' && c != '$') {
                    toRet.append(c);
                }
            }

            return toRet.toString();
        }
    }

    // take string from input and return longest palindromic substring
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        String x = sc.nextLine();

        Manacher m = new Manacher();
        m.init(x);

        System.out.println(m.runManacher());

        sc.close();
    }
}