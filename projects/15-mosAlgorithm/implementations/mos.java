import java.util.Scanner;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

class Query {
    int left;
    int right;
    int index;
    public Query(int l, int r, int i) {
        this.left = l;
        this.right = r;
        this.index = i;
    }
}

public class mos {

    /* We will be computing Mo's Algorithm where our queries will be the sum of elements in the array
     * from left to right (inclusive)
     */
    public static int[] mos_algorithm(int[] arr, ArrayList<Query> block, int[] answers) {
        // Because all of the queries are already grouped based on block, we only need to sort them based on their
        // rigthmost point

        Collections.sort(block, Comparator.comparingInt(q -> q.right));

        int curL = 0;
        int curR = -1;
        int cursum = 0;

        // Because our queries are sorted nicely, we can keep a running sum between queries, and instead of calculating
        // the sum every single time, we can calculate the difference between queries. For the sum function, this means
        // adding numbers that are in the new query and not in the old one, and subtracting numbers that were in the old
        // query but are not in the new one.

        for (Query query : block) {
            while (curL > query.left) {
                curL-=1;
                cursum+=arr[curL];
            }
            while (curR < query.right) {
                curR+=1;
                cursum+=arr[curR];
            }
            while (curL < query.left) {
                cursum-=arr[curL];
                curL++;
            }
            while (curR > query.right) {
                cursum-=arr[curR];
                curR--;
            }
        
            answers[query.index] = cursum;

        }

        return answers;

    }

    /* An Implementation of Mo\'s Algorithm in Java
     * Inputs will be of the form
     * x_1 x_2 x_3 ... x_n: int[] // array of length n
     * num_queries: int // number of queries
     * left right: int, int // left index and right index you want to query, for q lines
     * 
     * */
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String input = scan.nextLine().trim();
        String[] parts = input.split("\\s+");
        int N = parts.length;
        int[] arr = new int[N];
        for(int i = 0; i < N; i++) {
            arr[i] = Integer.parseInt(parts[i]);
        }
        int Q = Integer.parseInt(scan.nextLine().trim());

        
        // Here we create an array of queries grouped based off of which block of sqrt{n} length 
        // the left index of the query falls into.

        int block = (int)Math.ceil(Math.sqrt(N));
        if (block == 0) block = 1;
        @SuppressWarnings("unchecked")
        ArrayList<Query>[] queries = new ArrayList[block];
        for (int j = 0; j < block; j++) {
            queries[j] = new ArrayList<>();
        }
        for (int j = 0; j < Q; j++) {
            int left = scan.nextInt();
            int right = scan.nextInt();
            // We keep track of the index of each query as it is given to us because we will sort the queries
            // inside of Mo's algorithm

            queries[left / block].add(new Query(left, right, j));
        }

        // Now we call Mo's algorithm on each block of the square root decomposition

        int[] answers = new int[Q];
        for (ArrayList<Query> blk : queries) {
            answers = mos_algorithm(arr,blk,answers);
        }

        // Now we can just print the answers array one item at a time, because answers is in order based on the 
        // index of the query asked

        for (int answer : answers) {
            System.out.println(answer);
        }


        scan.close();

    }



}
