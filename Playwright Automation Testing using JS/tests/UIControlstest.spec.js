const {test, expect} = require('@playwright/test');

test('UI Controls Test', async ({page}) => {
    await page.goto("https://rahulshettyacademy.com/loginpagePractise/");
    const userName = page.locator('#username');
    const signIn = page.locator("#signInBtn");
    const dropdown = page.locator("select.form-control");
    dropdown.selectOption("consult");
    
    const documentLink = page.locator("[href*='documents-request']");

    await page.locator(".radiotextsty").last().click();
    await page.locator("#okayBtn").click();

    await expect(page.locator(".radiotextsty").last()).toBeChecked();
    console.log(page.locator(".radiotextsty").last().isChecked());

    await page.locator("#terms").click();
    await expect(page.locator("#terms")).toBeChecked();
    await page.locator("#terms").uncheck();
    await expect(page.locator("#terms").isChecked()).toBeFalsy();


    await expect(documentLink).toHaveAttribute("class", "blinkingText");
    // await page.pause();

})


test('Child Window handle', async ({browser}) => {
    const context = await browser.newContext();
    const page = await context.newPage();
    await page.goto("https://rahulshettyacademy.com/loginpagePractise/");
    const userName = page.locator('#username');
    const documentLink = page.locator("[href*='documents-request']");


    const [newPage] = await Promise.all(
        [
            //listen for new page pending/rejected/fulfilled
            context.waitForEvent('page'), 
            // new page is opened
            documentLink.click() 
        ]
    );

    const text = await newPage.locator(".red").textContent();
    const arrayText = text.split("@");
    const domain = arrayText[1].split(" ")[0];

    console.log(domain);

    await page.locator("#username").fill(domain);

    await page.pause();
    console.log(await page.locator("#username").inputValue());
    // textContent() will only work with values which are attached to the DOM and not
    // added dynamically at runtime.

    // Hence, used inputValue()
})