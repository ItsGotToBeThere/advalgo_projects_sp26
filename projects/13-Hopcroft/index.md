Project 13 - Hopcroft's Algorithm for DFA Minimization
===============================

By: Yu Cao, Brian Tran, Yog Tadhani

Go [back to home page](../../index.html)

<a name="overview"></a>Executive Summary
---------------------------------------

- [Executive Summary](./executiveSummary.pdf)

<a name="overview"></a>Implementation
---------------------------------------

- Implementations:
	- [C++](./implementations/hopcroft.cpp)
	- [Java](./implementations/Hopcroft.java)
	- [Python](./implementations/hopcroft.py)
	- io
		- [test input 1](./implementations/io/sample.in.1)
		- [test output 1](./implementations/io/sample.out.1)
		- [test input 2](./implementations/io/sample.in.2)
		- [test output 2](./implementations/io/sample.out.2)
		- [test input 3](./implementations/io/sample.in.3)
		- [test output 3](./implementations/io/sample.out.3)

Input Schema:
- Line 1: N S A (num states, alphabet size, num accepting states)
- Line 2: c1 c2 ... cN (state symbols, space-separated)
- Line 3: p1 p2 ... pS (alphabet symbols, space-seperated)
- Line 4: start_state
- Line 5: a1 a2 ... aA (accepting states, space-separated)
- Line 6: src symbol dest (one transition per line, N\*S lines total)

<a name="overview"></a>Slides
---------------------------------------

- [Presentation Slides](./slides/presentation_hopcroft.pptx)


<a name="overview"></a>Programming Challenge
---------------------------------------

- [Programming Challenge](./programmingChallenge/problemStatement.pdf)
	- Solutions:
		- [Python](./programmingChallenge/solutions/pcSol_py.py)
	- Test Cases:
		- [Case 1 input](./programmingChallenge/io/test.in.1)
		- [Case 1 output](./programmingChallenge/io/test.out.1)
		- [Case 2 input](./programmingChallenge/io/test.in.2)
		- [Case 2 output](./programmingChallenge/io/test.out.2)
		- [Case 3 input](./programmingChallenge/io/test.in.3)
		- [Case 3 output](./programmingChallenge/io/test.out.3)
		- [Case 4 input](./programmingChallenge/io/test.in.4)
		- [Case 4 output](./programmingChallenge/io/test.out.4)
		- [Case 5 input](./programmingChallenge/io/test.in.5)
		- [Case 5 output](./programmingChallenge/io/test.out.5)
		- [Case 6 input](./programmingChallenge/io/test.in.6)
		- [Case 6 output](./programmingChallenge/io/test.out.6)
		- [Case 7 input](./programmingChallenge/io/test.in.7)
		- [Case 7 output](./programmingChallenge/io/test.out.7)
		- [Case 8 input](./programmingChallenge/io/test.in.8)
		- [Case 8 output](./programmingChallenge/io/test.out.8)
		- [Case 9 input](./programmingChallenge/io/test.in.9)
		- [Case 9 output](./programmingChallenge/io/test.out.9)
		- [Case 10 input](./programmingChallenge/io/test.in.10)
		- [Case 10 output](./programmingChallenge/io/test.out.10)
		- [Case 11 input](./programmingChallenge/io/test.in.11)
		- [Case 11 output](./programmingChallenge/io/test.out.11)
		- [Case 12 input](./programmingChallenge/io/test.in.12)
		- [Case 12 output](./programmingChallenge/io/test.out.12)
		- [Case 13 input](./programmingChallenge/io/test.in.13)
		- [Case 13 output](./programmingChallenge/io/test.out.13)
		- [Case 14 input](./programmingChallenge/io/test.in.14)
		- [Case 14 output](./programmingChallenge/io/test.out.14)
		- [Case 15 input](./programmingChallenge/io/test.in.15)
		- [Case 15 output](./programmingChallenge/io/test.out.15)
		- [Case 16 input](./programmingChallenge/io/test.in.16)
		- [Case 16 output](./programmingChallenge/io/test.out.16)
		- [Case 17 input](./programmingChallenge/io/test.in.17)
		- [Case 17 output](./programmingChallenge/io/test.out.17)
		- [Case 18 input](./programmingChallenge/io/test.in.18)
		- [Case 18 output](./programmingChallenge/io/test.out.18)
		- [Case 19 input](./programmingChallenge/io/test.in.19)
		- [Case 19 output](./programmingChallenge/io/test.out.19)
		- [Case 20 input](./programmingChallenge/io/test.in.20)
		- [Case 20 output](./programmingChallenge/io/test.out.20)
	