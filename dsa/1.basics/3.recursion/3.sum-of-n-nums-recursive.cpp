#include <iostream>

using namespace std;

int sum(int num){
    if (num == 5){
        return 5;
    } else {
        return num + sum(++num);
    }
}

int main() {
    int n = 5;

    cout<<sum(1);
}