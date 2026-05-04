import java.util.Scanner;

public class rabinKarp {

    // The base we choose to use for our numbering system.
    // Since we are working with characters, we use 256, the number of distinct ASCII chars.
    public static final int d = 256;

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);

        // Safely read the text from input
        if(!scanner.hasNextLine()) return;
        String text = scanner.nextLine();

        // Safely read the pattern from input
        if(!scanner.hasNextLine()) return;
        String pattern = scanner.nextLine();

        // Prime number used for modulo arithmetic.
        // This prevents integer overflows.
        int q = 101;

        // Execute the search, and output (print) the result.
        int result = search(pattern, text, q);
        System.out.println(result);

        scanner.close();
    }

    // This method takes in a string 'txt' and tries to find an inputted pattern 'ptrn' inside it.
    // It returns the first index of the first occurrence of the pattern in the text.
    // If no occurrence is found, it returns -1.
    static int search(String ptrn, String txt, int q){

        // Start by grabbing the lengths of the text and the pattern to find.
        // These will be used to construct our iterators.
        int m = ptrn.length();
        int n = txt.length();

        // If the pattern is longer than the text string, there is no way to find it inside.
        // Return false case (-1).
        if (m > n) return -1;

        // Initialize variables to hold the pattern and text window hashes, and a place value multiplier.
        int ptrnHash = 0;
        int txtHash = 0;
        int h = 1;

        // Calculate h = d^(m-1), which gives us the place value multiplier for the leftmost character in the window.
        for (int i = 0; i < m - 1; i++){
            h = (h * d) % q;
        }

        // Calculate the hash value of the pattern and the starting window of text.
        // hash = base * hash + character, for every character in the string.
        // Through this formula, we build the hash digits place by digits place.
        for (int i = 0; i < m; i++){
            ptrnHash = (d * ptrnHash + ptrn.charAt(i)) % q;
            txtHash = (d * txtHash + txt.charAt(i)) % q;
        }

        // Slide the window over the text, one character by one.
        // 'i' denotes the index of the start of the window.
        for (int i = 0; i <= n - m; i++){
            if (ptrnHash == txtHash){
                int j;
                // If hashes match, verify the literal string match character by character.
                // This rules out hash collisions.
                for (j = 0; j < m; j++){
                    if (txt.charAt(i + j) != ptrn.charAt(j)){
                        break;
                    }
                }

                // If j reached the length of the pattern, the match is perfect.
                // Return the first index of the match.
                // We return only the first index of the first match for consistency with the algorithm defined in the wiki.
                // Alternative implementations return the first index of all matches.
                if (j == m){
                    return i;
                }
            }

            // If the window has not yet reached the end of the text, we can slide it over one index and check again.
            // To do this, we first recalculate the hash for the next window.
            // This is done by removing the calculated addition to the hash for the first digit, then adding the value of the incoming one.
            // Mathematically, This removes the leading place value of the hash, slides the remaining digits left one, then adds the new least significant digit.
            if (i < n - m){
                txtHash = (d * (txtHash - txt.charAt(i) * h) + txt.charAt(i + m)) % q;

                // If the hash value is negative, make it positive.
                // This is equivalent to a single modulo operation.
                if (txtHash < 0){
                    txtHash = (txtHash + q);
                }
            }
        }
        // Pattern not found; return -1.
        return -1;
    }
}
