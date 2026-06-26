#include <iostream>

using namespace std;

int main() {
    int n = 5;
    for (int i = n; i > 0; i--){
        for (char ch = 'A'; ch < 'A' + i; ch++){
            cout<<ch<<" ";
        }
        cout<<endl;
    }

}