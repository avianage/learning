const {test, expect, request} = require('@playwright/test')

const loginPayLoad = {userEmail:"aakash.joshi@gmail.com",userPassword:"Aj@12345678"}

let token;

test.beforeAll( async ()=> {
    const apiContext = await request.newContext();
    const loginResponse = await apiContext.post("	https://rahulshettyacademy.com/api/ecom/auth/login",
        {
            data: loginPayLoad
        }
    )
    expect(loginResponse.ok()).toBeTruthy();

    const loginResponseJson = await loginResponse.json()
    token = loginResponseJson.token
    console.log(token)

})


test('Assignment', async ({page}) => {


    page.addInitScript(value => {
        window.localStorage.setItem('token')
    }, token);
    const email = ""
    const productName = 'Zara Coat 3';

``


    // await page.goto("https://rahulshettyacademy.com/client/#/auth/login");

    // const userEmail = await page.locator("#userEmail");
    // const userPass = await page.locator("#userPassword");

    // await userEmail.fill("aakash.joshi@gmail.com");
    // await userPass.fill("Aj@12345678");

    // await page.locator("#login").click();

    // // Helps wait dynamically till all networks calls/ APIs are loaded
    // await page.waitForLoadState('networkidle'); // Discouraged

    // Alternative
    await page.locator(".card-body b").first().waitFor();

    const cardTitles = await page.locator(".card-body b");

    // console.log(await cardTitles.first().textContent());
    console.log(await cardTitles.allTextContents());

    
})