# Play Store publish checklist (step by step)

## A. Aage ja lagbe (tomar kaj)
1. Google Play Developer account (one-time $25 fee).
2. AdMob account > Apps > Add app (Android, "Bubble Shooter: Pop Mania", package com.rubel.bubblepop) > Rewarded ad unit banao.
   Pabe: App ID (~) ar Rewarded unit ID (/). Eigulo GitHub secrets-e dao (PRIVATE/GITHUB_SECRETS.txt).
3. AdMob > Privacy & messaging > EEA/UK consent message (GDPR) create + publish koro.

## B. Build (GitHub)
1. Notun GitHub repo, ei folder-er sob file upload (.github, docs soho).
2. Settings > Pages > Deploy from branch > main /docs  =>  privacy policy URL:
   https://USERNAME.github.io/REPO/privacy.html
3. docs/privacy.html-e YOUR_EMAIL_HERE bodlao (tomar email).
4. Settings > Secrets > Actions: PRIVATE/GITHUB_SECRETS.txt-er 6ta secret dao.
5. Actions > "Build APK + signed AAB" > Run workflow.
   Artifacts: debug-apk (phone-e test, test ads) ar release-aab-for-play-store (Play Store-e upload).

## C. Play Console
1. Create app: Game, Free, name "Bubble Shooter: Pop Mania".
2. Store listing: store/STORE_LISTING.md theke copy-paste; icon, feature graphic, screenshots upload.
3. App content: privacy policy URL, Ads = Yes, Target audience 13+, Content rating, Data safety (STORE_LISTING.md-te ache).
4. Release > Testing/Production > Create release > Play App Signing enable > release-aab upload > release notes > Review > Rollout.
5. Notun personal developer account hole Play Console closed testing requirement dekhabe (tester sonkha ar din Console-e dekhabe). Oi shorto pura na hole Production dewa jabe na.

## D. Important
- Real ad ID diye nijer ad kokhono click korba na (account ban hote pare). Test korte debug-apk use koro.
- Notun update dite hole app/build.gradle-e versionCode +1 koro.
- Target API 36 set kora ache (Play requirement).
