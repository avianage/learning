import {test, expect} from '@playwright/test'

test('Playwright Special Locators', async ({page}) => {
    await page.goto("https://rahulshettyacademy.com/angularpractice/")
    await page.getByLabel("Check me out if you Love IceCreams!")
    await page.getByLabel("Employed").check();
    await page.getByLabel("Gender").selectOption("Male");

    

})