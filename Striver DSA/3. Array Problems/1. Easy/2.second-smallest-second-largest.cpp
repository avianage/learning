#include<iostream>
#include<vector>
#include<algorithm>
#include<climits>

using namespace std;

vector<int> findSecondSmallSecondLarge(vector<int> &arr){
    int n = arr.size();

    if (n < 2){
        return {-1};
    }

    sort(arr.begin(), arr.end());

    return {arr[1], arr[n-2]};
}

vector<int> findSecondSmallSecondLargeBetter(vector<int> &arr){
    vector<int> result;
    
    int n = arr.size();

    if (n < 2){
        return {-1};
    }

    int smallest = INT_MAX;
    int largest = INT_MIN;
    for (int i = 0; i < n; i++){
        smallest = min(smallest, arr[i]);
        largest = max(largest, arr[i]);
    }

    int second_small = INT_MAX;
    int second_largest = INT_MIN;

    for (int i = 0; i <n;i++){
        if (arr[i] < second_small && arr[i] != smallest){
            second_small = arr[i];
        }
        if (arr[i] > second_largest && arr[i] != largest){
            second_largest = arr[i];
        }
    }

    return {second_small, second_largest};
}

vector<int> findSecondSmallSecondLargeOptimised(vector<int> &arr){
    int n = arr.size();
    if (n < 2){
        return {-1};
    }

    int small = INT_MAX;
    int second_small = INT_MAX;
    int large = INT_MIN; 
    int second_large = INT_MIN;

    for (int i = 0; i < n; i++){
        if (arr[i] < small){
            second_small = small;
            small = arr[i];
        } else if (arr[i] < second_small && arr[i] != small){
            second_small = arr[i];
        }  

        if (arr[i] > large){
            second_large = large;
            large = arr[i];
        } else if (arr[i] > second_large && arr[i] != large){
            second_large = arr[i];
        }
    }

    return {second_small, second_large};
}

int main(){
    vector<int> arr = {6,2,5,8,3,9};

    vector<int> output = findSecondSmallSecondLarge(arr);
    if (output.size() == 1){
        cout<<"Second Smallest or Second Largest Doesnt Exist";
    } else {    
        cout<<"Second Smallest Element is: "<<output[0]<<endl;
        cout<<"Second Largest Element is: "<<output[1]<<endl;
    }

    vector<int>outputBetter = findSecondSmallSecondLargeBetter(arr);
    if (outputBetter.size() == 1){
        cout<<"Second Smallest or Second Largest Doesnt Exist";
    } else {    
        cout<<"Second Smallest Element by Better method is: "<<outputBetter[0]<<endl;
        cout<<"Second Largest Element by Better method is: "<<outputBetter[1]<<endl;
    }

    vector<int>outputOptimised = findSecondSmallSecondLargeOptimised(arr);
    if (outputOptimised.size() == 1){
        cout<<"Second Smallest or Second Largest Doesnt Exist";
    } else {    
        cout<<"Second Smallest Element by Optimised method is: "<<outputOptimised[0]<<endl;
        cout<<"Second Largest Element by Optimised method is: "<<outputOptimised[1]<<endl;
    }
}