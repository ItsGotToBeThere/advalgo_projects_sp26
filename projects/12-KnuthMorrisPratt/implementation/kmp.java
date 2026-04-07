public class kmp {
    private String text;
    private String pattern;

    public kmp(String text, String pattern) {
        this.text = text;
        this.pattern = pattern;
    }

    // builds the table array to know how far to fall back on a mismatch
    public int[] buildTable() {
        int m = pattern.length();
        int[] table = new int[m + 1]; // table needs length m + 1 since search uses T[length(W)]

        int pos = 1; // the current position we are computing in the table
        int cnd = 0; // the zero-based index of the next character of the current candidate substring

        table[0] = -1;

        // loop through the pattern to fill in each table value
        while (pos < m) {
            if (pattern.charAt(pos) == pattern.charAt(cnd)) {
                // characters match, so inherit the fallback from the candidate
                table[pos] = table[cnd];
            } else {
                // mismatch, store current candidate length and fall back as needed
                table[pos] = cnd;
                while (cnd >= 0 && pattern.charAt(pos) != pattern.charAt(cnd)) {
                    cnd = table[cnd];
                }
            }
            pos++;
            cnd++;
        }

        // only needed when all word occurrences are searched
        table[pos] = cnd;

        return table;
    }

    // searches for all occurrences of pattern in text and returns their starting indices
    public java.util.List<Integer> search() {
        java.util.List<Integer> matches = new java.util.ArrayList<>();

        if (pattern.length() == 0 || text.length() == 0) {
            return matches;
        }

        int n = text.length();
        int m = pattern.length();
        int[] table = buildTable();

        int i = 0; // index for text
        int j = 0; // index for pattern

        // scan through the entire text with i while j tracks our position in the pattern
        // if characters match we advance both if they don't we use the table to fall back j without moving i backwards
        // once j reaches m we found a full match and reset j using the table to keep searching for more matches without starting over from the beginning.
        while (i < n) {
            if (pattern.charAt(j) == text.charAt(i)) {
                // characters match increase both indicies
                i++;
                j++;

                if (j == m) {
                    // full pattern matched, record the starting index
                    matches.add(i - j);
                    // use table to look for the next match
                    j = table[j];
                }
            } else {
                // mismatch after some matches use table to avoid redundant comparisons
                j = table[j];
                if (j < 0) {
                    // mismatch at the start of pattern just move text pointer forward
                    i++;
                    j++;
                }
            }
        }

        return matches;
    }

    // parsing code to handle input which will be the text on the first line, and the pattern on the second line
    public static void main(String[] args) throws Exception {
        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
        String text = br.readLine();
        String pattern = br.readLine();
        if (text == null) text = "";
        if (pattern == null) pattern = "";

        kmp solver = new kmp(text, pattern);
        java.util.List<Integer> matches = solver.search();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matches.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(matches.get(i));
        }
        System.out.println(sb.toString());
    }
}