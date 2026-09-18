import { test, expect, APIRequestContext } from '@playwright/test';
import jwt from 'jsonwebtoken';

// Must match the JWT_SECRET the app was started with (docker-compose dev default).
const JWT_SECRET = process.env.JWT_SECRET || 'dev-only-jwt-secret-change-me-0123456789';
const ISSUER = 'java-rest-example';
const PW = 'pw12345678';
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
    const res = await register(request, uniqueUsername('reg'), PW);
    expect(res.status()).toBe(201);
  });

  test('rejects a duplicate username', async ({ request }) => {
    const username = uniqueUsername('dupe');
    const first = await register(request, username, PW);
    expect(first.status()).toBe(201);

    const second = await register(request, username, 'someOtherPassword1');
    expect(second.status()).toBe(409);
  });

  test('rejects a missing username or password', async ({ request }) => {
    const res = await request.post('/auth/register', { data: { username: '', password: '' } });
    expect(res.status()).toBe(400);
  });

  test('rejects a too-short password', async ({ request }) => {
    const res = await request.post('/auth/register', { data: { username: uniqueUsername('short'), password: 'abc' } });
    expect(res.status()).toBe(400);
  });

  test('rejects a password longer than 72 bytes', async ({ request }) => {
    const res = await request.post('/auth/register', {
      data: { username: uniqueUsername('long'), password: 'a'.repeat(73) },
    });
    expect(res.status()).toBe(400);
  });

  test('rejects an over-long or malformed username', async ({ request }) => {
    for (const username of ['a'.repeat(65), 'ab', "bob'; drop table users;--", 'has space']) {
      const res = await request.post('/auth/register', { data: { username, password: PW } });
      expect(res.status(), username).toBe(400);
    }
  });

  test('rejects null fields and malformed JSON with 400, not 500', async ({ request }) => {
    const nulls = await request.post('/auth/register', { data: { username: null, password: null } });
    expect(nulls.status()).toBe(400);
    const bad = await request.post('/auth/register', { data: '{not json' });
    expect(bad.status()).toBe(400);
  });
});

test.describe('Account controller - login', () => {
  test('logs in a registered user and returns a JWT', async ({ request }) => {
    const username = uniqueUsername('login');
    await register(request, username, PW);

    const res = await request.post('/auth/login', { data: { username, password: PW } });
    expect(res.status()).toBe(200);
    const token = await res.text();
    expect(token.replace(/^"|"$/g, '')).toMatch(JWT_PATTERN);
  });

  test('issues a token valid for exactly 15 minutes, with security headers', async ({ request }) => {
    const username = uniqueUsername('ttl');
    await register(request, username, PW);
    const res = await request.post('/auth/login', { data: { username, password: PW } });
    expect(res.status()).toBe(200);
    expect(res.headers()['cache-control']).toBe('no-store');
    expect(res.headers()['x-content-type-options']).toBe('nosniff');
    expect(res.headers()['x-frame-options']).toBe('DENY');
    expect(res.headers()['server']).toBeUndefined();
    const claims = jwt.decode((await res.text()).replace(/^"|"$/g, '')) as { iat: number; exp: number };
    expect(claims.exp - claims.iat).toBe(900);
  });

  test('rejects the wrong password', async ({ request }) => {
    const username = uniqueUsername('wrongpw');
    await register(request, username, 'correct-password-1');

    const res = await request.post('/auth/login', {
      data: { username, password: 'incorrect-password-1' },
      failOnStatusCode: false,
    });
    expect(res.status()).toBe(401);
    expect(await res.json()).toEqual({ message: 'Invalid username or password' });
  });

  test('rejects a username that was never registered with the same response', async ({ request }) => {
    const res = await request.post('/auth/login', {
      data: { username: uniqueUsername('ghost'), password: 'whatever-123' },
      failOnStatusCode: false,
    });
    expect(res.status()).toBe(401);
    expect(await res.json()).toEqual({ message: 'Invalid username or password' });
  });

  test('throttles repeated failed logins for the same user with 429', async ({ request }) => {
    const username = uniqueUsername('brute');
    await register(request, username, PW);
    const statuses: number[] = [];
    for (let i = 0; i < 7; i++) {
      const res = await request.post('/auth/login', {
        data: { username, password: 'wrong-password-1' },
        failOnStatusCode: false,
      });
      statuses.push(res.status());
    }
    expect(statuses.slice(0, 5)).toEqual([401, 401, 401, 401, 401]);
    expect(statuses[6]).toBe(429);
    // Even the right password is refused while blocked.
    const ok = await request.post('/auth/login', { data: { username, password: PW }, failOnStatusCode: false });
    expect(ok.status()).toBe(429);
  });
});

test.describe('Math controller', () => {
  test('GET /math/add/{a}/{b} returns the correct sum with a valid token', async ({ request }) => {
    const username = uniqueUsername('math-get');
    await register(request, username, PW);
    const token = await login(request, username, PW);

    const res = await request.get('/math/add/3/4', {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(res.status()).toBe(200);
    expect(await res.json()).toEqual({ value: 7 });
  });

  test('POST /math/add returns the correct sum with a valid token', async ({ request }) => {
    const username = uniqueUsername('math-post');
    await register(request, username, PW);
    const token = await login(request, username, PW);

    const res = await request.post('/math/add', {
      headers: { Authorization: `Bearer ${token}` },
      data: { a: 10, b: 32 },
    });
    expect(res.status()).toBe(200);
    expect(await res.json()).toEqual({ value: 42 });
  });

  test('returns 400 (not 500) for non-numeric operands', async ({ request }) => {
    const username = uniqueUsername('math-bad');
    await register(request, username, PW);
    const token = await login(request, username, PW);
    const res = await request.get('/math/add/x/1', {
      headers: { Authorization: `Bearer ${token}` },
      failOnStatusCode: false,
    });
    expect(res.status()).toBe(400);
  });

  test('rejects requests with no Authorization header', async ({ request }) => {
    const res = await request.get('/math/add/1/1', { failOnStatusCode: false });
    expect(res.status()).toBe(401);
    expect(await res.json()).toEqual({ message: 'Unauthorized' });
  });

  test('rejects requests with a malformed token', async ({ request }) => {
    const res = await request.get('/math/add/1/1', {
      headers: { Authorization: 'Bearer not-a-real-token' },
      failOnStatusCode: false,
    });
    expect(res.status()).toBe(401);
  });

  test('rejects a bare, empty or non-Bearer Authorization header', async ({ request }) => {
    for (const header of ['Bearer', 'Bearer ', 'Basic abc', 'token-only']) {
      const res = await request.get('/math/add/1/1', {
        headers: { Authorization: header },
        failOnStatusCode: false,
      });
      expect(res.status(), `header: ${header}`).toBe(401);
    }
  });

  test('does not treat a math path containing /auth/login as a public route', async ({ request }) => {
    const res = await request.get('/math/add/auth/login', { failOnStatusCode: false });
    expect(res.status()).toBe(401);
  });

  test('rejects a validly-signed token for a user that was never registered', async ({ request }) => {
    // Signed with the app's real secret for a username that never went through
    // /auth/register: exercises the interceptor's DB existence check,
    // independent of signature/expiry validation.
    const forged = jwt.sign({ preferred_username: uniqueUsername('never-registered') }, JWT_SECRET, {
      algorithm: 'HS256',
      issuer: ISSUER,
      expiresIn: '15m',
    });
    const res = await request.get('/math/add/1/1', {
      headers: { Authorization: `Bearer ${forged}` },
      failOnStatusCode: false,
    });
    expect(res.status()).toBe(401);
  });

  test('rejects a token signed with the old hardcoded secret', async ({ request }) => {
    const forged = jwt.sign({ preferred_username: 'admin' }, 'secret', {
      algorithm: 'HS256',
      issuer: ISSUER,
      expiresIn: '15m',
    });
    const res = await request.get('/math/add/1/1', {
      headers: { Authorization: `Bearer ${forged}` },
      failOnStatusCode: false,
    });
    expect(res.status()).toBe(401);
  });

  test('rejects an unsigned (alg=none) token', async ({ request }) => {
    const b64 = (o: object) => Buffer.from(JSON.stringify(o)).toString('base64url');
    const now = Math.floor(Date.now() / 1000);
    const unsigned = `${b64({ alg: 'none', typ: 'JWT' })}.${b64({
      preferred_username: 'admin',
      iss: ISSUER,
      iat: now,
      exp: now + 900,
    })}.`;
    const res = await request.get('/math/add/1/1', {
      headers: { Authorization: `Bearer ${unsigned}` },
      failOnStatusCode: false,
    });
    expect(res.status()).toBe(401);
  });

  test('rejects an expired token', async ({ request }) => {
    const username = uniqueUsername('expired');
    await register(request, username, PW);
    const now = Math.floor(Date.now() / 1000);
    const expired = jwt.sign({ preferred_username: username, iat: now - 3600, exp: now - 60 }, JWT_SECRET, {
      algorithm: 'HS256',
      issuer: ISSUER,
    });
    const res = await request.get('/math/add/1/1', {
      headers: { Authorization: `Bearer ${expired}` },
      failOnStatusCode: false,
    });
    expect(res.status()).toBe(401);
  });
});
