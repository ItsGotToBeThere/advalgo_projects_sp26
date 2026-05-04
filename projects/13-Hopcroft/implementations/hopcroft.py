import sys


def hopcroft_minimize(states, alphabet, transitions, start, accepting):
    """
    Minimize a DFA using Hopcroft's algorithm.

    Hopcroft's algorithm works by iteratively refining a partition of states
    into equivalence classes. Two states are "equivalent" if no input string
    can distinguish them (i.e., cause one to accept and the other to reject).

    The algorithm starts with the coarsest useful partition — accepting vs.
    rejecting states — and repeatedly splits groups whenever some input symbol
    reveals that members of the group behave differently with respect to an
    existing partition block (the "splitter").

    Time complexity: O(|alphabet| · |states| · log |states|), achieved by
    always adding the smaller half of a split to the worklist.

    Args:
        states:      set of all DFA states (e.g. {0, 1, 2, 3, 4})
        alphabet:    set of input symbols (e.g. {'a', 'b'})
        transitions: dict mapping (state, symbol) -> state — must be total
                     (every state/symbol pair must have an entry)
        start:       the start state (a single element from `states`)
        accepting:   set of accepting (final) states, a subset of `states`

    Returns:
        A 5-tuple (states, alphabet, transitions, start, accepting)
        representing the minimized DFA, using the smallest-numbered member
        of each equivalence class as the canonical representative.
    """
    # The initial partition separates accepting from rejecting (non-accepting)
    # states. This is the base case: we *know* these two groups are
    # distinguishable (the empty string distinguishes them).
    rejecting = states - accepting

    # `partition` holds the current set of equivalence classes.
    # Each class is a frozenset of states believed to be equivalent.
    partition = set()
    if accepting:
        partition.add(frozenset(accepting))
    if rejecting:
        partition.add(frozenset(rejecting))

    # `worklist` tracks which groups still need to be tried as splitters.
    # A "splitter" is a group we use to test whether other groups can be
    # refined: if some states in a group transition into the splitter on a
    # given symbol and others don't, that group must be split.
    worklist = set()
    if accepting:
        worklist.add(frozenset(accepting))
    if rejecting:
        worklist.add(frozenset(rejecting))

    # Precompute inverse (backward) transitions for efficiency.
    # predecessors[(q, a)] = {set of states that transition to q on symbol a}
    # This lets us quickly find, for a given splitter, which states lead
    # into it — without scanning the entire transition table each time.
    predecessors = {}
    for (source, symbol), target in transitions.items():
        predecessors.setdefault((target, symbol), set()).add(source)

    # Main refinement loop: keep refining until no splitter can cause
    # any further splits (i.e., the partition has stabilized).
    while worklist:
        splitter = worklist.pop()

        # Try each symbol independently — a group might need splitting
        # with respect to one symbol but not another.
        for symbol in alphabet:
            # Gather every state that, on this symbol, transitions into
            # some state in the splitter. We're asking: "Who can reach
            # the splitter in one step on this symbol?"
            sources_into_splitter = set()
            for state in splitter:
                sources_into_splitter |= predecessors.get((state, symbol), set())

            # If no state transitions into the splitter on this symbol,
            # there's nothing to distinguish — skip.
            if not sources_into_splitter:
                continue

            # Walk through every current group and check whether the
            # splitter divides it. We build a fresh partition each time
            # because groups may be added or removed during splitting.
            updated_partition = set()
            for group in partition:
                # Intersect: states in this group that DO reach the splitter
                states_that_reach_splitter = group & sources_into_splitter
                # Difference: states in this group that do NOT reach it
                states_that_dont = group - sources_into_splitter

                if states_that_reach_splitter and states_that_dont:
                    # ---- Split required ----
                    # These two subsets behave differently on `symbol` with
                    # respect to the splitter, so they can't be equivalent.
                    updated_partition.add(frozenset(states_that_reach_splitter))
                    updated_partition.add(frozenset(states_that_dont))

                    if group in worklist:
                        # The unsplit group was already queued as a future
                        # splitter. Replace it with both halves so we don't
                        # miss any refinement its sub-parts might cause.
                        worklist.discard(group)
                        worklist.add(frozenset(states_that_reach_splitter))
                        worklist.add(frozenset(states_that_dont))
                    else:
                        # The group wasn't in the worklist. A key insight of
                        # Hopcroft's algorithm: we only need to add the
                        # *smaller* half. The larger half's contribution is
                        # already accounted for by the fact that the original
                        # group was previously processed. This is what gives
                        # the algorithm its O(n log n) per-symbol complexity.
                        if len(states_that_reach_splitter) <= len(states_that_dont):
                            worklist.add(frozenset(states_that_reach_splitter))
                        else:
                            worklist.add(frozenset(states_that_dont))
                else:
                    # ---- No split ----
                    # Every state in the group agrees (all reach the splitter
                    # or none do), so the group stays intact.
                    updated_partition.add(group)

            partition = updated_partition

    # ---- Build the minimized DFA from the final partition ----

    # Choose a canonical representative for each equivalence class.
    # We pick the smallest-numbered state for determinism and readability.
    state_to_representative = {}
    for group in partition:
        representative = min(group)
        for state in group:
            state_to_representative[state] = representative

    # The minimized state set is just the set of representatives.
    minimized_states = {state_to_representative[s] for s in states}

    # The start state maps to whatever representative its class has.
    minimized_start = state_to_representative[start]

    # A representative is accepting if any (equivalently, all) members
    # of its class were accepting in the original DFA.
    minimized_accepting = {state_to_representative[s] for s in accepting}

    # Rebuild the transition function over representatives.
    # Duplicate entries (from merged states) will map to the same value,
    # so simple overwriting is fine.
    minimized_transitions = {}
    for (source, symbol), target in transitions.items():
        new_source = state_to_representative[source]
        new_target = state_to_representative[target]
        minimized_transitions[(new_source, symbol)] = new_target

    return minimized_states, alphabet, minimized_transitions, minimized_start, minimized_accepting


def parse_dfa(filename):
    """
    Read a DFA specification from a text file.

    Expected format (one item per line):
        Line 1: three integers — number_of_states, alphabet_size, number_of_transitions
        Line 2: space-separated list of state identifiers (integers)
        Line 3: space-separated list of alphabet symbols (strings)
        Line 4: the start state (single integer)
        Line 5: space-separated list of accepting states (integers)
        Remaining lines: one transition per line as "source symbol destination"
    """
    with open(filename) as f:
        # Header: counts used to know how many transitions to read
        n, s, a = map(int, f.readline().split())
        states = set(int(x) for x in f.readline().split())
        alphabet = f.readline().split()
        start = int(f.readline().strip())
        accepting = set(int(x) for x in f.readline().split())

        # Read exactly n * s transitions (one for every state/symbol pair,
        # since DFAs must have total transition functions).
        transitions = {}
        for _ in range(n * s):
            parts = f.readline().split()
            src, symbol, dest = int(parts[0]), parts[1], int(parts[2])
            transitions[(src, symbol)] = dest

    return states, alphabet, transitions, start, accepting

def format_dfa_schema(states, alphabet, transitions, start, accepting):
    """
    Return a DFA formatted in the same schema as the input file.

    Output format:
    Line 1: N S A
    Line 2: states
    Line 3: alphabet
    Line 4: start state
    Line 5: accepting states
    Line 6+: transitions as 'src symbol dest'
    """
    sorted_states = sorted(states)
    alphabet_list = list(alphabet)
    sorted_accepting = sorted(accepting)

    lines = []

    # Line 1: N S A
    lines.append(f"{len(sorted_states)} {len(alphabet_list)} {len(sorted_accepting)}")

    # Line 2: states
    lines.append(" ".join(map(str, sorted_states)))

    # Line 3: alphabet
    lines.append(" ".join(map(str, alphabet_list)))

    # Line 4: start state
    lines.append(str(start))

    # Line 5: accepting states
    lines.append(" ".join(map(str, sorted_accepting)))

    # Line 6+: transitions
    for state in sorted_states:
        for symbol in alphabet_list:
            dest = transitions[(state, symbol)]
            lines.append(f"{state} {symbol} {dest}")

    return "\n".join(lines)

def main():
    filename = sys.argv[1] if len(sys.argv) > 1 else "io/sample.in.1"
    states, alphabet, transitions, start, accepting = parse_dfa(filename)

    min_states, min_alpha, min_trans, min_start, min_acc = hopcroft_minimize(
        states, alphabet, transitions, start, accepting
    )

    print(format_dfa_schema(min_states, min_alpha, min_trans, min_start, min_acc))

if __name__ == "__main__":
    main()