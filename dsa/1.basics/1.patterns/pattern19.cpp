#include<iostream>

using namespace std;

int main(){
    int n = 5;
    for(int i =0; i < n; i++){
        for(int j = n; j>i;j--){
            cout<<"* ";
        }
        for(int k = 1; k < i+1; k++){
            cout<<"  ";
        }
        for(int k = 1; k < i+1; k++){
            cout<<"  ";
        }
        for(int m = n; m>i;m--){
            cout<<"* ";
        }

        cout<<endl;
    }
    for(int i =0; i < n; i++){ 
        for(int k = 0; k < i+1; k++){
            cout<<"* ";
        }
        for(int j = n-1; j>i;j--){
            cout<<"  ";
        }
        for(int m = n-1; m>i;m--){
            cout<<"  ";
        }
        for(int k = 0; k < i+1; k++){
            cout<<"* ";
        }
        cout<<endl;
    }

}