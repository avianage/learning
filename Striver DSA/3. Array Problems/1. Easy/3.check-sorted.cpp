#include<iostream>
#include<vector>

using namespace std;

bool checkSorted(vector<int> &arr){
    int n = arr.size();
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            if (arr[j] < arr[i]) 
                return false;
        }
    }

    return true;
}


bool checkSortedOptimised(vector<int> &arr){
    bool check = true;
    for (int i = 0; i < arr.size()-1; i++){
        if (arr[i] > arr[i+1]){
            check = false;
            break;
        }
    }
    return check;

}

int main() {
    vector<int> arr = {1,4,6,8,9};
    
    cout<<"Sorted array: "<<checkSorted(arr);
}