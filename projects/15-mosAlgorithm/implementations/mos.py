import math, sys

class Query:
    left: int
    right: int
    idx: int

    def __init__(self, left, right, idx):
        self.left = left
        self.right = right
        self.idx = idx

"""
We will be computing Mo's Algorithm where our queries will be the sum of elements in the array
from left to right (inclusive)
"""
def mos_algorithm(array: list[int], queries, answers):
    """
    Because all of the queries are already grouped based on block, we only need to sort them based on their
    rigthmost point
    """
    sorted_queries = sorted(queries, key=lambda x: x.right)

    current_left = 0
    current_right = -1
    
    cursum = 0
    """
    Because our queries are sorted nicely, we can keep a running sum between queries, and instead of calculating
    the sum every single time, we can calculate the difference between queries. For the sum function, this means
    adding numbers that are in the new query and not in the old one, and subtracting numbers that were in the old
    query but are not in the new one.
    """
    for query in sorted_queries:
        while current_left > query.left:
            current_left-=1
            cursum+=array[current_left]
        
        while current_right < query.right:
            current_right+=1
            cursum+=array[current_right]

        while current_left < query.left:
            cursum-=array[current_left]
            current_left+=1
        
        while current_right > query.right:
            cursum-=array[current_right]
            current_right-=1
        
        answers[query.idx] = cursum
    
    return answers


if __name__ == "__main__":
    """
    An Implementation of Mo's Algorithm in Python
    Inputs will be of the form
    x_1 x_2 x_3 ... x_n: int[] // array of length n
    num_queries: int // number of queries
    left right: int, int // left index and right index you want to query, for q lines
    """

    array: list[int] = [int(x) for x in input().split(" ")]
    num_queries: int = int(input())

    """
    Here we create an array of queries grouped based off of which block of sqrt{n} length 
    the left index of the query falls into.
    """
    sqrt_n = int(math.ceil(math.sqrt(len(array))))
    query_blocks = [[] for _ in range(sqrt_n)]
    for i in range(num_queries):
        left, right = [int(x) for x in input().split(" ")]
        """
        We keep track of the index of each query as it is given to us because we will sort the queries
        inside of Mo's algorithm
        """
        query_blocks[left//sqrt_n].append(Query(left,right,i))

    """
    Now we call Mo's algorithm on each block of the square root decomposition
    """
    answers = [0 for _ in range(num_queries)]
    for block in query_blocks:
        answers = mos_algorithm(array,block,answers)

    """
    Now we can just print the answers array one item at a time, because answers is in order based on the 
    index of the query asked
    """
    for answer in answers:
        print(answer)
