# Rabin-Karp Algorithm Implementation in Python 
# Wadie Abboud - ucu8tt
# Input: First line contains the text, second line contains the pattern, alphabet is assumed to be ASCII characters
# Output: The starting index (zero-based) where pattern match is found in text, if not found, return -1

def rabin_karp(text, pattern):
    m = len(pattern)
    n = len(text)
    d = 256  # Number of characters in the input alphabet, set to 256 so it can handle any ASCII character
    q = 101  # A prime number to reduce the hash value and avoid collisions

    h = 1 # Precomputed pow(d, m-1)%q
    for i in range(m - 1): #calculate h to avoid int overflow
        h = (h * d) % q

    p = 0  # hash value for pattern
    t = 0  # hash value for text of current window (size m)
    for i in range(m): # Calculate the hash value of pattern and first window of text
        p = (d * p + ord(pattern[i])) % q #This summation method is equal to p = (pattern[0] × d(m−1) + pattern[1] × d(m−2) + ... + pattern[m−1] × d0) % q
        t = (d * t + ord(text[i])) % q

    for i in range(n - m + 1): # Sliding window over the text
        if p == t: #Hash values match, check for characters one by one in case of hash collision
            match = True
            for x in range(m):
                if text[i + x] != pattern[x]:
                    match = False
                    break
            if match:
                return i # If pattern matches, return the starting index
                # Some implementations return the first index of every match, but we refer to the Wiki page provided in the project description.
        if i < n - m: # Calculate next window's hash value, unless end of text
            t = (d * (t - ord(text[i]) * h) + ord(text[i + m])) % q # Remove leading digit, add trailing digit
            if t < 0:
                t += q # We might get negative value of t, converting it to positive
    return -1 # If no match is found, return -1

# Expected input format: First line contains the text, second line contains the pattern

def main():
    text = input()
    pattern = input()
    result = rabin_karp(text, pattern)
    print(result)


if __name__ == "__main__":
    main()