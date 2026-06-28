// Print all Divisors

#include<iostream>
#include<cmath>
#include<vector>
#include<algorithm>

using namespace std;

void divisorsBrute(int n){
    for(int i =1; i <= n; i++){
        if (n % i == 0){
            cout<< i << " ";
        }
    }
}
//O(sqrt(n))
void divisorOptimal(int n) {
    vector<int> list;
    for(int i = 1; i*i <= n;i++){
        if (n % i == 0){
            list.push_back(i);
            if (n/i != i){
                list.push_back(n/i);
            }
        }
    }
    //O(nlog(n)): n is number of factors
    sort(list.begin(), list.end());

    //O(number of factors)
    for(auto a: list){
        cout<<a<<" ";
    }
}

int main(){
    int n = 36;
    cout<<"Brute Force: "; 
    divisorsBrute(n);
    cout<<"\nOptimal: ";
    divisorOptimal(n);
}