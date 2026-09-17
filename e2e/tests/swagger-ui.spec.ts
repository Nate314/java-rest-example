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

  test('shows an Authorize button for attaching a bearer token', async ({ page }) => {
    await page.goto('/swagger-ui.html');
    const authorizeButton = page.getByRole('button', { name: 'Authorize' });
    await expect(authorizeButton).toBeVisible();
    // The lock icon that marks the button (and secured operations).
    await expect(authorizeButton.locator('svg')).toBeVisible();
  });

  test('exposes a valid OpenAPI document listing all endpoints', async ({ request }) => {
    const res = await request.get('/v2/api-docs');
    expect(res.status()).toBe(200);
    const doc = await res.json();
    const paths = Object.keys(doc.paths);
    expect(paths).toContain('/math/add');
    expect(paths).toContain('/math/add/{a}/{b}');
    expect(paths).toContain('/auth/login');
    expect(paths).toContain('/auth/register');
  });

  test('secures the math endpoints but not the public auth endpoints', async ({ request }) => {
    const res = await request.get('/v2/api-docs');
    expect(res.status()).toBe(200);
    const doc = await res.json();

    // A "Bearer" apiKey security definition backs the Authorize button.
    expect(doc.securityDefinitions).toBeTruthy();
    expect(doc.securityDefinitions.Bearer).toMatchObject({
      type: 'apiKey',
      name: 'Authorization',
      in: 'header',
    });

    const mathGet = doc.paths['/math/add/{a}/{b}'].get;
    const mathPost = doc.paths['/math/add'].post;
    expect(mathGet.security).toEqual([{ Bearer: expect.any(Array) }]);
    expect(mathPost.security).toEqual([{ Bearer: expect.any(Array) }]);

    const login = doc.paths['/auth/login'].post;
    const register = doc.paths['/auth/register'].post;
    expect(login.security).toBeUndefined();
    expect(register.security).toBeUndefined();
  });
});
