#include <iostream>
#include <unordered_set>
#include <vector>

namespace {

using std::unordered_set;
using std::vector;

// input:
// proposers rank preference of all acceptors n x n 2d array
// acceptors rank preference of all proposers n x n 2d array
vector<int> gale_shapley(const vector<vector<int>> &proposers_pref,
                         const vector<vector<int>> &acceptors_pref) {
  auto n = proposers_pref.size();

  // keeps track of which proposers have formed a match,
  // proposers will be added as they match and removed if the acceptor receives
  // a more appealing offer (more prefered proposer)
  unordered_set<int> unmatched_proposers;
  for (auto i = 0; i < n; ++i) {
    unmatched_proposers.insert(i);
  }

  // indexed by proposer
  // stores which number preference is next to be offered
  // (i.e. if index i of the list is 0, proposer i will offer their first
  // prefered acceptor)
  vector<int> next_pref(n);

  // indexed by acceptor
  // stores which proposer they are currently matched to, n if not matched yet
  // (proposers are enumerated from 0 to n-1)
  vector<int> acceptor_matching(n, -1);

  // 2d array indexed by acceptor and proposer
  // specifies preference rank of the proposer for each acceptor
  // (i.e. acceptor_pref_matrix[0][1] = 0 means that proposer 1 is acceptor 0's
  // first preference)
  vector<vector<int>> acceptor_pref_ranking(n, vector<int>(n));
  for (auto i = 0; i < n; ++i) {
    for (auto j = 0; j < n; ++j) {
      acceptor_pref_ranking[i][acceptors_pref[i][j]] = j;
    }
  }

  // 2d array indexed by proposer and i (from 0 to n - 1)
  // specifies the ith preference of each proposer
  // (i.e. proposer_pref_matrix[0][1] = 0 means that proposer 0's second
  // preference is acceptor 0)
  const auto &proposer_pref_ranking = proposers_pref;

  // go until all proposers have a match, this results in proposer optimal
  // stable matching
  vector<int> matches(n, -1);
  while (!unmatched_proposers.empty()) {
    // list of proposers, who are all unmatched, that will give out a proposal
    // this round
    vector<int> proposers_this_round(unmatched_proposers.begin(),
                                     unmatched_proposers.end());
    for (auto proposer : proposers_this_round) {
      // retrieve the index of the proposers next prefered acceptor
      auto next_preferred_index = next_pref[proposer];
      ++next_pref[proposer];

      // get the next prefered acceptor
      auto next_preferred_acceptor =
          proposer_pref_ranking[proposer][next_preferred_index];

      // get the rank for the proposer of the next prefered acceptor
      auto acceptor_match_rank =
          acceptor_pref_ranking[next_preferred_acceptor][proposer];

      // get the next prefered acceptor's previous proposer and their rank to
      // the next prefered acceptor
      auto previous_proposer = acceptor_matching[next_preferred_acceptor];
      auto prev_acceptor_match_rank =
          previous_proposer != -1
              ? acceptor_pref_ranking[next_preferred_acceptor]
                                     [previous_proposer]
              : n;

      // if the next prefered acceptor prefers the proposer to their current
      // match, match with the proposer and unmatch with the previous proposer
      if (acceptor_match_rank < prev_acceptor_match_rank) {
        // unmatch the previous proposer if there is one, and add them to the
        // unmatched proposers set
        if (previous_proposer != -1) {
          unmatched_proposers.insert(previous_proposer);
          matches[previous_proposer] = -1;
        }

        // match with the proposer and remove them from the unmatched proposers
        // set
        unmatched_proposers.erase(proposer);

        acceptor_matching[next_preferred_acceptor] = proposer;
        matches[proposer] = next_preferred_acceptor;
      }
    }
  }
  // return the matches, where the index is the proposer and the value is the
  // acceptor they are matched to
  return matches;
}

} // namespace

// to run
// g++ -std=c++17 gs.cpp -o gs
// ./gs < sample.in.1
int main() {
  // Determines the size of the preference matrices (n x n).
  long long n;
  std::cin >> n;

  // Read in the preferences of the proposers.
  vector<vector<int>> proposers(n, vector<int>(n));
  for (auto i = 0; i < n; ++i) {
    for (auto j = 0; j < n; ++j) {
      std::cin >> proposers[i][j];
    }
  }

  // Read in the preferences of the acceptors.
  vector<vector<int>> acceptors(n, vector<int>(n));
  for (auto i = 0; i < n; ++i) {
    for (auto j = 0; j < n; ++j) {
      std::cin >> acceptors[i][j];
    }
  }

  // Find the stable matching and print the result.
  const auto &result = gale_shapley(proposers, acceptors);
  for (auto i = 0; i < n; ++i) {
    std::cout << result[i] << " ";
  }

  std::cout << '\n';

  return 0;
}
