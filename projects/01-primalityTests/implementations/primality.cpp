#include <iostream>
#include <cstdint>
#include <cstdlib>
using namespace std;

// Helper function for computing exponentials with modulo quickly. Calculates a^b % m
long long binpow(uint64_t a, uint64_t b, uint64_t m) {
    // Modulo can be done frequently. It will make our numbers smaller and doesn't interfere with future multiplications
    a %= m;
    uint64_t res = 1;
    // Works starting with low digits of b, and shifts once per loop iteration
    while (b > 0) {
        // If the current final binary digit is 1, multiply the result by a and mod by m
        if (b & 1) {
            res = (__uint128_t) res * a % m;
        }
        // Doubles the exponent of a every iteration, so that higher powers can be calculated using middle exponential values rather than from scratch every time
        a = (__uint128_t) a * a % m;
        b >>= 1;
    }
    return res;
}

// Helper function to check if number n is composite, based on random value a and values d and s based on the decomposition of (n-1) into 2^s multiplied by odd number d
// Used by the Miller-Rabin probabilistic test for primality
bool check_composite(uint64_t n, uint64_t a, uint64_t d, uint64_t s) {
    // Find the exponent a^d 
    uint64_t x = binpow(a, d, n);

    // If a^d % n results in modulo 1 or -1, by the Miller-Rabin decomposition of Fermat's little theorem n cannot be prime.
    if (x == 1 || x == n - 1) {
        return false;
    }

    // We must also check a^(2^r * d) for all values r from 1 to s-1 (r = 0 was tested with previous conditional) 
    for (int r = 1; r < s; r ++) {
        x = (__uint128_t) x * x % n;
        if (x == n - 1) {
            return false;
        }
    }

    return true;
}

// Very basic (but slow) brute-force test for primality
bool basic(uint64_t c) {
    uint64_t j = 2;

    // From 2 to sqrt(c), find if any number evenly divides c; if it does, c cannot be prime
    // sqrt(c) is the upper bound since no factor would be found at any higher value without a lower factor already having been discovered
    // at least one factor must be less than sqrt(c)
    while (j * j <= c) {
        if (c % j == 0) {
            return false;
        }
        j ++;
    }

    return true;
}

// Probabilistic Fermat primality test. Faster than brute force method
bool probabilisticFermat(uint64_t c, int tests=5) {
    // The only prime numbers less than 5 are 2 and 3
    if (c <= 4) {
        return c == 2 || c == 3;
    }
    
    // Randomly find integers to test against Fermat's little theorem
    for (int i = 2; i < tests; i ++) {
        int a = 2 + rand() % (c-3);
        // By Fermat's little theorem, a^(c-1) % c == 1 for any prime number c and coprime number a
        // if c is prime and a < (c-3) as produced by the modulo above, then a is coprime
        if (binpow(a, c-1, c) != 1) {
            return false;
        }
    }
    return true;
}

// Fast and more thorough test for primality compared to Fermat's test, though still not guaranteed to be correct.
bool probabilisticMillerRabin(uint64_t c, int tests=5) {
    // The only prime numbers less than 5 are 2 and 3
    if (c <= 4) {
        return c == 2 || c == 3; 
    }

    // We want to find values for s and d that decompose (c-1) into 2^s and odd number d
    int s = 0;

    // Right shifting through 0 bits gives us the greatest power of 2 which goes into c - 1
    // The remaining value in d is the odd number factor of c-1
    uint64_t d = c - 1;
    while ((d & 1) == 0) {
        d >>= 1;
        s ++;
    }

    // Randomly test the primality of c with (tests) # of tests.
    for (int i = 0; i < tests; i ++) {
        int a = 2 + rand() % (c-3);
        // Check composite works by testing against an extension of Fermat's little theorem. See function above for more details
        if (check_composite(c, a, d, s)) {
            return false;
        }
    }
    return true;
}

// reads in input and tests -- i think you get this one
int main() {
    srand(time(NULL));
    int n;
    cin >> n;

    for (int i = 0; i < n; i ++) {
        uint64_t v;
        char c;

        cin >> c >> v;
        bool retval;
        switch (c) {
            case 'b':
                retval = basic(v);
                break;
            case 'f':
                retval = probabilisticFermat(v);
                break;
            case 'm':
                retval = probabilisticMillerRabin(v);
        }

        if (retval) {
            cout << "yes" << '\n';
        } else {
            cout << "no" << '\n';
        }
    }
}
