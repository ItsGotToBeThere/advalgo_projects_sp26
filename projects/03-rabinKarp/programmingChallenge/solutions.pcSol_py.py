# assigns int value to each char
def dna_ord(char):
    char_d = {'G':0, 'A':1, 'T':2, 'C': 3}
    return char_d[char]

# finds patterns in text using the multi pattern Rabin Karp algorithm.
# groups patterns by length to process varying pattern lengths
def rabin_karp_multi_pattern(text, patterns_info):
    n = len(text)
    
    # Group patterns by their length
    # length -> { hash_value: [(name, pattern), ...] }
    length_groups = {}
    for name, pattern in patterns_info:
        m = len(pattern)
        if m == 0 or m > n:
            continue
        if m not in length_groups:
            length_groups[m] = []
        length_groups[m].append((name, pattern))
        
    # Only ATCG
    base = 4
    # good prime
    prime = 101
    results = []
    
    # process each unique pattern length
    for m, group in length_groups.items():
        
        # hpatterns stores the set of pattern hashes.
        # we store the actual pattern strings to resolve hash collisions later.
        hpatterns = {}
        for name, pattern in group:
            p_hash = 0
            for char in pattern:
                p_hash = (base * p_hash + dna_ord(char)) % prime
            
            if p_hash not in hpatterns:
                hpatterns[p_hash] = []
            hpatterns[p_hash].append((name, pattern))
            
        # Calculate base^(m-1) % mod
        h = 1
        for _ in range(m - 1):
            h = (h * base) % prime
            
        # Calculate the hash for the initial window
        t_hash = 0
        for i in range(m):
            t_hash = (base * t_hash + dna_ord(text[i])) % prime
            
        #Slide the window over the text
        for i in range(n - m + 1):
            
            # Check if the rolling hash exists in our set of pattern hashes
            if t_hash in hpatterns:
                # verify char by char
                window = text[i:i+m]
                for name, pattern in hpatterns[t_hash]:
                    if window == pattern:
                        results.append((i, name))
                        
            # roll the hash to the next window
            if i < n - m:
                t_hash = (base * (t_hash - dna_ord(text[i]) * h) + dna_ord(text[i+m])) % prime  
                # correct negative hashes
                if t_hash < 0:
                    t_hash += prime
    return results

def main():

    #get the genome as a string
    genome = input().strip()
        
    # get the number of mutations
    try:
        num_mutations = int(input().strip())
    except ValueError:
        return
        
    # put the mutations into a list
    patterns_info = []
    for i in range(num_mutations):
        parts = input().strip().rsplit(' ', 1)
        if len(parts) == 2:
            patterns_info.append((parts[0], parts[1]))
            
    # do the multi-pattern search
    detected_mutations = rabin_karp_multi_pattern(genome, patterns_info)
    
    # Output the detected mutations 
    for index, name in detected_mutations:
        print(f"{name} {index}")

if __name__ == '__main__':
    main()