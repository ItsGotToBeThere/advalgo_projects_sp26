# Run: python3 BloomFilter.py io/test_1.txt
#   or redirect stdin: python3 BloomFilter.py < io/test_1.txt
# Deps: pip install -r requirements.txt (same folder)

import math
import sys

import mmh3
from bitarray import bitarray


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


def main():
    """
    stdin or path: first line = t (number of test cases in this stream).
    Each case: line 'n p insert_count query_count',
    then insert_count strings, a line exactly '---', then query_count strings.
    Prints m, k, n, p and one line per query (member / false_positive / absent).
    """
    if len(sys.argv) > 1:
        stream = open(sys.argv[1], encoding="utf-8")
        own = True
    else:
        stream = sys.stdin
        own = False

    try:
        num_tests = int(stream.readline().strip())

        for t in range(num_tests):
            n, p, i, q = stream.readline().split()
            expected_item_count = int(n)
            false_positive_prob = float(p)
            insert_count = int(i)
            query_count = int(q)

            inserted_keys = []
            for _ in range(insert_count):
                inserted_keys.append(stream.readline().strip())

            sep = stream.readline().strip()
            if sep != "---":
                sys.exit(f"expected --- after inserts, got {sep!r}")

            query_keys = []
            for _ in range(query_count):
                query_keys.append(stream.readline().strip())

            # Ground truth for printing; the Bloom structure alone does not separate member vs FP. We use a set here.
            inserted_set = set(inserted_keys)
            bloom = BloomFilter(expected_item_count, false_positive_prob)
            for key in inserted_keys:
                bloom.add(key)

            print(
                f"m={bloom.size} k={bloom.hash_count} n={expected_item_count} p={false_positive_prob:.2f}"
            )
            for key in query_keys:
                if bloom.check(key):
                    if key in inserted_set:
                        print(f"{key}\tmember")
                    else:
                        print(f"{key}\tfalse_positive")
                else:
                    print(f"{key}\tabsent")
            if t < num_tests - 1:
                print()
    finally:
        if own:
            stream.close()


if __name__ == "__main__":
    main()
