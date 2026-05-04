#include <iostream>
#include <fstream>
#include <vector>
#include <cmath>
#include <string>
#include <algorithm>
#include <set>
#include <cstdint>
#include <iomanip>
#include <cstring>
#include "MurmurHash3.h"

class HyperLogLog {
private:
    uint8_t b; // log(m), total number of bits (used to index buckets)
    uint32_t m; // number of registers or counters
    std::vector<uint8_t> registers;
    double alpha_m; // constant to correct hash collisions

    // using MurmurHash3 algorithm to ensure bits uniformly distributed
    uint64_t hash(const std::string& item) {
        uint64_t out[2];
        // Using a seed for consistency
        MurmurHash3_x64_128(item.data(), static_cast<int>(item.length()), 0xADC83B19, out);
        return out[0]; // Use the first 64 bits
    }

    uint8_t countLeadingZeros(uint64_t x) {
        // if x is 0, all 64 - b bits are 0, so first 1 should be at 64 - b + 1
        if (x == 0) return 64 - b + 1;
        // __builtin_clzll counts the leading zeros, then add 1 for first 1
        return static_cast<uint8_t>(__builtin_clzll(x) + 1);
    }

public:
    HyperLogLog(uint8_t precision) : b(precision) {
        m = 1 << b;
        registers.assign(m, 0);

        // alpha_m can be approximated like this (from wikipedia page)
        if (m == 16) alpha_m = 0.673;
        else if (m == 32) alpha_m = 0.697;
        else if (m == 64) alpha_m = 0.709;
        else alpha_m = 0.7213 / (1.0 + 1.079 / m);
    }

    // add to set
    void add(const std::string& item) {
        uint64_t x = hash(item);
        
        // register to modify (using top bits for index)
        uint32_t idx = static_cast<uint32_t>(x >> (64 - b));
        
        // remaining 64-b bits for rho calculation
        uint64_t remaining_bits = x << b;
        
        uint8_t rho = countLeadingZeros(remaining_bits); // position of first 1
        
        if (rho > registers[idx]) {
            registers[idx] = rho;
        }
    }

    // merge two HLLs
    void merge(const HyperLogLog& other) {
        if (this->b != other.b) {
            std::cerr << "Error: Cannot merge HLLs with different b." << std::endl;
            return;
        }
        // just need to get maximum of each pair
        for (uint32_t j = 0; j < m; ++j) {
            if (other.registers[j] > this->registers[j]) {
                this->registers[j] = other.registers[j];
            }
        }
    }

    // estimate cardinality of HLL
    uint64_t count() const {
        double sum = 0.0;
        int empty_registers = 0;
        
        for (uint8_t val : registers) {
            sum += 1.0 / (1ULL << val);
            if (val == 0) empty_registers++;
        }

        // harmonic mean raw estimate
        double estimate = alpha_m * m * m / sum;

        // biased when estimate < 5/2 * m, so use linear counting
        if (estimate <= 2.5 * m) {
            if (empty_registers > 0) {
                estimate = m * std::log(static_cast<double>(m) / empty_registers);
            }
        } 
        
        // biased when cardinalities approach limit of 32bit registers
        else if (estimate > 4294967296.0 / 30.0) {
            estimate = -4294967296.0 * std::log(1.0 - (estimate / 4294967296.0));
        }

        // error should be around 1.04 * sqrt(m)
        return static_cast<uint64_t>(estimate);
    }
};

int main(int argc, char* argv[]) {
    if (argc < 2) {
        std::cerr << "Usage: " << argv[0] << " <input_file>" << std::endl;
        return 1;
    }

    std::string filename = argv[1];
    std::ifstream input_file(filename);

    if (!input_file.is_open()) {
        std::cerr << "Error: Could not open file " << filename << std::endl;
        return 1;
    }

    HyperLogLog hll(14);
    
    std::string item;
    while (input_file >> item) {
        hll.add(item);
    }

    uint64_t estimated_val = hll.count();

    std::cout << "Estimated Cardinality: " << estimated_val << std::endl;
    
    return 0;
}
