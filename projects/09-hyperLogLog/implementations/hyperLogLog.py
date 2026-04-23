import math
import sys
import mmh3

class HyperLogLog:
    """
    HyperLogLog implementation for approximate distinct counting.
    Supports adding elements, merging multiple sketches, and estimating cardinality.
    """

    def __init__(self, p):
        # p determines the number of registers (2^p) and accuracy
        self.p = p
        self.num_registers = 1 << p
        self.reg = [0] * self.num_registers

    def add_element(self, value):
        """
        Hash the element and update the appropriate register
        based on the number of leading zeros in the hash.
        """
        h = mmh3.hash64(str(value), seed=0xADC83B19, signed=False)[0]

        # Use the first p bits to select the register
        register_index = h >> (64 - self.p)

        # Remaining bits determine the zero count
        remaining_bits = 64 - self.p
        w = h & ((1 << remaining_bits) - 1)

        if w == 0:
            num_leading_zeros = remaining_bits + 1
        else:
            num_leading_zeros = (remaining_bits - w.bit_length()) + 1

        # Update register with the maximum observed value
        if num_leading_zeros > self.reg[register_index]:
            self.reg[register_index] = num_leading_zeros

    def merge(self, other):
        """
        Merge another HyperLogLog object into this one by taking
        the register-wise maximum.
        """
        for i in range(self.num_registers):
            if other.reg[i] > self.reg[i]:
                self.reg[i] = other.reg[i]

    def estimate_cardinality(self):
        """
        Compute the HyperLogLog cardinality estimate
        with standard small and large-range corrections (values taken from the original paper).
        """
        m = self.num_registers

        # Bias-correction constant depending on number of registers (values taken from the original paper)
        if m == 16:
            alpha = 0.673
        elif m == 32:
            alpha = 0.697
        elif m == 64:
            alpha = 0.709
        else:
            alpha = 0.7213 / (1 + 1.079 / m)

        harmonic_sum = sum(2.0 ** -r for r in self.reg)
        estimate = alpha * m * m / harmonic_sum

        # Small-range correction (linear counting)
        num_zero = self.reg.count(0)
        if estimate <= 2.5 * m and num_zero > 0:
            return m * math.log(m / num_zero)

        # Large-range correction
        two64 = float(1 << 64)
        if estimate > two64 / 30.0:
            return -two64 * math.log(1.0 - estimate / two64)

        return estimate

if __name__ == "__main__":
    if len(sys.argv) > 1:
        t = open(sys.argv[1], "r", encoding="utf-8").read().split()
    else:
        t = sys.stdin.read().split()

    precision = 14
    if len(sys.argv) > 2:
        precision = int(sys.argv[2])

    h = HyperLogLog(precision)
    for tok in t:
        h.add_element(tok)

    sys.stdout.write(str(int(round(h.estimate_cardinality()))))
