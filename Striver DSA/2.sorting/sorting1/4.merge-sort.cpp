#include<iostream>
#include<vector>
#include<algorithm>

using namespace std;

void merge(vector<int> &arr, int low, int mid, int high){
    
    vector<int> result;
    
    int left = low;
    int right = mid+1;

    while (left <= mid && right <= high){
        if (arr[left] <= arr[right]){
            result.push_back(arr[left]);
            left++;
        } else {
            result.push_back(arr[right]);
            right++;
        }
    }

    while (left <= mid) {
        result.push_back(arr[left]);
        left++;
    }

    while (right <= high){
        result.push_back(arr[right]);
        right++;
    }

    for (int i = low; i <= high;i++){
        arr[i] = result[i-low];
    }

}

void mergeSort(vector<int> &arr, int low, int high){
    
    if (low >= high) return;
    
    int mid = (high + low)/2;

    mergeSort(arr, low, mid);
    mergeSort(arr, mid+1, high);

    merge(arr, low, mid, high);

}

int main(){
    vector<int> arr = {3,6,7,2,9,4};
    
    mergeSort(arr, 0, (arr.size()-1));
    
    for (int num: arr){
        cout<<num<<" ";
    }
}