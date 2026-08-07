import { test, expect } from "@playwright/test"

test('Playwright Special Locators', async ({ page }) => {

    // Complete test timeout
    test.setTimeout(60000)

    // Custom except timeout only for Test Level. Better instead of messing with 
    // the global timeout 
    const slowExpect = expect.configure({timeout: 9000})

    // Test Level action timeout
    page.setTimeout(9000);

    await page.goto("https://rahulshettyacademy.com/angularpractice/")
    await page.getByLabel("Check me out if you Love IceCreams!")
    await page.getByLabel("Employed").check();
    await page.getByLabel("Gender").selectOption("Male");

    await page.getByPlaceholder("Password").fill("abc123");

    await page.getByRole('button', { name: "Submit" }).click();

    await page.getByText("Success! The Form has been submitted successfully!.").isVisible();

    // 5 second default timeout for expect assertions
    // To overwrite this, use timeout
    await expect(await page.getByText("Success! The Form has been submitted successfully!."))
        .toBeVisible({ timeout: 10_000 });

    await page.getByRole("link", { name: "Shop" }).click();
    await slowExpect(page.locator(".my-4").first()).toHaveText("Shop");

    await page.locator("app-card").filter({ hasText: 'Nokia Edge' }).getByRole("button").click();


    // Global -> Test -> Step


})