const {test, expect} = require('@playwright/test');

test('Browser Playwright Test', async ({browser} /* Necessary to be in {}*/) => {
    // playwright code
    // step1 - open browser
    // step2 - enter u/p
    // step3 - click
    // JS is asynchronous

    // chrome - plugins/ cookies
    const context = await browser.newContext();
    // Creates a new instance of a browser

    const page = await context.newPage();
    // Opened a Page

    await page.goto("https://avianage.in");
    // Redirecting to the link

    console.log(await page.title());

});

// Alternative

test('Page Playwright Simplified', async ({page}) => {
    await page.goto("https://google.com");
    
    // get title - assertion
    console.log(await page.title());
    await expect(page).toHaveTitle("Google")

    



});

// Use test.only() for running only this test

test('Login Playwright Test', async ({page}) => {
    await page.goto("https://rahulshettyacademy.com/loginpagePractise/");
    const userName = page.locator('#username');
    const signIn = page.locator("#signInBtn");
    const cardTitles = page.locator(".card-body a")
    console.log(await page.title());

    // CSS (preferred) or Xpath
    // ID: tagname#id (or) #id
    // Class: tagname.class (or) .class
    // Attribute: [attribute='value']

    // Can use type (depreciated) or fill
    await userName.fill("rahulshetty");
    await page.locator("[type='password']").fill("Learning@830$3mK2");
    await signIn.click();

    // Need to explicitly wait in selenium using Webdriverwait
    // until this locator shows up on page.
    // One reason Playwright is better
    console.log(await page.locator("[style*='block']").textContent());

    await expect(page.locator("[style*='block']")).toContainText('Incorrect');


    await userName.fill("");
    await userName.fill("rahulshettyacademy");
    await signIn.click();

    console.log(await cardTitles.first().textContent());
    console.log(await cardTitles.nth(1).textContent());
    // If we comment all two lines above, then allTitles will give out 
    // an empty list as output as it isnt checking if the element exists 
    // or not as a list can have 0 elements or 100 elements.
    // So, output is empty list.

    const allTitles = await cardTitles.allTextContents();
    console.log(allTitles);
});
