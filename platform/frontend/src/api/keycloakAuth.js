/**
 * Keycloak Authorization Code + PKCE authentication.
 *
 * Replaces the Resource Owner Password Credentials (ROPC) grant used in Phase 3A.
 *
 * Security properties:
 *   - No client_secret in browser JavaScript or build output.
 *   - PKCE (code_verifier / code_challenge) prevents authorization code interception.
 *   - Tokens are stored in sessionStorage (not localStorage) to limit XSS exposure.
 *   - The Keycloak client must be set to "public" access type (no secret required).
 *   - Silent token refresh uses the Keycloak iframe check-sso endpoint.
 *
 * Required Keycloak client configuration (manual — see deployment steps):
 *   - Access Type: public
 *   - Standard Flow Enabled: ON
 *   - Direct Access Grants Enabled: OFF  (disables ROPC)
 *   - Valid Redirect URIs: http://localhost:3000/auth/callback (dev)
 *                          https://app.yourdomain.com/auth/callback (prod)
 *   - Web Origins: + (or explicit origins)
 */

const KC_URL    = process.env.REACT_APP_KC_URL    || 'http://localhost:8080';
const KC_REALM  = process.env.REACT_APP_KC_REALM  || 'vve-platform';
const KC_CLIENT = process.env.REACT_APP_KC_CLIENT_ID || 'vve-frontend';

const TOKEN_ENDPOINT    = `${KC_URL}/realms/${KC_REALM}/protocol/openid-connect/token`;
const AUTH_ENDPOINT     = `${KC_URL}/realms/${KC_REALM}/protocol/openid-connect/auth`;
const LOGOUT_ENDPOINT   = `${KC_URL}/realms/${KC_REALM}/protocol/openid-connect/logout`;
const REDIRECT_URI      = `${window.location.origin}/auth/callback`;

// ─── PKCE helpers ─────────────────────────────────────────────────────────────

function generateCodeVerifier() {
  const array = new Uint8Array(64);
  crypto.getRandomValues(array);
  return btoa(String.fromCharCode(...array))
    .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
}

async function generateCodeChallenge(verifier) {
  const data = new TextEncoder().encode(verifier);
  const digest = await crypto.subtle.digest('SHA-256', data);
  return btoa(String.fromCharCode(...new Uint8Array(digest)))
    .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
}

function generateState() {
  const array = new Uint8Array(16);
  crypto.getRandomValues(array);
  return Array.from(array, b => b.toString(16).padStart(2, '0')).join('');
}

// ─── Storage (sessionStorage — cleared on tab close) ─────────────────────────

const store = {
  set: (k, v) => sessionStorage.setItem(`kc_${k}`, v),
  get: (k)    => sessionStorage.getItem(`kc_${k}`),
  del: (k)    => sessionStorage.removeItem(`kc_${k}`),
  clear: ()   => ['verifier', 'state', 'access_token', 'refresh_token', 'id_token']
                   .forEach(k => sessionStorage.removeItem(`kc_${k}`)),
};

// ─── Public API ───────────────────────────────────────────────────────────────

/**
 * Redirects the browser to Keycloak's authorization endpoint.
 * Stores PKCE verifier and state in sessionStorage before redirecting.
 */
export async function initiateLogin() {
  const verifier   = generateCodeVerifier();
  const challenge  = await generateCodeChallenge(verifier);
  const state      = generateState();

  store.set('verifier', verifier);
  store.set('state', state);

  const params = new URLSearchParams({
    response_type:         'code',
    client_id:             KC_CLIENT,
    redirect_uri:          REDIRECT_URI,
    scope:                 'openid profile email',
    state,
    code_challenge:        challenge,
    code_challenge_method: 'S256',
  });

  window.location.href = `${AUTH_ENDPOINT}?${params}`;
}

/**
 * Handles the authorization callback — exchanges the code for tokens.
 * Call this from the /auth/callback route component.
 *
 * @returns {{ accessToken, refreshToken, idToken, userInfo }}
 * @throws if state mismatch or token exchange fails
 */
export async function handleCallback(searchParams) {
  const code          = searchParams.get('code');
  const returnedState = searchParams.get('state');
  const error         = searchParams.get('error');

  if (error) throw new Error(`Keycloak error: ${error} — ${searchParams.get('error_description')}`);
  if (!code)  throw new Error('No authorization code in callback');

  const savedState = store.get('state');
  if (!savedState || savedState !== returnedState) {
    store.clear();
    throw new Error('State mismatch — possible CSRF attack');
  }

  const verifier = store.get('verifier');
  store.del('state');
  store.del('verifier');

  const body = new URLSearchParams({
    grant_type:    'authorization_code',
    client_id:     KC_CLIENT,
    redirect_uri:  REDIRECT_URI,
    code,
    code_verifier: verifier,
  });

  const res = await fetch(TOKEN_ENDPOINT, {
    method:  'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(`Token exchange failed: ${err.error_description || res.status}`);
  }

  const tokens = await res.json();
  storeTokens(tokens);
  return extractUserInfo(tokens);
}

/**
 * Refreshes the access token using the stored refresh token.
 * Returns new user info or null if refresh fails (session expired).
 */
export async function refreshTokens() {
  const refreshToken = store.get('refresh_token');
  if (!refreshToken) return null;

  const body = new URLSearchParams({
    grant_type:    'refresh_token',
    client_id:     KC_CLIENT,
    refresh_token: refreshToken,
  });

  const res = await fetch(TOKEN_ENDPOINT, {
    method:  'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
  });

  if (!res.ok) {
    store.clear();
    return null;
  }

  const tokens = await res.json();
  storeTokens(tokens);
  return extractUserInfo(tokens);
}

/**
 * Returns the current access token from sessionStorage.
 */
export function getAccessToken() {
  return store.get('access_token');
}

/**
 * Logs out: clears local session and redirects to Keycloak logout endpoint.
 * Keycloak will invalidate the SSO session and redirect back to the app.
 */
export function logout() {
  const idToken = store.get('id_token');
  store.clear();

  const params = new URLSearchParams({
    client_id:                KC_CLIENT,
    post_logout_redirect_uri: window.location.origin,
  });
  if (idToken) params.set('id_token_hint', idToken);

  window.location.href = `${LOGOUT_ENDPOINT}?${params}`;
}

// ─── Internal helpers ─────────────────────────────────────────────────────────

function storeTokens(tokens) {
  store.set('access_token',  tokens.access_token);
  store.set('refresh_token', tokens.refresh_token);
  if (tokens.id_token) store.set('id_token', tokens.id_token);
}

function extractUserInfo(tokens) {
  const payload = JSON.parse(atob(tokens.access_token.split('.')[1]));
  return {
    accessToken:  tokens.access_token,
    refreshToken: tokens.refresh_token,
    idToken:      tokens.id_token,
    userInfo: {
      userId:    payload.sub,
      companyId: payload.companyId,
      email:     payload.email,
      roles:     payload.realm_access?.roles ?? [],
    },
  };
}
