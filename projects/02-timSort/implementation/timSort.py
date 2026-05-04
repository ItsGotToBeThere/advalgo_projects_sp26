"""
Example Scenario: Receive data from multiple servers all already in chronological order with respect to their own server
    -> When combined entries are sorted within each server, they are not necessarily sorted across all servers
    -> We want to sort all entries across all servers by timestamp in an efficient way
"""

# stores entries from each server as their 3 key components: timestamp (main sorting feature), server_id, and the associated message
class Entry:
    def __init__(self, timestamp, server_id, message):
        self.timestamp = timestamp
        self.server_id = server_id
        self.message = message

# the main method and format for printing the pre-sorted and then post-sorted list
def print_entries(entries):
    for entry in entries:
        print(f"[{entry.timestamp}] {entry.server_id} : {entry.message}")
    print()

# insertion sort to make small runs larger for merge sort from left to right index -> creates sorted chunks
# idea: find each elements correct position within the left and right bounds
def insertion_sort(entries, left, right):
    for i in range(left + 1, right + 1):
        key = entries[i]
        j = i - 1
        while j >= left and entries[j].timestamp > key.timestamp:
            entries[j + 1] = entries[j]
            j -= 1
        entries[j + 1] = key

# binary search to find the index where the key should be inserted
# allow_equal is true when the leftside is galloping and false when the rightside is galloping
# this is because to maintain stability, values of the left that are equal to values on the right should come first
def binary_search(part, left, key, allow_equal):
    right = len(part) - 1
    while left <= right:
        mid = left + (right - left) // 2

        # if left side is galloping, it will return the rightmost equal element if there are equal elements to the key
        # if the right side is galloping, it will return the greatest element smaller than the key
        if part[mid].timestamp < key.timestamp or (allow_equal and part[mid].timestamp == key.timestamp):
            left = mid + 1
        else:
            right = mid - 1
    return left

# galloping mode: quickly finds how many more elements in a part should be copied over
# does this by exponentially increasing index, and then use binary search to find the exact element to stop at
# returns the amount of elements to copy over
def gallop(part, left, key, allow_equal):
    # if the index is out of bounds or the element should come after the key, don't gallop
    if left >= len(part) or part[left].timestamp > key.timestamp or (not allow_equal and part[left].timestamp == key.timestamp):
        return 0
    idx = 1

    while left + idx < len(part) and (
        part[left + idx].timestamp < key.timestamp or 
        
        # if the left side is galloping we want to pass elements that are equal to maintain stability
        # if the right side is galloping we stop before elements that are equal
        (allow_equal and part[left + idx].timestamp == key.timestamp)
    ):
        # each time, double the index so we check elements faster and faster until we find one greater than the key
        idx *= 2

    # then we use binary search to find the exact element to stop at
    return binary_search(part, left + idx // 2, key, allow_equal) - left

# merge two pre-sorted runs into one: combine left->mid with mid+1->right
# fast on large pre-sorted chunks
def merge(entries, left, mid, right, min_gallop=7):
    # create copies of each run
    left_part = entries[left:mid + 1]
    right_part = entries[mid + 1:right + 1]

    # idx for left run, right run
    i = j = 0

    # idx to write to in original array
    k = left
    
    # counters to track how many times in a row we have taken from the left or right run
    # used to determine when to switch to galloping mode
    left_count = right_count = 0

    # while both runs have elements remaining
    while i < len(left_part) and j < len(right_part):

        # choose lower timestamp
        if left_part[i].timestamp <= right_part[j].timestamp:

            # if we haven't taken the left run too many times, take the next element from the left run and update counters
            if left_count < min_gallop:
                entries[k] = left_part[i]
                i += 1
                left_count += 1
                right_count = 0
            
        else:
            # if we haven't taken the right run too many times, take the next element from the right run and update counters
            if right_count < min_gallop:
                entries[k] = right_part[j]
                j += 1
                right_count += 1
                left_count = 0
        k += 1
        
        # if we've been taking from the left run a couple times in a row, it probably means the left run has a lot of elements smaller than or equal to the current element in the right run, 
        # so we can switch to galloping mode instead of comparing each element one by one
        if left_count >= min_gallop:
            # the amount of elements to copy over from the left run
            gallop_count = gallop(left_part, i, right_part[j], True)
            for _ in range(gallop_count):
                entries[k] = left_part[i]
                i += 1
                k += 1
            
            # reset counters after galloping, as we will be switching to the right run after this
            left_count = 0
        # if we've been taking from the right run a couple times in a row, it probably means the right run has a lot of elements smaller than the current element in the left run,
        # so we can switch to galloping mode instead of comparing each element one by one
        elif right_count >= min_gallop:
            # the amount of elements to copy over from the right run
            gallop_count = gallop(right_part, j, left_part[i], False)
            for _ in range(gallop_count):
                entries[k] = right_part[j]
                j += 1
                k += 1
            # reset counters after galloping, as we will be switching to the left run after this
            right_count = 0

            
        
    # when one of the runs is empty, copy over the remaining elements from the other run
    while i < len(left_part):
        entries[k] = left_part[i]
        i += 1
        k += 1
    while j < len(right_part):
        entries[k] = right_part[j]
        j += 1
        k += 1

# function that determines where ascending and decreasing runs exist within the data
# returns the start and end idx of all runs found
def find_runs(entries, min_run):
    # store all runs found
    runs = []
    n = len(entries)
    # current position of run search within entries
    i = 0

    while i < n:
        start = i

        # if last element, nothing to compare to
        if i == n - 1:
            i+=1

        else:
            # if current timestamp is less than or equal to next -> start of ascending run
            if entries[i].timestamp <= entries[i + 1].timestamp:
                # as long as next is greater than previous keep going
                while i < n - 1 and entries[i].timestamp <= entries[i + 1].timestamp:
                    i += 1
            # else start of decreasing run
            else:
                # as long as next is less than previous keep going
                while i < n - 1 and entries[i].timestamp > entries[i + 1].timestamp:
                    i += 1
                # reverse descending run since mergesort is designed for ascending runs only
                entries[start:i + 1] = reversed(entries[start:i + 1])
                
            i += 1

        
        end = i - 1

        # check if the run is too small
        if end - start + 1 < min_run:
            # enlarge the run to fit the min size
            end = min(start + min_run - 1, n - 1)
            # use insertion sort for the this small run as it's efficient for small sections
            insertion_sort(entries, start, end)
            i = end + 1
        runs.append((start, end))
    
    return runs

# the main sorting function that combines partially sorted parts and efficiently merges them using runs
# min_run sets the minimum length of a run to prevent inefficiency of small runs
def tim_sort(entries, min_run=4):

    # find and store all runs within entries
    runs = find_runs(entries, min_run)

    # add runs to a stack
    stack = []
    for run in runs:
        stack.append(run)

        # if there are at least 3 runs on the stack, check if the last 3 runs satisfy the merging conditions
        if len(stack) >= 3:
            # get the lengths of the last 3 runs
            x = stack[-1][1] - stack[-1][0] + 1
            y = stack[-2][1] - stack[-2][0] + 1
            z = stack[-3][1] - stack[-3][0] + 1

            # if the two topmost runs combined are smaller than the third, and the topmost run is smaller than the second, do not merge yet
            # BUT if either of these conditions are broken, merge the middle run with the smaller of the top and third run and repeat until the conditions are satisfied again
            # This reduces the amount of times we have to copy large runs of elements and prioritizes merging smaller runs first
            while x + y >= z or x >= y:
                # if the top run is smaller than the third run, merge the top run with the middle run
                if x < z:
                    merge(entries, stack[-2][0], stack[-2][1], stack[-1][1])
                    
                    # update stack to reflect the merge
                    stack[-2] = (stack[-2][0], stack[-1][1])
                    stack.pop()
                # else merge the middle run with the third run
                else:
                    merge(entries, stack[-3][0], stack[-3][1], stack[-2][1])
                    
                    # update stack to reflect the merge
                    stack[-3] = (stack[-3][0], stack[-2][1])
                    stack.pop(-2)
                
                # update the lengths of the last 3 runs after the merge
                if len(stack) >= 3:
                    x = stack[-1][1] - stack[-1][0] + 1
                    y = stack[-2][1] - stack[-2][0] + 1
                    z = stack[-3][1] - stack[-3][0] + 1
                else:
                    break
            
    # once all runs have been added to the stack, merge remaining runs until only one run remains which is the fully sorted list
    while len(stack) > 1:
        # merge the top two runs on the stack, which are the smallest runs remaining
        merge(entries, stack[-2][0], stack[-2][1], stack[-1][1])
        stack[-2] = (stack[-2][0], stack[-1][1])
        stack.pop()
            



if __name__ == "__main__":
    # number of entries to be sorted
    n = int(input())
    entries = []

    # loop through reading in each line as an entry and storing it as appropriate
    for _ in range(n):
        timestamp, server_id, message = input().split()
        entries.append(Entry(int(timestamp), int(server_id), message))

    # print the pre-sorted entries list
    print_entries(entries)

    print("Running TimSort Now ...\n")

    # sort entries by timestamp using tim_sort algorithm
    tim_sort(entries)

    # print out the final sorted entries list
    print_entries(entries)