# input: 
#   proposers rank preference of all acceptors n x n list of list
#   acceptors rank preference of all proposers n x n list of list

def gale_shapley(proposers_pref: list[list], acceptors_pref: list[list]) -> list[int]:
    
    n = len(proposers_pref)

    # keeps track of which proposers have formed a match, 
    # proposers will be added as they match and removed if the acceptor receives a more appealing offer (more prefered proposer)
    unmatched_proposers = set(range(n))
    # indexed by proposer
    # stores which number preference is next to be offered 
    # (i.e. if index i of the list is 0, proposer i will offer their first prefered acceptor)
    proposer_next_pref = [0] * n
    # indexed by acceptor
    # stores which proposer they are currently matched to, n if not matched yet (proposers are enumerated from 0 to n-1)
    acceptor_matching = [-1] * n
    # 2d array indexed by acceptor and proposer
    # specifies preference rank of the proposer for each acceptor
    # (i.e. acceptor_pref_matrix[0][1] = 0 means that proposer 1 is acceptor 0's first preference)
    acceptor_pref_matrix = [[0] * n for _ in range(n)]
    # 2d array indexed by proposer and i (from 0 to n - 1)
    # specifies the ith preference of each proposer
    # (i.e. proposer_pref_matrix[0][1] = 0 means that proposer 0's second preference is acceptor 0)
    proposer_pref_matrix = proposers_pref

    # iterate over each acceptor and their preferences, marking the applicant_pref_matrix with the specified acceptor, proposer rank
    for acceptor, preference in enumerate(acceptors_pref):
        for rank, proposer in enumerate(preference):
            acceptor_pref_matrix[acceptor][proposer] = rank
    
    # go until all proposers have a match, this results in proposer-optimal stable matching
    matches = [-1] * n
    while (len(unmatched_proposers) != 0):
        # for each unmatched proposers make a proposal for the next prefered index and increment the index
        # for those acceptors, if that proposer is more prefered than the current proposer, accept and unmatch with the previous proposer
        
        # list of proposers, who are all unmatched, that will give out a proposal this round
        proposers_this_round = list(unmatched_proposers)
        for proposer in proposers_this_round:
            # retrieve the index of the proposers next prefered acceptor
            next_prefered_index = proposer_next_pref[proposer]
            proposer_next_pref[proposer] += 1


            # get the next prefered acceptor
            next_prefered_acceptor = proposer_pref_matrix[proposer][next_prefered_index]
            # get the rank for the proposer of the next prefered acceptor
            acceptor_match_rank = acceptor_pref_matrix[next_prefered_acceptor][proposer]

            # get the next prefered acceptor's previous proposer and their rank to the next prefered acceptor
            previous_proposer = acceptor_matching[next_prefered_acceptor]
            prev_acceptor_match_rank = acceptor_pref_matrix[next_prefered_acceptor][previous_proposer] if previous_proposer != -1 else n

            # if the next prefered acceptor prefers the proposer to their current match, match with the proposer and unmatch with the previous proposer
            if acceptor_match_rank < prev_acceptor_match_rank:
                # unmatch the previous proposer if there is one, and add them to the unmatched proposers set
                if previous_proposer != -1:
                    unmatched_proposers.add(previous_proposer)
                    matches[previous_proposer] = -1
                
                # match with the proposer and remove them from the unmatched proposers set
                unmatched_proposers.remove(proposer)

                acceptor_matching[next_prefered_acceptor] = proposer
                matches[proposer] = next_prefered_acceptor

    # return the matches, where the index is the proposer and the value is the acceptor they are matched to
    return matches

# to run
# python3 gs.py < sample.in.1
def main():
    import sys

    data = sys.stdin.read().strip().split()
    if not data:
        return

    idx = 0
    # determines size of preference matrices, n x n
    n = int(data[idx])
    idx += 1

    # read in the proposers and acceptors preference matrices
    proposers = []
    for _ in range(n):
        row = list(map(int, data[idx:idx+n]))
        proposers.append(row)
        idx += n

    acceptors = []
    for _ in range(n):
        row = list(map(int, data[idx:idx+n]))
        acceptors.append(row)
        idx += n

    # find stable matching
    result = gale_shapley(proposers, acceptors)
    print(" ".join(map(str, result)))


if __name__ == "__main__":
    main()