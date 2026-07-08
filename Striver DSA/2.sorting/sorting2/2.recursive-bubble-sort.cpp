#include<iostream>

using namespace std;

void bubble_sort_brute(int arr[], int n){
    if (n == 1) return;

    for (int j = 0; j<=n-2;j++){
        if (arr[j] > arr[j+1]){
            int temp = arr[j+1];
            arr[j+1] = arr[j];
            arr[j] = temp;
        }
    }

    bubble_sort_brute(arr, n-1);
}

void bubble_sort_optimised(int arr[], int n){
    if (n == 1) return;

    bool swap = false;
    for (int j = 0; j<=n-2;j++){
        if (arr[j] > arr[j+1]){
            int temp = arr[j+1];
            arr[j+1] = arr[j];
            arr[j] = temp;
            swap = true;
        }
        if (swap == true){
            return;
        }
    }

    bubble_sort_optimised(arr, n-1);
}

int main() {
    int arr[] = {13, 46, 24, 52, 20, 9};
    int n = sizeof(arr) / sizeof(arr[0]);

    cout << "Before Using Bubble Sort: " << endl;
    for (int i = 0; i < n; i++)
        cout << arr[i] << " ";
    cout << endl;

    // Call the recursive Bubble Sort function
    bubble_sort_brute(arr, n);

    cout << "After Using Bubble Sort: " << endl;
    for (int i = 0; i < n; i++)
        cout << arr[i] << " ";
    cout << endl;

    return 0;
}