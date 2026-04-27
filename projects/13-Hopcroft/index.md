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
		- [Python](./programmingChallenge/solutions.pcSol_python.py)
	- Test Cases:
		- [Case 1 input](./programmingChallenge/io/test.in.1)
		- [Case 1 output](./programmingChallenge/io/test.out.1)
		- ...add the others here as needed
	