/*
Example Scenario: Receive data from multiple servers all already in chronological order with respect to their own server
    -> When combined entries are sorted within each server, they are not necessarily sorted across all servers
    -> We want to sort all entries across all servers by timestamp in an efficient way
*/

import java.util.*;

// stores entries from each server as their 3 key components: timestamp (main sorting feature), server_id, and the associated message
class Entry {
    int timestamp;
    int server_id;
    String message;

    public Entry(int timestamp, int server_id, String message) {
        this.timestamp = timestamp;
        this.server_id = server_id;
        this.message = message;
    }
}

public class timSort {

    // the main method and format for printing the pre-sorted and then post-sorted list
    public static void printEntries(List<Entry> entries) {
        for (Entry entry : entries) {
            System.out.println("[" + entry.timestamp + "] " + entry.server_id + " : " + entry.message);
        }
        System.out.println();
    }

    // insertion sort to make small runs larger for merge sort from left to right index -> creates sorted chunks
    // idea: find each elements correct position within the left and right bounds
    public static void insertionSort(List<Entry> entries, int left, int right) {
        for (int i = left + 1; i <= right; i++) { // sorts only from left to right index
            Entry key = entries.get(i); // value to insert into correct position first
            int j = i - 1;
            // move right until current key is less than next or right bound reached
            while (j >= left && entries.get(j).timestamp > key.timestamp) {
                entries.set(j + 1, entries.get(j));
                j--;
            }
            entries.set(j + 1, key); // emplace in correct position
        }
    }

    // binary search to find the index where the key should be inserted
    private static int binarySearch(List<Entry> part, int left, Entry key, boolean allowEqual) {
        int right = part.size() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (part.get(mid).timestamp < key.timestamp || (allowEqual && part.get(mid).timestamp == key.timestamp)) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return left;
    }

    // galloping mode: quickly finds how many more elements in a part should be copied over
    private static int gallop(List<Entry> part, int left, Entry key, boolean allowEqual) {
        if (left >= part.size() || part.get(left).timestamp > key.timestamp || (!allowEqual && part.get(left).timestamp == key.timestamp)) {
            return 0;
        }
        int idx = 1;
        while (left + idx < part.size() && (part.get(left + idx).timestamp < key.timestamp || (allowEqual && part.get(left + idx).timestamp == key.timestamp))) {
            idx *= 2;
        }
        return binarySearch(part, left + idx / 2, key, allowEqual) - left;
    }

    // merge two pre-sorted runs into one: combine left->mid with mid+1->right
    // fast on large pre-sorted chunks
    public static void merge(List<Entry> entries, int left, int mid, int right) {

        int minGallop = 7;
        // create copies of each run
        List<Entry> leftPart = new ArrayList<>(entries.subList(left, mid + 1));
        List<Entry> rightPart = new ArrayList<>(entries.subList(mid + 1, right + 1));

        int i = 0; // idx in left run
        int j = 0; // idx in right run
        int k = left; // idx to write to in original array

        int leftCount = 0;
        int rightCount = 0;

        // while both runs have elements remaining
        while (i < leftPart.size() && j < rightPart.size()) {
            if (leftPart.get(i).timestamp <= rightPart.get(j).timestamp) { // choose lower timestamp
                if (leftCount < minGallop) {
                    entries.set(k, leftPart.get(i)); // left run is smaller
                    i++;
                    leftCount++;
                    rightCount = 0;
                }
            } else {
                if (rightCount < minGallop) {
                    entries.set(k, rightPart.get(j)); // right run is smaller
                    j++;
                    rightCount++;
                    leftCount = 0;
                }
            }
            k++;

            if (leftCount >= minGallop) {
                int gallopCount = gallop(leftPart, i, rightPart.get(j), true);
                for (int c = 0; c < gallopCount; c++) {
                    entries.set(k++, leftPart.get(i++));
                }
                leftCount = 0;
            } else if (rightCount >= minGallop) {
                int gallopCount = gallop(rightPart, j, leftPart.get(i), false);
                for (int c = 0; c < gallopCount; c++) {
                    entries.set(k++, rightPart.get(j++));
                }
                rightCount = 0;
            }
        }

        // left run had leftover items after right is empty, copy leftovers over as is
        while (i < leftPart.size()) {
            entries.set(k, leftPart.get(i));
            i++;
            k++;
        }

        // right run had leftover items after left is empty, copy leftovers over as is
        while (j < rightPart.size()) {
            entries.set(k, rightPart.get(j));
            j++;
            k++;
        }
    }

    // function that determines where ascending and decreasing runs exist within the data
    // returns the start and end idx of all runs found
    public static List<int[]> findRuns(List<Entry> entries, int minRun) {
        List<int[]> runs = new ArrayList<>(); // store all runs found
        int n = entries.size();
        int i = 0; // current position of run search within entries

        while (i < n) {
            int start = i;
            if (i == n - 1) { // if last element, nothing to compare to
                i++;
            } else {
                if (entries.get(i).timestamp <= entries.get(i + 1).timestamp) { // if current timestamp is <= next -> start of ascending run
                    while (i + 1 < n && entries.get(i).timestamp <= entries.get(i + 1).timestamp) { // as long as next is >= previous keep going
                        i++;
                    }
                } else { // start of a descending run
                    while (i + 1 < n && entries.get(i).timestamp > entries.get(i + 1).timestamp) { // as long as next is less than previous keep going
                        i++;
                    }
                    // reverse descending run since mergesort is designed for ascending runs only
                    Collections.reverse(entries.subList(start, i + 1));
                }
                i++;
            }

            int end = i - 1;
            if (end - start + 1 < minRun) { // check if this run is too small (small runs are inefficient)
                end = Math.min(start + minRun - 1, n - 1); // enlarges the run to fit the minimum size
                insertionSort(entries, start, end); // uses insertion sort to sort the enlarged run -> fast for small sections
                i = end + 1;
            }
            runs.add(new int[]{start, end}); // store this run
        }
        return runs;
    }

    // the main sorting function that combines partially sorted parts and efficiently merges them using runs
    public static void timSortAlgo(List<Entry> entries) {
        final int MIN_RUN = 4; // sets the minimum length of a run to prevent inefficiency of small runs
        List<int[]> runs = findRuns(entries, MIN_RUN); // find and store all runs within entries

        Stack<int[]> stack = new Stack<>();
        for (int[] run : runs) {
            stack.push(run);

            while (stack.size() >= 3) {
                int x = stack.get(stack.size() - 1)[1] - stack.get(stack.size() - 1)[0] + 1;
                int y = stack.get(stack.size() - 2)[1] - stack.get(stack.size() - 2)[0] + 1;
                int z = stack.get(stack.size() - 3)[1] - stack.get(stack.size() - 3)[0] + 1;

                if (x + y >= z || x >= y) {
                    if (x < z) {
                        int[] r2 = stack.pop();
                        int[] r1 = stack.pop();
                        merge(entries, r1[0], r1[1], r2[1]);
                        stack.push(new int[]{r1[0], r2[1]});
                    } else {
                        int[] r2 = stack.pop();
                        int[] r1 = stack.pop();
                        int[] r0 = stack.pop();
                        merge(entries, r0[0], r0[1], r1[1]);
                        stack.push(new int[]{r0[0], r1[1]});
                        stack.push(r2);
                    }
                } else break;
            }
        }

        while (stack.size() > 1) { // loop through every run merging until only one run remains (fully sorted)
            int[] r2 = stack.pop();
            int[] r1 = stack.pop();
            merge(entries, r1[0], r1[1], r2[1]); // merge the two runs using mergesort algorithm
            stack.push(new int[]{r1[0], r2[1]}); // store the new merged run
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextInt()) return;
        
        int n = sc.nextInt(); // the number of entries to be read in
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < n; i++) { // loop through reading in each line as an entry and storing it as appropriate
            int ts = sc.nextInt();
            int sid = sc.nextInt();
            String msg = sc.next();
            entries.add(new Entry(ts, sid, msg));
        }

        printEntries(entries); // print the pre-sorted entries list
        System.out.println("Running TimSort Now ...\n");

        timSortAlgo(entries); // sort entries by timestamp using tim_sort algorithm

        printEntries(entries); // print out the final sorted entries list
        sc.close();
    }
}