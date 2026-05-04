import sys
from typing import Sequence

def buildTable(pattern: Sequence) -> list[int]:
    """Builds the fallback table for KMP."""
    # Creates the initial table of size m + 1 since it will store information for each index of the pattern and one extra for overlapping matches
    m = len(pattern)
    table = [0] * (m + 1)
    
    pos = 1 # current position in the pattern
    cnd = 0 # current fallback position for this position
    table[0] = -1 # -1 indicates there is no valid prefix/fallback

    # Preprocessing the pattern starting from index 1 until end of pattern
    while (pos < m):
        # if elements match
        if (pattern[pos] == pattern[cnd]):
            table[pos] = table[cnd]
        # if the elements dont match, then the best fallback will initially be set to current candidate (cnd)
        else:
            table[pos] = cnd
            # there may still be a smaller prefix that matches current pos, so we keep following fallbacks until there are no more valid ones left (-1)
            while (cnd >= 0) and (pattern[pos] != pattern[cnd]):
                cnd = table[cnd]
        pos += 1
        cnd += 1
    
    # the last position indicates fallback after the full pattern has been matched, so the KMP search can continue without restarting (useful in overlapping matches)
    table[pos] = cnd
    return table

def kmpSearch(text: Sequence, pattern: Sequence) -> list[int]:
    """Searches 'text' for all occurrences of 'pattern'."""
    # quick check for inputs, if either sequence is empty we should return no matches
    if (not pattern) or (not text):
        return []
    
    # Generate the fallback table
    table = buildTable(pattern)
    matches = []
    
    j = 0 # pointer to current index in text
    k = 0 # pointer current index in pattern

    # Scans through the text exactly once
    while (j < len(text)):
        # Case 1: if the current elements in text and pattern match, then move both pointers
        if (pattern[k] == text[j]):
            k += 1
            j += 1
            
            # if the pattern was fully found, this counts as a match so append its start (index j - k) to the output list
            if (k == len(pattern)):
                matches.append(j - k)
                k = table[k]
        # Case 2: if the current elements in text and pattern don't match
        else:
            k = table[k]
            # if k is -1, this means there is no valid prefix left to reuse from the table, so we advance in both text and pattern
            if (k < 0):
                j += 1
                k += 1
    
    return matches


def find_2d_pattern(page_matrix: list[str], pattern_matrix: list[str]) -> list[tuple[int, int]]:
    """
    Finds all occurrences of a 2D pattern inside a larger 2D page matrix.
    Returns a list of (row, col) coordinates where the top-left of the pattern appears.
    """
    # quick check to ensure neither the page nor pattern is empty, as an empty matrix cannot yield valid coordinates
    if not page_matrix or not pattern_matrix:
        return []

    # extract the dimensions of both matrices to determine boundaries for our search iterations
    R, C = len(page_matrix), len(page_matrix[0]) if page_matrix else 0
    r, c = len(pattern_matrix), len(pattern_matrix[0]) if pattern_matrix else 0

    # trivially reject cases where the pattern physically cannot fit inside the page matrix bounds
    if r > R or c > C:
        return []

    # identify all unique rows in the pattern and assign them integer IDs so we can compress the 2D search into a series of 1D states
    unique_pattern_rows = list(set(pattern_matrix))
    row_to_state = {row: idx for idx, row in enumerate(unique_pattern_rows)}
    
    # transform the 2D pattern into a single 1D column of state IDs representing the required vertical sequence of rows
    target_vertical_pattern = [row_to_state[row] for row in pattern_matrix]
    
    # initialize a grid of the same dimensions as the page to track where specific pattern rows successfully match within the page
    state_matrix = [[-1] * C for _ in range(R)]
    
    # HORIZONTAL KMP: find all horizontal occurrences of each unique pattern row within the page
    for unique_row in unique_pattern_rows:
        state_id = row_to_state[unique_row]
        
        # scan through every row in the page matrix independently to search for the current unique pattern row
        for i in range(R):
            matches = kmpSearch(page_matrix[i], unique_row)
            
            # record the successful state ID at the starting column index for this specific row in our tracker grid
            for j in matches:
                state_matrix[i][j] = state_id
                
    final_matches = []
    
    # VERTICAL KMP: treat the columns of our populated state tracker as text to find the required vertical sequence of row IDs
    # we only need to scan up to C - c + 1 since any column beyond that couldn't possibly fit the pattern width horizontally
    for j in range(C - c + 1):
        
        # extract the current column of state IDs from the tracker to act as our "text" for the vertical KMP search
        column_data = [state_matrix[i][j] for i in range(R)]
        col_matches = kmpSearch(column_data, target_vertical_pattern)
        
        # any match found vertically means we have verified both horizontal and vertical conditions, marking a complete 2D match
        for i in col_matches:
            final_matches.append((i, j))
            
    return final_matches

if __name__ == "__main__":
    input = sys.stdin.readline
    R, C = map(int, input().split())

    page_matrix = [input().strip() for _ in range(R)]
    r, c = map(int, input().split())
    pattern_matrix = [input().strip() for _ in range(r)]

    matches = find_2d_pattern(page_matrix, pattern_matrix)

    matches.sort(key=lambda rc: (rc[1], rc[0]))
    print(len(matches))
    for row, col in matches:
        print(row, col)