import sys


# Aho-Corasick algorithm implementation in Python
# Aho-Corasick is a string searching algorithm that efficiently finds all
# occurrences of a set of patterns in a text. It builds a trie of the patterns
# and uses failure links to allow for fast transitions when a mismatch occurs.


# To implement an initial trie, we define a vertex and what it should contain.
# We have a map of next vertices, an output boolean to mark if this vertex is the end of a pattern,
# a parent index and character to keep track of the trie structure, a link for failure transitions,
# and a go map for memoized transitions.
class Vertex:
    def __init__(self, p=-1, pch="$"):
        self.next = {}  # trie edges to children
        self.output = False
        self.p = p  # parent index
        self.pch = pch  # character from parent to this node
        self.link = -1  # suffix/fail link (lazy)
        self.go = {}  # memoized transition function
        self.word = None


# The root vertex is created and added to the trie. Since the root has no parent,
# we can leave its parent index and character as default values.
t = [Vertex()]  # trie, root at index 0


# To add a string to the trie, we start at the root and for each character in the string,
# we check if there is already a vertex for that character. If not, we create a new
# vertex and link it to the current vertex. Finally, we mark the last vertex as an output AKA, the accept state.
# and store the full word there so we can print the match position later.
def add_string(s: str) -> None:
    v = 0
    # for every character in string
    for ch in s:
        # if we havent already made a vertex for this character, make one
        if ch not in t[v].next:
            # next[ch] is index of the next vertex we reach with character ch
            t[v].next[ch] = len(t)
            t.append(Vertex(p=v, pch=ch))
        # setting v to the next vertex in the trie
        v = t[v].next[ch]
    # this marks the final vertex of the string as an accept state
    t[v].output = True
    t[v].word = s


# failure link, gets the longest suffix of the current vertex that is also a
# prefix for some other pattern
# This is also where the lazy calculation of failure links starts and also
# what sets this trie apart from a normal trie. We only calculate the failure link when we need it,
# and we store it for future use. The failure link of a vertex is the longest proper
# suffix of the string represented by that vertex which is also a prefix of some pattern in the trie.
# This basically means that even after one pattern fails or succeeds, whereever it stops,
# we can look at the failure link to see if there's another pattern that can be matched from that
# point, using the same characters we already have
def get_link(v: int) -> int:
    # calculate failure link if the link hasnt been made yet
    if t[v].link == -1:
        # if its the root or direct child AKA the first character of a pattern
        # the link is 0 because the longest suffix that's also a prefix is the
        # empty string hence why it would just be the root
        if v == 0 or t[v].p == 0:
            t[v].link = 0
        else:
            # otherwise, get the failure link of the parent
            t[v].link = go(get_link(t[v].p), t[v].pch)
    return t[v].link


# go computes the automaton transition for a character. If the trie already has an edge,
# we use it directly. Otherwise, we follow suffix links until we find the longest suffix
# that can still consume the character, or stay at the root if no such suffix exists.
def go(v: int, ch: str) -> int:
    if ch not in t[v].go:  # if we havent calculated this transition before
        if ch in t[v].next:  # if an edge already exist for this character
            t[v].go[ch] = t[v].next[ch]  # then we can just go to that vertex
        else:
            # assuming we're at the root, we stay if theres no character which is
            # the start of a pattern,
            # otherwise we follow the failure link and try to go from there
            t[v].go[ch] = 0 if v == 0 else go(get_link(v), ch)
    return t[v].go[ch]


def is_output(vertex: int) -> bool:
    return t[vertex].output


def get_word(vertex: int) -> str:
    return t[vertex].word


def main() -> None:
    lines = sys.stdin.read().split("\n")
    if not lines or lines[0] == "":
        return

    number_of_words = int(lines[0].strip())
    word_map = {}  # word -> original index

    # build the trie by adding each pattern, and map each word to its corresponding
    # input index so we can report matches in the correct order
    for i in range(number_of_words):
        word = lines[i + 1]
        add_string(word)
        word_map[word] = i

    text = lines[number_of_words + 1]

    # run the trie over the text one character at a time.
    # v tracks our current state/vertex in the trie
    v = 0
    for i, ch in enumerate(text):
        # transition to the next state using the go function,
        # following failure links automatically when no trie edge exists
        v = go(v, ch)

        # after each character traverse the failure link chain
        # every output vertex represents a pattern that
        # ends exactly at position i in the text
        u = v
        while u != 0:
            if is_output(u):
                word = get_word(u)
                # get the start index which ends at i so it starts at i - len + 1
                index = i - len(word) + 1
                print(f"{word_map[word]} {index}")
            # move to the next failure link to check for shorter overlapping patterns
            u = get_link(u)


if __name__ == "__main__":
    main()
