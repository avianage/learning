#include<iostream>

using namespace std;

int main(){
    int n = 5;
    for (int i = 0; i < n; i++){
        for (int j = n; j > i+1;j--){
            cout<<"  ";
        }
        for (int k = 0; k<i+1;k++){
            cout<<"* ";
        }
        for (int l = 1; l<i+1; l++){
            cout<<"* ";
        }

        cout<<endl;
    }
}