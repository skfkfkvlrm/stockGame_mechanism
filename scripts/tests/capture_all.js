const puppeteer = require('puppeteer-core');
const fs = require('fs');
const path = require('path');

const outputDir = 'd:\\samuel\\java\\stockGame_mechanism\\docs\\screenshots';
if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
}

const sleep = (ms) => new Promise((res) => setTimeout(res, ms));

async function run() {
    console.log('[Capture] Launching Chrome...');
    const browser = await puppeteer.launch({
        executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
        headless: 'new',
        defaultViewport: { width: 1440, height: 900, deviceScaleFactor: 1.5 },
        args: ['--no-sandbox', '--disable-setuid-sandbox', '--disable-gpu']
    });

    const page = await browser.newPage();

    // 1. Student Login
    console.log('[1/15] Student Login...');
    await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle2' });
    await sleep(1000);
    await page.screenshot({ path: path.join(outputDir, '01_student_login.png') });

    // 2. Student Register
    console.log('[2/15] Student Register...');
    await page.goto('http://localhost:5173/register', { waitUntil: 'networkidle2' });
    await sleep(1000);
    await page.screenshot({ path: path.join(outputDir, '02_student_register.png') });

    // Perform Student Login
    console.log('Logging in as student (abc)...');
    await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle2' });
    await page.type('input[type="text"]', 'abc');
    await page.type('input[type="password"]', '123');
    await page.click('button[type="submit"]');
    await sleep(2500);

    // 3. Student Dashboard
    console.log('[3/15] Student Dashboard...');
    await page.goto('http://localhost:5173/', { waitUntil: 'networkidle2' });
    await sleep(2500);
    await page.screenshot({ path: path.join(outputDir, '03_student_dashboard.png') });

    // 4. Student Stock Market
    console.log('[4/15] Student Stock Market...');
    await page.goto('http://localhost:5173/stocks', { waitUntil: 'networkidle2' });
    await sleep(2500);
    await page.screenshot({ path: path.join(outputDir, '04_student_stock_market.png') });

    // 5. Student Stock Detail (Click first trade button)
    console.log('[5/15] Student Stock Detail (clicking trade-btn)...');
    try {
        await page.waitForSelector('.trade-btn', { timeout: 3000 });
        await page.click('.trade-btn');
        await sleep(3500);
    } catch(e) {
        console.log('Fallback to direct url /stocks/1: ' + e.message);
        await page.goto('http://localhost:5173/stocks/1', { waitUntil: 'networkidle2' });
        await sleep(3500);
    }
    await page.screenshot({ path: path.join(outputDir, '05_student_stock_detail.png') });

    // 6. Student Ranking
    console.log('[6/15] Student Ranking...');
    await page.goto('http://localhost:5173/ranking', { waitUntil: 'networkidle2' });
    await sleep(2000);
    await page.screenshot({ path: path.join(outputDir, '06_student_ranking.png') });

    // 7. Student News
    console.log('[7/15] Student News...');
    await page.goto('http://localhost:5173/news', { waitUntil: 'networkidle2' });
    await sleep(2500);
    await page.screenshot({ path: path.join(outputDir, '07_student_news.png') });

    // 8. Student Points History
    console.log('[8/15] Student Points History...');
    await page.goto('http://localhost:5173/points', { waitUntil: 'networkidle2' });
    await sleep(2000);
    await page.screenshot({ path: path.join(outputDir, '08_student_point_history.png') });

    // 9. Student Coupon Store
    console.log('[9/15] Student Coupon Store...');
    await page.goto('http://localhost:5173/coupons', { waitUntil: 'networkidle2' });
    await sleep(2000);
    await page.screenshot({ path: path.join(outputDir, '09_student_coupon_store.png') });

    // 10. Student My Coupons
    console.log('[10/15] Student My Coupons...');
    await page.goto('http://localhost:5173/my-coupons', { waitUntil: 'networkidle2' });
    await sleep(2000);
    await page.screenshot({ path: path.join(outputDir, '10_student_my_coupons.png') });

    // Switch to Admin Context
    console.log('Switching to Admin Context...');
    const adminPage = await browser.newPage();
    await adminPage.setViewport({ width: 1440, height: 900, deviceScaleFactor: 1.5 });

    // 11. Admin Login
    console.log('[11/15] Admin Login...');
    await adminPage.goto('http://localhost:5174/login', { waitUntil: 'networkidle2' });
    await sleep(1000);
    await adminPage.screenshot({ path: path.join(outputDir, '11_admin_login.png') });

    // Perform Admin Login
    console.log('Logging in as admin...');
    await adminPage.type('input[type="text"]', 'admin');
    await adminPage.type('input[type="password"]', 'admin1234');
    await adminPage.click('button[type="submit"]');
    await sleep(2500);

    // 12. Admin Dashboard - Students Tab (Default)
    console.log('[12/15] Admin Dashboard (Students Tab)...');
    await adminPage.goto('http://localhost:5174/', { waitUntil: 'networkidle2' });
    await sleep(2500);
    await adminPage.screenshot({ path: path.join(outputDir, '12_admin_dashboard_students.png') });

    // 13. Admin Dashboard - Stocks Tab
    console.log('[13/15] Admin Dashboard (Stocks Tab)...');
    try {
        await adminPage.click('.admin-tabs button:nth-child(2)');
        await sleep(2000);
    } catch(e) {
        console.log('Click stocks tab failed: ' + e.message);
    }
    await adminPage.screenshot({ path: path.join(outputDir, '13_admin_dashboard_stocks.png') });

    // 14. Admin Dashboard - Coupons Tab
    console.log('[14/15] Admin Dashboard (Coupons Tab)...');
    try {
        await adminPage.click('.admin-tabs button:nth-child(3)');
        await sleep(2000);
    } catch(e) {
        console.log('Click coupons tab failed: ' + e.message);
    }
    await adminPage.screenshot({ path: path.join(outputDir, '14_admin_dashboard_coupons.png') });

    // 15. Admin Student Detail (abc)
    console.log('[15/15] Admin Student Detail (abc)...');
    await adminPage.goto('http://localhost:5174/students/abc', { waitUntil: 'networkidle2' });
    await sleep(2500);
    await adminPage.screenshot({ path: path.join(outputDir, '15_admin_student_detail.png') });

    await browser.close();
    console.log('[Capture Complete] All 15 screenshots saved to: ' + outputDir);
}

run().catch((err) => {
    console.error('[Capture Error]', err);
    process.exit(1);
});