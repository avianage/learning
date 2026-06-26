#include<iostream>

using namespace std;

int main() {
    int n = 5;

    for(int i =0; i <n; i++){
        for (char ch = 'A'+n-i-1;ch < 'A'+n; ch++){
            cout<<ch<<" ";
        }

        cout<<endl;
    }
}