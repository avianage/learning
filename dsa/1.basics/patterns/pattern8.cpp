#include<iostream>

using namespace std;

int main(){
    int n = 5;
    for (int i = 0; i < n; i++){
        for (int j = 0; j < i;j++){
            cout<<"  ";
        }
        for (int k = n; k > i;k--){
            cout<<"* ";
        }
        for (int k = n-1; k > i;k--){
            cout<<"* ";
        }
        cout<<endl;
    }
}