import java.util.Scanner;

public class dna {

    public static void main(String[] args) {
        // get input
        Scanner sc = new Scanner(System.in);
        int maxLength = sc.nextInt();
        String seq = sc.next();
        sc.close();

        // run modified Manacher's algorithm
        DnaManacher m = new DnaManacher();
        m.init(seq);
        m.runModifiedManacher();

        // find the furthest we can keep from the input string
        int maxReach = -1;
        int maxReachCenter = -1;

        // only consider centers that would fit in the max length of the output sequence
        // convert max length of folded DNA sequence to max length of unfolded output sequence
        // only consider centers in between letters (odd indices)
        int maxIndex = Math.min(2 * maxLength + 1, m.s.length() - 2); 
        for (int i = 1; i <= maxIndex; i += 2) {
            // calculate the reach of the palindrome centered at i
            int reach = i + m.palindromes[i];

            // update max reach and center if this palindrome reaches further than any we've seen before
            if (reach > maxReach) {
                maxReach = reach;
                maxReachCenter = i;
            }
        }

        // build the output string
        // start with the characters until the center of the palindrome that reaches furthest
        StringBuilder output = new StringBuilder();
        for (int i = 2; i <= maxReachCenter; i += 2) {
            output.append(m.s.charAt(i));
        }

        // then complete genomic palindrome by adding the complements of the characters 
        // in reverse order until we reach the beginning of the string
        for (int i = maxReachCenter - 1; i > 0; i -= 2) {
            char c = m.s.charAt(i);
            if (c == 'A') output.append('T');
            else if (c == 'T') output.append('A');
            else if (c == 'C') output.append('G');
            else if (c == 'G') output.append('C');
        }
        
        System.out.println(output.toString());
    }

    static class DnaManacher {
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

        // run modified Manacher's algorithm to find genomically palindromic substrings in the DNA sequence
        void runModifiedManacher() {
            int l = 0;
            int r = 0;
            int n = s.length();

            // only consider centers in between letters (odd indices)
            for (int i = 1; i < n - 1; i += 2) { 

                // initialize palindrome radius using mirror logic
                int mirror = l + r - i;
                if (i < r) {
                    palindromes[i] = Math.min(r - i, palindromes[mirror]);
                }

                // while the palindrome centered at i can still expand, increase its radius
                // use isComplement logic for DNA sequences
                while (isComplement(s.charAt(i + 1 + palindromes[i]), s.charAt(i - 1 - palindromes[i]))) {
                    palindromes[i]++;
                }

                // update l and r to represent right-most palindrome found so far
                if (i + palindromes[i] > r) {
                    l = i - palindromes[i];
                    r = i + palindromes[i];
                }
            }
        }

        // check if two characters are complements in DNA sequences
        boolean isComplement(char a, char b) {
            return (a == '#' && b == '#') || 
                (a == 'A' && b == 'T') || (a == 'T' && b == 'A') || 
                (a == 'C' && b == 'G') || (a == 'G' && b == 'C');
        }
    }
}