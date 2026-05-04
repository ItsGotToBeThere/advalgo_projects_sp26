#!/bin/bash

passed=0
total=0
TESTDIR="tests"

for i in {1..24}
do
    infile="$TESTDIR/test.in.$i"
    outfile="$TESTDIR/test.out.$i"

    if [ -f "$infile" ] && [ -f "$outfile" ]; then
        total=$((total+1))

        java Solution < "$infile" > temp.out

    if diff temp.out "$outfile" > /dev/null; then
        echo "Test $i: PASS"
        passed=$((passed+1))
    else
        echo "Test $i: FAIL"
        echo "---- Expected vs Actual ----"
        diff temp.out "$outfile"
        echo "----------------------------"
    fi
    fi
done

rm -f temp.out

echo "Passed $passed / $total tests"