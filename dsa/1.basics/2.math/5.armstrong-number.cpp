// Check if number is armstrong number

#include<iostream>
#include<cmath>
#include<string>

using namespace std;

int main(){
    int num = 1634;
    int sum = 0;
    int temp = num;
    int n = to_string(num).length();
    while(temp > 0){
        int a = temp % 10;
        int power = pow(a,n);
        sum += power;
        temp /= 10;
    }
    if (sum == num){
        cout<<"The number is an armstrong number";
    } else {
        cout<<"The number is not an armstrong number";
    }
}