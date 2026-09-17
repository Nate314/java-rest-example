import { test, expect } from '@playwright/test';

test.describe('Swagger UI', () => {
  test('renders the API docs in a real browser', async ({ page }) => {
    await page.goto('/swagger-ui.html');
    await expect(page).toHaveTitle(/Swagger UI/);
    await expect(page.locator('.info .title')).toHaveText(/Java REST API Example/);
    // Both controllers should show up as expandable operation groups.
    await expect(page.getByText('account-controller')).toBeVisible();
    await expect(page.getByText('math-controller')).toBeVisible();
  });

  test('exposes a valid OpenAPI document listing all endpoints', async ({ request }) => {
    const res = await request.get('/v2/api-docs');
    expect(res.status()).toBe(200);
    const doc = await res.json();
    const paths = Object.keys(doc.paths);
    expect(paths).toContain('/math/add');
    expect(paths).toContain('/math/add/{a}/{b}');
    expect(paths).toContain('/auth/login');
  });
});
