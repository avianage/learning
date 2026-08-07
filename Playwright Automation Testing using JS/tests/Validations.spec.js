import {test, expect} from "@playwright/test"

test.only('More Validations', async ({page}) => {
    await page.goto("https://eventhub.rahulshettyacademy.com");
    await page.goto("https://google.com")

    // Use to go back to the previous page
    await page.goBack();

    // Use to navigate to forward page
    await page.goForward();

    // Handling JS Dialogs
    page.on('dialog', dialog => dialog.accept()); // For accepting
    // page.on('dialog', dialog => dialog.dismiss()); // For rejecting
    

    // use .hover() to hover 
    


})