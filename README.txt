BUILD (easiest, no PC):
1. Create a GitHub repo, upload everything in this folder (keep .github/ folder).
2. Actions tab -> "Build APK" runs automatically (or Run workflow).
3. Download artifact "BubblePop-debug-apk" -> install app-debug.apk.

BUILD (Android Studio): open folder -> Build > Build APK(s).

AdMob TEST IDs (Google official) are set in app/build.gradle:
  App ID   : ca-app-pub-3940256099942544~3347511713
  Rewarded : ca-app-pub-3940256099942544/5224354917
Before publishing: replace both with your real AdMob IDs.
