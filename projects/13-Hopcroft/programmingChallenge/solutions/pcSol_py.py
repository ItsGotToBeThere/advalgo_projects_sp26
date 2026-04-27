import heapq
import sys
from collections import defaultdict, deque
from math import log2, floor

def solve(input_str):
    tokens = iter(input_str.split())

    def next_int():
        return int(next(tokens))

    num_states = next_int()
    alphabet_size = next_int()
    num_accepting = next_int()

    accepting = set(next_int() for _ in range(num_accepting))

    # transitions[s][c]      = next state from s on symbol c
    # inspection_costs[s][c] = inspection cost of taking that transition
    transitions = [[0] * alphabet_size for _ in range(num_states)]
    inspection_costs = [[0] * alphabet_size for _ in range(num_states)]
    for state in range(num_states):
        for symbol in range(alphabet_size):
            transitions[state][symbol] = next_int()
            inspection_costs[state][symbol] = next_int()

    # forward reachability from the start state (state 0)
    reachable = [False] * num_states
    reachable[0] = True
    bfs_queue = deque([0])
    while bfs_queue:
        state = bfs_queue.popleft()
        for symbol in range(alphabet_size):
            successor = transitions[state][symbol]
            if not reachable[successor]:
                reachable[successor] = True
                bfs_queue.append(successor)

    # build reverse adjacency, restricted to reachable states
    # reverse_predecessors[target] = list of (source, edge_cost) such that
    # transition(source, c) = target with cost edge_cost, for some symbol c.
    reverse_predecessors = [[] for _ in range(num_states)]
    for source in range(num_states):
        if not reachable[source]:
            continue
        for symbol in range(alphabet_size):
            target = transitions[source][symbol]
            reverse_predecessors[target].append((source, inspection_costs[source][symbol]))

    # multi-source Dijkstra from accepting states on reverse graph
    min_cost_to_accept = [float("inf")] * num_states
    worklist = []
    for accepting_state in accepting:
        if reachable[accepting_state]:
            min_cost_to_accept[accepting_state] = 0
            heapq.heappush(worklist, (0, accepting_state))

    while worklist:
        current_dist, current_state = heapq.heappop(worklist)
        if current_dist > min_cost_to_accept[current_state]:
            continue
        for predecessor, edge_cost in reverse_predecessors[current_state]:
            new_dist = current_dist + edge_cost
            if new_dist < min_cost_to_accept[predecessor]:
                min_cost_to_accept[predecessor] = new_dist
                heapq.heappush(worklist, (new_dist, predecessor))

    # cost classes
    def cost_class_of(state):
        distance = min_cost_to_accept[state]
        if distance == float("inf"):
            return -1
        return floor(log2(1 + distance))

    # Hopcroft's algorithm with refined initial partition
    states = [s for s in range(num_states) if reachable[s]]
    if not states:
        return 0

    # Group reachable states by (is_accepting, cost_class) to form initial groups.
    initial_partition = defaultdict(list)
    for state in states:
        key = (state in accepting, cost_class_of(state))
        initial_partition[key].append(state)

    # Each partition group is a set of states; state_to_block[s] tracks which group each
    # state currently belongs to (mutated during refinement).
    partition = []
    state_to_block = [-1] * num_states
    for group in initial_partition.values():
        new_block_id = len(partition)
        partition.append(set(group))
        for state in group:
            state_to_block[state] = new_block_id

    # Reverse adjacency used during partition refinement:
    # predecessors[(target, symbol)] = list of source states with
    # transition(source, symbol) = target, restricted to reachable sources.
    predecessors = defaultdict(list)
    for source in states:
        for symbol in range(alphabet_size):
            target = transitions[source][symbol]
            predecessors[(target, symbol)].append(source)

    # Hopcroft worklist: splitter block ids still to process.
    # With a multi-block initial partition (refined by cost class), seed all
    # initial blocks to guarantee correctness.
    worklist = set()

    for block_id in range(len(partition)):
        worklist.add(block_id)

    while worklist:
        splitter_block_id = worklist.pop()
        splitter = partition[splitter_block_id]

        # Try each symbol independently — a block may split on one symbol
        # but stay intact on others.
        for symbol in range(alphabet_size):
            # Gather every state that, on this symbol, transitions into the splitter.
            sources_into_splitter = set()
            for target_state in splitter:
                sources_into_splitter.update(predecessors.get((target_state, symbol), ()))

            # If no state transitions into the splitter on this symbol, skip.
            if not sources_into_splitter:
                continue

            # Group sources by their current block, so we split only touched blocks.
            sources_into_splitter_by_group = defaultdict(list)
            for source_state in sources_into_splitter:
                sources_into_splitter_by_group[state_to_block[source_state]].append(source_state)

            for group_id, sources_into_splitter_list in sources_into_splitter_by_group.items():
                group = partition[group_id]
                if len(sources_into_splitter_list) == len(group):
                    # The whole block maps into the splitter — no split needed.
                    continue

                # Split group into states that do/don't transition into splitter.
                states_that_reach_splitter = set(sources_into_splitter_list)
                states_that_dont = group - states_that_reach_splitter

                # Keep the larger piece in the original block id; move the smaller
                # piece into a freshly allocated block.
                if len(states_that_reach_splitter) <= len(states_that_dont):
                    smaller_piece, larger_piece = states_that_reach_splitter, states_that_dont
                else:
                    smaller_piece, larger_piece = states_that_dont, states_that_reach_splitter

                new_block_id = len(partition)
                partition.append(smaller_piece)
                partition[group_id] = larger_piece
                for state in smaller_piece:
                    state_to_block[state] = new_block_id

                # Update worklist per Hopcroft's rule:
                #   - If group_id was already pending, adding new_block_id keeps both halves pending.
                #   - Otherwise, add only the smaller half (new_block_id).
                worklist.add(new_block_id)

    # Count non-empty groups (all should be non-empty by construction).
    return sum(1 for group in partition if group)

def main():
    print(solve(sys.stdin.read()))

if __name__ == "__main__":
    main()
