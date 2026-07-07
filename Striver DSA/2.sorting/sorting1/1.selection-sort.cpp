#include<iostream>
#include<vector>

using namespace std;

void selectionSort(vector<int> &arr){
    int n = arr.size();
    for (int i = 0; i < n; i++){
        int minEleIdx = i;
        for(int j = i+1; j < n;j++){
            if (arr[minEleIdx] > arr[j]){
                minEleIdx = j;
            }
        }
        int temp = arr[minEleIdx];
        arr[minEleIdx] = arr[i];
        arr[i] = temp;
    }
}

int main(){
    vector<int> arr = {3,7,1,6,5};

    selectionSort(arr);

    cout<<"\nSorted Array: ";
    for (int num: arr){
        cout<<num<<" ";
    }
}