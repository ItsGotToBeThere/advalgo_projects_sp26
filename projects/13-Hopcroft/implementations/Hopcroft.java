import java.io.*;
import java.util.*;

/**
 * Minimize a DFA using Hopcroft's algorithm.
 *
 * Hopcroft's algorithm works by iteratively refining a partition of states
 * into equivalence classes. Two states are "equivalent" if no input string
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
public class Hopcroft {

  // ---- Result container for a DFA ----
  static class DFA {
    Set<Integer> states;
    Set<String> alphabet;
    Map<Long, Integer> transitions; // encoded key: pack(source, symbolIndex)
    int start;
    Set<Integer> accepting;

    DFA(Set<Integer> states, Set<String> alphabet,
        Map<Long, Integer> transitions, int start, Set<Integer> accepting) {
      this.states = states;
      this.alphabet = alphabet;
      this.transitions = transitions;
      this.start = start;
      this.accepting = accepting;
    }
  }

  // ---- Encode a (state, symbolIndex) pair into a single long key ----
  // This avoids creating short-lived wrapper objects for the transition map.
  private static long pack(int state, int symbolIndex) {
    return ((long) state << 32) | (symbolIndex & 0xFFFFFFFFL);
  }

  /**
   * Core minimization routine.
   *
   * @param states      set of all DFA states
   * @param alphabet    list of input symbols (order gives each symbol an index)
   * @param transitions map from (state, symbolIndex) -> target state
   * @param start       the start state
   * @param accepting   set of accepting (final) states
   * @return a new DFA representing the minimized automaton
   */
  static DFA minimize(Set<Integer> states, List<String> alphabet,
      Map<Long, Integer> transitions, int start,
      Set<Integer> accepting) {

    // ---- Initial partition: accepting vs. rejecting ----
    // This is the base case: we *know* these two groups are distinguishable
    // (the empty string distinguishes them).
    Set<Integer> rejecting = new HashSet<>(states);
    rejecting.removeAll(accepting);

    // `partition` holds the current set of equivalence classes.
    // Each class is an unmodifiable set of states believed to be equivalent.
    List<Set<Integer>> partition = new ArrayList<>();
    if (!accepting.isEmpty())
      partition.add(new HashSet<>(accepting));
    if (!rejecting.isEmpty())
      partition.add(new HashSet<>(rejecting));

    // `worklist` tracks which groups still need to be tried as splitters.
    // A "splitter" is a group we use to test whether other groups can be
    // refined: if some states in a group transition into the splitter on a
    // given symbol and others don't, that group must be split.
    Deque<Set<Integer>> worklist = new ArrayDeque<>();
    if (!accepting.isEmpty())
      worklist.add(new HashSet<>(accepting));
    if (!rejecting.isEmpty())
      worklist.add(new HashSet<>(rejecting));

    // ---- Precompute inverse (backward) transitions ----
    // predecessors[symbolIndex][(targetState)] = {source states that go to
    // targetState on that symbol}.
    // This lets us quickly find, for a given splitter, which states lead
    // into it — without scanning the entire transition table each time.
    @SuppressWarnings("unchecked")
    Map<Integer, Set<Integer>>[] predecessors = new HashMap[alphabet.size()];
    for (int i = 0; i < alphabet.size(); i++) {
      predecessors[i] = new HashMap<>();
    }
    for (Map.Entry<Long, Integer> entry : transitions.entrySet()) {
      long key = entry.getKey();
      int source = (int) (key >> 32);
      int symIdx = (int) key;
      int target = entry.getValue();
      predecessors[symIdx]
          .computeIfAbsent(target, k -> new HashSet<>())
          .add(source);
    }

    // ---- Main refinement loop ----
    // Keep refining until no splitter can cause any further splits
    // (i.e., the partition has stabilized).
    while (!worklist.isEmpty()) {
      Set<Integer> splitter = worklist.poll();

      // Try each symbol independently — a group might need splitting
      // with respect to one symbol but not another.
      for (int symIdx = 0; symIdx < alphabet.size(); symIdx++) {

        // Gather every state that, on this symbol, transitions into
        // some state in the splitter. We're asking: "Who can reach
        // the splitter in one step on this symbol?"
        Set<Integer> sourcesIntoSplitter = new HashSet<>();
        for (int state : splitter) {
          Set<Integer> preds = predecessors[symIdx].get(state);
          if (preds != null) {
            sourcesIntoSplitter.addAll(preds);
          }
        }

        // If no state transitions into the splitter on this symbol,
        // there's nothing to distinguish — skip.
        if (sourcesIntoSplitter.isEmpty())
          continue;

        // Walk through every current group and check whether the
        // splitter divides it. We build a fresh partition each time
        // because groups may be added or removed during splitting.
        List<Set<Integer>> updatedPartition = new ArrayList<>();

        for (Set<Integer> group : partition) {
          // Intersect: states in this group that DO reach the splitter.
          Set<Integer> reach = new HashSet<>();
          // Difference: states in this group that do NOT reach it.
          Set<Integer> noReach = new HashSet<>();

          for (int s : group) {
            if (sourcesIntoSplitter.contains(s)) {
              reach.add(s);
            } else {
              noReach.add(s);
            }
          }

          if (!reach.isEmpty() && !noReach.isEmpty()) {
            // ---- Split required ----
            // These two subsets behave differently on `symbol` with
            // respect to the splitter, so they can't be equivalent.
            updatedPartition.add(reach);
            updatedPartition.add(noReach);

            if (worklist.remove(group)) {
              // The unsplit group was already queued as a future
              // splitter. Replace it with both halves so we don't
              // miss any refinement its sub-parts might cause.
              worklist.add(reach);
              worklist.add(noReach);
            } else {
              // The group wasn't in the worklist. A key insight of
              // Hopcroft's algorithm: we only need to add the
              // *smaller* half. The larger half's contribution is
              // already accounted for by the fact that the original
              // group was previously processed. This is what gives
              // the algorithm its O(n log n) per-symbol complexity.
              if (reach.size() <= noReach.size()) {
                worklist.add(reach);
              } else {
                worklist.add(noReach);
              }
            }
          } else {
            // ---- No split ----
            // Every state in the group agrees (all reach the splitter
            // or none do), so the group stays intact.
            updatedPartition.add(group);
          }
        }

        partition = updatedPartition;
      }
    }

    // ---- Build the minimized DFA from the final partition ----

    // Choose a canonical representative for each equivalence class.
    // We pick the smallest-numbered state for determinism and readability.
    Map<Integer, Integer> rep = new HashMap<>();
    for (Set<Integer> group : partition) {
      int minState = Collections.min(group);
      for (int s : group) {
        rep.put(s, minState);
      }
    }

    // The minimized state set is just the set of representatives.
    Set<Integer> minStates = new HashSet<>();
    for (int s : states)
      minStates.add(rep.get(s));

    // The start state maps to whatever representative its class has.
    int minStart = rep.get(start);

    // A representative is accepting if any (equivalently, all) members
    // of its class were accepting in the original DFA.
    Set<Integer> minAccepting = new HashSet<>();
    for (int s : accepting)
      minAccepting.add(rep.get(s));

    // Rebuild the transition function over representatives.
    // Duplicate entries (from merged states) will map to the same value,
    // so simple overwriting is fine.
    Map<Long, Integer> minTransitions = new HashMap<>();
    for (Map.Entry<Long, Integer> entry : transitions.entrySet()) {
      long key = entry.getKey();
      int source = (int) (key >> 32);
      int symIdx = (int) key;
      int target = entry.getValue();
      minTransitions.put(pack(rep.get(source), symIdx), rep.get(target));
    }

    return new DFA(minStates, new HashSet<>(alphabet), minTransitions, minStart, minAccepting);
  }

  // ---- File I/O ----

  /**
   * Read a DFA specification from a text file.
   *
   * Expected format (one item per line):
   * Line 1: three integers — number_of_states, alphabet_size, unused
   * Line 2: space-separated list of state identifiers (integers)
   * Line 3: space-separated list of alphabet symbols (strings)
   * Line 4: the start state (single integer)
   * Line 5: space-separated list of accepting states (integers)
   * Remaining lines: one transition per line as "source symbol destination"
   */
  static Object[] parseDFA(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename));

    // Header: counts used to know how many transitions to read.
    StringTokenizer st = new StringTokenizer(br.readLine());
    int n = Integer.parseInt(st.nextToken()); // number of states
    int s = Integer.parseInt(st.nextToken()); // alphabet size
    int a = Integer.parseInt(st.nextToken()); // (unused / number of accepting)

    // State set
    Set<Integer> states = new HashSet<>();
    st = new StringTokenizer(br.readLine());
    while (st.hasMoreTokens())
      states.add(Integer.parseInt(st.nextToken()));

    // Alphabet (order matters — index used as part of the transition key)
    List<String> alphabet = new ArrayList<>();
    st = new StringTokenizer(br.readLine());
    while (st.hasMoreTokens())
      alphabet.add(st.nextToken());

    // Build a quick symbol-to-index lookup
    Map<String, Integer> symToIdx = new HashMap<>();
    for (int i = 0; i < alphabet.size(); i++)
      symToIdx.put(alphabet.get(i), i);

    // Start state
    int start = Integer.parseInt(br.readLine().trim());

    // Accepting states
    Set<Integer> accepting = new HashSet<>();
    st = new StringTokenizer(br.readLine());
    while (st.hasMoreTokens())
      accepting.add(Integer.parseInt(st.nextToken()));

    // Read exactly n * s transitions (one for every state/symbol pair,
    // since DFAs must have total transition functions).
    Map<Long, Integer> transitions = new HashMap<>();
    for (int i = 0; i < n * s; i++) {
      st = new StringTokenizer(br.readLine());
      int src = Integer.parseInt(st.nextToken());
      String sym = st.nextToken();
      int dest = Integer.parseInt(st.nextToken());
      transitions.put(pack(src, symToIdx.get(sym)), dest);
    }
    br.close();

    return new Object[] { states, alphabet, transitions, start, accepting };
  }

  static String formatDFASchema(DFA dfa, List<String> alphabetOrder) {
    StringBuilder sb = new StringBuilder();

    List<Integer> sortedStates = new ArrayList<>(dfa.states);
    Collections.sort(sortedStates);

    List<Integer> sortedAccepting = new ArrayList<>(dfa.accepting);
    Collections.sort(sortedAccepting);

    // Line 1: N S A
    sb.append(sortedStates.size()).append(" ")
        .append(alphabetOrder.size()).append(" ")
        .append(sortedAccepting.size()).append("\n");

    // Line 2: states
    for (int i = 0; i < sortedStates.size(); i++) {
      if (i > 0)
        sb.append(" ");
      sb.append(sortedStates.get(i));
    }
    sb.append("\n");

    // Line 3: alphabet
    for (int i = 0; i < alphabetOrder.size(); i++) {
      if (i > 0)
        sb.append(" ");
      sb.append(alphabetOrder.get(i));
    }
    sb.append("\n");

    // Line 4: start state
    sb.append(dfa.start).append("\n");

    // Line 5: accepting states
    for (int i = 0; i < sortedAccepting.size(); i++) {
      if (i > 0)
        sb.append(" ");
      sb.append(sortedAccepting.get(i));
    }
    sb.append("\n");

    // Line 6+: transitions in state order, alphabet order
    for (int state : sortedStates) {
      for (int symIdx = 0; symIdx < alphabetOrder.size(); symIdx++) {
        int dest = dfa.transitions.get(pack(state, symIdx));
        sb.append(state).append(" ")
            .append(alphabetOrder.get(symIdx)).append(" ")
            .append(dest).append("\n");
      }
    }

    return sb.toString();
  }

  // ---- Entry point ----

  public static void main(String[] args) throws IOException {
    // Default to a test input if no file is given on the command line.
    String filename = args.length > 0 ? args[0] : "io/sample.sample.1.in";

    Object[] dfa = parseDFA(filename);
    @SuppressWarnings("unchecked")
    Set<Integer> states = (Set<Integer>) dfa[0];
    @SuppressWarnings("unchecked")
    List<String> alphabet = (List<String>) dfa[1];
    @SuppressWarnings("unchecked")
    Map<Long, Integer> trans = (Map<Long, Integer>) dfa[2];
    int startState = (int) dfa[3];
    @SuppressWarnings("unchecked")
    Set<Integer> accepting = (Set<Integer>) dfa[4];

    DFA result = minimize(states, alphabet, trans, startState, accepting);

    System.out.print(formatDFASchema(result, alphabet));
  }
}