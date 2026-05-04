/*
Example Scenario: Receive data from multiple servers all already in chronological order with respect to their own server
    -> When combined entries are sorted within each server, they are not necessarily sorted across all servers
    -> We want to sort all entries across all servers by timestamp in an efficient way
*/

#include <iostream>
#include <vector>
#include <string>
#include <algorithm>

using namespace std;

// stores entries from each server as their 3 key components: timestamp (main sorting feature), server_id, and the associated message
struct Entry {
    int timestamp;
    int server_id;
    string message;
};

// the main method and format for printing the pre-sorted and then post-sorted list
void print_entries(const vector<Entry>& entries) {
    for (const auto& entry: entries) {
        cout << "[" << entry.timestamp << "] " << entry.server_id << " : " << entry.message << '\n';
    }
    cout << '\n';
}

// insertion sort to make small runs larger for merge sort from left to right index -> creates sorted chunks
// idea: find each elements correct position within the left and right bounds
void insertion_sort(vector<Entry>& entries, int left, int right) {
    for (int i = left + 1; i <= right; i++) { // sorts only from left to right index 
        Entry key = entries[i]; // value to insert into correct position first
        int j = i - 1;
        while (j >= left && entries[j].timestamp > key.timestamp) { // move right until current key is less than next or right bound reached
            entries[j + 1] = entries[j];
            j--;
        }
        entries[j + 1] = key; // emplace in correct position
    }
}

// binary search to find the index where the key should be inserted
// allow_equal is true when the leftside is galloping and false when the rightside is galloping
// this is because to maintain stability, values of the left that are equal to values on the right should come first
int binary_search(const vector<Entry>& part, int left, const Entry& key, bool allow_equal) {
    int right = static_cast<int>(part.size()) - 1;
    while (left <= right) {
        int mid = left + (right - left) / 2;
        // if left side is galloping, it will return the rightmost equal element if there are equal elements to the key
        // if the right side is galloping, it will return the greatest element smaller than the key
        if (part[mid].timestamp < key.timestamp || (allow_equal && part[mid].timestamp == key.timestamp)) {
            left = mid + 1; 
        } else {
            right = mid - 1;
        }
    }
    return left;
}

// galloping mode: quickly finds how many more elements in a part should be copied over
// does this by exponentially increasing index, and then use binary search to find the exact element to stop at
// returns the amount of elements to copy over
int gallop(const vector<Entry>& part, int left, const Entry& key, bool allow_equal) {
    // if the index is out of bounds or the element should come after the key, don't gallop
    if (left >= static_cast<int>(part.size())) {
        return 0;
    }
    // if first element already should not be taken, no gallop
    if (part[left].timestamp > key.timestamp || (!allow_equal && part[left].timestamp == key.timestamp)) {
        return 0;
    }
    int idx = 1;
    // if the left side is galloping we want to pass elements that are equal to maintain stability
    // if the right side is galloping we stop before elements that are equal
    while (left + idx < static_cast<int>(part.size()) &&
           (part[left + idx].timestamp < key.timestamp ||
            (allow_equal && part[left + idx].timestamp == key.timestamp))) {
        idx *= 2;
    }
    int start = left + idx / 2;
    return binary_search(part, start, key, allow_equal) - left;
}

// merge two pre-sorted runs into one: combine left->mid with mid+1->right
// fast on large pre-sorted chunks
void merge(vector<Entry>& entries, int left, int mid, int right, int min_gallop = 7) {
    // create copies of each run
    vector<Entry> left_part(entries.begin() + left, entries.begin() + mid + 1);
    vector<Entry> right_part(entries.begin() + mid + 1, entries.begin() + right + 1);
    // idx for left run, right run
    int i = 0;
    int j = 0;
    // idx to write to in original array
    int k = left;
    int left_size = left_part.size();
    int right_size = right_part.size();
    // counters to track how many times in a row we have taken from the left or right run
    // used to determine when to switch to galloping mode
    int left_count = 0;
    int right_count = 0;
    // while both runs have elements remaining
    while (i < left_size && j < right_size) {
        // choose lower timestamp
        if (left_part[i].timestamp <= right_part[j].timestamp) {
            // if we haven't taken the left run too many times, take the next element from the left run and update counters
            if (left_count < min_gallop) {
                entries[k] = left_part[i];
                i++;
                k++;
                left_count++;
                right_count = 0;
            }
        } else {
            // if we haven't taken the right run too many times, take the next element from the right run and update counters
            if (right_count < min_gallop) {
                entries[k] = right_part[j];
                j++;
                k++;
                right_count++;
                left_count = 0;
            }
        }
        // if we've been taking from the left run a couple times in a row,
        // switch to galloping mode instead of comparing each element one by one
        if (left_count >= min_gallop && j < right_size) {
            // the amount of elements to copy over from the left run
            int gallop_count = gallop(left_part, i, right_part[j], true);
            for (int t = 0; t < gallop_count; t++) {
                entries[k] = left_part[i];
                i++;
                k++;
            }
            // reset counters after galloping, as we will be switching to the right run after this
            left_count = 0;
        }

        // if we've been taking from the right run a couple times in a row,
        // switch to galloping mode instead of comparing each element one by one
        else if (right_count >= min_gallop && i < left_size) {
            // the amount of elements to copy over from the right run
            int gallop_count = gallop(right_part, j, left_part[i], false);
            for (int t = 0; t < gallop_count; t++) {
                entries[k] = right_part[j];
                j++;
                k++;
            }
            // reset counters after galloping, as we will be switching to the left run after this
            right_count = 0;
        }
    }
    // when one of the runs is empty, copy over the remaining elements from the other run
    while (i < left_size) {
        entries[k] = left_part[i];
        i++;
        k++;
    }
    while (j < right_size) {
        entries[k] = right_part[j];
        j++;
        k++;
    }
}

// function that determines where ascending and decreasing runs exist within the data
// returns the start and end idx of all runs found
vector<pair<int, int>> find_runs(vector<Entry>& entries, int min_run) {
    vector<pair<int, int>> runs; // store all runs found
    int n = entries.size();
    int i = 0; // current position of run search within entries
    while (i < n) { 
        int start = i;
        if (i == n - 1) { // if last element, nothing to compare to
            i++;
        } else {
            if (entries[i].timestamp <= entries[i + 1].timestamp) { // if current timestamp is less than or equal to next -> start of ascending run
                while (i + 1 < n && entries[i].timestamp <= entries[i + 1].timestamp) { // as long as next is greater than previous keep going
                    i++;
                }
            } else { // start of a descending run
                while (i + 1 < n && entries[i].timestamp > entries[i + 1].timestamp) { // as long as next is less than previous keep going
                    i++;
                }
                reverse(entries.begin() + start, entries.begin() + i + 1); // reverse descending run since mergesort is designed for ascending runs only
            }
            i++;
        }
        int end = i - 1;
        if (end - start + 1 < min_run) { // check if this run is too small (small runs are inefficient)
            end = min(start + min_run - 1, n - 1); // enlarges the run to fit the minimum size 
            insertion_sort(entries, start, end); // uses insertion sort to sort the enlarged run -> fast for small sections
            i = end + 1;
        }
        runs.push_back({start, end}); // store this run
    }
    return runs;
}


// the main sorting function that combines partially sorted parts and efficiently merges them using runs
// min_run sets the minimum length of a run to prevent inefficiency of small runs
void tim_sort(vector<Entry>& entries, int min_run = 4) {
    // find and store all runs within entries
    vector<pair<int, int>> runs = find_runs(entries, min_run);
    // add runs to a stack
    vector<pair<int, int>> stack;
    for (pair<int, int> run : runs) {
        stack.push_back(run);
        // if there are at least 3 runs on the stack, check if the last 3 runs satisfy the merging conditions
        if (stack.size() >= 3) {
            // get the lengths of the last 3 runs
            int x = stack[stack.size() - 1].second - stack[stack.size() - 1].first + 1;
            int y = stack[stack.size() - 2].second - stack[stack.size() - 2].first + 1;
            int z = stack[stack.size() - 3].second - stack[stack.size() - 3].first + 1;
            // if the two topmost runs combined are smaller than the third, and the topmost run is smaller than the second, do not merge yet
            // BUT if either of these conditions are broken, merge the middle run with the smaller of the top and third run and repeat until the conditions are satisfied again
            // this reduces the amount of times we have to copy large runs of elements and prioritizes merging smaller runs first
            while (x + y >= z || x >= y) {
                // if the top run is smaller than the third run, merge the top run with the middle run
                if (x < z) {
                    merge(entries, stack[stack.size() - 2].first, stack[stack.size() - 2].second, stack[stack.size() - 1].second);
                    // update stack to reflect the merge
                    stack[stack.size() - 2] = {stack[stack.size() - 2].first, stack[stack.size() - 1].second};
                    stack.pop_back();
                }
                // else merge the middle run with the third run
                else {
                    merge(entries, stack[stack.size() - 3].first, stack[stack.size() - 3].second, stack[stack.size() - 2].second);
                    // update stack to reflect the merge
                    stack[stack.size() - 3] = {stack[stack.size() - 3].first, stack[stack.size() - 2].second};
                    stack.erase(stack.end() - 2);
                }
                // update the lengths of the last 3 runs after the merge
                if (stack.size() >= 3) {
                    x = stack[stack.size() - 1].second - stack[stack.size() - 1].first + 1;
                    y = stack[stack.size() - 2].second - stack[stack.size() - 2].first + 1;
                    z = stack[stack.size() - 3].second - stack[stack.size() - 3].first + 1;
                } else {
                    break;
                }
            }
        }
    }
    // once all runs have been added to the stack, merge remaining runs until only one run remains which is the fully sorted list
    while (stack.size() > 1) {
        // merge the top two runs on the stack, which are the smallest runs remaining
        merge(entries, stack[stack.size() - 2].first, stack[stack.size() - 2].second, stack[stack.size() - 1].second);
        stack[stack.size() - 2] = {stack[stack.size() - 2].first, stack[stack.size() - 1].second};
        stack.pop_back();
    }
}



int main() {
    int n;
    cin >> n; // the number of entries to be read in
    vector<Entry> entries; // the vector to store the entries read in
    for (int i = 0; i < n; i++) { // loop through reading in each line as an entry and storing it as appropriate
        Entry e;
        cin >> e.timestamp >> e.server_id >> e.message;
        entries.push_back(e);
    }
    print_entries(entries); // print the pre-sorted entries list
    cout << "Running TimSort Now ..." << '\n' << '\n';
    tim_sort(entries); // sort entries by timestamp using tim_sort algorithm
    print_entries(entries); // print out the final sorted entries list
    return 0;
}