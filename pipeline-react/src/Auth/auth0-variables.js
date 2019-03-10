export const AUTH_CONFIG = {
  domain: 'in-a-pinch.auth0.com',
  clientId: process.env.REACT_APP_AUTH0_CLIENTID,
  callbackUrl: `${process.env.PUBLIC_URL || 'http://localhost:3000'}/callback`
}
