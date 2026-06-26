#include <iostream>

using namespace std;

int main() {
    int n = 5; 

    for (int i = 1; i <= n; i++){
        int j = 1;
        for (j; j < i+1; j++){
            cout<<j<<" ";
        }
        for (int k = 1; k <= n-i;k++){
            cout<<"  ";
        }
        for (int k = 1; k <= n-i;k++){
            cout<<"  ";
        }
        for (int k = i; k>0;k--){
            cout<<k<<" ";
        }
        cout<<endl;
    }
}