#include<iostream>

using namespace std;

int main(){
    int n = 5;
    int flip = 1;
    for (int i = 0; i < n; i++){
        if (i % 2 == 0){
            flip = 1;
        } else {
            flip = 0;
        }
        for (int j = 0; j < i+1; j++){
            cout<<flip<<" ";
            flip = 1 - flip;   
        }   
        cout<<endl;
    }
}