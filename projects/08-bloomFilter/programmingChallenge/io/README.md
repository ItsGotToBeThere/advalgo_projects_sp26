# Programming challenge I/O

Each test is a **pair of files with the same basename**: `testcase_NN_description.in` and `testcase_NN_description.out`.  
The `.out` file is the expected output for running a correct solution on the matching `.in` file (same order: one line per request line).

| # | Input file | Output file | What it stresses |
|---|------------|-------------|------------------|
| 1 | `testcase_01_smoke_baseline.in` | `testcase_01_smoke_baseline.out` | Small `n`, mixed hits and empty query; smoke test |
| 2 | `testcase_02_all_allowed_no_hits.in` | `testcase_02_all_allowed_no_hits.out` | Queries avoid blocklist tokens (no true hits) |
| 3 | `testcase_03_all_blocked.in` | `testcase_03_all_blocked.out` | Every query embeds a blocklist token |
| 4 | `testcase_04_empty_or_whitespace_queries.in` | `testcase_04_empty_or_whitespace_queries.out` | Empty and whitespace-only lines (no tokens) |
| 5 | `testcase_05_single_word_tokens_low_epsilon.in` | `testcase_05_single_word_tokens_low_epsilon.out` | Single-word queries; low ε |
| 6 | `testcase_06_n100_m500_repeated_titles.in` | `testcase_06_n100_m500_repeated_titles.out` | Medium blocklist, many repeated query patterns |
| 7 | `testcase_07_case_sensitive_tokens.in` | `testcase_07_case_sensitive_tokens.out` | Blocklist vs query case must differ |
| 8 | `testcase_08_very_long_token_200char.in` | `testcase_08_very_long_token_200char.out` | 200+ character token hashing |
| 9 | `testcase_09_punctuation_dashes_and_hashes.in` | `testcase_09_punctuation_dashes_and_hashes.out` | Hyphens, `#`, and split/s token boundaries |
| 10 | `testcase_10_high_epsilon_28.in` | `testcase_10_high_epsilon_28.out` | High ε (more false positive pressure) |
| 11 | `testcase_11_n12_m400_many_queries.in` | `testcase_11_n12_m400_many_queries.out` | Small blocklist, large query count |
| 12 | `testcase_12_m1000_throughput_stress.in` | `testcase_12_m1000_throughput_stress.out` | **1000** query lines (throughput) |
| 13 | `testcase_13_n200_m800_synthetic_blocklist.in` | `testcase_13_n200_m800_synthetic_blocklist.out` | Large `n` and `M_q` |
| 14 | `testcase_14_sparse_blocklist_hits.in` | `testcase_14_sparse_blocklist_hits.out` | Most lines clean, few true hits |
| 15 | `testcase_15_utf8_beyond_ascii.in` | `testcase_15_utf8_beyond_ascii.out` | UTF-8 blocklist and query text |
| 16 | `testcase_16_mixed_query_line_lengths.in` | `testcase_16_mixed_query_line_lengths.out` | Short vs long titles, many tokens |
| 17 | `testcase_17_sequential_id_blocklist.in` | `testcase_17_sequential_id_blocklist.out` | Dense id-style keys, prefix/suffix padding |
| 18 | `testcase_18_n400_m800_soak.in` | `testcase_18_n400_m800_soak.out` | Near-max `n`, many queries (soak) |
| 19 | `testcase_19_epsilon_29_boundary.in` | `testcase_19_epsilon_29_boundary.out` | ε = 29 (upper band of spec) |
| 20 | `testcase_20_many_tokens_per_request_line.in` | `testcase_20_many_tokens_per_request_line.out` | **15+ tokens/line**; few intentional hits (many `check` calls) |

**Illustration handout example** (format only; not counted in the 20 grader cases above): `sample_illustration.in` / `sample_illustration.out`

**Check a solution (example):**

```bash
cd solutions && pip3 install -r requirements.txt
python3 pcSol_python.py < ../io/testcase_01_smoke_baseline.in | diff - ../io/testcase_01_smoke_baseline.out
```

Expected: no diff output.
