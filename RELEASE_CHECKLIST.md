# Parallel Timer - Release Checklist

Complete this checklist before releasing a new version of Parallel Timer to Google Play Store.

## 1. Pre-Release Development Tasks

### Code Verification
- [ ] Run all tests: `./gradlew test`
- [ ] Run Android lint checks: `./gradlew lint`
- [ ] Build release APK: `./gradlew assembleRelease`
- [ ] Test on multiple Android devices (physical or emulator):
  - [ ] Android 8.0 (API 26) minimum version device
  - [ ] Android 10-12 device (testing dark mode)
  - [ ] Android 13+ device (testing notification permissions)
  - [ ] Large screen device (tablet)
- [ ] Verify all features work:
  - [ ] Create multiple timers simultaneously
  - [ ] Run multiple timers at once
  - [ ] Pause/resume/reset timers
  - [ ] Delete and undo delete
  - [ ] Create and use custom presets
  - [ ] Verify notifications trigger on timer completion
  - [ ] Test dark mode toggle
  - [ ] Verify language switching (English/Korean)
  - [ ] Test all timer colors display correctly

### Version Bumping
- [ ] Update `versionCode` in `app/build.gradle.kts`
- [ ] Update `versionName` in `app/build.gradle.kts`
- [ ] Add changelog entry to release notes or GitHub releases

### Documentation
- [ ] Update README.md with latest features (if applicable)
- [ ] Verify fastlane metadata files are up to date
- [ ] Ensure all strings.xml entries are properly translated (en and ko)

## 2. Signing Configuration

### Required: Generate or Retrieve Signing Key
- [ ] Ensure you have your release keystore file
  - If new release: Generate a new signing key using Android Studio
  - If existing app: Use the same keystore from previous releases
- [ ] Store keystore file securely (never commit to version control)
- [ ] Document keystore details:
  - Keystore filename: `_____________________`
  - Key alias: `_____________________`
  - Keystore password: Store securely in password manager
  - Key password: Store securely in password manager

### Important Notes on Signing
- **DO NOT** lose your signing key - you cannot update the app without it
- **DO NOT** commit keystore file to git - add to `.gitignore`
- **DO NOT** share keystore password in code or documentation
- Always use the same keystore for app updates (Google Play requires consistency)

### Build Signed Release APK/Bundle
```bash
./gradlew bundleRelease
```
This creates a release AAB (Android App Bundle) for Google Play Store submission.

## 3. Google Play Developer Account Setup

### Developer Account Requirements
- [ ] Google Play Developer account created and verified
- [ ] Developer account in good standing (no policy violations)
- [ ] Billing setup complete (one-time $25 registration fee)

### App Registration on Play Store
- [ ] App created in Google Play Console
- [ ] Application ID matches build.gradle.kts: `com.donghun.paralleltimer`
- [ ] App category selected: "Productivity" (or appropriate category)
- [ ] Content rating questionnaire completed
- [ ] Privacy policy URL added

## 4. Play Store Listing - Text Content

### English Listing (en-US)
File: `fastlane/metadata/android/en-US/`

- [ ] **title.txt** - Max 30 characters
  - Current: "Parallel Timer" (14 chars) ✓

- [ ] **short_description.txt** - Max 80 characters
  - Current: "Run multiple timers simultaneously with custom colors and presets." (66 chars) ✓

- [ ] **full_description.txt** - Max 4000 characters
  - Current content includes:
    - Key features list
    - Permissions explanation
    - Privacy policy
    - Requirements
    - Support information

### Korean Listing (ko-KR)
File: `fastlane/metadata/android/ko-KR/`

- [ ] **title.txt** - Max 30 characters
  - Current: "병렬 타이머" (5 chars in Korean) ✓

- [ ] **short_description.txt** - Max 80 characters
  - Current: "여러 타이머를 동시에 실행하세요. 맞춤 색상과 프리셋 지원." (31 chars in Korean) ✓

- [ ] **full_description.txt** - Max 4000 characters
  - Current content includes Korean translations of all features

### Additional Metadata Files to Prepare (if using fastlane)
- [ ] `changelogs/en-US/default.txt` - Version changelog
- [ ] `changelogs/ko-KR/default.txt` - Korean changelog

## 5. Screenshots Requirements

### Screenshot Specifications
Google Play Store requires at minimum 2 and maximum 8 screenshots per language.

#### Portrait Screenshots (Phone - 1080x1920 px or 9:16 aspect ratio)
Prepare at least 3 screenshots showing:
1. [ ] Main timer list with multiple running timers
2. [ ] Quick start presets section
3. [ ] Custom timer creation with color picker
4. [ ] (Optional) Notification behavior

Tools for creating screenshots:
- Use emulator: `./gradlew emulator`
- Use Android Studio device recording
- Manual screenshot: Hold Power + Volume Down (varies by device)

#### Tablet Screenshots (Optional but recommended - 1920x1200 px or 16:10 aspect ratio)
- [ ] 1-2 tablet screenshots showing layout optimization

#### Screenshot File Locations
- English: `fastlane/metadata/android/en-US/images/phoneScreenshots/` (PNG format)
- Korean: `fastlane/metadata/android/ko-KR/images/phoneScreenshots/` (PNG format)

**Important:** Naming convention - name screenshots 1.png, 2.png, 3.png in desired display order

### Design Tips for Screenshots
- Include app name/branding in first screenshot
- Show key features (multiple timers, colors, presets)
- Use actual app content, not mock-ups
- Keep text minimal - screenshots are visual
- Ensure text is readable (16pt minimum size)
- Test on Google Play Console preview to ensure proper display

## 6. Feature Graphic

### Feature Graphic Specifications
Dimensions: **1024x500 pixels** (required)
Format: PNG or JPEG
File location: `fastlane/metadata/android/en-US/images/featureGraphic.png`

### Design Guidelines
- Display app icon and main features
- Include key selling points text (if readable at small sizes)
- Should be eye-catching and represent app purpose clearly
- No text overlay on app icon area
- Safe zone: 100px margin on all sides for text
- Create versions for both en-US and ko-KR

### What to Include
- App name: "Parallel Timer"
- Visual representation: Multiple timer cards or stopwatch icon
- Key benefit: "Run Multiple Timers Simultaneously"
- App icon/logo

## 7. Privacy Policy

### Privacy Policy Requirements
- [ ] Privacy policy URL created and publicly accessible
- [ ] Privacy policy covers:
  - [ ] What data is collected (none for this app)
  - [ ] How data is used (local storage only)
  - [ ] User rights (data deletion, access)
  - [ ] Contact information for privacy concerns
  - [ ] Policy update date

### Sample Privacy Policy URL
Host on your website or GitHub Pages:
```
Example format:
https://yourdomain.com/privacy-policy
or
https://github.com/yourusername/parallel-timer/blob/main/PRIVACY_POLICY.md
```

### Key Points for Parallel Timer Privacy Policy
- App does NOT collect personal information
- App does NOT require internet connection
- App does NOT share data with third parties
- Timer data stored locally on device
- Users can delete data through standard Android app uninstall

## 8. Permissions Disclosure

Verify that all requested permissions are properly disclosed:

- [ ] **POST_NOTIFICATIONS** (Android 13+)
  - Reason: Send timer completion alerts to user
  - In full_description.txt: ✓

- [ ] **SCHEDULE_EXACT_ALARM**
  - Reason: Trigger alarms at exact timer completion time
  - In full_description.txt: ✓

- [ ] **USE_EXACT_ALARM**
  - Reason: Support exact alarm functionality
  - In full_description.txt: ✓

- [ ] **VIBRATE**
  - Reason: Provide haptic feedback on timer completion
  - In full_description.txt: ✓

All permissions must have business justification in store listing.

## 9. Content Rating

### Complete IARC Content Rating
- [ ] Visit Google Play Console > Content Rating
- [ ] Complete questionnaire about app content
- [ ] Categories to answer:
  - [ ] Does app include ads?
    - Answer: No
  - [ ] Does app collect personal data?
    - Answer: No
  - [ ] Violence, profanity, drugs, alcohol content?
    - Answer: No (all negative)

- [ ] Rating typically results: **Everyone** (3+) / **Everyone** (All Ages)

## 10. Target Audience

- [ ] Target audience: Universal (all ages)
- [ ] Primary use cases documented
- [ ] No mature content present

## 11. Final Checks Before Submission

### Build Verification
- [ ] Release build created and signed properly
- [ ] APK/Bundle file size acceptable (typical: 5-15 MB)
- [ ] Build tools version compatible with minimum API 26

### Testing on Real Devices
- [ ] Install signed APK on test device
- [ ] All features functional in production build
- [ ] No crashes or errors observed
- [ ] Notifications work correctly
- [ ] Dark mode switching works
- [ ] Language switching works (en/ko)
- [ ] Permissions requested correctly on Android 13+

### Store Listing Review
- [ ] All text fields filled correctly
- [ ] No spelling or grammar errors
- [ ] Screenshots display properly
- [ ] Feature graphic has correct dimensions
- [ ] All required fields completed
- [ ] Contact information for support included

### Compliance Check
- [ ] App complies with Google Play policies
  - [ ] No illegal functionality
  - [ ] No malware or suspicious code
  - [ ] Proper use of requested permissions
  - [ ] Privacy policy accessible and accurate
  - [ ] No deceptive claims about functionality
- [ ] Version code is higher than previous release
- [ ] Version name follows semantic versioning

## 12. Manual Steps in Google Play Console

You MUST complete these manually - they cannot be automated:

1. **Sign In to Google Play Console**
   - Go to: https://play.google.com/console/
   - Use your developer account

2. **Select Your App**
   - Click on "Parallel Timer" app

3. **Upload Build**
   - Go to: Testing > Internal Testing > Create Release
   - Or: Release > Production > Create Release
   - Upload the signed AAB file from: `app/build/outputs/bundle/release/app-release.aab`

4. **Update Store Listing** (if changed)
   - Go to: Store Listing
   - Update: Title, Short description, Full description
   - Update: Screenshots, Feature graphic
   - Save changes

5. **Review Content Rating** (if new)
   - Go to: Content Rating
   - Complete questionnaire if not done

6. **Configure Release Notes**
   - Go to: Release > Production
   - Add changelog/release notes
   - Describe new features and fixes

7. **Review App Content**
   - Go to: App Content
   - Confirm: Target audience, Content rating
   - Accept: Google Play policies

8. **Submit for Review**
   - Go to: Release > Production > Review Release
   - Verify all information is correct
   - Click: "Start Rollout" or "Save"
   - Submit for review

## 13. Post-Release

### After Successful Review and Publication
- [ ] Confirm app appears on Google Play Store
- [ ] Test installation from Play Store link
- [ ] Monitor initial user feedback
- [ ] Watch for crash reports in Play Console
- [ ] Check analytics for install trends

### Create Release Notes
- [ ] Tag release in GitHub (if using git)
  ```bash
  git tag -a v1.0.0 -m "Release version 1.0.0"
  git push origin v1.0.0
  ```
- [ ] Update changelog with release date and version

### Announce Release
- [ ] Share release on relevant channels
- [ ] Update app website/landing page (if applicable)
- [ ] Notify beta testers of final release

## Troubleshooting Common Issues

### "Build rejected due to API mismatch"
- Verify `compileSdk = 34` matches targetSdk
- Ensure `minSdk = 26` is not changed

### "Permissions not justified"
- Ensure all permissions in AndroidManifest.xml have clear explanations in store listing
- Review Google Play policy on permission use

### "Screenshots too small"
- Screenshots must be at least 1080px height minimum
- Use recommended: 1080x1920px for phone screenshots

### "App crashes on certain devices"
- Test on multiple API levels (26, 30, 34+)
- Check Android 13+ permissions handling
- Verify dark mode rendering on various devices

### "Release not visible immediately"
- Google Play typically processes within 2-4 hours for updates
- Can take up to 24 hours for full propagation
- Check app in other regions/countries from Play Store

## Release Version History

Use this table to track releases:

| Version | Code | Date Released | Notes |
|---------|------|---|---|
| 1.0.0   | 1    | YYYY-MM-DD    | Initial release |
|         |      |       |       |
|         |      |       |       |

---

**Last Updated:** 2026-01-30
**For Support:** Contact developer through Google Play Store listing
