#include <iostream>

using namespace std;

int main() {
    int n = 5;
    for(int i = 0; i < n; i++){
        for (int j = 0; j < n-i-1; j++){
            cout<<"* ";
        }
        char ch = 'A';
        for (ch; ch < 'A' + i+1;ch++){
            cout<<ch<<" ";
        }
        ch--;
        for (ch;ch >= 'A';ch--){
            cout<<ch<<" ";
        }

        cout<<endl;
    }
}