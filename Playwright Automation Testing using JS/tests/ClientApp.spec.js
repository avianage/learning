const {test, expect} = require('@playwright/test');

// Register and then Log into the account and Print the 1st item visible

test('Assignment', async ({page}) => {
    page.goto("https://rahulshettyacademy.com/client/#/auth/register");

    const firstName = page.locator("#firstName");
    const lastName = page.locator("#lastName");
    const email = page.locator("#userEmail");
    const phone = page.locator("#userMobile");
    const occ = page.locator("select[formcontrolname='occupation']");
    const pass = page.locator("#userPassword");
    const conPass = page.locator("#confirmPassword");
    const login = page.locator("#login");

    await firstName.fill("Aakash");
    await lastName.fill("Joshi");
    await email.fill("aakash.joshi@gmail.com");
    await phone.fill("1234567890");
    await occ.selectOption({label: 'Engineer'});
    await page.getByRole('radio', {name: 'Male', exact: true}).check();
    await pass.fill("Aj@12345678");
    await conPass.fill("Aj@12345678");

    await page.locator("input[formcontrolname='required']").check();
    await login.click();

    await page.goto("https://rahulshettyacademy.com/client/#/auth/login");

    const userEmail = await page.locator("#userEmail");
    const userPass = await page.locator("#userPassword");

    await userEmail.fill("aakash.joshi@gmail.com");
    await userPass.fill("Aj@12345678");

    await page.locator("#login").click();

    // Helps wait dynamically till all networks calls/ APIs are loaded
    await page.waitForLoadState('networkidle'); // Discouraged

    // Alternative
    await page.locator(".card-body b").first().waitFor();

    const cardTitles = await page.locator(".card-body b");

    // console.log(await cardTitles.first().textContent());
    console.log(await cardTitles.allTextContents());

    
})