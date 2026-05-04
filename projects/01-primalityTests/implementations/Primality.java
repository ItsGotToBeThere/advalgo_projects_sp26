import java.util.Scanner;
import java.math.BigInteger;

public class Primality {
    // This is the simplest primality algorithm. We exhaustively
    // check if n is divisible by every number 2 <= i <= sqrt(n)
	public static boolean trialDivision(long n) {
		for (long i = 2; i * i <= n; i++) {
			if(n % i == 0)
				return false;
		}
		return true;
	}

    // Binary exponentiation is implemented by Java's BigInteger
    // library and should not be implemented using longs to avoid
    // integer overflows.
    public static long binaryExponentiation(long base, long exponent, long modulus) {
        return BigInteger.valueOf(base).modPow(BigInteger.valueOf(exponent), BigInteger.valueOf(modulus)).longValue();
    }

    // Check Fermat's Little Theorem - a^n-1 % p = 1
    // If this is does not hold, n is composite. Otherwise, the 
    // test is inconclusive, and either n is prime, or n will 
    // likely be found to be composite on another iteration. The 
    // variable interations determines how much certainty you want 
    // that a number that is outputted as prime, is actaully prime.
    public static boolean probablyPrimeFermat(long n, int iterations) {
        // The formula for finding a base 'a' does not work for n < 4
        // so the primality test cannot work. We have to hard-code 
        // these in as prime.
        if (n < 4)
            return n == 2 || n == 3;

        for (int i = 0; i < iterations; i++) {
            // Generates a psuedorandom number in [2, n-2]
            long a = 2 + (long) (Math.random() * (n-3));

            // Check Fermat's Little Theorem - a^n-1 % p = 1
            // Binary Exponentiation is required to compute this
            // in O(log(exponent)) time (naively O(exponent) time).
            if (binaryExponentiation(a, n - 1, n) != 1)
                return false; // n is certainly composite
        }
        return true; // n is *probably* prime
    }

    // Performs one round of the Miller-Rabin test for a given base a.
    // Returns true if n is definitely composite with respect to base a.
    // Returns false if n might still be prime.
	public static boolean check_composite(long n, long a, long d, long r) {
        long x = binaryExponentiation(a, d, n);
        if (x == 1 || x == n - 1)
            return false; // n may still be prime
        
        // Repeatedly compute x = x^(2^r) % n, r-1 times
        for (long i = 1; i < r; i++) {
            x = (long) x * x % n;

            if (x == n - 1)
                return false; // n may still be prime
        }
        return true;
    };

    // returns true if n is prime, else returns false.
    public static boolean MillerRabin(long n) {
        int r = 0;
        long d = n - 1;

        // Factor n-1 into the form d * 2^r where d is odd
        while ((d & 1) == 0) {
            d >>= 1;
            r++;
        }

        // With the first 12 primes, all n < 9,223,372,036,854,775,807
        // (all signed longs) can be deterministically checked.
        long[] aArray = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37};
        for (long a : aArray) {
            if (n == a)
                return true;
            if (check_composite(n, a, d, r))
                return false;
        }
        // If none of the bases showed that n is composite, then it
        // must be prime.
        return true;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String []line = scanner.nextLine().strip().split(" ");
        boolean answer = true;
        for (int i = 0; i < Integer.parseInt(line[0]); i++) {
            String[] curLine = scanner.nextLine().strip().split(" ");
            long n = Long.parseLong(curLine[1]);
            switch (curLine[0].charAt(0)) {
                case 'b' -> {
                    answer = trialDivision(n);
                }
                case 'f' -> {
                    answer = probablyPrimeFermat(n, 3);
                }
                case 'm' -> {
                    answer = MillerRabin(n);
                }
            }

            if (answer) {
                System.out.println("yes");
            } else {
                System.out.println("no");
            }
        }
    }
}
