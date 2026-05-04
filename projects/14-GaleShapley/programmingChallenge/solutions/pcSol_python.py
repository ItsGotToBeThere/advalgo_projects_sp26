# input:
#   m hospitals with capacities, n patients
#   m x n distance matrix (distances[h][p] = distance from hospital h to patient p)
# output:
#   stable matching of patients to hospitals using Gale-Shapley

# the gale shapley algorithm, mostly reused from the base implementation
# proposer_pref is a list of lists, where proposer_pref[i] is the ordered preference list for proposer i
# acceptor_rank is a 2d array where acceptor_rank[a][p] gives the rank of proposer p for acceptor a
def gale_shapley(proposer_pref, acceptor_rank):
    n = len(proposer_pref)

    # keeps track of which proposers have not yet formed a match
    # proposers are added back if the acceptor receives a more preferred proposer
    unmatched = set(range(n))
    # indexed by proposer
    # stores which number preference is next to be proposed to
    # (i.e. if next_pref[0] = 2, proposer 0 will next propose to their 3rd preferred acceptor)
    next_pref = [0] * n
    # indexed by acceptor
    # stores which proposer they are currently matched to, -1 if not matched yet
    acceptor_match = [-1] * n
    # indexed by proposer
    # stores which acceptor they are currently matched to, -1 if not matched yet
    matches = [-1] * n

    # go until all proposers have a match, this results in proposer-optimal stable matching
    while len(unmatched) != 0:
        # for each unmatched proposer, make a proposal to the next preferred acceptor and increment the index
        # for those acceptors, if the new proposer is more preferred than the current one, accept and unmatch the previous
        proposers_this_round = list(unmatched)
        for prop in proposers_this_round:
            # retrieve the index of the proposer's next preferred acceptor
            pref_idx = next_pref[prop]
            next_pref[prop] += 1

            # get the next preferred acceptor
            acceptor = proposer_pref[prop][pref_idx]
            # get the rank for the proposer from the perspective of the acceptor
            prop_rank = acceptor_rank[acceptor][prop]

            # get the acceptor's current match and their rank from the acceptor's perspective
            prev = acceptor_match[acceptor]
            prev_rank = acceptor_rank[acceptor][prev] if prev != -1 else n

            # if the acceptor prefers the new proposer to their current match, switch
            if prop_rank < prev_rank:
                # unmatch the previous proposer if there is one, and add them back to unmatched
                if prev != -1:
                    unmatched.add(prev)
                    matches[prev] = -1

                # match with the new proposer and remove them from unmatched
                unmatched.remove(prop)
                acceptor_match[acceptor] = prop
                matches[prop] = acceptor

    # return the matches, where the index is the proposer and the value is the acceptor they are matched to
    return matches


def main():
    # read input
    m, n = map(int, input().split())
    capacities = list(map(int, input().split()))
    distances = []
    for h in range(m):
        distances.append(list(map(int, input().split())))

    # convert distances to preference lists by sorting
    # hospital_pref[h] = list of patient indices sorted by distance (closest first)
    hospital_pref = []
    for h in range(m):
        hospital_pref.append(sorted(range(n), key=lambda p: distances[h][p]))

    # patient_pref[p] = list of hospital indices sorted by distance (closest first)
    patient_pref = []
    for p in range(n):
        patient_pref.append(sorted(range(m), key=lambda h: distances[h][p]))

    # expand hospitals into individual proposers
    # a hospital with capacity k becomes k separate proposers that share the same preference list
    # exp_hospital[i] stores which original hospital expanded proposer i belongs to
    # (i.e. if capacities = [2, 3], then exp_hospital = [0, 0, 1, 1, 1])
    exp_hospital = []
    for h in range(m):
        for _ in range(capacities[h]):
            exp_hospital.append(h)

    # build the proposer preference lists for the expanded proposers
    # each copy just uses its original hospital's preference list
    proposer_pref = []
    for i in range(n):
        proposer_pref.append(hospital_pref[exp_hospital[i]])

    # build the acceptor (patient) rank matrix over the expanded proposers
    # first figure out each patient's rank for each original hospital
    # patient_hosp_rank[p][h] = rank of hospital h for patient p
    # (i.e. patient_hosp_rank[0][1] = 0 means hospital 1 is patient 0's first preference)
    patient_hosp_rank = [[0] * m for _ in range(n)]
    for p in range(n):
        for rank, h in enumerate(patient_pref[p]):
            patient_hosp_rank[p][h] = rank

    # build the rank matrix over expanded proposers
    # patients rank expanded proposers by their original hospital's rank, tiebreaking by index
    # acceptor_rank[p][i] = rank of expanded proposer i for patient p
    acceptor_rank = [[0] * n for _ in range(n)]
    for p in range(n):
        # sort the expanded proposers by (hospital rank, index) and assign ranks
        order = []
        for i in range(n):
            order.append((patient_hosp_rank[p][exp_hospital[i]], i))
        order.sort()

        for rank in range(n):
            prop = order[rank][1]
            acceptor_rank[p][prop] = rank

    # run gale shapley with expanded hospitals as proposers, patients as acceptors
    matches = gale_shapley(proposer_pref, acceptor_rank)

    # collect results back by original hospital
    hospital_patients = [[] for _ in range(m)]
    for i in range(n):
        h = exp_hospital[i]
        hospital_patients[h].append(matches[i])

    # print output
    for h in range(m):
        patients = sorted(hospital_patients[h])
        print(str(h) + ": " + " ".join(map(str, patients)))


# to run
# python3 solution.py < input.txt
if __name__ == "__main__":
    main()