# WebToAPK Pro - Setup (shob free)

Page ar API dutoi Render-er ekta server theke chole. Alada hosting lage na.

## Folder
- `.github/workflows/build.yml` -> GitHub Actions (asol APK build)
- `android/`                    -> Android WebView template
- `scripts/prepare.py`          -> config theke app banay
- `server/`                     -> Render-e deploy hobe
- `server/public/index.html`    -> builder page (server nijei dekhay)

## Step 1: GitHub repo
New repository -> **Public** -> name: webtoapk -> shob file upload koro
(`.github` folder ta jeno upload hoy)

## Step 2: Token
Settings -> Developer settings -> Fine-grained tokens -> Generate
- Only ei repo
- Actions: Read and write, Contents: Read-only

## Step 3: Render (Free)
New -> Web Service -> repo select
- Root Directory: `server`
- Build: `npm install`   Start: `npm start`   Plan: Free
- Env variables: GITHUB_TOKEN, GITHUB_REPO (username/webtoapk)

Deploy hole je URL pabe (https://xxxx.onrender.com) oitai tomar app. Oi link share koro.

## Optional
- Keep-alive: uptimerobot.com diye https://xxxx.onrender.com/health ping koro (5 min por por)
- Fixed signing key: Repo Secrets -> KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS
