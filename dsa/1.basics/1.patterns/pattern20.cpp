#include<iostream>

using namespace std;

int main() {
    int n = 5;
    for(int i =0; i < n; i++){
        for(int j = 0; j < i+1; j++){
            cout<<"* ";
        }
        for(int k = n-1;k>i;k--){
            cout<<"  ";
        }
        for(int k = n-1;k>i;k--){
            cout<<"  ";
        }
        for(int j = 0; j < i+1; j++){
            cout<<"* ";
        }
        cout<<endl;
    }
    for(int i =0; i < n; i++){
        for(int k = n-1;k>i;k--){
            cout<<"* ";
        }
        for(int j = 0; j < i+1; j++){
            cout<<"  ";
        }
        for(int j = 0; j < i+1; j++){
            cout<<"  ";
        }
        for(int k = n-1;k>i;k--){
            cout<<"* ";
        }
        cout<<endl;
    }
}