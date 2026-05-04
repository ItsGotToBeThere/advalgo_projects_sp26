#include <cmath>
#include <iostream>
#include <set>
#include <vector>

uint64_t FNV_prime = 0x100000001b3;
// This is a custom hash function, not part of the Bloom Filter itself
// Typically the seed is 0xcbf29ce484222325, but we can supply custom seeds
uint64_t fnv1a_hash(std::string data, uint64_t seed) {
    // Standard FNV-1a hash algorithm. Takes the seed and XORs with each character
    // then multiplies with the FNV prime
    uint64_t hash = seed;
    for (char c : data) {
        hash = hash ^ c;
        hash = hash * FNV_prime;
    }

    return hash;
}

class BloomFilter {
    std::vector<bool> bit_array;
    int hash_functions;
    uint64_t seed1 = 0xcbf29ce484222325; // standard FNV-1a seed
    uint64_t seed2 = 0xdeadbeef; // random stuff lol

public:
    BloomFilter(int bits, int hash_functions)
        : bit_array(bits, false),
        hash_functions(hash_functions) {
    }

    void add(const std::string& str) {
        // Our two hash functions (same algo with different seeds)
        uint64_t hash1 = fnv1a_hash(str, seed1);
        uint64_t hash2 = fnv1a_hash(str, seed2);

        // Double hashing to generate more hash functions
        // For each function compute its hash, and set the corresponding bit in the bit array
        for (uint64_t i = 0; i < hash_functions; i++) {
            uint64_t index = (hash1 + i * hash2) % bit_array.size();
            bit_array[index] = true;
        }
    }

    // Checks if an element is possibly in the set
    bool contains(const std::string& element) const {
        uint64_t hash1 = fnv1a_hash(element, seed1);
        uint64_t hash2 = fnv1a_hash(element, seed2);

        // Double hashing again
        // For each function compute its hash, check if the corresponding bit is set
        // If any bit is not set, the element is definitely not in the set
        for (uint32_t i = 0; i < hash_functions; ++i) {
            size_t index = (hash1 + i * hash2) % bit_array.size();
            if (!bit_array[index]) {
                return false;
            }
        }
        return true; // could be a false positive though
    }


};

// constant used later
double ln2pow2 = std::pow(std::log(2), 2);

int main() {
    int testCases;
    std::cin >> testCases;

    for (int i = 0; i < testCases; i++) {
        // input
        int expectedItems, entries, queries;
        double falsePosRate;
        std::cin >> expectedItems >> falsePosRate >> entries >> queries;

        // determine size of bloom filter (in bits) = -(expectedItems * ln(falsePosRate)) / (ln(2)^2)
        // matches python implementation
        int bloomSize = static_cast<int>(-(expectedItems * std::log(falsePosRate)) / ln2pow2);
        bloomSize = std::max(1, bloomSize);

        // determine amount of hash functions to use
        int numHashFunctions;
        if (entries <= 0) {
            numHashFunctions = 1;
        } else {
            // hash function count (k) = (bloomSize/expectedItems) * ln(2)
            // matches python implementation
            numHashFunctions = static_cast<double>(bloomSize) / expectedItems * std::log(2);
            numHashFunctions = std::max(1, numHashFunctions);
        }

        std::cout << "m=" << bloomSize << " k=" << numHashFunctions <<
            " n=" << expectedItems << " p=" << falsePosRate << "\n";

        // let's do it! keep a ground truth set as well so we can see false positives
        BloomFilter filter = BloomFilter(bloomSize, numHashFunctions);
        std::set<std::string> groundTruth;

        // adding entries
        for (int j = 0; j < entries; j++) {
            std::string entry;
            std::cin >> entry;
            filter.add(entry);
            groundTruth.insert(entry);
        }

        // test files use a --- line between inserts and queries
        std::string separator;
        std::cin >> separator;
        if (separator != "---") {
            std::cerr << "Could not find the --- separator\n";
            return 1;
        }

        // query the filter
        for (int j = 0; j < queries; j++) {
            std::string query;
            std::cin >> query;
            if (filter.contains(query) && groundTruth.contains(query)) {
                std::cout << query << " member\n";
            } else if (!filter.contains(query)) {
                std::cout << query << " absent\n";
            } else if (filter.contains(query) && !groundTruth.contains(query)) {
                std::cout << query << " false_positive\n";
            }
        }

        std::cout << "\n";
    }

    // yay we're done

    return 0;
}