const puppeteer = require('puppeteer-core');
const path = require('path');

async function test() {
    const browser = await puppeteer.launch({
        executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
        headless: 'new',
        defaultViewport: { width: 1440, height: 900, deviceScaleFactor: 1.5 },
        args: ['--no-sandbox', '--disable-setuid-sandbox']
    });

    const page = await browser.newPage();
    page.on('console', msg => console.log('[BROWSER CONSOLE]', msg.type(), msg.text()));
    page.on('pageerror', err => console.log('[BROWSER PAGE ERROR]', err.message));

    // Login first
    await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle2' });
    await page.type('input[type="text"]', 'abc');
    await page.type('input[type="password"]', '123');
    await page.click('button[type="submit"]');
    await new Promise(r => setTimeout(r, 2000));

    // Navigate to stock market
    await page.goto('http://localhost:5173/stocks', { waitUntil: 'networkidle2' });
    await new Promise(r => setTimeout(r, 2000));

    // Click trade button
    console.log('Clicking trade button...');
    await page.waitForSelector('.trade-btn');
    await page.click('.trade-btn');
    await new Promise(r => setTimeout(r, 4000));

    await page.screenshot({ path: 'd:\\samuel\\java\\stockGame_mechanism\\docs\\screenshots\\05_student_stock_detail.png' });
    console.log('Current URL after click:', page.url());

    await browser.close();
}

test().catch(e => console.error(e));