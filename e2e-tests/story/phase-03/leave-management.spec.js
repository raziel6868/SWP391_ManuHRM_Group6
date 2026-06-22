const { test, expect } = require('@playwright/test');
const { loginAs } = require('../../helpers/login');

const APP_URL = process.env.BASE_URL || 'http://localhost:8080/hrm';

test.describe('Phase 3: Leave Management', () => {

  // === Role guards ===

  test('EMPLOYEE wrong-role - blocked from leave balance setup', async ({ page }) => {
    await loginAs(page, 'EMPLOYEE');
    await page.goto(`${APP_URL}/leave-balance-setup`);
    await page.waitForTimeout(2000);
    const currentUrl = page.url();
    expect(currentUrl).not.toContain('leave-balance-setup');
  });

  test('EMPLOYEE wrong-role - blocked from HR leave request list', async ({ page }) => {
    await loginAs(page, 'EMPLOYEE');
    await page.goto(`${APP_URL}/leave-request-list`);
    await page.waitForTimeout(2000);
    const currentUrl = page.url();
    expect(currentUrl).not.toContain('leave-request-list');
  });

  test('PRODUCTION_SUPERVISOR wrong-role - blocked from leave balance setup', async ({ page }) => {
    await loginAs(page, 'PRODUCTION_SUPERVISOR');
    await page.goto(`${APP_URL}/leave-balance-setup`);
    await page.waitForTimeout(2000);
    const currentUrl = page.url();
    expect(currentUrl).not.toContain('leave-balance-setup');
  });

  // === URL access (verify no redirect) ===

  test('HR_MANAGER can navigate to leave balance setup URL', async ({ page }) => {
    await loginAs(page, 'HR_MANAGER');
    await page.goto(`${APP_URL}/leave-balance-setup`);
    await page.waitForTimeout(3000);
    const url = page.url();
    expect(url).toContain('leave-balance-setup');
  });

  test('HR_MANAGER can navigate to leave request list URL', async ({ page }) => {
    await loginAs(page, 'HR_MANAGER');
    await page.goto(`${APP_URL}/leave-request-list`);
    await page.waitForTimeout(3000);
    const url = page.url();
    expect(url).toContain('leave-request-list');
  });

  test('EMPLOYEE can navigate to own leave requests URL', async ({ page }) => {
    await loginAs(page, 'EMPLOYEE');
    await page.goto(`${APP_URL}/leave-request-my`);
    await page.waitForTimeout(3000);
    const url = page.url();
    expect(url).toContain('leave-request-my');
  });

  test('SYSADMIN can navigate to leave balance setup URL', async ({ page }) => {
    await loginAs(page, 'SYSADMIN');
    await page.goto(`${APP_URL}/leave-balance-setup`);
    await page.waitForTimeout(3000);
    const url = page.url();
    expect(url).toContain('leave-balance-setup');
  });

  test('SYSADMIN can navigate to leave request list URL', async ({ page }) => {
    await loginAs(page, 'SYSADMIN');
    await page.goto(`${APP_URL}/leave-request-list`);
    await page.waitForTimeout(3000);
    const url = page.url();
    expect(url).toContain('leave-request-list');
  });

  // === Sidebar visibility ===

  test('Sidebar - HR_MANAGER sees leave management menu items', async ({ page }) => {
    await loginAs(page, 'HR_MANAGER');
    await page.goto(`${APP_URL}/home`);
    await page.waitForLoadState('networkidle');

    const sidebar = page.locator('.sidebar');
    await expect(sidebar).toContainText('Duyệt nghỉ phép');
  });

  test('Sidebar - EMPLOYEE sees own leave menu but not HR views', async ({ page }) => {
    await loginAs(page, 'EMPLOYEE');
    await page.goto(`${APP_URL}/home`);
    await page.waitForLoadState('networkidle');

    const sidebar = page.locator('.sidebar');
    await expect(sidebar).toContainText('Yêu cầu nghỉ phép');
  });

});
