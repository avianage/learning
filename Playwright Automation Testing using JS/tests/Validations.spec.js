import {test, expect} from "@playwright/test"

test.only('More Validations', async ({page}) => {
    await page.goto("https://rahulshettyacademy.com/AutomationPractice");
    // await page.goto("https://google.com")

    // Use to go back to the previous page
    // await page.goBack();

    // Use to navigate to forward page
    // await page.goForward();

    // Handling JS Dialogs
    page.on('dialog', dialog => dialog.accept()); // For accepting
    // page.on('dialog', dialog => dialog.dismiss()); // For rejecting
    
    await page.locator("#confirmbtn").click();
    
    // use .hover() to hover 
    await page.locator("#mousehover").hover();

    const framesPage = page.frameLocator("#courses-iframe")

    await framesPage.locator("li a[href*='lifetime-access']:visible").click();
    const textCheck = await  framesPage.locator(".text h2").textContent()

    console.log(textCheck.split(" ")[1]);

})
