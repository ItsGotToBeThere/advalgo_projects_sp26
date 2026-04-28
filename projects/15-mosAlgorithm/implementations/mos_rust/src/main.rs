mod mos;
use std::io::stdin;

use crate::mos::MosAlg;

/// The main function just parses the input, passes it to the solver, and runs the solver,
/// but I guess I can explain it anyway.
fn main() {
    // We create an iterator that reads from stdin one line at a time, and only takes valid strings.
    let mut lines = stdin().lines().filter_map(|x| x.ok());

    // This reads in a line from the iterator,
    // It returns a Vec of usizes.
    // A vec is like an ArrayList in Java.
    let array: Vec<usize> = lines
        .next()
        .expect("has array string") // Expect just says that we expect to have a line and to panic if we dont
        .split_whitespace() // Splits it at the whitespace
        .filter_map(|x| x.parse().ok()) // Then maps all the strings to usize and filters for valid usizes
        .collect(); // And collects it to a vec

    // We do a similar thing for the next line of the input
    let num_queries = lines
        .next()
        .expect("num queries line in input") // The use of `expect`s here is a bit lazy, but we aren't focused on proper input parsing or error handling
        .parse::<usize>()
        .expect("num queries input is a usize");

    let mut mos_solver = MosAlg::new(array); // we initialize the solver with our data vec.
                                             // It consumes and takes ownership of the data

    for _ in 0..num_queries {
        // For each query, we parse the range line
        let parts: Vec<usize> = lines
            .next()
            .expect("query in input")
            .split_whitespace() // Split at whitespace
            .map(|index| index.parse().expect("range index is usize")) // parse as usizes
            .take(2) // And take the first two (there should only be two)
            .collect();
        mos_solver.add_query(parts[0], parts[1]); // And add it to our solver
    }

    // Then we execute our solver, and iterate through the results, printing each one.
    for result in mos_solver.execute().into_iter() {
        println!("{result}");
    }
}
