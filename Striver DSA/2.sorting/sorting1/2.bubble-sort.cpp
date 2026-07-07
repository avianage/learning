#include<iostream>
#include<vector>

using namespace std;

void bubbleBrute(vector<int> &arr){
    int n = arr.size();
    for (int i = n-1;  i >= 0; i--){
        for (int j = 0 ; j <= i-1; j++){
            if(arr[j]>arr[j+1]){
                int temp = arr[j];
                arr[j] = arr[j+1];
                arr[j+1] = temp;
            }
        }
    }
}

void bubbleOptimize(vector<int> &arr){
    int n = arr.size();
    for(int i = n-1; i >=0 ; i--){
        int swap = 0;
        for(int j = 0; j <= i-1;j++){
            if(arr[j]>arr[j+1]){
                int temp = arr[j];
                arr[j] = arr[j+1];
                arr[j+1] = temp;
                swap = 1;
            }
        }
        if (swap == 0){
            break;
        }
    }
}

int main() {
    vector<int> arr = {3,6,5,8,4};

    bubbleBrute(arr);

    cout<<"BubbleSort Brute: ";
    for(int num: arr){
        cout<<num<<" ";
    }

    vector<int> arr2 = {4,7,5,1,9};

    bubbleOptimize(arr2);
    cout<<"BubbleSort Optimised: ";
    for(int num: arr){
        cout<<num<<" ";
    }
}