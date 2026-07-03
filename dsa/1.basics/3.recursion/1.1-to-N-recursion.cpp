#include <iostream>

using namespace std;

void foo(int count, int n){
    if (count == n+1){
        return;
    }
    cout<<count<<endl;
    foo(++count,n);
}



int main(){

    int n = 10;
    int count = 1;
    foo(count,n);
}