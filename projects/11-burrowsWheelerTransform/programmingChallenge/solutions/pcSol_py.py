import sys
import io
from pathlib import Path


# Set this to True to run the 20 test cases.
TEST = False

# This problem doesn't use Python's default character ordering.
# We compare characters using the following order:
ORDERED_CHARACTERS = [
    "$", " ", "a", "b", "c", "d", "e", 
    "f", "g", "h", "i", "j", "k", "l", 
    "m", "n", "o", "p", "q", "r", "s", 
    "t", "u", "v", "w", "x", "y", "z"
    ]


# Map each character to its index in the custom order. This lets us
# compare and sort characters according to the problem's lexicographic
# order rather than normal ASCII order.
# For example:
#   "$" -> 0
#   " " -> 1
#   "a" -> 2
CHAR_TO_INDEX = {ch: i for i, ch in enumerate(ORDERED_CHARACTERS)}


# Read all transmissions from standard input.
# Use readlines() so we can process input one line at a time
# as described in the input section of the problem statement.
def parse_input():
    # Turn this into an iterator so that we can use next(lines)
    # to easily give us the next line.
    lines = iter(sys.stdin.readlines())

    # Keep track of the number of transmissions to decode
    # and allocate an array to store the transmission data.
    num_transmissions = int(next(lines).strip())
    transmissions = []

    # Loop through each transmission in the input and extract 
    # the following:
    #   1. its length n
    #   2. its corrupted BWT string
    for _ in range (num_transmissions):
        n = int(next(lines).strip())

        # Remove only the trailing new line character because spaces
        # are valid characters and might be at the end of the BWT.
        bwt = next(lines).rstrip("\n")

        # Primarily used while creating test cases, but check if the
        # BWT string correctly matches the stated length. 
        if len(bwt) != n:
            raise ValueError("Transmission length does not match n")

        # Add the parsed transmission to the array for later processing.
        transmissions.append(bwt)

    return transmissions


# This function returns all the possible characters that could replace ?
# in the BWT. All ? characters in one transmission must be replaced by 
# the same character, which can only be either a space or lowercase letter.
def get_replacement_characters(corrupted_bwt):
    # If there is no corruption, then there are no replacements to be made.
    if "?" not in corrupted_bwt:
        return []
    
    # We do not include the $ because the input already contains it.
    return ORDERED_CHARACTERS[1:]
    

# Replace every ? in the corrupted BWT with the chosen character.
# We use the same replacement character everywhere because all ?
# characters in one transmission must be the same character.
def apply_replacement_character(corrupted_bwt, replacement):
    return corrupted_bwt.replace("?", replacement)


# Compute the Burrows-Wheeler Transform (BWT) of a string.
# 
# BWT in 3 steps:
# 1. List every cyclic rotation of the string.
#    Example:
#       s = "banana$"
#
#       rotations = [
#           "banana$",
#           "anana$b",
#           "nana$ba",
#           "ana$ban",
#           "na$bana",
#           "a$banan",
#           "$banana"            
#       ]
#
# 2. Sort cyclic rotations into lexicographic order.
#    Example:    
#       rotations = [
#           "$banana",
#           "a$banan",
#           "ana$ban",
#           "anana$b",
#           "banana$",
#           "na$bana",
#           "nana$ba"        
#       ]
#
# 3. Form a new string by taking the last character from each sorted rotation.
#    Example:
#       last_column = "annb$aa"
#
#    The resulting string is the Burrows-Wheeler Transform
#
# This solution uses the naive version of BWT because it is the easiest to follow
# for students learning the algorithm. Optimized approaches exist and were
# discussed in our presentation. 
def bwt(s):
    # Allocate an array to keep track of all cyclic rotations of the 
    # input string. 
    rotations = []

    # Generate each cyclic rotation of s by first choosing start position i.
    # s[i:] takes everything from i to the end.
    # s[:i] takes everything from the beginning to i.
    # Concatenating the two creates one cyclic rotation starting at i.
    for i in range(len(s)):
        rotation = s[i:] + s[:i]
        rotations.append(rotation)

    # Sort the rotations lexicographically using the problem's custom order.
    # $ < " " < a < b < c < ... < z
    rotations.sort(key=lambda row: [CHAR_TO_INDEX[ch] for ch in row])

    # After sorting, the BWT is formed by taking the last character from
    # each row of the rotation table. 
    last_column = []
    for rotation in rotations:
        last_column.append(rotation[-1])

    # Join the collected characters into one string and return the BWT.
    return "".join(last_column)


# Invert a Burrows-Wheeler Transform (BWT) string.
#
# Inverse BWT in 3 steps:
# 1. Start with an empty table with one row for each character in the BWT.
#    Example:
#       bwt = "annb$aa"
#       
#       table = [
#           "",
#           "",
#           "",
#           "",
#           "",
#           "",
#           ""
#       ]
#
# 2. Rebuild the sorted rotation table.
#    Each iteration:
#       - prepends the i'th BWT character to the i'th row
#       - sorts the rows lexicographically
#    Example:
#       After iteration 1:
#           table = [
#               "$",
#               "a",
#               "a",
#               "a",
#               "b",
#               "n",
#               "n"
#           ]
#
#       After iteration 2:
#           table = [
#               "$b",
#               "a$",
#               "an",
#               "an",
#               "ba",
#               "na",
#               "na",
#           ]
#
#       ...
#
#       After all n iterations:
#           table = [
#               "$banana",
#               "a$banan",
#               "ana$ban",
#               "anana$b",
#               "banana$",
#               "na$bana",
#               "nana$ba",
#           ]
#
# 3. Extract the row that ends with $. This is the original string.
#    Example:
#       original_string = "banana"
# 
# This solution uses the naive version of inverse BWT because it is the easiest
# to follow for students learning the algorithm. Optimized approaches exist and 
# were discussed in our presentation. 
def inverse_bwt(bwt):
    # The number of rows in the rotation table.
    n = len(bwt)
    
    # Allocate an array to help reconstruct the cyclic rotations of the 
    # original string.
    table = [""] * n

    # Rebuild the full sorted rotation table one column at a time.
    for _ in range(n):
        # Prepend the i'th BWT character to the i'th row.
        # This gradually reconstructs the rotations from right to left.
        for i in range(n):
            table[i] = bwt[i] + table[i]

        # After adding the new column, sort again using the problem's
        # custom order, so that the rows stay in the same lexicographic 
        # order they would have in the full rotation table. 
        table.sort(key=lambda row: [CHAR_TO_INDEX[ch] for ch in row])

    # Extract the row that ends with $. 
    # This is the original string.
    for row in table:
        if row.endswith("$"):
            # Remove the $ before returning the decoded message as per the
            # problem statement.
            return row[:-1]


# Try all the valid replacement characters, decode the BWT, and return
# the lexicographically smallest original message. 
def decode_transmission(corrupted_bwt):
    # The list of possible replacement characters for ?.
    # If there are no ?, this will be an empty list.
    replacement_characters = get_replacement_characters(corrupted_bwt)

    # If the BWT has no ?, you can decode it immediately.
    if len(replacement_characters) == 0:
        return inverse_bwt(corrupted_bwt)
    
    # Keep track of the smallest decoded message seen.
    smallest_message = None

    # Try each valid character to replace all ?'s.
    # Each iteration tests one possible BWT. 
    for ch in replacement_characters:
        # Build a candidate BWT by replacing every ? with ch.
        complete_bwt = apply_replacement_character(corrupted_bwt, ch)

        # Attempt to decode the BWT back into its original message.
        decoded_message = inverse_bwt(complete_bwt)

        # Inverse BWT gives us a candidate original string, but we have
        # to verify that it is actually consistent with this complete BWT.
        # To do that:
        #   - append $ back to the decoded message.
        #   - run BWT and check if we get the same string. 
        if bwt(decoded_message + "$") != complete_bwt:
            # If the BWT does not match, then ch did not produce a valid transmission.
            continue

        # In this case, the replacement is valid.
        # So, keep the smallest decoded message as per the problem statement.
        if smallest_message is None or decoded_message < smallest_message:
            smallest_message = decoded_message
    
    # Return the smallest decoded message.
    return smallest_message


# Run all of the test cases and report how many passed.
# This function reads each test.in.* file then compares the produced output
# against the corresponding test.out.* file. 
def run_tests():
    # Find the project root and locate the io directory that stores the
    # test.in.* and test.out.* files.
    project_root = Path(__file__).resolve().parent.parent
    io_dir = project_root / "io"

    # Keep track of how many test cases pass.
    tests_passed = 0

    # Run tests 1-20.
    for i in range(1, 21):
        # Build the file paths for this test case.
        # For example, if i = 1, the corresponding files are
        # test.in.1 and test.out.1.
        input_path = io_dir / ("test.in." + str(i))
        output_path = io_dir / ("test.out." + str(i))

        # Read the input file in the same way the program would from stdin.
        with input_path.open("r", encoding="utf-8") as input_file:
            input_text = input_file.read()

        # Read the output file to compare actual output against. 
        with output_path.open("r", encoding="utf-8") as output_file:
            output_text = output_file.read()

        # Print basic information about the test to see which one is running.
        print(f"Test {i}")
        print(f"Input file: {input_path.name}")
        print(f"Output file: {output_path.name}")

        # Save stdin to restore it after the test in case something crashes.
        prev_stdin = sys.stdin
        try:
            # This treats a string as a file-like object so that parse_input()
            # will behave properly.
            sys.stdin = io.StringIO(input_text)
            
            # Parse the input and store each corrupted BWT.
            transmissions = parse_input()

            # Allocate an array for keeping track of the original messages so
            # we can print output in the correct order.
            output = []

            # Decode each transmission independently.
            # For each corrupted BWT, decode the lexicographically smallest original message
            # and store it later for output. 
            for corrupted_bwt in transmissions:
                decoded_message = decode_transmission(corrupted_bwt)
                output.append(decoded_message)

            # Print all answers at once, one original message per line.
            actual_output = "\n".join(output)
        finally:
            # Restore the original stdin in case something crashes. 
            sys.stdin = prev_stdin

        # Check whether the program's output matches the expected output.
        outputs_match = (actual_output == output_text)

        # Print the program's actual output to see why a test fails.
        print(f"Program output:\n{actual_output}")
        
        # Print the results of the test case.
        if outputs_match:
            tests_passed += 1
            print("Result: PASS")
        else:
            print("Result: FAIL")

        print()
    
    # Print a final summary after all test cases have ran.
    print(f"Passed {tests_passed}/20 tests")


# Read all corrupted transmissions, decode each one, and print the original messages.
def main():
    # Runs the 20 test cases if TEST is set to True.
    if TEST:
        run_tests()
        return

    # Parse the input and store each corrupted BWT.
    transmissions = parse_input()

    # Allocate an array for keeping track of the original messages so
    # we can print output in the correct order.
    output = []

    # Decode each transmission independently.
    # For each corrupted BWT, decode the lexicographically smallest original message
    # and store it later for output. 
    for corrupted_bwt in transmissions:
        decoded_message = decode_transmission(corrupted_bwt)
        output.append(decoded_message)
    
    # Print all answers at once, one original message per line.
    sys.stdout.write("\n".join(output))


if __name__ == "__main__":
    main()
