PC Test Cases 

- Each input txt file starts with Q, the number of operations.
- Only query operations produce output lines: SEARCH, KTH, COUNT, RANGE, SUM.
- SEARCH outputs lowercase true/false.
- For RANGE l r, if l > r then the expected output is 0.

Files are named with a the number of the test and a short description of the edge case being tested.

Case index:
- `01_empty_tree_all_queries`: all query types on an empty set (includes reversed RANGE).
- `02_basic_build_then_queries`: insert a small set, then SEARCH and KTH and COUNT and RANGE.
- `03_reversed_range_after_small_build`: small build, then reversed RANGE, plus normal RANGE and COUNT and KTH.
- `04_edge_queries_full_span`: edge queries after build (KTH n, COUNT at/above max, full-span RANGE).
- `05_delete_on_empty_then_queries`: delete on empty (should do nothing), then query behavior.
- `06_single_element_edges`: single-key set, boundary behavior for SEARCH and KTH and COUNT and RANGE.
- `07_repeat_delete_idempotent`: delete the same key twice (second delete does nothing), then queries.
- `08_delete_leaf_node`: delete a leaf key, then verify with queries that our custom fucntions work.
- `09_delete_node_with_two_children`: delete basically the “middle” key from a larger set, then verify with queries.
- `10_delete_nonexistent_key`: delete missing key (does nothing), and then queries unchanged.
- `11_range_single_point_present_absent`: RANGE between the same numbers (for example [5,5]) absent vs present, plus full-span range.
- `12_count_below_minimum`: COUNT below min, at min, and between keys.
- `13_count_between_values`: COUNT between keys and exactly on keys.
- `14_range_outside_bounds`: RANGE fully below min, above max, and covering all.
- `15_increasing_inserts_then_queries`: insert increasing sequence (worst case for BST), then queries.
- `16_decreasing_inserts_then_queries`: insert decreasing sequence (worst case for BST), then queries.
- `17_mixed_updates_then_queries`: mixed insert/delete/insert churn, then queries.
- `18_sum_focus`: It is designed to catch: normal interval sums, full-span sums, single-element sums, empty-range sums, reversed-range behavior, sum after deletion, sum when the queried interval has no elements.
- speed test: `19_speed_bulk_insert_delete_prefix`: humongous insert then bulk delete some starting values, then a few queries (includes reversed RANGE).
- speed test: `20_speed_two_phase_delete_reinsert_block`: insert all, delete a block, reinsert part of it, then queries (includes reversed RANGE).

specific speed-focused cases:
- I created 19 and 20 tc to use bulk inserts/deletes (thousands of operations) to discourage slow per-operation linear scans. We'll test if the treap is optimal and fast!
