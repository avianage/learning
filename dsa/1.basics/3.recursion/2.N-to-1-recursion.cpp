#include<iostream>

using namespace std;

void foo(int n){
    if (n < 1){
        return;
    }
    cout<<n<<endl;
    foo(--n);
}

int main() {
    int n = 10;

    foo(n);
}