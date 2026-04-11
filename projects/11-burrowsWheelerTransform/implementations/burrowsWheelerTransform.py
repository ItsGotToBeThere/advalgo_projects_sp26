"""
Burrows-Wheeler Transform (BWT)

The Burrows-Wheeler transform rearranges the characters of a string based on
the lexicographic ordering of its cyclic rotations. It is useful in string
compression and encoding.

Sources:
    - https://en.wikipedia.org/wiki/Burrows%E2%80%93Wheeler_transform
    - https://www.youtube.com/watch?v=OKd534EWcdk
    - https://cp-algorithms.com/string/suffix-array.html
    - https://zork.net/~st/jottings/sais.html
    - https://www.geeksforgeeks.org/dsa/counting-sort/
    - https://www.geeksforgeeks.org/python/ord-function-python/
"""


"""
Builds the suffix array of s using the O(nlog(n)) cyclic shift algorithm.

This function sorts suffixes by repeatedly sorting cyclic substrings whose
lengths double with each pass.

Build suffix array in 6 steps:

1. Sort all indices by their single characters using counting sort.
   Example:
       s = "banana$"

       Indices sorted by character:
       [6, 1, 3, 5, 0, 2, 4]

       because:
       6 -> '$'
       1 -> 'a'
       3 -> 'a'
       5 -> 'a'
       0 -> 'b'
       2 -> 'n'
       4 -> 'n'

2. Assign each index an equivalence class. 
   Indices in the same class represent equal substrings.
   Example:
       class[6] = 0
       class[1] = 1
       class[3] = 1
       class[5] = 1
       class[0] = 2
       class[2] = 3
       class[4] = 3

3. Repeatedly double the substring length from 2^h to 2^(h + 1).
   At each round, treat evesry substring of length 2^(h + 1) as two halves:
       (first half of length 2^h, second half of length 2^h)
   Example:
       when h = 0, substrings of length 2 are built from two substrings of length 1
       when h = 1, substrings of length 4 are build from two substrings of length 2

4. Shift each sorted index left by 2^h to produce the starting indices of
   the substrings of length 2^(h + 1). Then use counting sort on the first half
   equivalence classes to reorder them.
   Example:
       If h = 1, then 2^h = 2

       Shifting index 5 left by 2 gives:
       5 - 2 = 3

       So if index 5 marks the start of the second half, then index 3 marks the
       start of the full substring of length 4

5. Recompute equivalence classes for the new order.
   Two indices are places in the same new class if their first halves are in the
   same class and their second halves are also in the same class.
   Example:
       Two substrings are equal if (class of first half, class of second half) 
       is the same for both

       So the substring at index i is compared using:
       (c[i], c[i + 2^h])
   
6. Continue doubling until the substring length is at least n.
   Example:
       build_suffix_array("banana$") -> [6, 5, 3, 1, 0, 4, 2]

       6 -> "$"
       5 -> "a$"
       3 -> "ana$"
       1 -> "anana$"
       0 -> "banana$"
       4 -> "na$"
       2 -> "nana$"
"""


def build_suffix_array(s: str) -> list[int]:
    # Store the length so that it can be used repeatedly below.
    n = len(s)

    # Number of possible character values in the input alphabet.
    # Use an alphabet size of 256 so each character can be mapped to its ASCII 
    # value with ord().
    alphabet = 256

    # p stores the current sorted order of starting indices
    # c stores the current equivalence class for each index
    # cnt = a counting array used for counting sort
    p = [0] * n
    c = [0] * n
    cnt = [0] * max(alphabet, n)

    # Count how many times each character appears in the string.
    # cnt[i] stores the number of occurences of the character with the ASCII value i.
    for i in range(n):
        cnt[ord(s[i])] += 1

    # Convert cnt into prefix sums.
    # After this, cnt[i] tells us how many characters are <= i, which lets us place
    # indices into sorted order.
    for i in range(1, alphabet):
        cnt[i] += cnt[i - 1]

    # Place each index i into its correct position in p based on its character.
    # This is a counting sort of the indices by their characters.
    for i in range(n):
        char_index = ord(s[i])
        cnt[char_index] -= 1
        p[cnt[char_index]] = i

    # Begin assigning equivalence classes.
    # The first index in sorted order gets class 0.
    c[p[0]] = 0
    classes = 1

    # Walk through the sorted indices.
    # If the current character is different from the previous one, it starts a new class. 
    # Otherwise, it belongs to the same class. 
    for i in range(1, n):
        if s[p[i]] != s[p[i - 1]]:
            classes += 1
        c[p[i]] = classes - 1

    # pn is the temporary array for shifted indices during sorting
    # cn is the temporary array for the next round of equivalence classes
    # h is the current doubling step, sorting substrings of length 2^h
    pn = [0] * n
    cn = [0] * n
    h = 0

    # Continue until the substring length 2^h is at least n.
    while (1 << h) < n:
        # To sort substrings of length 2^(h + 1), think of each one as:
        #   (first half of length 2^h, second half of length 2^h)
        #
        # The previous round already sorted substrings of length 2^h. 
        # By shifting each index in p left by 2^h, we produce the starting
        # indices of the length 2^(h + 1) substrings.
        # Counting sort by the first half class then finishes the ordering. 
        for i in range(n):
            pn[i] = p[i] - (1 << h)
            if pn[i] < 0:
                pn[i] += n

        # Reset cnt so it can count how many shifted indices fall into each class
        for i in range(classes):
            cnt[i] = 0

        # Count how many shifted indices belong to each equivalence class.
        # Sort by the first half's class, since the second half is already sorted
        for i in range(n):
            cnt[c[pn[i]]] += 1

        # Convert the class counts into prefix sums for counting sort.
        for i in range(1, classes):
            cnt[i] += cnt[i - 1]

        # Place the shifted indices into their new sorted order.
        # Go from right to left to keep the counting sort stable.
        # Stability is necessary here because indices with the same first half
        # class must remain in correct relative order by their second half class.
        for i in range(n - 1, -1, -1):
            cls = c[pn[i]]
            cnt[cls] -= 1
            p[cnt[cls]] = pn[i]

        # The first item in the new sorted order gets class 0.
        cn[p[0]] = 0
        classes = 1

        # Recompute equivalence classes for substrings of length 2^(h + 1).
        # If two adjacent indices in sorted order have different pairs, they
        # must belong to different classes.
        # Otherwise, they are in the same class.
        for i in range(1, n):
            cur = (c[p[i]], c[(p[i] + (1 << h)) % n])
            prev = (c[p[i - 1]], c[(p[i - 1] + (1 << h)) % n])

            if cur != prev:
                classes += 1

            cn[p[i]] = classes - 1

        # Move the computed equivalence classes into c and double substring length.
        c, cn = cn, c
        h += 1

    # p now contains the suffix array for s. 
    return p

"""
Computes the Burrows-Wheeler Transform (BWT) of a string.

The input string is assumed to not already contain the end of string
marker '$'.

BWT in 5 steps:
1. Start with a string and append a unique end of string marker '$'.
   Example:
      s = "banana$"

2. List every cyclic rotation of the string.
   Example:  
      rotations = [
            "banana$",
            "anana$b",
            "nana$ba",
            "ana$ban",
            "na$bana",
            "a$banan",
            "$banana"            
        ]

3. Sort cyclic rotations into lexicographic order:
   Example    
      rotations = [
            "$banana",
            "a$banan",
            "ana$ban",
            "anana$b",
            "banana$",
            "na$bana",
            "nana$ba"        
        ]

4. Form a new string by taking the last character from each sorted rotation.
   Example:
      last_column = "annb$aa"

5. The resulting string is the Burrows-Wheeler Transform.
   Example:   
      output = "annb$aa"

Instead of explicitly generating and sorting all rotations, this implementation
uses a suffix array to determine sorted order more efficiently. This avoids building
the full rotation matrix and reduces the time complexity from O(n^2 * log(n)) to 
O(nlog(n)) and the space complexity from O(n^2) to O(n).
"""

def bwt(s: str) -> str:
    # The BWT requires a unique end of string marker so that all suffixes
    # are distinct and the original string has a well defined ending. 
    # The algorithm assumes the function does not already contain '$'. 
    if '$' in s:
        raise ValueError("Input string cannot contain '$'")

    # Append the end of string marker so that all suffixes are distinct
    # and the original string can be recovered from the BWT.
    s += '$'

    # Build the suffix array of the string
    # The suffix array stores the starting indices of all suffixes in
    # lexicographic order.
    suffix_array = build_suffix_array(s)
    
    # Store the length so that it can be used repeatedly below.
    n = len(s)

    # This list will hold the characters of the transform.
    result = []

    # Loop through the suffixes in sorted order.
    # For a suffix that starts at the index start, the BWT uses 
    # the character right before that index in the original string.
    for start in suffix_array:
        # (start - 1) % n gives the index of the character before
        # the suffix start. That character becomes the next in the
        # BWT output
        #
        # The mod handles the wraparound case if start == 0
        result.append(s[(start - 1) % n])

    # Join the characters into the final transformed string
    return "".join(result)


if __name__ == "__main__":
    import sys

    data = sys.stdin.read().split()
    if data:
        print(bwt(data[0]), end="")
