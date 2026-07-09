#include<iostream>
#include<vector>
#include<algorithm>

using namespace std;

int findLargestElement(vector<int> &arr) {
    // Sort the Array
    // Find last element
    // TC: O(n**2)
    int n = arr.size();
    for (int i = n-1; i >= 0; i--){
        bool swap = false;
        for (int j = 0; j < i; j++){
            if (arr[j] > arr[j+1]){
                int temp = arr[j+1];
                arr[j+1] = arr[j];
                arr[j] = temp;
                swap = true;
            }
        }
        if (swap == false){
            break;
        }
    }
    // sort(arr.begin(), arr.end());
    return arr[n-1];
}

int findLargestElementOptimised(vector<int> &arr){
    // Set 1 var as max;
    // Compare it to every ele in 1 iteration
    int n = arr.size();
    if (n == 0){
        return -1;
    }

    int max = arr[0];
    for (int i = 0; i <n; i++){
        if (arr[i] > max){
            max = arr[i];
        }
    }

    return max;

}

int main () {
    vector<int> arr = {7,2,5,3,9,6};

    cout<<"Largest Element in Array using Brute Force: "<<findLargestElement(arr);\
    cout<<"\nLargest Element in Array using Optimised Method: "<<findLargestElementOptimised(arr);
}