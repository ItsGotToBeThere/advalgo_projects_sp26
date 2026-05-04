/**
 * Minimize a DFA using Hopcroft's algorithm.
 *
 * Hopcroft's algorithm works by iteratively refining a partition of states
 * into equivalence classes.  Two states are "equivalent" if no input string
 * can distinguish them (i.e., cause one to accept and the other to reject).
 *
 * The algorithm starts with the coarsest useful partition — accepting vs.
 * rejecting states — and repeatedly splits groups whenever some input symbol
 * reveals that members of the group behave differently with respect to an
 * existing partition block (the "splitter").
 *
 * Time complexity: O(|alphabet| · |states| · log |states|), achieved by
 * always adding the smaller half of a split to the worklist.
 */

#include <algorithm>
#include <cstdio>
#include <fstream>
#include <iostream>
#include <map>
#include <queue>
#include <set>
#include <sstream>
#include <string>
#include <unordered_map>
#include <unordered_set>
#include <vector>

// ---- DFA representation ----
struct DFA {
    std::set<int> states;
    std::vector<std::string> alphabet;
    // transitions[(source, symbolIndex)] = target state
    std::map<std::pair<int, int>, int> transitions;
    int start;
    std::set<int> accepting;
};

// ---- Hopcroft minimization ----

/**
 * Core minimization routine.
 *
 * @param dfa  A fully-specified DFA (total transition function).
 * @return     A new DFA representing the minimized automaton, using the
 *             smallest-numbered member of each equivalence class as the
 *             canonical representative.
 */
DFA hopcroft_minimize(const DFA& dfa) {
    // ---- Initial partition: accepting vs. rejecting ----
    // This is the base case: we *know* these two groups are distinguishable
    // (the empty string distinguishes them).
    std::set<int> rejecting;
    for (int s : dfa.states) {
        if (dfa.accepting.find(s) == dfa.accepting.end()) {
            rejecting.insert(s);
        }
    }

    // We identify each group by a unique integer index so that we can
    // cheaply store and compare groups without copying entire sets.
    // `groups` is our pool of living sets; `partition` lists which
    // group-indices currently form the partition.
    std::vector<std::set<int>> groups;  // index -> member set
    std::vector<int> partition;         // active group indices

    auto add_group = [&](const std::set<int>& members) -> int {
        int id = static_cast<int>(groups.size());
        groups.push_back(members);
        return id;
    };

    // Seed the partition with the two initial groups.
    if (!dfa.accepting.empty()) {
        int id = add_group(dfa.accepting);
        partition.push_back(id);
    }
    if (!rejecting.empty()) {
        int id = add_group(rejecting);
        partition.push_back(id);
    }

    // `worklist` tracks which groups still need to be tried as splitters.
    // A "splitter" is a group we use to test whether other groups can be
    // refined: if some states in a group transition into the splitter on a
    // given symbol and others don't, that group must be split.
    std::set<int> inWorklist;  // group indices currently in the worklist
    std::queue<int> worklist;  // FIFO of group indices
    for (int id : partition) {
        worklist.push(id);
        inWorklist.insert(id);
    }

    // ---- Precompute inverse (backward) transitions ----
    // predecessors[symbolIndex][targetState] = {source states that go to
    //     targetState on that symbol}.
    // This lets us quickly find, for a given splitter, which states lead
    // into it — without scanning the entire transition table each time.
    int numSymbols = static_cast<int>(dfa.alphabet.size());
    std::vector<std::unordered_map<int, std::vector<int>>> predecessors(numSymbols);

    for (auto& [key, target] : dfa.transitions) {
        int source = key.first;
        int symIdx = key.second;
        predecessors[symIdx][target].push_back(source);
    }

    // ---- Main refinement loop ----
    // Keep refining until no splitter can cause any further splits
    // (i.e., the partition has stabilized).
    while (!worklist.empty()) {
        int splitterIdx = worklist.front();
        worklist.pop();
        inWorklist.erase(splitterIdx);

        const std::set<int> splitter = groups[splitterIdx];

        // Try each symbol independently — a group might need splitting
        // with respect to one symbol but not another.
        for (int symIdx = 0; symIdx < numSymbols; symIdx++) {
            // Gather every state that, on this symbol, transitions into
            // some state in the splitter.  We're asking: "Who can reach
            // the splitter in one step on this symbol?"
            std::unordered_set<int> sourcesIntoSplitter;
            for (int state : splitter) {
                auto it = predecessors[symIdx].find(state);
                if (it != predecessors[symIdx].end()) {
                    for (int src : it->second) {
                        sourcesIntoSplitter.insert(src);
                    }
                }
            }

            // If no state transitions into the splitter on this symbol,
            // there's nothing to distinguish — skip.
            if (sourcesIntoSplitter.empty()) continue;

            // Walk through every current group and check whether the
            // splitter divides it.  We build a fresh partition list each
            // time because groups may be added or removed during splitting.
            std::vector<int> updatedPartition;

            for (int gid : partition) {
                const std::set<int>& group = groups[gid];

                // Intersect: states in this group that DO reach the splitter.
                std::set<int> reach;
                // Difference: states in this group that do NOT reach it.
                std::set<int> noReach;

                for (int s : group) {
                    if (sourcesIntoSplitter.count(s)) {
                        reach.insert(s);
                    } else {
                        noReach.insert(s);
                    }
                }

                if (!reach.empty() && !noReach.empty()) {
                    // ---- Split required ----
                    // These two subsets behave differently on this symbol with
                    // respect to the splitter, so they can't be equivalent.
                    int reachId = add_group(reach);
                    int noReachId = add_group(noReach);
                    updatedPartition.push_back(reachId);
                    updatedPartition.push_back(noReachId);

                    if (inWorklist.count(gid)) {
                        // The unsplit group was already queued as a future
                        // splitter.  Replace it with both halves so we don't
                        // miss any refinement its sub-parts might cause.
                        // (We can't easily remove from std::queue, so we just
                        //  add both and let stale entries be skipped.)
                        inWorklist.erase(gid);
                        worklist.push(reachId);
                        inWorklist.insert(reachId);
                        worklist.push(noReachId);
                        inWorklist.insert(noReachId);
                    } else {
                        // The group wasn't in the worklist.  A key insight of
                        // Hopcroft's algorithm: we only need to add the
                        // *smaller* half.  The larger half's contribution is
                        // already accounted for by the fact that the original
                        // group was previously processed.  This is what gives
                        // the algorithm its O(n log n) per-symbol complexity.
                        if (reach.size() <= noReach.size()) {
                            worklist.push(reachId);
                            inWorklist.insert(reachId);
                        } else {
                            worklist.push(noReachId);
                            inWorklist.insert(noReachId);
                        }
                    }
                } else {
                    // ---- No split ----
                    // Every state in the group agrees (all reach the splitter
                    // or none do), so the group stays intact.
                    updatedPartition.push_back(gid);
                }
            }

            partition = updatedPartition;
        }
    }

    // ---- Build the minimized DFA from the final partition ----

    // Choose a canonical representative for each equivalence class.
    // We pick the smallest-numbered state for determinism and readability.
    std::unordered_map<int, int> rep;  // original state -> representative
    for (int gid : partition) {
        int minState = *groups[gid].begin();  // std::set is sorted
        for (int s : groups[gid]) {
            rep[s] = minState;
        }
    }

    DFA result;
    result.alphabet = dfa.alphabet;

    // The minimized state set is just the set of representatives.
    for (int s : dfa.states) result.states.insert(rep[s]);

    // The start state maps to whatever representative its class has.
    result.start = rep[dfa.start];

    // A representative is accepting if any (equivalently, all) members
    // of its class were accepting in the original DFA.
    for (int s : dfa.accepting) result.accepting.insert(rep[s]);

    // Rebuild the transition function over representatives.
    // Duplicate entries (from merged states) will map to the same value,
    // so simple overwriting is fine.
    for (auto& [key, target] : dfa.transitions) {
        int newSrc = rep[key.first];
        int newTgt = rep[target];
        result.transitions[{newSrc, key.second}] = newTgt;
    }

    return result;
}

// ---- File I/O ----

/**
 * Read a DFA specification from a text file.
 *
 * Expected format (one item per line):
 *   Line 1: three integers — number_of_states, alphabet_size, unused
 *   Line 2: space-separated list of state identifiers (integers)
 *   Line 3: space-separated list of alphabet symbols (strings)
 *   Line 4: the start state (single integer)
 *   Line 5: space-separated list of accepting states (integers)
 *   Remaining lines: one transition per line as "source symbol destination"
 */
DFA parse_dfa(const std::string& filename) {
    std::ifstream fin(filename);
    if (!fin.is_open()) {
        std::cerr << "Error: cannot open " << filename << "\n";
        std::exit(1);
    }

    DFA dfa;

    // Header: counts used to know how many transitions to read.
    int n, s, a;
    fin >> n >> s >> a;

    // State set
    for (int i = 0; i < n; i++) {
        int st;
        fin >> st;
        dfa.states.insert(st);
    }

    // Alphabet (order matters — index used as part of the transition key)
    std::unordered_map<std::string, int> symToIdx;
    for (int i = 0; i < s; i++) {
        std::string sym;
        fin >> sym;
        symToIdx[sym] = i;
        dfa.alphabet.push_back(sym);
    }

    // Start state
    fin >> dfa.start;

    // Accepting states
    for (int i = 0; i < a; i++) {
        int st;
        fin >> st;
        dfa.accepting.insert(st);
    }

    // Read exactly n * s transitions (one for every state/symbol pair,
    // since DFAs must have total transition functions).
    for (int i = 0; i < n * s; i++) {
        int src, dest;
        std::string sym;
        fin >> src >> sym >> dest;
        dfa.transitions[{src, symToIdx[sym]}] = dest;
    }

    fin.close();
    return dfa;
}

std::string format_dfa_schema(const DFA& dfa) {
    std::ostringstream out;

    // Line 1: N S A
    out << dfa.states.size() << " "
        << dfa.alphabet.size() << " "
        << dfa.accepting.size() << "\n";

    // Line 2: states
    bool first = true;
    for (int state : dfa.states) {
        if (!first) out << " ";
        out << state;
        first = false;
    }
    out << "\n";

    // Line 3: alphabet
    for (size_t i = 0; i < dfa.alphabet.size(); i++) {
        if (i > 0) out << " ";
        out << dfa.alphabet[i];
    }
    out << "\n";

    // Line 4: start state
    out << dfa.start << "\n";

    // Line 5: accepting states
    first = true;
    for (int state : dfa.accepting) {
        if (!first) out << " ";
        out << state;
        first = false;
    }
    out << "\n";

    // Line 6+: transitions in state order, alphabet order
    for (int state : dfa.states) {
        for (int symIdx = 0; symIdx < (int)dfa.alphabet.size(); symIdx++) {
            int dest = dfa.transitions.at({state, symIdx});
            out << state << " " << dfa.alphabet[symIdx] << " " << dest << "\n";
        }
    }

    return out.str();
}

// ---- Entry point ----

int main(int argc, char* argv[]) {
    // Default to a test input if no file is given on the command line.
    std::string filename = (argc > 1) ? argv[1] : "io/sample.in.1";

    DFA dfa = parse_dfa(filename);

    DFA minimized = hopcroft_minimize(dfa);
    std::cout << format_dfa_schema(minimized);

    return 0;
}