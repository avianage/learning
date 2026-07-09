// Reverse a number

#include<iostream>

using namespace std;

int main(){
    int n = 2134;
    int output = 0;

    while(n > 0){
        output = output*10+ n % 10;
        n/=10;
    }
    cout<<output<<endl;
}