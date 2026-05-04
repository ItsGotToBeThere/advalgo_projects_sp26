import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class gs {
    // input: 
    //   proposers rank preference of all acceptors n x n 2d array
    //   acceptors rank preference of all proposers n x n 2d array
    public static int[] gale_shapley(int[][] proposersPref, int[][] acceptorsPref) {
        int n = proposersPref.length;
        
        // keeps track of which proposers have formed a match, 
        // proposers will be added as they match and removed if the acceptor receives a more appealing offer (more prefered proposer)
        HashSet<Integer> unmatchedProposers = new HashSet<>();
        for (int i = 0; i < n; i++) {
            unmatchedProposers.add(i);
        }
        
        // indexed by proposer
        // stores which number preference is next to be offered 
        // (i.e. if index i of the list is 0, proposer i will offer their first prefered acceptor)
        int[] proposersNextPrefIndex = new int[n];

        // indexed by acceptor
        // stores which proposer they are currently matched to, n if not matched yet (proposers are enumerated from 0 to n-1)
        int[] acceptorMatching = new int[n];
        Arrays.fill(acceptorMatching, -1);

        // 2d array indexed by acceptor and proposer
        // specifies preference rank of the proposer for each acceptor
        // (i.e. acceptor_pref_matrix[0][1] = 0 means that proposer 1 is acceptor 0's first preference)
        int[][] acceptorPrefRank = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                acceptorPrefRank[i][acceptorsPref[i][j]] = j;
            }
        }

        // 2d array indexed by proposer and i (from 0 to n - 1)
        // specifies the ith preference of each proposer
        // (i.e. proposer_pref_matrix[0][1] = 0 means that proposer 0's second preference is acceptor 0)
        int[][] proposerPrefRank = proposersPref;

        // go until all proposers have a match, this results in proposer optimal stable matching
        int[] matches = new int[n];
        Arrays.fill(matches, -1);
        while (!unmatchedProposers.isEmpty()) {
            // list of proposers, who are all unmatched, that will give out a proposal this round
            List<Integer> proposersThisRound = new ArrayList<>(unmatchedProposers);
            for (int proposer : proposersThisRound) {
                // retrieve the index of the proposers next prefered acceptor
                int nextPreferedIndex = proposersNextPrefIndex[proposer];
                proposersNextPrefIndex[proposer]++;

                // get the next prefered acceptor
                int nextPreferedAcceptor = proposerPrefRank[proposer][nextPreferedIndex];
                // get the rank for the proposer of the next prefered acceptor
                int acceptorMatchRank = acceptorPrefRank[nextPreferedAcceptor][proposer];

                // get the next prefered acceptor's previous proposer and their rank to the next prefered acceptor
                int previousProposer = acceptorMatching[nextPreferedAcceptor];
                int prevAcceptorMatchRank = (previousProposer != -1) ? acceptorPrefRank[nextPreferedAcceptor][previousProposer] : n;

                // if the next prefered acceptor prefers the proposer to their current match, match with the proposer and unmatch with the previous proposer
                if (acceptorMatchRank < prevAcceptorMatchRank) {
                    // unmatch the previous proposer if there is one, and add them to the unmatched proposers set
                    if (previousProposer != -1) {
                        unmatchedProposers.add(previousProposer);
                        matches[previousProposer] = -1;
                    }
                    
                    // match with the proposer and remove them from the unmatched proposers set
                    unmatchedProposers.remove(proposer);

                    acceptorMatching[nextPreferedAcceptor] = proposer;
                    matches[proposer] = nextPreferedAcceptor;
                }
            }
        }
        // return the matches, where the index is the proposer and the value is the acceptor they are matched to
        return matches;
    }
    
    // to run
    // javac gs.java
    // java gs < sample.in.1
    public static void main(String[] args) {
        java.util.Scanner sc = new java.util.Scanner(System.in);
        
        // determines size of preference matrices, n x n
        int n = sc.nextInt();

        // read proposer preference matrix
        int[][] proposersPref = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                proposersPref[i][j] = sc.nextInt();
            }
        }

        // read acceptor preference matrix
        int[][] acceptorsPref = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                acceptorsPref[i][j] = sc.nextInt();
            }
        }

        // find stable matching
        int[] result = gale_shapley(proposersPref, acceptorsPref);

        for (int i = 0; i < result.length; i++) {
            System.out.print(result[i]);
            if (i < result.length - 1) {
                System.out.print(" ");
            }
        }
        System.out.println();

        sc.close();
    }
}
