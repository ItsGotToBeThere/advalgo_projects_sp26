import sys
import math
import mmh3
from collections import deque

class HyperLogLog:
    def __init__(self, p):
        self.p = p
        self.num_registers = 1 << p
        self.reg = [0] * self.num_registers

    def add_element(self, value):
        h = mmh3.hash64(str(value), seed=0xADC83B19, signed=False)[0]
        register_index = h >> (64 - self.p)
        remaining_bits = 64 - self.p
        w = h & ((1 << remaining_bits) - 1)

        if w == 0:
            num_leading_zeros = remaining_bits + 1
        else:
            num_leading_zeros = (remaining_bits - w.bit_length()) + 1

        if num_leading_zeros > self.reg[register_index]:
            self.reg[register_index] = num_leading_zeros

    def merge(self, other):
        for i in range(self.num_registers):
            if other.reg[i] > self.reg[i]:
                self.reg[i] = other.reg[i]

    def estimate_cardinality(self):
        m = self.num_registers

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

        # small range correction
        num_zero = self.reg.count(0)
        if estimate <= 2.5 * m and num_zero > 0:
            return m * math.log(m / num_zero)

        # large range correction
        two64 = float(1 << 64)
        if estimate > two64 / 30.0:
            return -two64 * math.log(1.0 - estimate / two64)

        return estimate

class SlidingWindowHLL:
    def __init__(self, window_size, block_size, p):
        self.window_size = window_size
        self.block_size = block_size
        self.p = p

        self.blocks = deque()
        self.current_block = HyperLogLog(p)
        self.current_block_count = 0

    def add(self, value):
        self.current_block.add_element(value)
        self.current_block_count += 1

        if self.current_block_count == self.block_size:
            self.blocks.append(self.current_block)
            self.current_block = HyperLogLog(self.p)
            self.current_block_count = 0

        while len(self.blocks) * self.block_size >= self.window_size:
            self.blocks.popleft()

    def query(self):
        merged = HyperLogLog(self.p)
        for block in self.blocks:
            merged.merge(block)
        merged.merge(self.current_block)
        return int(merged.estimate_cardinality())

def main():
    lines = sys.stdin.read().strip().splitlines()
    n, W = map(int, lines[0].split())

    P = 10
    BLOCK_SIZE = max(1, W // 20)

    sw_hll = SlidingWindowHLL(W, BLOCK_SIZE, P)

    for line in lines[1:]:
        if line.startswith("ADD"):
            _, x = line.split()
            sw_hll.add(x)
        else:
            print(sw_hll.query())


if __name__ == "__main__":
    main()