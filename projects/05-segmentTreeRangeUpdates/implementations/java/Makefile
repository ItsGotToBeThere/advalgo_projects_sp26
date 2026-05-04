CXX ?= g++
CXXFLAGS = -std=c++17 -O2 -Wall -Wextra -Wpedantic

PROGRAM_NAME = segment_tree

all: $(PROGRAM_NAME)

$(PROGRAM_NAME): segmentTree.cpp
	$(CXX) $(CXXFLAGS) segmentTree.cpp -o $(PROGRAM_NAME)

run: $(PROGRAM_NAME)
	./$(PROGRAM_NAME)

# Runs all tests/testN.in through the binary and diffs against tests/testN.expected.
# Exits with a clear PASS / FAIL per test case.
test: $(PROGRAM_NAME)
	@PASS=0; FAIL=0; \
	for input in ../io/sample.in.*; do \
		base=$$(basename $$input .in); \
		num=$$(echo $$input | grep -o '[0-9]*$$'); \
		expected=../io/sample.out.$$num; \
		actual=$$(./$(PROGRAM_NAME) < $$input); \
		if [ "$$actual" = "$$(cat $$expected)" ]; then \
			echo "[PASS] $$base"; \
			PASS=$$((PASS + 1)); \
		else \
			echo "[FAIL] $$base"; \
			echo "  expected: $$(cat $$expected)"; \
			echo "  actual:   $$actual"; \
			FAIL=$$((FAIL + 1)); \
		fi; \
	done; \
	echo ""; \
	echo "Results: $$PASS passed, $$FAIL failed"

clean:
	rm -f $(PROGRAM_NAME)