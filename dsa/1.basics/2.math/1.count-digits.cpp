// Count digits of a number

#include<iostream>
#include <cmath>

using namespace std;

// Brute Force
int count(int n){
    int count = 0;
    while (n > 0){
        count++;
        n /= 10;
    }
    return count;
}

//Optimal
// The logarithmic base 10 of a positive integers gives the number of digits in n. 
// We add 1 to the result to ensure that the count is correct even for numbers that 
// are powers of 10.
int countOptimal(int n){
    return int(log10(n) + 1);
}

int main() {
    int num = 5234;
    cout<<"\nBrute Force: "<<count(num);
    cout<<"\nOptimal Method by taking 'log10 + 1': "<<countOptimal(num)<<endl;;


}