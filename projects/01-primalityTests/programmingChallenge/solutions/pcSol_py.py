from collections import deque

# Faster exponentiation function (explained in base implementation)
def power_mod(base, exp, mod):
    result = 1
    base = base % mod
    
    while exp > 0:

        if (exp % 2) == 1:
            result = (result * base) % mod

        exp = exp >> 1
        base = (base * base) % mod

    # Result now holds (base^exp) % mod
    return result

# Miller-Rabin composite check (explained in base implementation)
def check_composite(n, a, d, r):
    x = power_mod(a, d, n)

    if x == 1 or x == n-1:
        return False
    
    for _ in range(r-1):
        x = (x*x) % n

        if x == n-1:
            return False
        
    return True

# Miller Rabin implementation
def is_prime_miller_rabin(n, k=5):
    if n <= 1:
        return False
    
    if n <= 3:
        return True
    
    if n % 2 == 0:
        return False

    r = 0
    d = n - 1
    while d % 2 == 0:
        d //= 2
        r += 1

    # Deterministic implementation for 64-bit integers, checking these bases will catch all non-primes
    for a in {2, 325, 9375, 28178, 450775, 9780504, 1795265022}:
        if a % n == 0:
            continue
        if (n == a):
            return True
        if (check_composite(n, a, d, r)):
            return False
    
    return True

#Finding all numbers that can be created by changing one digit of the current number.
def get_neighbors(num):
    neighbors = []
    s = list(str(num))

    for i in range(len(s)):
        original = s[i]
        for d in '0123456789':
            if d == original:
                continue

            s[i] = d
            new_num = int("".join(s))

            # avoid leading zero
            if s[0] != '0':
                neighbors.append(new_num)

        s[i] = original

    return neighbors


def prime_path(src, target):
    queue = deque([src])
    visited = set([src])
    parent = {src: -1}

    while queue:
        cur = queue.popleft()

        if cur == target:
            # reconstruct path
            path = []
            while cur != -1:
                path.append(cur)
                cur = parent[cur]
            return path[::-1]

        for nei in get_neighbors(cur):
            if nei % 2 == 0 or nei % 5 == 0 or nei % 3 == 0:
                continue
            if nei not in visited and is_prime_miller_rabin(nei):
                visited.add(nei)
                parent[nei] = cur
                queue.append(nei)

    return None

def reconstruct(meet, parent1, parent2):
    path1 = []
    cur = meet
    while cur != -1:
        path1.append(cur)
        cur = parent1[cur]
    path1.reverse()

    path2 = []
    cur = parent2[meet]
    while cur != -1:
        path2.append(cur)
        cur = parent2[cur]

    return path1 + path2

# Bidirectional search code -- faster than basic BFS/DFS to find path between two nodes.
def bidirectional_prime_path(src, target):
    if src == target:
        return [src]

    # frontiers
    front1 = {src}
    front2 = {target}

    # visited
    visited1 = {src}
    visited2 = {target}

    # parents
    parent1 = {src: -1}
    parent2 = {target: -1}

    while front1 and front2:
        # expand smaller frontier
        if len(front1) > len(front2):
            front1, front2 = front2, front1
            visited1, visited2 = visited2, visited1
            parent1, parent2 = parent2, parent1

        next_front = set()

        for node in front1:
            for nei in get_neighbors(node):
                if nei in visited1:
                    continue

                if not is_prime_miller_rabin(nei):
                    continue

                parent1[nei] = node

                if nei in visited2:
                    return reconstruct(nei, parent1, parent2)

                visited1.add(nei)
                next_front.add(nei)

        front1 = next_front

    return None


def main():
    src, target = map(int, input().split())

    if not is_prime_miller_rabin(src) or not is_prime_miller_rabin(target):
        print("impossible")
        return

    path = bidirectional_prime_path(src, target)

    if path is None:
        print("impossible")
    else:
        if (path[0] == src):
            for x in path:
                print(x)
        else:
            for x in reversed(path):
                print(x)


if __name__ == "__main__":
    main()
