use std::io::{BufRead, BufReader, stdin};

use crate::mos::MosSolver;

pub mod mos;

fn main() {
    let mut lines = BufReader::new(stdin()).lines().filter_map(|x| x.ok());

    let _n = lines
        .next()
        .expect("first line contains length of data input")
        .trim()
        .parse::<usize>()
        .expect("data length is a unsigned integer");

    let data: Vec<u32> = lines
        .next()
        .expect("Line for the data")
        .split_whitespace()
        .map(|x| {
            x.parse::<u32>()
                .expect("ids should be 32-bit unsigned integers")
        })
        .collect();
    let (q, k) = if let [q, k] = lines
        .next()
        .expect("query info line")
        .split_whitespace()
        .map(|info| {
            info.parse::<usize>()
                .expect("Query info items should both be unsigned integers")
        })
        .collect::<Vec<usize>>()[0..2]
    {
        (q, k)
    } else {
        panic!("failed to parse query info line")
    };

    let mut solver = MosSolver::init(&data, k);

    for _query_n in 0..q {
        let parts: Vec<String> = lines
            .next()
            .expect("There should be another query!")
            .split_whitespace()
            .map(|s| s.to_owned())
            .collect();
        match parts[0].as_str() {
            "F" => solver.insert_query(
                mos::QueryType::Frequency(parts[1].parse().expect("frequency is u32")),
                parts[2].parse().expect(""),
                parts[3].parse().expect(""),
            ),
            "U" => solver.insert_query(
                mos::QueryType::Unique,
                parts[1].parse().expect(""),
                parts[2].parse().expect(""),
            ),
            _ => unreachable!("You should not be here... how did you get here?!"),
        }
    }

    solver
        .execute_queries()
        .iter()
        .for_each(|result| println!("{result}"));
}
