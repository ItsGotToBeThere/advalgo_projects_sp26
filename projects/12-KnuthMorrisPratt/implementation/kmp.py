

def buildTable(pattern: str) -> list[int]:
    """
    Builds a fallback table that the KMP search uses to decide where to fall back to when a mismatch occurs (as opposed to restarting from the beginning of the pattern)

    This has O(m) time complexity where m is the length of the pattern, as it scans through every element in pattern.
    """

    # Creates the initial table of size m + 1 since it will store information for each index of the pattern and one extra for overlapping matches
    m = len(pattern)
    table = [0] * (m + 1)

    pos = 1 # current position in the pattern
    cnd = 0 # current fallback position for this position
    table[0] = -1 # -1 indicates there is no valid prefix/fallback

    # Preprocessing the pattern starting from index 1 until end of pattern
    while (pos < m):

        # if characters match
        if (pattern[pos] == pattern[cnd]):
            table[pos] = table[cnd]

        # if the characters dont match, then the best fallback will initially be set to current candidate (cnd)
        else:
            table[pos] = cnd

            # there may still be a smaller prefix that matches current pos, so we keep following fallbacks until there are no more valid ones left (-1)
            while (cnd >= 0) and (pattern[pos] != pattern[cnd]):
                cnd = table[cnd]
        
        pos += 1
        cnd += 1
    
    # the last position indicates fallback after the full pattern has been matches, so the KMP search can continue without restarting (useful in overlapping matches)
    table[pos] = cnd
    return table

def kmpSearch(text: str, pattern: str) -> list[int]:
    """
    Searches "text" for all occurrences of "pattern" and returns a list of all starting positions of "pattern".

    Unlike naive search, KMP doesn't move backwards in teh text, instead using the fallback table to reuse what has already been matched
    while traversing the text. This achieves O(n + m) overall time complexity where n is length of text and m is length of pattern.
    """

    # quick check for inputs, if either string is we should return no matches
    if (not pattern) or (not text):
        return []
    
    # Generate the fallback table
    table = buildTable(pattern)
    matches = []

    j = 0 # pointer to current index in text
    k = 0 # pointer current index in pattern

    # Scans through the text exactly once
    while (j < len(text)):

        # Case 1: if the current characters in text and pattern match, then move both pointers
        if (pattern[k] == text[j]):
            k += 1
            j += 1

            # if the pattern was fully found, this counts as a match so append its start (index j - k) to the output list
            if (k == len(pattern)):
                matches.append(j - k)
                k = table[k]
        
        # Case 2: if the current characters in text and pattern don't match
        else:
            k = table[k]

            # if k is -1, this means there is no valid prefix left to reuse from the table, so we advance in both text and pattern
            if (k < 0):
                j += 1
                k += 1
    
    return matches

# parsing code to handle input which will be the text on the first line, and the pattern on the second line
if __name__ == "__main__":
    import sys
    lines = sys.stdin.read().splitlines()
    text = lines[0] if len(lines) > 0 else ""
    pattern = lines[1] if len(lines) > 1 else ""
    result = kmpSearch(text, pattern)
    print(" ".join(map(str, result)))