#include<iostream>

using namespace std;

// O(min(a,b))
void bruteHcf(int a, int b){
    int gcd = 1;
    for(int i = 1; i <=min(a,b);i++){
        if(a % i == 0 && b % i == 0){
            gcd = i;
        }
    }
    cout<<"GCD: "<<gcd;
}

// Alternative to that is from min(a,b) to 1 -> still O(min(a,b)) for Worst Case
void betterHcf(int a, int b){
    int gcd = min(a,b);
    for(int i = min(a,b); i >= 1;i--){
        if(a % i == 0 && b % i == 0){
            gcd = i;
            break;
        }
    }
    cout<<"GCD: "<<gcd;
}

//Optimal Approach using Euclidean Algorithm
// O(logφ(min(a, b)))
void optimalHcf(int a, int b){
    while(a > 0 && b > 0){
        if (a > b){
            a = a % b;
        } else {
            b = b % a;
        }
    }
    if ( a == 0){
        cout<<"HCF: "<<b;
    } else {
        cout<<"HCF: "<<a;
    }
}

int main(){
    int a = 15;
    int b = 20;

    cout<<"Brute Force HCF: ";
    bruteHcf(a,b);
    cout<<"\nBetter HCF: ";
    betterHcf(a,b);
    cout<<"\nOptimal HCF: ";
    optimalHcf(a,b);
}