/*
Burrows Wheeler Transform Implementation in C++

Burrows Wheeler Transform rearranges characters based on the 
lexicographic sort of all rotations of a string. 
It is used as a preprocessing step of compression including in bzip2 and in bioinformatics.
It clusters same characters together generally in a way that is reversible to recover original string.

general function:
example:
banana$

all rotations:
banana$
anana$b
nana$ba
ana$ban
na$bana
a$banan
$banana

sort lexicographicaly
$banana
a$banan
ana$ban
anana$b
banana$
na$bana
nana$ba

take last char of each: annb$aa
however, just doing directly is slow and takes memory. 
O(n^2logn) time
O(n^2) space complexity



This program does it in a more efficient way O(nlogn)
prefix doubling algorithm
iteratively sort cyclic shifts using equivalence classes. 
At each step, we double the substring length,
reusing previous results to sort efficiently with counting sort.
$ used for reversal

initially,
sort characters using counting sort
assign each position a class/rank associated with the order of that character
    equal characters receive same rank
    different characters receive increasing ranks

repeatedly, for each k sort substrings of length 2^k+1:
    sort substrings of length 2^(k+1)    
    two halves of length 2^k
    use previous iteration for sorting of second half. shift by 2^k and use sort of 2^k length substrings
    Use counting sort (stable) to sort the equivalence classes of the first half while keeping ordering of second half.
    recompute equivalence classes by comparing first halves and second halves of adjacent substrings
until 2^k is at least n

take last character of each substring in order

example:
banana$

p is the ordering of indices of substrings
c is ranks/classes

b a n a n a $           p =[6($), 1(a), 3(a), 5(a), 0(b), 2(n), 4(n)]   c=[2,1,3,1,3,1,0]
ba an na an na a$ $b    
ordering of second characters given by p-1 (looping to n when negative) pn = [5($),1(a),2,4,6,1,3]
0: ba -> (2,1)
1: an -> (1,3)
2: na -> (3,1)
3: an -> (1,3)
4: na -> (3,1)
5: a$ -> (1,0)
6: $b -> (0,2)  

counting sort on ranks in c in order of pn:
(0,2) -> 6
(1,0) -> 5
(1,3) -> 1
(1,3) -> 3
(2,1) -> 0
(3,1) -> 2
(3,1) -> 4
p = [6,5,1,3,0,2,4]
recompute ranks/classes
c = [3 2 4 2 4 1 0]

repeat 
ba|na 
an|an
na|na
an|a$
na|$b
a$|ba
$b|an

until substrings are longer than length of banana$ (7)
[6, 5, 3, 1, 0, 4, 2]
for each, take character in position before that index of string
a n n b $ a a
*/


#include <algorithm>
#include <string>
#include <vector>
#include <iostream>

using namespace std;

string burrowsWheelerTransform(string s){
    //append $ to be able to reverse
    s += '$';

    //initialize variables
    //n is the size of the input string
    //alphabet size to be able to perform counting sort on all possible characters
    //p(n) is ordering of indices of substrings
    //pn(n) is temporary shifted version of p(n) to sort second halves of substrings
    //c(n) is ranks of substrings
    //cn(n) is temporary version used to compute new ranks
    int n = s.size();
    const int alphabet = 256;
    vector<int> p(n), pn(n), c(n), cn(n), cnt(alphabet, 0);


    //perform counting sort on all characters of substring
    //count number of each character
    for (int i = 0; i <n; i++){
        cnt[(unsigned char)s[i]] += 1;
    }
    //prefix sum the counts
    for (int i=1; i<alphabet; i++){
        cnt[i] += cnt[i-1];
    }
    //assign indexes for p (ordering of indices in lexicographical order) based on prefix sums of counts
    //use prefix sums as last index for each character
    for (int i=n-1; i>=0; i--){
        p[--cnt[(unsigned char)s[i]]] = i;
    }

    //assign ranks in c based on equivalence classes
    //start from class 0 and compare characters in adjacent indices. 
    //start new class if characters are different
    int cat = 0;
    c[p[0]] = cat;
    for (int i=1; i<n; i++){
        if (s[p[i]] != s[p[i-1]]){
            cat++;
        }
        c[p[i]] = cat;
    }
    //resize counts array to be max size of length of array since classes will now be grouped on previous substrings instead of all characters
    cnt.resize(n);

    //iterate substring sizes of 2^k from 0 until greater than length of string
    //this doublses substring length each iteration
    //computer ordering (p) and ranks of indices (c) for each substring length
    //the last one will be as long as or logner than string. if it is longer, that is fine, since it will wrap around and they will still be in the same order

    for (int k = 0; (1<<k) < n; k++){
        //pn is indices of second halves of substring of length 2^(k+1)
        //computed by shifting p (orderings of substrings of length 2^k) by 2^k to account for the rotations since the last iteration
        for (int i=0; i<n; i++){
            pn[i] = p[i]-(1<<k);
            if (pn[i] < 0){
                pn[i] += n;
            }
        }
        //perform counting sort
        //reset array of counts. Counting sort will be by ranks of substrings in previous iteration
        //this is done in order of pn to keep ordering of second halves
        //This performs a stable sort
        fill(cnt.begin(), cnt.begin() + cat + 1, 0);
        //compute counts of ranks
        for (int i = 0; i <n; i++){
            cnt[c[pn[i]]] += 1;
        }
        //prefix sum
        for (int i=1; i<cat+1; i++){
            cnt[i] += cnt[i-1];
        }
        //assign prefix sums in reverse order of second half order
        //each index in counts is treated as last index of substrings with a specific class (first half)
        for (int i=n-1; i>=0; i--){
            p[--cnt[c[pn[i]]]] = pn[i];
        }
        //compute new classes/ranks of new substrings of length 2^(k+1)
        //assign in order of p (lexicographical ordering of these substrings)
        //compare adjacent substrings by comparing their first and second halves
        //if they are different, incerement number of classes
        cn[p[0]] = 0;
        cat = 0;
        for (int i = 1; i <n; i++){
            if (c[p[i]] != c[p[i-1]] || c[(p[i]+(1<<k))%n] != c[(p[i-1]+(1<<k))%n]){
                cat++;
            }
            cn[p[i]] = cat;
        }
        c.swap(cn);


    }
    string result;
    //each index in p refers to a rotation of the input string by p
    //extract last character of that rotation
    for (int i = 0; i <n; i++){
        result += s[(p[i] + n -1) % n];
    }
    return result;
}




int main(){
    string s;
    cin >> s;
    cout << burrowsWheelerTransform(s);
}