#include<iostream>

using namespace std;

bool checkPalindrome(string str){
    int n = str.length();

    for(int i = 0 ; i < (n/2); i++){
        if (str[i] != str[n-i-1]){
            return false;
        }
    }
    return true;
}

// While the recursive version uses more memory, algorithmic classifications often 
// prioritize reducing execution time.
bool checkPalindromeOptimal(int i, string s){
    if (i > s.length()/2) return true;
    else if (s[i] != s[s.length()-i-1]) return false;

    else return checkPalindromeOptimal(i+1, s);
}

int main() {
    string s = "abba";

    cout<<checkPalindrome(s)<<endl;
    cout<<checkPalindromeOptimal(0, s)<<endl;

}