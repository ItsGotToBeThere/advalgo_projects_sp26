import sys

# holds student info. we keep the original index so we don't 
# lose track of who is who after moving them around in the list
class Student:
    def __init__(self, original_index, scores):
        self.original_index = original_index
        self.scores = scores

# good for small chunks. we're sorting high-to-low here.
# if scores are tied, we don't swap, which keeps the sort stable
def insertion_sort(students, left, right, event_idx):
    for i in range(left + 1, right + 1):
        key = students[i]
        j = i - 1
        # keep shifting elements right until we find the spot for key
        while j >= left and students[j].scores[event_idx] < key.scores[event_idx]:
            students[j + 1] = students[j]
            j -= 1
        students[j + 1] = key

# faster way to find an insertion point than checking one by one.
# it splits the search area in half each time to save steps
def binary_search(part, left, key, event_idx, allow_equal):
    right = len(part) - 1
    while left <= right:
        mid = left + (right - left) // 2
        if part[mid].scores[event_idx] > key.scores[event_idx] or \
           (allow_equal and part[mid].scores[event_idx] == key.scores[event_idx]):
            left = mid + 1
        else:
            right = mid - 1
    return left

# if one list is way bigger than the other, we gallop.
# instead of 1, 2, 3... we check 1, 2, 4, 8 to find the end of a block fast
def gallop(part, left, key, event_idx, allow_equal):
    if left >= len(part): return 0
    if part[left].scores[event_idx] < key.scores[event_idx] or \
       (not allow_equal and part[left].scores[event_idx] == key.scores[event_idx]):
        return 0
    
    idx = 1
    # double the jump size until we go past where the key should be
    while left + idx < len(part) and (
        part[left + idx].scores[event_idx] > key.scores[event_idx] or 
        (allow_equal and part[left + idx].scores[event_idx] == key.scores[event_idx])
    ):
        idx *= 2
    # then use binary search to find the exact stopping point
    return binary_search(part, left + idx // 2, key, event_idx, allow_equal) - left

# this puts two sorted halves together. If one side is winning
# a lot, we trigger the galloping mode to copy big chunks at once
def merge(students, left, mid, right, event_idx, min_gallop=7):
    left_part = students[left:mid + 1]
    right_part = students[mid + 1:right + 1]
    i = j = 0
    k = left
    left_count = right_count = 0

    while i < len(left_part) and j < len(right_part):
        if left_part[i].scores[event_idx] >= right_part[j].scores[event_idx]:
            if left_count < min_gallop:
                students[k] = left_part[i]
                i += 1; left_count += 1; right_count = 0; k += 1
        else:
            if right_count < min_gallop:
                students[k] = right_part[j]
                j += 1; right_count += 1; left_count = 0; k += 1
        
        # if we've taken from the same side min_gallop times, just gallop
        if left_count >= min_gallop:
            count = gallop(left_part, i, right_part[j], event_idx, True)
            for _ in range(count):
                students[k] = left_part[i]; i += 1; k += 1
            left_count = 0
        elif right_count >= min_gallop:
            count = gallop(right_part, j, left_part[i], event_idx, False)
            for _ in range(count):
                students[k] = right_part[j]; j += 1; k += 1
            right_count = 0
            
    # copy whatever is left over after one side runs out
    while i < len(left_part):
        students[k] = left_part[i]; i += 1; k += 1
    while j < len(right_part):
        students[k] = right_part[j]; j += 1; k += 1

# this looks for parts of the list that are already sorted.
# it saves a ton of work by not re-sorting stuff that's already fine
def find_runs(students, min_run, event_idx):
    runs = []
    n = len(students)
    i = 0
    while i < n:
        start = i
        if i < n - 1:
            # check if this part is decreasing
            if students[i].scores[event_idx] >= students[i + 1].scores[event_idx]:
                while i < n - 1 and students[i].scores[event_idx] >= students[i + 1].scores[event_idx]:
                    i += 1
            else:
                # if it's increasing, we flip it so it fits our high-to-low sort
                while i < n - 1 and students[i].scores[event_idx] < students[i + 1].scores[event_idx]:
                    i += 1
                students[start:i + 1] = reversed(students[start:i + 1])
        i += 1
        end = i - 1
        # if the run is too tiny, we force it to be 'min_run' and sort it
        if end - start + 1 < min_run:
            end = min(start + min_run - 1, n - 1)
            insertion_sort(students, start, end, event_idx)
            i = end + 1
        runs.append((start, end))
    return runs

# timsort keeps a stack of runs and merges them when they meet 
# certain size rules. This keeps the merge tree from getting lopsided
def tim_sort(students, event_idx, min_run=32):
    runs = find_runs(students, min_run, event_idx)
    stack = []
    for run in runs:
        stack.append(run)
        # rules are to make sure the runs we're 
        # merging are roughly the same size for efficiency
        while len(stack) > 1:
            if len(stack) >= 3:
                x, y, z = (stack[-1][1]-stack[-1][0]+1), (stack[-2][1]-stack[-2][0]+1), (stack[-3][1]-stack[-3][0]+1)
                if x + y >= z or x >= y:
                    if x < z:
                        merge(students, stack[-2][0], stack[-2][1], stack[-1][1], event_idx)
                        stack[-2] = (stack[-2][0], stack[-1][1]); stack.pop()
                    else:
                        merge(students, stack[-3][0], stack[-3][1], stack[-2][1], event_idx)
                        stack[-3] = (stack[-3][0], stack[-2][1]); stack.pop(-2)
                    continue
            
            x, y = (stack[-1][1]-stack[-1][0]+1), (stack[-2][1]-stack[-2][0]+1)
            if x >= y:
                merge(students, stack[-2][0], stack[-2][1], stack[-1][1], event_idx)
                stack[-2] = (stack[-2][0], stack[-1][1]); stack.pop()
            else: break
            
    # merge everything left on the stack into one big run
    while len(stack) > 1:
        merge(students, stack[-2][0], stack[-2][1], stack[-1][1], event_idx)
        stack[-2] = (stack[-2][0], stack[-1][1]); stack.pop()

def solve():
    # generator for reading tokens. Standard input() is too slow for big files (just like in the homeworks)
    def get_tokens():
        for line in sys.stdin:
            for token in line.split():
                yield token
    
    tokens = get_tokens()
    
    try:
        n, m = int(next(tokens)), int(next(tokens))
    except (StopIteration, ValueError):
        return

    # load up the students. idx is their original rank
    students = [Student(idx, [int(next(tokens)) for _ in range(m)]) for idx in range(n)]
            
    try:
        num_commands = int(next(tokens))
    except (StopIteration, ValueError):
        return
        
    # S = sort by an event, Q = print the original IDs in this rank range
    first_query = True 
    for _ in range(num_commands):
        try:
            cmd_type = next(tokens)
            if cmd_type == 'S':
                tim_sort(students, int(next(tokens)))
            elif cmd_type == 'Q':
                i, j = int(next(tokens)), int(next(tokens))
                # grab the ids and join them with spaces for the output
                result = " ".join(str(s.original_index) for s in students[i:j])
                
                # careful with newlines so we don't have an extra one at the end
                if not first_query: sys.stdout.write("\n")
                sys.stdout.write(result)
                first_query = False 
        except StopIteration:
            break
    sys.stdout.flush()

if __name__ == "__main__":
    solve()