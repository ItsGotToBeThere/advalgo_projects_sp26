import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class pcSol_java {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        int numOfBannedWords = Integer.parseInt(st.nextToken());
        int lineWidth = Integer.parseInt(st.nextToken());
        int linesPerPost = Integer.parseInt(st.nextToken());
        int numOfPosts = Integer.parseInt(st.nextToken());

        String[] bannedWords = new String[numOfBannedWords];

        for (int i = 0; i < numOfBannedWords; i++) {
            bannedWords[i] = br.readLine();
        }

        AhoCorasickAutomata automata = new AhoCorasickAutomata(bannedWords);

        for (int p = 0; p < numOfPosts; p++) {
            char[][] postMatrix = new char[linesPerPost][lineWidth];

            for (char[] line : postMatrix) {
                Arrays.fill(line, ' ');
            }

            for (int i = 0; i < linesPerPost; i++) {
                String line = br.readLine();
                System.arraycopy(line.toCharArray(), 0, postMatrix[i], 0, line.length());
            }

            List<int[]> matches = automata.searchMatrix(postMatrix);

            // based on the matches found in postMatrix, censor given the following row and column ranges
            for (int[] match : matches) {
                int startRow = match[0];
                int startCol = match[1];
                int endRow = match[2];
                int endCol = match[3];

                if (startRow == endRow) {
                    for (int col = startCol; col <= endCol; col++) {
                        postMatrix[startRow][col] = '*';
                    }
                } else {
                    for (int row = startRow; row <= endRow; row++) {
                        postMatrix[row][startCol] = '*';
                    }
                }
            }

            StringBuilder sb = new StringBuilder();
            for (char[] line : postMatrix) {
                sb.append(new String(line)).append('\n');
            }
            System.out.println(sb);
        }
    }
}

// This implementation of AhoCorasick uses arrays instead of Vertex objects to improve performance
class AhoCorasickAutomata {
    // Next represents the transitions of the trie where next[state][character] = nextState
    int[][] next;
    // Fail represents the transitions to a new state if no transition exists at the current state
    // it is the suffix links in cp-algorithms where fail[state] = newState
    int[] fail;
    // Output represents which states are terminal with a non-zero value represents
    // the maximum pattern length. This variable can be extended to store several things such as
    // all strings matched, their length, and their indices depending on the problem
    int[] output;
    // Used to keep track of the current size of the trie and also used when adding new nodes
    int size;

    public AhoCorasickAutomata(String[] words) {
        // The problem is bounded by 100 words with 80 characters horizontally
        // and 10 characters vertically, therefore the worst case trie size is 100 * 80 + 1 root node
        int maxNodes = 8001;
        // next[] have a size of 26 since we are bounding the problem to the English alphabet
        next = new int[maxNodes][26];
        fail = new int[maxNodes];
        output = new int[maxNodes];
        size = 0;

        buildTrie(words);
        buildFailureLinks();
    }

    public List<int[]> search(String text) {
        // Stores the start and indices of each found word
        List<int[]> results = new ArrayList<>();
        int currentNode = 0;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            // Reset if a non-alphabet character is encountered
            if (ch < 'a' || ch > 'z') {
                currentNode = 0;
                continue;
            }

            // Transition to the next state
            currentNode = next[currentNode][ch - 'a'];

            // Check if the current state is a terminal state
            // and, if true,
            if (output[currentNode] > 0) {
                int start = i - output[currentNode] + 1;
                results.add(new int[]{start, i});
            }
        }

        return results;
    }

    public List<int[]> searchMatrix(char[][] matrix) {
        // results stores every instance of a found banned character sequence in the form of
        // {startRow, startCol, endRow, endCol}
        List<int[]> results = new ArrayList<>();
        int rows = matrix.length;
        int cols = matrix[0].length;

        // iterates through the matrix left to right
        for (int row = 0; row < rows; row++) {
            String line = new String(matrix[row]);
            for (int[] match: search(line)) {
                results.add(new int[]{row, match[0], row, match[1]});
            }
        }

        // iterates through the matrix right to left where each row is reversed
        for (int row = 0; row < rows; row++) {
            String line = new String(matrix[row]);
            String reversed = new StringBuilder(line).reverse().toString();
            for (int[] match: search(reversed)) {
                int startCol = cols - 1 - match[1];
                int endCol = cols - 1 - match[0];
                results.add(new int[]{row, startCol, row, endCol});
            }
        }

        // iterates through the matrix top to bottom
        for (int col = 0; col < cols; col++) {
            StringBuilder sb = new StringBuilder();
            for (char[] chars : matrix) sb.append(chars[col]);
            for (int[] match: search(sb.toString())) {
                results.add(new int[]{match[0], col, match[1], col});
            }
        }

        // iterates through the matrix bottom to top where each column is reversed
        for (int col = 0; col < cols; col++) {
            StringBuilder sb = new StringBuilder();
            for (char[] chars : matrix) sb.append(chars[col]);
            String reversed = sb.reverse().toString();
            for (int[] match: search(reversed)) {
                int startRow = rows - 1 - match[1];
                int endRow = rows - 1 - match[0];
                results.add(new int[]{startRow, col, endRow, col});
            }
        }

        return results;
    }

    private void buildTrie(String[] words) {
        // creates the root node and initially sets all the transitions for the root to -1, nonexistent
        newNode();
        // iterate through each word to build the trie
        for (String word : words) {
            int cur = 0;
            // iterate through each character of the word to build the nodes and transitions
            for (char ch : word.toCharArray()) {
                int c = ch - 'a';
                // check if the transition of the current node with the character c exists
                // if it does not, create a new node; otherwise get the next node following the transition c
                if (next[cur][c] == -1)
                    next[cur][c] = newNode();
                cur = next[cur][c];
            }
            // after iterating through all the characters in word, set the value of output at the current node
            // to the max of what is already at output[currentNode] or the word's length. Since this problem
            // hinges on censoring a port of text, we store whichever is longer
            output[cur] = Math.max(output[cur], word.length());
        }
    }

    private void buildFailureLinks() {
        Queue<Integer> queue = new ArrayDeque<>();

        // start with the root's children
        for (int i = 0; i < 26; i++) {
            // if the root node has any -1 left for unused letters, create transitions with those letters to
            // go back to the root. Otherwise, for each direct child of the root, create failure links that go
            // back to the root and add them to the queue for a breadth-first processing
            if (next[0][i] == -1) {
                next[0][i] = 0;
            } else {
                fail[next[0][i]] = 0;
                queue.add(next[0][i]);
            }
        }

        while (!queue.isEmpty()) {
            int cur = queue.poll();
            // propagates the longest pattern from the failure link into cur
            output[cur] = Math.max(output[cur], output[fail[cur]]);

            for (int i = 0; i < 26; i++) {
                // if no transition exists for the current node, copy the failure link from fail[cur]'s row.
                // Since fail[cur] was preprocessed, it's entire next row is complete so it always produces a valid
                // state. Otherwise, compute the failure link of next[cur][i] by following the failure link and
                // taking the i transition from there and enqueue its child for processing
                if (next[cur][i] == -1) {
                    next[cur][i] = next[fail[cur]][i];
                } else {
                    fail[next[cur][i]] = next[fail[cur]][i];
                    queue.add(next[cur][i]);
                }
            }
        }
    }

    // creates a new node to add to the trie
    private int newNode() {
        // initially set all the transitions for this node to be -1, meaning it has no valid transitions yet
        next[size] = new int[26];
        Arrays.fill(next[size], -1);
        // initially set the failure link to go back to the root
        fail[size] = 0;
        // Initially set the node to not be terminal
        output[size] = 0;
        return size++;
    }
}
