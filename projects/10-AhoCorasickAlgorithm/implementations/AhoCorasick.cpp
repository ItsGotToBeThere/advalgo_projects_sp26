#include <algorithm>
#include <iostream>
#include <sstream>
#include <string>
#include <unordered_map>
#include <vector>

using namespace std;

class AhoCorasickTrie {
public:
    AhoCorasickTrie() {
        trie.push_back(Vertex());
    }

    // Adds a String s to the trie by iterating over each char ch in s and creating a new vertex
    // if the current vertex's next[ch] does not have a transition yet, otherwise continue down the trie.
    // At the last vertex, we set the output member variable to true to indicate that the vertex is a terminating state
    // in the DFA. We also set the word member variable to s at the terminal vertex so that we can retrieve what
    // pattern was matched
    void addString(const string& s) {
        int v = 0;
        for (char ch : s) {
            unsigned char c = static_cast<unsigned char>(ch);
            if (!trie[v].next.count(c)) {
                trie[v].next[c] = static_cast<int>(trie.size());
                trie.push_back(Vertex(v, ch));
            }
            v = trie[v].next[c];
        }
        trie[v].output = true;
        trie[v].word = s;
    }

    // Computes the link for a vertex and memoizes it if it does not exist. If the vertex is the
    // root (v == 0) or a child of the root (vertex.p == 0), then the suffix link will always be the root.
    int getLink(int v) {
        Vertex& vertex = trie[v];
        if (vertex.link == -1) {
            if (v == 0 || vertex.p == 0) {
                vertex.link = 0;
            } else {
                vertex.link = go(getLink(vertex.p), vertex.pch);
            }
        }
        return vertex.link;
    }

    // Computes the transitions for the DFA. If the normal trie edge exists, then use it. Otherwise, if you are at
    // the root with no edge, stay at the root or follow the suffix link and retry
    int go(int v, char ch) {
        Vertex& vertex = trie[v];
        unsigned char c = static_cast<unsigned char>(ch);

        if (!vertex.go.count(c)) {
            if (vertex.next.count(c)) {
                vertex.go[c] = vertex.next[c];
            } else {
                vertex.go[c] = v == 0 ? 0 : go(getLink(v), ch);
            }
        }
        return vertex.go[c];
    }

    bool isOutput(int vertex) const {
        return trie[vertex].output;
    }

    string getWord(int vertex) const {
        return trie[vertex].word;
    }

private:
    struct Vertex {
        // The next map stores which characters can be used to transition from the current vertex.
        // If next does not contain a character, then no transition exists using that character, otherwise it is the index used
        // to get the next vertex in the trie using trie.get(next.get(character)). Used for constructing the actual transitions
        // in the DFA
        unordered_map<unsigned char, int> next;
        // output is a flag to mark if the vertex is a terminating state in the DFA or not.
        bool output = false;
        // p is the index of the parent vertex in the trie
        int p = -1;
        // pch is the character used to transition from the parent vertex to this vertex
        char pch;
        // link is the index to the vertex with the longest proper suffix when the next character does not match any
        // other transitions
        int link = -1;
        // Stores the actual transitions for a given character.
        unordered_map<unsigned char, int> go;
        // Stores the word being added to the trie but is only non-null if the vertex is terminal
        string word;

        // Creates a vertex where the parent vertex index is -1, and the character to transition to this vertex from the
        // parent is a sentinel value '$'.
        Vertex() : Vertex(-1, '$') {}

        Vertex(int parent, char ch) : p(parent), pch(ch) {}
    };

    vector<Vertex> trie;
};

int main() {
    string firstLine;
    if (!getline(cin, firstLine)) {
        return 0;
    }
    istringstream header(firstLine);
    int numberOfWords;
    header >> numberOfWords;

    unordered_map<string, int> wordMap;
    AhoCorasickTrie trie;

    for (int i = 0; i < numberOfWords; i++) {
        string word;
        getline(cin, word);
        trie.addString(word);
        wordMap[word] = i;
    }

    string text;
    getline(cin, text);

    // Iterate over each character in the input text, then goes through the trie given the current vertex v and character,
    // updating v and then check if the current vertex is terminal. If it is terminal, we print out the word's ID
    // and starting index. However, we continue to follow any suffix links to find every completed word ending at
    // that position as well before continuing to the next character.
    int v = 0;
    for (int i = 0; i < static_cast<int>(text.length()); i++) {
        v = trie.go(v, text[i]);
        int u = v;
        while (u != 0) {
            if (trie.isOutput(u)) {
                string word = trie.getWord(u);
                int index = i - static_cast<int>(word.length()) + 1;
                cout << wordMap[word] << ' ' << index << '\n';
            }
            u = trie.getLink(u);
        }
    }

    return 0;
}
