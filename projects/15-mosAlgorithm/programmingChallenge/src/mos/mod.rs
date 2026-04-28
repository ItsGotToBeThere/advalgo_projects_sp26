use std::{
    cmp::Reverse,
    collections::{BinaryHeap, HashMap},
    ops::{AddAssign, SubAssign},
    rc::Rc,
};

mod query;
use query::Query;
pub use query::QueryType;

pub struct MosSolver {
    queries: BinaryHeap<Reverse<Rc<Query>>>,
    threshold: usize,
    block_size: usize,
    data: Vec<u32>,
}

impl MosSolver {
    /// Initiates a new instance of the MosSolver struct, used to solve
    /// the PC using mos algorithm
    /// Takes in an array of IDs, `a`,
    /// and a threshold for the number of unique ids, `k`.
    pub fn init(a: &[u32], k: usize) -> Self {
        let block_size = (a.len() as f64).sqrt().ceil() as usize;
        Self {
            threshold: k,
            queries: BinaryHeap::new(),
            block_size,
            data: a.to_vec(),
        }
    }

    /// Inserts a query into the binary heap struct of the solver.
    /// When the solver runs, the results will be in the order that
    /// they were added in.
    pub fn insert_query(&mut self, qtype: QueryType, left: usize, right: usize) {
        self.queries.push(Reverse(Rc::new(Query {
            index: self.queries.len(),
            query_type: qtype,
            left,
            block_index: left / self.block_size,
            right,
        })));
    }

    pub fn execute_queries(&self) -> Vec<u64> {
        let mut results = vec![0; self.queries.len()];

        let mut cloned = self.queries.clone();
        let mut block: usize = 0;
        let mut first_block = true;

        let mut solv_data = SolverData::new(self.threshold);

        let mut start: usize = 0;
        let mut end: usize = 0;
        while let Some(Reverse(q)) = cloned.pop() {
            if first_block || q.block_index != block {
                first_block = false;
                block = q.block_index;
                start = q.left;
                end = q.left;
                solv_data.reset();
                solv_data += self.data[start];
            }
            // Do loops to adjust
            while end < q.right {
                end += 1;
                solv_data += self.data[end];
            }
            while end > q.right {
                solv_data -= self.data[end];
                end -= 1;
            }
            while start < q.left {
                solv_data -= self.data[start];
                start += 1;
            }
            while start > q.left {
                start -= 1;
                solv_data += self.data[start]
            }
            results[q.index] = match q.query_type {
                QueryType::Frequency(key) => match solv_data.frequencies.get(&key) {
                    Some(&freq) => freq as u64,
                    None => 0 as u64,
                },
                QueryType::Unique => solv_data.unique as u64,
            };
        }

        results
    }
}

struct SolverData {
    unique: usize,
    freq_query_threshold: usize,
    frequencies: HashMap<u32, usize>,
}

impl SolverData {
    fn new(thresh: usize) -> Self {
        Self {
            unique: 0,
            freq_query_threshold: thresh,
            frequencies: HashMap::new(),
        }
    }
    fn add(&mut self, n: u32) {
        if let Some(freq) = self.frequencies.get_mut(&n) {
            *freq += 1;
            if *freq == self.freq_query_threshold {
                self.unique += 1;
            }
        } else {
            self.frequencies.insert(n, 1);
            if self.freq_query_threshold == 1 {
                self.unique += 1;
            }
        }
    }
    fn sub(&mut self, n: u32) {
        if let Some(freq) = self.frequencies.get_mut(&n) {
            if *freq == self.freq_query_threshold {
                self.unique -= 1;
            }
            *freq -= 1;
        }
    }
    fn reset(&mut self) {
        self.frequencies.clear();
        self.unique = 0;
    }
}

impl AddAssign<u32> for SolverData {
    fn add_assign(&mut self, n: u32) {
        self.add(n);
    }
}

impl SubAssign<u32> for SolverData {
    fn sub_assign(&mut self, n: u32) {
        self.sub(n);
    }
}
