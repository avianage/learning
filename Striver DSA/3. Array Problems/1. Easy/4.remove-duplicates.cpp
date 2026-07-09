#include<bits/stdc++.h>

using namespace std;

vector<int> removeDuplicate(vector<int> &arr){
    unordered_set<int> set;

    for (auto num: arr){
        set.insert(num);
    }

    vector<int> result;
    int n = set.size();

    int index = 0;
    for (auto num: set){
        result.push_back(num);
    }

    return result;
}

int main() {
    vector<int> arr = {2,2,3,4,5,6,6,7,7,8,9};

    cout<<"Removed Duplicate Elements Brute Force: ";
    vector<int> output = removeDuplicate(arr);
    for(int num: output){
        cout<<num<<" "; 
    }

}