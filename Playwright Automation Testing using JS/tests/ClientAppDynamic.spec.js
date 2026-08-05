const { test, expect } = require("@playwright/test");

test('Client App Login', async ({ page }) => {
    const productName = "ZARA COAT 3";
    const email = "aakash.joshi@gmail.com"
    const cardNumber = "4542 9931 9292 2293";

    await page.goto("https://rahulshettyacademy.com/client/#/auth/login");
    const userEmail = await page.locator("#userEmail");
    const userPass = await page.locator("#userPassword");
    const products = page.locator(".card-body");

    await userEmail.fill(email);
    await userPass.fill("Aj@12345678");

    await page.locator("#login").click();

    // Wait for cards to load
    await page.locator(".card-body b").first().waitFor();
    const cardTitles = await page.locator(".card-body b").allTextContents();
    console.log(cardTitles);

    // Zara Coat 3
    const count = await products.count();
    for (let i = 0; i < count; ++i) {
        if (await products.nth(i).locator("b").textContent() === productName) {
            // add product to cart
            await products.nth(i).locator("text= Add to Cart").click();
            break;
        }
    }
    await page.locator("[routerlink*='cart']").click();

    // Wait for items to load
    await page.locator("div li").first().waitFor();
    const bool = await page.locator("h3:has-text('Zara Coat 3')").isVisible();
    expect(bool).toBeTruthy();

    await page.locator("text=Checkout").click();

    // assignment to add/validate creditcard details
    expect(await page.locator(".form__cc input.text-validated").inputValue()).toMatch(cardNumber);


    await page.locator("[placeholder*='Country']").pressSequentially(
        "ind", {
        // Better to add delay while pressing number sequentially
        delay: 150
    });

    // Waiting for all options in the dropdown box to open
    const dropdown = await page.locator(".ta-results");
    await dropdown.waitFor();
    const optionsCount = await dropdown.locator("button").count();

    for (let i = 0; i < optionsCount; ++i) {
        const country = await dropdown.locator("button").nth(i).textContent()
        if (country === " India") {
            await dropdown.locator("button").nth(i).click();
            break;
        }
    }

    await expect(page.locator(".user__name [type='text']").first()).toHaveText(email);

    await page.locator(".action__submit").click();

    expect(page.locator(".hero-primary")).toHaveText(" Thankyou for the order. ");
    const orderId = await page.locator(".em-spacer-1 .ng-star-inserted").textContent();

    console.log(orderId);

    await page.locator("button[routerlink*='myorders']").click();
    await page.locator("tbody").waitFor();

    const rows = await page.locator("tbody tr");

    for (let i = 0; i < await rows.count(); ++i){
        const rowOrderId = await rows.nth(i).locator("th").textContent();

        if (orderId.includes(rowOrderId)){
            await rows.nth(i).locator("button").first().click();
            break;
        }
    }

    const orderIdDetails = await page.locator(".col-text").textContent();
    expect(orderId.includes(orderIdDetails)).toBeTruthy();
   

});