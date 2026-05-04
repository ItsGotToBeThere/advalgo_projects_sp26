#include <iostream>
#include <vector>
#include <string>

using namespace std;

vector<int> kmp_table(const string& W) {
    // Allocate partial match table to dictate how far we can skip ahead after a mismatch.
    // First entry is set to -1, represents that a mismatch on the first character means we must advance the text pointer without pattern backtracking.
    vector<int> T(W.length() + 1);
    int pos = 1;
    int cnd = 0;
    T[0] = -1;

    // Iterate through the search word to map out internal patterns.
    // If the current character perfectly extends a previously identified prefix, inherit that prefix's fallback value to optimize future skips.
    while (pos < W.length()) {
        if (W[pos] == W[cnd]) {
            T[pos] = T[cnd];

        // When the pattern breaks, establish the current prefix length as the fallback step.
        // We then repeatedly consult previously computed table values to dynamically shrink our candidate prefix until we find a valid sub-pattern or go through all options.
        } else {
            T[pos] = cnd;
            while (cnd >= 0 && W[pos] != W[cnd]) {
                cnd = T[cnd];
            }
        }

        // Advance both the table builder and candidate prefix pointers to continue to the next state.
        pos = pos + 1;
        cnd = cnd + 1;
    }

    // Store the final prefix length at the very end of the table.
    T[pos] = cnd;
    
    return T;
}

vector<int> kmp_search(const string& S, const string& W) {
    // Prepare a container to hold the starting indices of all successful matches. 
    // Immediately exit and return empty if the search string is blank, which prevents out of bounds errors during the table generation phase.
    vector<int> P;
    
    if (W.empty()) {
        return P;
    }

    // Initialize pointers for both the main text and the search word. 
    // Also pre-compute the failure table, essentially building a state machine that will guide the pattern pointer whenever we encounter a non-matching character.
    int j = 0;
    int k = 0;
    vector<int> T = kmp_table(W);

    // Begin scanning the target text from start to finish. 
    // Whenever the current text character aligns with the search pattern's active character, step both tracking pointers forward to verify the next character.
    while (j < S.length()) {
        if (W[k] == S[j]) {
            j = j + 1;
            k = k + 1;

            // Detect when pattern pointer has completely traversed the search word.
            // Calculate the starting index by subtracting the word length and record it.
            if (k == W.length()) {
                P.push_back(j - k);

                // To catch overlapping sequences (like finding "ABA" twice in "ABABA"), we avoid resetting the search entirely. 
                // Instead, use the table to rewind the pattern pointer to the last valid prefix boundary.
                k = T[k];
            }

        // When encountering a mismatch, consult the table to shift the pattern pointer backward only as much as necessary.
        } else {
            k = T[k];

            // If the pattern pointer falls below zero, it means search failed on the first character of the word. 
            // Shift text focus forward by one and realign the pattern to start over.
            if (k < 0) {
                j = j + 1;
                k = k + 1;
            }
        }
    }

    // Return the compiled list of valid starting indices. 
    return P;
}

// parsing code to handle input which will be the text on the first line, and the pattern on the second line
int main() {
    string S, W;
    getline(cin, S);
    getline(cin, W);

    vector<int> P = kmp_search(S, W);

    for (int i = 0; i < (int)P.size(); i++) {
        if (i > 0) cout << " ";
        cout << P[i];
    }
    cout << "\n";

    return 0;
}