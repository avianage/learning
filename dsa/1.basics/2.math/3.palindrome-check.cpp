// Check if number is palindrome or not
#include<iostream>

using namespace std;

int main(){
    int n = 2134;
    int temp = n;
    int output = 0;

    while(n > 0){
        output = output*10+ n % 10;
        n/=10;
    }

    if (temp == output){
        cout<<"Number is a Palindrome";
    } else {
        cout<<"Number is not a Palindrome";
    }
}