Project 08 - Bloom Filter
===============================

Go [back to home page](../../index.html)

<a name="overview"></a>Executive Summary
---------------------------------------

- [Executive Summary](./executiveSummary.pdf)

<a name="overview"></a>Implementation
---------------------------------------

- Implementations:
	- [C++](./implementation/bloomFilter.cpp)
	- [Java](./implementation/BloomFilter.java)
	- [Python](./implementation/BloomFilter.py)
- I/O (three separate cases; first line of each test file is the case count, usually `1`)
	- Tests: [sample.in.1](./implementation/io/sample.in.1), [sample.in.2](./implementation/io/sample.in.2), [sample.in.3](./implementation/io/sample.in.3)
	- Python expected: [expected_output_python_1.txt](./implementation/io/expected_output_python_1.txt), [_2](./implementation/io/expected_output_python_2.txt), [_3](./implementation/io/expected_output_python_3.txt)
	- Java expected: [expected_output_java_1.txt](./implementation/io/expected_output_java_1.txt), [_2](./implementation/io/expected_output_java_2.txt), [_3](./implementation/io/expected_output_java_3.txt)
	- C++ expected: [expected_output_cpp_1.txt](./implementation/io/expected_output_cpp_1.txt), [_2](./implementation/io/expected_output_cpp_2.txt), [_3](./implementation/io/expected_output_cpp_3.txt) (placeholder)

**Implementation note.** Different hash schemes change false positives vs absent; compare each language to its own expected file. Example: `python3 BloomFilter.py implementation/io/sample.in.1` then diff against `expected_output_python_1.txt`.

<a name="overview"></a>Slides
---------------------------------------

- [Presentation Slides](./slides/FinalPresentation_BloomFilter.pptx)


<a name="overview"></a>Programming Challenge
---------------------------------------

- [Programming Challenge](./programmingChallenge/problem_statement.pdf)
	- Solutions:
		- [Python](./programmingChallenge/solutions/pcSol_python.py)
	- Test Cases:
		- [Case 1 input](./programmingChallenge/io/testcase_01_smoke_baseline.in)
		- [Case 1 output](./programmingChallenge/io/testcase_01_smoke_baseline.out)
		- [Case 2 input](./programmingChallenge/io/testcase_02_all_allowed_no_hits.in)
		- [Case 2 output](./programmingChallenge/io/testcase_02_all_allowed_no_hits.out)
		- [Case 3 input](./programmingChallenge/io/testcase_03_all_blocked.in)
		- [Case 3 output](./programmingChallenge/io/testcase_03_all_blocked.out)
		- [Case 4 input](./programmingChallenge/io/testcase_04_empty_or_whitespace_queries.in)
		- [Case 4 output](./programmingChallenge/io/testcase_04_empty_or_whitespace_queries.out)
		- [Case 5 input](./programmingChallenge/io/testcase_05_single_word_tokens_low_epsilon.in)
		- [Case 5 output](./programmingChallenge/io/testcase_05_single_word_tokens_low_epsilon.out)
		- [Case 6 input](./programmingChallenge/io/testcase_06_n100_m500_repeated_titles.in)
		- [Case 6 output](./programmingChallenge/io/testcase_06_n100_m500_repeated_titles.out)
		- [Case 7 input](./programmingChallenge/io/testcase_07_case_sensitive_tokens.in)
		- [Case 7 output](./programmingChallenge/io/testcase_07_case_sensitive_tokens.out)
		- [Case 8 input](./programmingChallenge/io/testcase_08_very_long_token_200char.in)
		- [Case 8 output](./programmingChallenge/io/testcase_08_very_long_token_200char.out)
		- [Case 9 input](./programmingChallenge/io/testcase_09_punctuation_dashes_and_hashes.in)
		- [Case 9 output](./programmingChallenge/io/testcase_09_punctuation_dashes_and_hashes.out)
		- [Case 10 input](./programmingChallenge/io/testcase_10_high_epsilon_28.in)
		- [Case 10 output](./programmingChallenge/io/testcase_10_high_epsilon_28.out)
		- [Case 11 input](./programmingChallenge/io/testcase_11_n12_m400_many_queries.in)
		- [Case 11 output](./programmingChallenge/io/testcase_11_n12_m400_many_queries.out)
		- [Case 12 input](./programmingChallenge/io/testcase_12_m1000_throughput_stress.in)
		- [Case 12 output](./programmingChallenge/io/testcase_12_m1000_throughput_stress.out)
		- [Case 13 input](./programmingChallenge/io/testcase_13_n200_m800_synthetic_blocklist.in)
		- [Case 13 output](./programmingChallenge/io/testcase_13_n200_m800_synthetic_blocklist.out)
		- [Case 14 input](./programmingChallenge/io/testcase_14_sparse_blocklist_hits.in)
		- [Case 14 output](./programmingChallenge/io/testcase_14_sparse_blocklist_hits.out)
		- [Case 15 input](./programmingChallenge/io/testcase_15_utf8_beyond_ascii.in)
		- [Case 15 output](./programmingChallenge/io/testcase_15_utf8_beyond_ascii.out)
		- [Case 16 input](./programmingChallenge/io/testcase_16_mixed_query_line_lengths.in)
		- [Case 16 output](./programmingChallenge/io/testcase_16_mixed_query_line_lengths.out)
		- [Case 17 input](./programmingChallenge/io/testcase_17_sequential_id_blocklist.in)
		- [Case 17 output](./programmingChallenge/io/testcase_17_sequential_id_blocklist.out)
		- [Case 18 input](./programmingChallenge/io/testcase_18_n400_m800_soak.in)
		- [Case 18 output](./programmingChallenge/io/testcase_18_n400_m800_soak.out)
		- [Case 19 input](./programmingChallenge/io/testcase_19_epsilon_29_boundary.in)
		- [Case 19 output](./programmingChallenge/io/testcase_19_epsilon_29_boundary.out)
		- [Case 20 input](./programmingChallenge/io/testcase_20_many_tokens_per_request_line.in)
		- [Case 20 output](./programmingChallenge/io/testcase_20_many_tokens_per_request_line.out)
	- More: [descriptions of each test case (README)](./programmingChallenge/io/README.md) · [sample handout I/O (illustration only)](./programmingChallenge/io/sample_illustration.in) / [out](./programmingChallenge/io/sample_illustration.out)
