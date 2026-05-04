#include <iostream> 
#include <string>

using namespace std;

// d is the number possible characters (1 char is 8 bits so 2^8=256 possibilities)
int d = 256;
// prime number to create a better hash function
int q = 101;

// simple function that checks if p and s.substring(start, start+p.length) are equal
bool check_strings_equal (string s, string p, int start) {
    for (int j = 0; j < p.length(); j++) {
        if (s[start+j] != p[j]) {
            return false;
        }
    }
    return true;
}

// Given a string s, and a pattern to find in the string p, this function will return the first index of the pattern p in the string s
// If the pattern is not found in s or the length of p is greater than s, the function returns -1
// we use the standard equation for the rolling hash h = (d * h + s[i]) % q;
int rabin_karp(string s, string p) {
    // get the length of s and p for later use, as well as ensuring s is longer than p
    int len_p = p.length();
    int len_s = s.length();
    if (len_p > len_s) return -1;

    // represents the constant d^(len_p-1), used to remove the left-most character from hash_s
    int d_p_1 = 1;
    // represents the hash of the pattern, stays constant after computing 
    int hash_p = 0;
    // the hash of the substring of s we are comparing against the pattern
    int hash_s = 0;

    // calculate hash_p and the initial hash_s
    for (int i = 0; i < len_p; i++) {
        hash_p = (d * hash_p + p[i]) % q;
        hash_s = (d * hash_s + s[i]) % q;
    }
    // calculating d^(len_p-1), we mod by q to prevent overflow. The math stays the same, as hash_s is also modded by q, 
    for (int i = 0; i < len_p - 1; i++) {
        d_p_1 = (d_p_1 * d) % q;
    }

    // loops through each possible substring
    for (int i = 0; i < len_s - len_p; i++) {
        // checks if hashes, and then strings are equal
        if (hash_p == hash_s && check_strings_equal(s,p,i)) {
            return i;
        }

        // first we remove the leftmost character from the string by substracting s[i] * d_p_1 from hash_s
        // then we add s[i + len_p] by performing our standard rolling hash equation h = (d * h + s[i]) % q;
        hash_s = (d * (hash_s - s[i] * d_p_1) + s[i + len_p] ) % q;
        // ensure the hash is positive
        if (hash_s < 0) {
            hash_s += q;
        }
    }

    // final check at the end of the string
    if (hash_p == hash_s && check_strings_equal(s,p,len_s - len_p)) {
        return len_s - len_p;
    }
    return -1;
}

int main()
{
    // s stores long string and p stores pattern
    string s;
    string p;

    // prompt the user for the long string
    getline(cin, s);

    // prompt the user for the search pattern
    getline(cin, p);

    // output the first index of 
    cout << rabin_karp(s, p);
    return 0;
}