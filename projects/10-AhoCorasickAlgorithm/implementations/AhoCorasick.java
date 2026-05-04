import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class AhoCorasick {
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int numberOfWords = Integer.parseInt(scanner.nextLine().trim());
        HashMap<String, Integer> wordMap = new HashMap<>();

        AhoCorasickTrie trie = new AhoCorasickTrie();

        for (int i = 0; i < numberOfWords; i++) {
            String word = scanner.nextLine();
            trie.addString(word);
            wordMap.put(word, i);
        }

        String text = scanner.nextLine();

        // Iterate over each character in the input text, then goes through the trie given the current vertex v and character,
        // updating v and then check if the current vertex is terminal. If it is terminal, we print out the word's ID
        // and starting index. However, we continue to follow any suffix links to find every completed word ending at
        // that position as well before continuing to the next character.
        int v = 0;
        for (int i = 0; i < text.length(); i++) {
            v = trie.go(v, text.charAt(i));
            int u = v;
            while (u != 0) {
                if (trie.isOutput(u)) {
                    String word = trie.getWord(u);
                    int index = i - word.length() + 1;
                    System.out.printf("%d %d\n", wordMap.get(word), index);
                }
                u = trie.getLink(u);
            }
        }
    }
}

class AhoCorasickTrie {
    final private ArrayList<Vertex> trie;

    public AhoCorasickTrie() {
        trie = new ArrayList<>();
        trie.add(new Vertex());
    }

    // Adds a String s to the trie by iterating over each char ch in s and creating a new vertex
    // if the current vertex's next[ch] does not have a transition yet, otherwise continue down the trie.
    // At the last vertex, we set the output member variable to true to indicate that the vertex is a terminating state
    // in the DFA. We also set the word member variable to s at the terminal vertex so that we can retrieve what
    // pattern was matched
    public void addString(String s) {
        int v = 0;
        for (char ch : s.toCharArray()) {
            if (!trie.get(v).next.containsKey(ch)) {
                trie.get(v).next.put(ch, trie.size());
                trie.add(new Vertex(v, ch));
            }
            v = trie.get(v).next.get(ch);
        }
        trie.get(v).output = true;
        trie.get(v).word = s;
    }

    // Computes the link for a vertex and memoizes it if it does not exist. If the vertex is the
    // root (v == 0) or a child of the root (vertex.p == 0), then the suffix link will always be the root.
    public int getLink(int v) {
        Vertex vertex = trie.get(v);
        if (vertex.link == -1) {
            if (v == 0 || vertex.p == 0)
                vertex.link = 0;
            else
                vertex.link = go(getLink(vertex.p), vertex.pch);
        }
        return vertex.link;
    }

    // Computes the transitions for the DFA. If the normal trie edge exists, then use it. Otherwise, if you are at
    // the root with no edge, stay at the root or follow the suffix link and retry
    public int go(int v, char ch) {
        Vertex vertex = trie.get(v);
        if (!vertex.go.containsKey(ch)) {
            if (vertex.next.containsKey(ch))
                vertex.go.put(ch, vertex.next.get(ch));
            else
                vertex.go.put(ch, v == 0 ? 0 : go(getLink(v), ch));
        }
        return vertex.go.get(ch);
    }

    public boolean isOutput(int vertex) {
        return trie.get(vertex).output;
    }

    public String getWord(int vertex) {
        return trie.get(vertex).word;
    }

    static class Vertex {
        // The next map stores which characters can be used to transition from the current vertex.
        // If next does not contain a character, then no transition exists using that character, otherwise it is the index used
        // to get the next vertex in the trie using trie.get(next.get(character)). Used for constructing the actual transitions
        // in the DFA
        HashMap<Character, Integer> next = new HashMap<>();
        // output is a flag to mark if the vertex is a terminating state in the DFA or not.
        boolean output = false;
        // p is the index of the parent vertex in the trie
        int p = -1;
        // pch is the character used to transition from the parent vertex to this vertex
        char pch;
        // link is the index to the vertex with the longest proper suffix when the next character does not match any
        // other transitions
        int link = -1;
        // Stores the actual transitions for a given character.
        HashMap<Character, Integer> go = new HashMap<>();
        // Stores the word being added to the trie but is only non-null if the vertex is terminal
        String word = null;

        // Creates a vertex where the parent vertex index is -1, and the character to transition to this vertex from the
        // parent is a sentinel value '$'.
        public Vertex() {
            this(-1, '$');
        }

        public Vertex(int p, char ch) {
            this.p = p;
            this.pch = ch;
        }
    }
}
