#[derive(PartialEq, Eq, Debug)]
pub enum QueryType {
    Frequency(u32),
    Unique,
}

#[derive(Debug, Eq)]
pub struct Query {
    pub index: usize,
    pub query_type: QueryType,
    pub left: usize,
    pub block_index: usize,
    pub right: usize,
}

impl PartialEq for Query {
    fn eq(&self, other: &Self) -> bool {
        self.left == other.left && self.right == other.right && self.query_type == other.query_type
    }
}
impl PartialOrd for Query {
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        use std::cmp::Ordering::*;
        Some(if self.block_index > other.block_index {
            Greater
        } else if self.block_index < other.block_index {
            Less
        } else if self.right > other.right {
            Greater
        } else if self.right < other.right {
            Less
        } else {
            Equal
        })
    }
}
impl Ord for Query {
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        match self.partial_cmp(other) {
            Some(ord) => ord,
            None => unreachable!(),
        }
    }
}
