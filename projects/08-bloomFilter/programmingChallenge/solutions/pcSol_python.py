#!/usr/bin/env python3
# Reference solution for the "Brainrot Blacklist" programming challenge.
# Deps: pip install -r requirements.txt in this same folder.
#
# BloomFilter class below is copied from ../../implementation/BloomFilter.py

import math
import sys

import mmh3
from bitarray import bitarray


# --- copied from implementation/BloomFilter.py ---


class BloomFilter:
    """
    Bloom filter: one bit array, several hash positions per key.
    Uses mmh3 (MurmurHash3-style 32-bit hashes) with different seeds as the hash functions.
    """

    def __init__(self, items_count, fp_prob):
        """
        items_count : int
            Expected number of distinct inserts for sizing (n in the usual formulas).
        fp_prob : float
            Target false-positive probability p (0 < p < 1) used to choose bit length and k.
        """
        # Desired false-positive rate (design input, not recomputed from the bit array later).
        self.fp_prob = fp_prob

        # Number of bits in the filter (m).
        self.size = self.get_size(items_count, fp_prob)

        # How many hash values we take per key (k).
        self.hash_count = self.get_hash_count(self.size, items_count)

        # All bits start cleared.
        self.bit_array = bitarray(self.size)
        self.bit_array.setall(0)

    def add(self, item):
        """
        Insert a key: set bit to 1 at every index this key maps to.
        """
        for i in range(self.hash_count):
            # Seed i changes the hash output so we get k different indices for the same string.
            bit_index = mmh3.hash(item, i) % self.size
            self.bit_array[bit_index] = 1

    def check(self, item):
        """
        Lookup: same indices as add.
        Returns False if any mapped bit is still 0 (key definitely not inserted).
        Returns True if all mapped bits are 1 (may be a member or a false positive).
        """
        for i in range(self.hash_count):
            bit_index = mmh3.hash(item, i) % self.size
            if not self.bit_array[bit_index]:
                return False
        return True

    @staticmethod
    def get_size(n, p):
        """
        Bit array length m from expected item count n and false-positive probability p:

            m = -(n * ln p) / (ln 2)^2

        n : int
            Expected inserts used for design.
        p : float
            Target false-positive probability.

        Rounds down with int(); this implementation forces m >= 1 so modulo in hashing is safe.
        """
        m = -(n * math.log(p)) / (math.log(2) ** 2)
        return max(1, int(m))

    @staticmethod
    def get_hash_count(m, n):
        """
        Number of hashes k to pair with bit length m and expected count n:

            k = (m / n) * ln 2

        m : int
            Bit array length.
        n : int
            Expected inserts used for design.

        If n is 0, returns 1 so add/check still run and we avoid division by zero.
        """
        if n <= 0:
            return 1
        k = (m / n) * math.log(2)
        return max(1, int(k))


# --- brainrot problem logic ---

def should_block(bf: BloomFilter, request_line: str) -> bool:
    """
    Decide whether a single request line should be blocked for school policy.

    Tokenization: split the line on any run of whitespace (see str.split with no
    arguments). Punctuation attached to a word (e.g. "gyatt...") becomes part
    of that token and is hashed as-is; the filter does not strip punctuation.

    bf : BloomFilter
        The filter that already contains all n blocklist keys from this case.
    request_line : str
        One line from the request section (may be empty or whitespace-only).

    Returns True if the line should be BLOCKED (at least one token is a positive
    Bloom look-up), or False if there are no tokens, or every token is definitely
    absent in the filter.
    """
    # str.split() with no args: trim runs of whitespace; empty/whitespace-only
    # lines yield an empty list, so we never call check and return False.
    for word in request_line.split():
        if bf.check(word):
            return True
    return False


def run_case(lines: list[str]) -> list[str]:
    """
    Parse one full problem instance and return the list of M_q output strings.

    Input layout (as a list of line strings, after splitlines()):
        line 0:  "n M_q eps"
        lines 1..n:  blocklist entries (one string per line; may contain spaces;
            each is one key to insert)
        lines n+1 .. n+M_q:  request lines to classify

    lines : list[str]
        Entire test case, one line per list element, including the header.
        If empty, returns [].

    Returns a list of exactly M_q strings, each "BLOCKED" or "ALLOWED", in
    the same order as the request section.
    """
    if not lines:
        return []

    # Header: n = blocklist size, m = M_q = number of query lines, eps = false
    # positive *percentage* (e.g. 10 -> design for p=0.10 for never-inserted).
    first = lines[0].split()
    n, m, eps = int(first[0]), int(first[1]), int(first[2])
    p = eps / 100.0

    # Blocklist: distinct strings, each stored as a whole key in the Bloom.
    blocked = [lines[1 + i].rstrip("\n") for i in range(n)]
    # Request section: M_q lines, same order as required in the output.
    queries = [lines[1 + n + j].rstrip("\n") for j in range(m)]

    # One Bloom filter: n and p pick its bit length and number of hash functions.
    bloom = BloomFilter(n, p)
    for w in blocked:
        bloom.add(w)

    out: list[str] = []
    for q in queries:
        out.append("BLOCKED" if should_block(bloom, q) else "ALLOWED")
    return out


def main() -> None:
    """
    Read the entire test case from stdin, run the pipeline, print one result
    line per request (no extra blank lines between them).
    """
    data = sys.stdin.read().splitlines()
    if not data:
        return
    out = run_case(data)
    # Newline-join so the process ends with a single trailing newline
    sys.stdout.write("\n".join(out) + "\n")


if __name__ == "__main__":
    main()
