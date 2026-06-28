#include<iostream>

using namespace std;

// O(n)
void brutePrime(int n){
    int count = 0;
    for (int i = 1; i <=n; i++){
        if(n % i == 0){
            count++;
        }
    }

    if (count == 2){
        cout<<"It is a Prime Number";
    } else {
        cout<<"Not a Prime Number";
    }
}

// O(sqrt(n))
void optimalPrime(int n){
    int count = 0;
    for (int i = 1; i*i <=n; i++){
        if(n % i == 0){
            count++;
            if (n / i != i){
                count++;
            }
        }
    }

    if (count == 2){
        cout<<"It is a Prime Number";
    } else {
        cout<<"Not a Prime Number";
    }
}

int main(){
    int n = 13;
    cout<<"Brute Force: ";
    brutePrime(n);
    cout<<"\nOptimal Prime: ";
    optimalPrime(n);
}