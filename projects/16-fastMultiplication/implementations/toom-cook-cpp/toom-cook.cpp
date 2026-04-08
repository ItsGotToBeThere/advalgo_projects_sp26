#include <iostream>
#include <string>
#include <algorithm>
#include <gmpxx.h> // GMP C++ Wrapper

// Using mpz_class from GMP library to act as BigInt
// To install: 
// $ sudo apt-get install libgmp-dev


// Intuition of algo: 
// Convert each number into a polynomial: (when x = Base) 
//      a = P(x) = u2*x^2 + u1*x + u0
//      b = Q(x) = v2*x^2 + v1*x + v0
// Solve for x at 5 distinct points (0, 1, -1, -2, and inf)
// Solve linear equation to arrive at coeffients for R(x) = P(x)Q(x)
// Reconstruct R(x) when x is base to arrive at answer

mpz_class toom3_magnitude_multiply(const mpz_class& a, const mpz_class& b) {
    // 1. Base Case: If the numbers are small, just use standard multiplication.
    // (Using a threshold of 200 bits to stop the recursion)
    // Toom-Cook is only efficient for incredibly large numbers, the overhead is not worth it
    // for numbers of this size
    if (mpz_sizeinbase(a.get_mpz_t(), 2) < 200 || mpz_sizeinbase(b.get_mpz_t(), 2) < 200) {
        return a * b;
    }

    // 2. Split: Chunking the (very large) numbers into k = 3 parts
    // We first find out how many bits long the numbers are 
    size_t bits_a = mpz_sizeinbase(a.get_mpz_t(), 2);
    size_t bits_b = mpz_sizeinbase(b.get_mpz_t(), 2);

    // Shift is chunk size (k = 3) 
    // It's found by dividing the longest length by 3
    // + 2 to simulate .ceil() since 3 is hardcoded
    size_t shift = (std::max(bits_a, bits_b) + 2) / 3;

    // Create a mask of 'shift' bits of 1s to extract chunks
    mpz_class mask = (mpz_class(1) << shift) - 1;

    // Splitting 'a' into 3 chunks: u2 (highest) , u1 (middle) , u0 (lowest)
    mpz_class u0 = a & mask;
    mpz_class u1 = (a >> shift) & mask;
    mpz_class u2 = a >> (2 * shift);

    // Splitting 'b' into 3 chunks: v2 (highest) , v1 (middle) , v0 (lowest)
    mpz_class v0 = b & mask;
    mpz_class v1 = (b >> shift) & mask;
    mpz_class v2 = b >> (2 * shift);

    // 3. Evaluate points (P and Q)
    // Toom-cook turns each number into a polynomial: 
    // P(x) = u2*x^2 + u1*x + u0 = a (when x is Base)
    // Solve for x at 5 distinct points (0, 1, -1, -2, and inf) 
    mpz_class p0 = u0;
    mpz_class p1 = u0 + u1 + u2;
    mpz_class p_m1 = u0 - u1 + u2;
    mpz_class p_m2 = u0 - 2*u1 + 4*u2;
    mpz_class p_inf = u2;

    // Repeat process for b
    mpz_class q0 = v0;
    mpz_class q1 = v0 + v1 + v2;
    mpz_class q_m1 = v0 - v1 + v2;
    mpz_class q_m2 = v0 - 2*v1 + 4*v2;
    mpz_class q_inf = v2;

    // 4. Recurse (The 5 Multiplications)
    // Instead of the 9 multiplications of schoolbook multiplication (for chunks of 3), 
    // Toom-cook only needs 5 multiplications
    // Doing it recursively incase terms are still very large 
    mpz_class r0 = toom3_magnitude_multiply(p0, q0);
    mpz_class r1 = toom3_magnitude_multiply(p1, q1);
    mpz_class r_m1 = toom3_magnitude_multiply(p_m1, q_m1);
    mpz_class r_m2 = toom3_magnitude_multiply(p_m2, q_m2);
    mpz_class r_inf = toom3_magnitude_multiply(p_inf, q_inf);

    // 5. Interpolate (Bodrato's Matrix)
    // With 5 points on the new curve, solve the linear equation using 
    // specific algebraic sequence detailed by Toom-cook
    // Note: divisions by 2 and 3 are incredibly optimized and guarenteed to have no remainder
    mpz_class r4 = r_inf;
    mpz_class M1 = r1 - r0 - r4;
    mpz_class Mm1 = r_m1 - r0 - r4;
    mpz_class Mm2 = r_m2 - r0 - 16 * r4;

    mpz_class r2 = (M1 + Mm1) / 2;
    mpz_class A = M1 - r2;
    mpz_class B = (4 * r2 - Mm2) / 2;
    mpz_class r3 = (B - A) / 3;
    mpz_class r1_final = A - r3;

    // 6. Recompose
    // With the final chunks, they must be put back in their respective binary place values
    // Same as the final step in schoolbook multiplication 
    mpz_class result = r0 + 
                       (r1_final << shift) + 
                       (r2 << (2 * shift)) + 
                       (r3 << (3 * shift)) + 
                       (r4 << (4 * shift));

    return result;
}

// Wrapper to handle signs seamlessly
mpz_class toom3_multiply(const mpz_class& a, const mpz_class& b) {

    // Toom-Cook 3 expects positive magnitudes. To handle negatives: strip signs, multiply, and reapply.

    bool is_negative = (sgn(a) != sgn(b));
    
    mpz_class magnitude_a = abs(a);
    mpz_class magnitude_b = abs(b);

    mpz_class result = toom3_magnitude_multiply(magnitude_a, magnitude_b);

    if (is_negative && result != 0) {
        return -result;
    }
    return result;
}

int main() {
    std::string str1, str2;

    while (std::cin >> str1 >> str2) {

        // Convert strings to multi-precision ints
        // GMP automatically handles converting between base 10 and raw bytes
        // Essentially BigInt class in python
        mpz_class num1(str1);
        mpz_class num2(str2);

        // Call algorithm on the two extracted numbers
        mpz_class result = toom3_multiply(num1, num2);

        std::cout << result.get_str() << "\n";
    }

    return 0;
}
