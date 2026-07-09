#include<iostream>

using namespace std;

int fibOptimal(int n){
    if (n == 0){
        return 0;
    } else if (n == 1){
        return 1;
    } else {
        return fibOptimal(n-1) + fibOptimal(n-2);
    }
}

int main() {
    int n = 5;
    // Brute Force
    if (n == 0){
        cout<<0;
    } else if (n == 1){
        cout<<0<<" "<<1;
    } else {
        int fib[n+1];

        fib[0] = 0;
        fib[1] = 1;

        for (int i = 2; i <= n; i++){
            fib[i] = fib[i-1] + fib[i-2];
        }

        cout << "The Fibonacci Series up to " << n << "th term:" << endl;
        for (int i = 0; i <= n; i++) {
            cout << fib[i] << " ";
        }
        cout<<endl;
    }
    cout<<"Optimal Way"<<endl;
    cout<<fibOptimal(n);
}