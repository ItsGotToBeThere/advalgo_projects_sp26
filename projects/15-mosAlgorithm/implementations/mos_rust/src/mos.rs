use std::{cmp::Reverse, collections::BinaryHeap};

/// Ah, the query struct!
/// Holds all data we need to conduct a query.
/// The left indecies are sorted into blocks of size
/// sqrt(n). This way we never move more than sqrt(n).
#[derive(Debug, Clone, Copy, Eq)]
struct Query {
    index: usize,
    left_index_block: usize,
    left: usize,
    right: usize,
}

impl PartialOrd for Query {
    /// For queries we order them by the left block index,
    /// and then if that is equal, we order them by the
    /// right index. This way we limit the amount of
    /// operations we have to complete.
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        use std::cmp::Ordering::*;
        match self.left_index_block.partial_cmp(&other.left_index_block) {
            Some(Equal) => {}
            ord => return ord,
        }
        self.right.partial_cmp(&other.right)
    }
}

// PartialOrd returns the ordering for cases including ones
// that dont have an ordering, in which case it returns
// `None`.
// But for the binary heap we need to have a true ordering,
// so we say if there is no ordering we just return equal.
impl Ord for Query {
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        match self.partial_cmp(other) {
            Some(ord) => ord,
            None => std::cmp::Ordering::Equal,
        }
    }
}
// Blocks are equal if their left block index and right
// index are equal, as far as sorting is concerned.
impl PartialEq for Query {
    fn eq(&self, other: &Self) -> bool {
        self.left_index_block == other.left_index_block && self.right == other.right
    }
}

/// A struct containing the data used by out implementation
/// of Mo's Algorithm. Contains the data, and a max heap of
/// all the queries.
pub struct MosAlg {
    data: Vec<usize>,
    block_size: usize,
    /// The binary heap is the max heap struct in rust.
    queries: BinaryHeap<Reverse<Query>>,
}

/// This is out implementation of Mo's algorithm in Rust.
/// It conducts the sum inplementation on the ranges
/// queried.
impl MosAlg {
    /// I know yall are likelt not super familiar with
    /// rust, so here's a quick crash course:
    /// The data lives in the structs. The implementations
    /// are applications of functions that act upon
    /// structs. Kinda like methods in java.
    /// If it has pub before it it is allowed to be used
    /// outside of the other implementations for the
    /// struct, kinda like public in java.
    /// The function signatures of the implementations can
    /// take in a `self`, kinda like python. However,
    /// unless it is a reference (`&self`), the `self` is
    /// consumed. This is an example of a function that
    /// does not use self.
    pub fn new(data: Vec<usize>) -> Self {
        let size = data.len();
        let block_size = (size as f32).sqrt().ceil() as usize;
        Self {
            data,
            block_size,
            queries: BinaryHeap::new(),
        }
    }

    /// When we want the implementation to modify the
    /// struct, we need to pass in a mutable reference,
    /// like `&mut self`.
    ///
    /// This function adds a query to our query list.
    /// Since it is a binary heap, this takes O(log Q)
    pub fn add_query(&mut self, left: usize, right: usize) {
        self.queries.push(Reverse(Query {
            index: self.queries.len(),
            left_index_block: left / self.block_size,
            left,
            right,
        }));
    }

    /// So often when applying actions to structs we use a
    /// reference.
    /// The arrow syntax is also used to indicate the
    /// return type.
    /// This function actually executes mo's algorithm
    pub fn execute(&self) -> Vec<usize> {
        // We define an output for each query, so that we
        // can put them back in order.
        let mut output = vec![0; self.queries.len()];
        let mut tmp = self.queries.clone(); // We clone the queries so we can pop without modifying it.

        // We define the start of our query range, the end,
        // and the sum. These values don't matter, as they
        // will be overwritten in the first iteration of
        // the loop.
        // usize and isize are the unsigned and signed
        // integer types respectively, defined by the
        // architecture rust is compiling to.
        let mut start: usize = 42;
        let mut end: usize = 42;
        let mut sum: isize = 42;

        // We set the last block to be something **extremely** unlikely.
        // This is so that everything is overwritten.
        let mut lastblock = usize::MAX;

        // We pop off queries as often as there are queries
        while let Some(Reverse(q)) = tmp.pop() {
            // If we are in a new block (or the first one)
            // we reset the start and end to the left index
            // of the first query in the block, and update
            // the sum and last block.
            if q.left_index_block != lastblock {
                start = q.left;
                end = q.left;
                sum = self.data[start] as isize;
                lastblock = q.left_index_block;
            }
            // This loop moves the start to the left until it
            // is at the left range index and adds to the sum
            while start > q.left {
                start -= 1;
                sum += self.data[start] as isize;
            }
            // This loop moves the start to the right until it
            // is at the left range index and subtracts from the sum
            while start < q.left {
                sum -= self.data[start] as isize;
                start += 1;
            }
            // This loop moves the end to the right until it
            // is at the right range index and adds to the sum
            while end < q.right {
                end += 1;
                sum += self.data[end] as isize;
            }
            // This loop moves the end to the left until it
            // is at the right range index and subtracts from the sum
            while end > q.right {
                sum -= self.data[end] as isize;
                end -= 1;
            }
            // We write the result to its proper index
            output[q.index] = sum as usize;
        }
        // And we return the list of results to the queries.
        output
    }
}
