import { test, expect } from '@playwright/test';

const JWT_PATTERN = /^[\w-]+\.[\w-]+\.[\w-]+$/;

test.describe('Account controller', () => {
  test('login returns a JWT', async ({ request }) => {
    const res = await request.post('/auth/login', {
      data: { username: 'alice', password: 'pw123' },
    });
    expect(res.status()).toBe(200);
    const token = await res.text();
    expect(token.replace(/^"|"$/g, '')).toMatch(JWT_PATTERN);
  });
});

test.describe('Math controller', () => {
  async function login(request: import('@playwright/test').APIRequestContext) {
    const res = await request.post('/auth/login', {
      data: { username: 'alice', password: 'pw123' },
    });
    const raw = await res.text();
    return raw.replace(/^"|"$/g, '');
  }

  test('GET /math/add/{a}/{b} returns the correct sum with a valid token', async ({ request }) => {
    const token = await login(request);
    const res = await request.get('/math/add/3/4', {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(res.status()).toBe(200);
    expect(await res.json()).toEqual({ value: 7 });
  });

  test('POST /math/add returns the correct sum with a valid token', async ({ request }) => {
    const token = await login(request);
    const res = await request.post('/math/add', {
      headers: { Authorization: `Bearer ${token}` },
      data: { a: 10, b: 32 },
    });
    expect(res.status()).toBe(200);
    expect(await res.json()).toEqual({ value: 42 });
  });

  test('rejects requests with no Authorization header', async ({ request }) => {
    const res = await request.get('/math/add/1/1', { failOnStatusCode: false });
    expect(res.status()).not.toBe(200);
  });

  test('rejects requests with a malformed token', async ({ request }) => {
    const res = await request.get('/math/add/1/1', {
      headers: { Authorization: 'Bearer not-a-real-token' },
      failOnStatusCode: false,
    });
    expect(res.status()).not.toBe(200);
  });
});
