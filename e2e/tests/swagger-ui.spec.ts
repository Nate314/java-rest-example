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

  test('Authorize flow: register, login, paste token, Try it out returns 200', async ({ page, request }) => {
    const username = `swagger-${Date.now()}`;
    const password = 'pw12345678';
    await request.post('/auth/register', { data: { username, password } });
    const login = await request.post('/auth/login', { data: { username, password } });
    const token = (await login.text()).replace(/^"|"$/g, '');

    await page.goto('/swagger-ui.html');
    await page.getByRole('button', { name: 'Authorize' }).click();
    // HTTP bearer scheme: only the raw token is needed, Swagger UI adds "Bearer ".
    await page.getByRole('textbox', { name: 'auth-bearer-value' }).fill(token);
    await page.getByRole('button', { name: 'Apply credentials' }).click();
    await page.getByRole('button', { name: 'Close' }).click();

    await page.locator('.opblock-get .opblock-summary-control').first().click();
    await page.getByRole('button', { name: 'Try it out' }).click();
    await page.locator('input[placeholder="a"]').fill('2');
    await page.locator('input[placeholder="b"]').fill('5');
    await page.getByRole('button', { name: 'Execute' }).click();
    await expect(page.locator('.live-responses-table tbody .response-col_status').first()).toHaveText(/200/);
    await expect(page.locator('.live-responses-table .microlight').first()).toContainText('"value": 7');
  });

  test('exposes a valid OpenAPI document listing all endpoints', async ({ request }) => {
    const res = await request.get('/v3/api-docs');
    expect(res.status()).toBe(200);
    const doc = await res.json();
    const paths = Object.keys(doc.paths);
    expect(paths).toContain('/math/add');
    expect(paths).toContain('/math/add/{a}/{b}');
    expect(paths).toContain('/auth/login');
    expect(paths).toContain('/auth/register');
  });

  test('secures the math endpoints but not the public auth endpoints', async ({ request }) => {
    const res = await request.get('/v3/api-docs');
    expect(res.status()).toBe(200);
    const doc = await res.json();

    // An HTTP bearer scheme named "Bearer" backs the Authorize button.
    expect(doc.components.securitySchemes.Bearer).toMatchObject({
      type: 'http',
      scheme: 'bearer',
      bearerFormat: 'JWT',
    });

    const mathGet = doc.paths['/math/add/{a}/{b}'].get;
    const mathPost = doc.paths['/math/add'].post;
    expect(mathGet.security).toEqual([{ Bearer: [] }]);
    expect(mathPost.security).toEqual([{ Bearer: [] }]);

    const login = doc.paths['/auth/login'].post;
    const register = doc.paths['/auth/register'].post;
    expect(login.security).toBeUndefined();
    expect(register.security).toBeUndefined();
  });
});
