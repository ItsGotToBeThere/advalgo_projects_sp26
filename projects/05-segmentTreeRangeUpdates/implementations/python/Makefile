PYTHON ?= python3

PROGRAM_NAME = segmentTree.py

all:
	@echo "Python program ready"

run:
	$(PYTHON) $(PROGRAM_NAME)

# Runs all tests/testN.in through the binary and diffs against tests/testN.expected.
# Exits with a clear PASS / FAIL per test case.
test:
	@PASS=0; FAIL=0; \
	for input in ../io/sample.in.*; do \
		base=$$(basename $$input .in); \
		num=$$(echo $$input | grep -o '[0-9]*$$'); \
		expected=../io/sample.out.$$num; \
		actual=$$($(PYTHON) $(PROGRAM_NAME) < $$input); \
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
	@echo "Nothing to clean for Python"