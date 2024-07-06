# GAuth Demo

## Auth Flow

1. Android:
  1. Sign in with Google (using `requestServerAuthCode` with the correct `requestScopes`)
  2. Handle result, send `serverAuthCode` to server
2. Server:
  1. Exchange `serverAuthCode` for `access_token`
  2. Use `access_token` to access required Google APIs

## Notes

- Need to setup both Android Client (with your keystore SHA-1), and Web client, and use Web client ID in the app
- Make sure app is signed using the debug key
- Device has to be signed into Google account (especially emulator)
- During early development if app isn't published - signed in account has to be the same one as the API keys owner account
