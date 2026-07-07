#include<iostream>
#include<vector>
#include<algorithm>

using namespace std;

class Solution {
    public:
        vector<int> reverseBrute(vector<int> &arr){
            int n = arr.size();
            
            vector<int> answer(n);

            for (int i = 0 ; i < n; i++){
                answer[i] = arr[n-1-i]; 
            }

            return answer;
        }

        vector<int> reverseBetter(vector<int> &arr){
            int n = arr.size();

            int left = 0;
            int right = n-1;

            while(left < right){
                int temp = arr[left];
                arr[left] = arr[right];
                arr[right] = temp;

                left++;
                right--;
            }

            return arr;
        }

        void reverseStl(vector<int> &arr){
            reverse(arr.begin(), arr.end());
        }
};

int main() {
    vector<int> arr = {1,2,3,4,5};

    Solution obj;

    vector<int> result = obj.reverseBrute(arr);

    cout<<"Reversed Array BruteForce"<<endl;
    for (int num : result){
        cout<<num<<" ";
    }
    cout<<endl;

    Solution obj1;

    vector<int> resultBetter = obj1.reverseBetter(arr);
     cout<<"Reversed Array Better Approach"<<endl;
    for (int num : resultBetter){
        cout<<num<<" ";
    }
    cout<<endl;

    Solution obj2;
    
    obj2.reverseStl(arr);
     cout<<"Reversed Array Using STL"<<endl;
    for(int num: arr){
        cout<<num<<" ";
    }

}