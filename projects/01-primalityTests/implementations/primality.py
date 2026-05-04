# The following functions in this file aim to determine whether a given number
# is prime or not. We will implement three different methods to check for
# primality, each with its own advantages and disadvantages. 
# 
# 
# The first method is the trial division method, which is a straightforward 
# approach that checks for divisibility by all integers up to the square root 
# of the number. 
# 
# 
# The second method is the Fermat primality test, which is a probabilistic 
# algorithm that can quickly determine if a number is composite or probably prime.
# 
# 
# The third method is the Miller-Rabin primality test, which is a probabilistic 
# algorithm that can quickly determine if a number is composite or probably prime.


import random


# Method 1: trial division
# This method uses a brute force approach, checking if the input number n is 
# divisible by any of the numbers leading up to it, stopping at the highest 
# possible factor.
def is_prime_trial_division(n):
    # Return false if n is less than or equal to one because 0, 1, and negative
    # numbers are not considered prime
    if n <= 1:
        return False
    
    # Starting with lowest possible factor i=2, iterate up to the highest possible
    # factor (the square root of n)
    for i in range(2, int(n**0.5) + 1):

        # At each possible factor, return false if n is divisible by that factor
        if n % i == 0:
            return False
    
    # If n is not divisible by any of the possible factors, it must be prime
    return True


# Method 2: Fermat primality test
# This method relies on Fermat's little theorem, which states that if p is a prime
# number and a is an integer not divisible by p, then a^(p-1) ≡ 1 mod p. If this
# congruence does not hold for some a, then n is composite. However, if it holds for
# several values of a, n is likely prime, but not guaranteed (hence "probably prime").

# helper function using binary exponentiation to calculate a^(p-1) mod p
def power_mod(base, exp, mod):
    # Start with multiplicative identity, which will hold the final result
    result = 1

    # Reduce base modulo mod before starting to reduce numbers from getting unnecessarily 
    # large. This reduction does not affect the final result.
    #
    # This comes from the theorem (a * b) mod m = ((a mod m) * (b mod m)) mod m, which
    # shows that any number we muliply by base later and modulo by mod will yield the
    # same result as if we used the original base.
    base = base % mod
    
    # Iterate through each bit of the exponent
    while exp > 0:

        # If the current exponent bit is 1, multiply base into the current result
        if (exp % 2) == 1:
            result = (result * base) % mod

        # Rightshift the exponet by one to examine the next bit
        exp = exp >> 1

        # Square the base for the next binary position, corresponding to moving to the
        # next power of two
        base = (base * base) % mod

    # Result now holds (base^exp) % mod
    return result

# Implementation of Fermat test, where n is the number to check for primality and k is
# the number of random tests performed
def is_prime_fermat(n, k=5):
    # Numbers <= 1 are not considered prime, return False
    if n <= 1:
        return False
    
    # 2 and 3 are prime, handle them early
    if n <= 3:
        return True
    
    # Handle any even numbers early
    if n % 2 == 0:
        return False

    # Perform the Fermat test k times with random bases
    for i in range(k):

        # Select a base randomly between 2 and n-2
        # 1 and n-1 are trivial, so those are avoided
        a = random.randint(2, n - 2)

        # Compute a^(n-1) mod n using efficient modular exponentiation and check that
        # the result is equal to 1 as defined in Fermat's little theorem
        if power_mod(a, n - 1, n) != 1:
            return False
    
    # Return that the number is prime if none of the tests fail
    return True


# Method 3: Miller-Rabin primality test
# This method is an improvement over the Fermat test and is more reliable. It works 
# by expressing n-1 as 2^r * d, where d is odd, and then performing several rounds 
# of testing with random bases. If any round fails, n is composite. If all rounds 
# pass, n is probably prime.

# helper function to determine if a number is definitely composite
def check_composite(n, a, d, r):
    # Compute a^d mod n
    # This is the first value in the Miller-Rabin chain
    x = power_mod(a, d, n)

    # If the value of a^d mod n is 1 or n-1, the round is inconclusive and n might
    # still be prime, continue on to the next test
    if x == 1 or x == n-1:
        return False
    
    # Repeatedly square x up to r-1 times
    # If n is prime, one of these must become n-1
    for _ in range(r-1):
        x = (x*x) % n

        # If we reach n-1, this round passes. Continue on to the next test
        if x == n-1:
            return False
        
    # If n-1 was never reached, conclude that n cannot be prime
    return True

def is_prime_miller_rabin(n, k=5):
    # Numbers <= 1 are not considered prime, return False
    if n <= 1:
        return False
    
    # 2 and 3 are prime, handle them early
    if n <= 3:
        return True
    
    # Handle any even numbers early
    if n % 2 == 0:
        return False

    # Factor n-1 into the form 2^r * d
    # This allows the algorithm to apply the Fermat test
    r = 0
    d = n - 1
    while d % 2 == 0:  # While d is divisible by 2, divide d by 2 and increment r. Increase r as much as possible (until d is odd) while keeping the form 2^r * d
        d //= 2
        r += 1

    # Perform k independent tests with random bases
    for _ in range(k):

        # Select a base randomly between 2 and n-2
        # 1 and n-1 are trivial, so those are avoided
        a = random.randint(2, n - 2) 

        # Use helper function to determine if n is composite
        # Return that n is probably not prime if this function returns True
        if check_composite(n, a, d, r):
            return False

    
    # If all rounds pass, n is very likely to be prime
    return True


if __name__ == "__main__":
    num_tests = int(input().strip())
    
    for i in range(num_tests):
        line = input().strip().split(' ')
        t = line[0]
        n = int(line[1])
        res = False
        if t == 'b':
            res = is_prime_trial_division(n)
        elif t == 'f':
            res = is_prime_fermat(n)
        elif t == 'm':
            res = is_prime_miller_rabin(n)
        
        if res:
            print("yes")
        else:
            print("no")