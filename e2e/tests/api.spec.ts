import { test, expect, APIRequestContext } from '@playwright/test';
import jwt from 'jsonwebtoken';

const JWT_PATTERN = /^[\w-]+\.[\w-]+\.[\w-]+$/;

function uniqueUsername(prefix: string) {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 1e6)}`;
}

async function register(request: APIRequestContext, username: string, password: string) {
  return request.post('/auth/register', { data: { username, password } });
}

async function login(request: APIRequestContext, username: string, password: string) {
  const res = await request.post('/auth/login', { data: { username, password } });
  const raw = await res.text();
  return raw.replace(/^"|"$/g, '');
}

test.describe('Account controller - register', () => {
  test('registers a new user', async ({ request }) => {
    const username = uniqueUsername('reg');
    const res = await register(request, username, 'pw123');
    expect(res.status()).toBe(201);
  });

  test('rejects a duplicate username', async ({ request }) => {
    const username = uniqueUsername('dupe');
    const first = await register(request, username, 'pw123');
    expect(first.status()).toBe(201);

    const second = await register(request, username, 'someOtherPassword');
    expect(second.status()).toBe(409);
  });

  test('rejects a missing username or password', async ({ request }) => {
    const res = await request.post('/auth/register', { data: { username: '', password: '' } });
    expect(res.status()).toBe(400);
  });
});

test.describe('Account controller - login', () => {
  test('logs in a registered user and returns a JWT', async ({ request }) => {
    const username = uniqueUsername('login');
    await register(request, username, 'pw123');

    const res = await request.post('/auth/login', { data: { username, password: 'pw123' } });
    expect(res.status()).toBe(200);
    const token = await res.text();
    expect(token.replace(/^"|"$/g, '')).toMatch(JWT_PATTERN);
  });

  test('rejects the wrong password', async ({ request }) => {
    const username = uniqueUsername('wrongpw');
    await register(request, username, 'correct-password');

    const res = await request.post('/auth/login', {
      data: { username, password: 'incorrect-password' },
      failOnStatusCode: false,
    });
    expect(res.status()).toBe(401);
  });

  test('rejects a username that was never registered', async ({ request }) => {
    const res = await request.post('/auth/login', {
      data: { username: uniqueUsername('ghost'), password: 'whatever' },
      failOnStatusCode: false,
    });
    expect(res.status()).toBe(401);
  });
});

test.describe('Math controller', () => {
  test('GET /math/add/{a}/{b} returns the correct sum with a valid token', async ({ request }) => {
    const username = uniqueUsername('math-get');
    await register(request, username, 'pw123');
    const token = await login(request, username, 'pw123');

    const res = await request.get('/math/add/3/4', {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(res.status()).toBe(200);
    expect(await res.json()).toEqual({ value: 7 });
  });

  test('POST /math/add returns the correct sum with a valid token', async ({ request }) => {
    const username = uniqueUsername('math-post');
    await register(request, username, 'pw123');
    const token = await login(request, username, 'pw123');

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

  test('rejects a validly-signed token for a user that was never registered', async ({ request }) => {
    // Forges a token with the app's own hardcoded HMAC secret ("secret" -
    // see AccountController), for a username that has never been through
    // /auth/register. This specifically exercises the interceptor's DB
    // existence check, independent of signature/expiry validation.
    const forged = jwt.sign(
      { preferred_username: uniqueUsername('never-registered') },
      'secret',
      { algorithm: 'HS256', issuer: 'auth0', expiresIn: '15m' }
    );
    const res = await request.get('/math/add/1/1', {
      headers: { Authorization: `Bearer ${forged}` },
      failOnStatusCode: false,
    });
    expect(res.status()).not.toBe(200);
  });
});
