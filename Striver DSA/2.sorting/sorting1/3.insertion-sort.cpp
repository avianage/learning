#include<iostream>
#include<vector>

using namespace std;

void insertionSort(vector<int> &nums){
    int n = nums.size();

    for(int i = 0; i<n;i++){
        int key = nums[i];
        int j = i-1;

        while (j>=0 && nums[j] > key){
            nums[j+1] = nums[j];
            j--;
        }

        nums[j+1] = key;
    }
}

int main() {
    vector<int> nums = {13, 46, 24, 52, 20, 9};

    insertionSort(nums);

    cout << "After Using Insertion Sort: " << endl;
    for (int num : nums) {
        cout << num << " ";
    }
    cout << endl;
}