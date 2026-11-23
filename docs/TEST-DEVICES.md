# Test devices: chosen from real usage (DD-272)

We pick the test devices from what our SDK users really have, not by guessing. This page has
the matrix, the 2 or 3 physical devices to keep, and a 10 minute manual check with a real
finger before every native release.

The usage numbers behind it are client data, so they are not in this public repository: they
are in the internal ticket DD-272. Refresh them every few months, or when a client goes live,
with [`test-devices/device-usage.sql`](test-devices/device-usage.sql) (read only, run by the
team in our analytics project).

## Where the data comes from

- The SDK sends `deepdots_device_model`, `deepdots_os_version` and `deepdots_device_type` only
  through App Analytics (`analytics/FeedbackPayload.kt`). So we only know the devices of
  clients with App Analytics on.
- Clients using the native popups without App Analytics send no device data. For them we only
  know their app's minimum OS version from the stores.

## What real users have (summary, November 2026)

- Most users are on Android; tablets are almost none.
- Android 16 is about half of Android users, and Android 17 already shows up. Nobody below
  Android 10.
- iOS 26 and iOS 27 together are about 90% of iOS users; iOS 18 is most of the rest.
- Two out of three Android users are on Chinese OEM skins (vivo, OPPO, Redmi, Honor), not
  Samsung or stock Android. Samsung is the largest single brand, mostly Galaxy A.
- Big iPhones (Pro Max) lead; older iPhones (11, 12, 13) still show up.

## Matrix

### Emulators and simulators (CI and the release validation report)

| Platform | Versions | Why |
|---|---|---|
| Android | API 30 (11), 34 (14), 36 (16), **37 (17)** | 16 is half of users; 17 is already in use. |
| Android | API 26 (8) | Kept only as the floor until we know every client's Android minimum. |
| iOS | **16.4**, 18.x, 26.x, **27.x** | 16.4 is the highest client minimum we know of (the 0.7.5 crash was iOS 16 only); 26 and 27 are most users. |

Changes from the current range (API 26/30/34/36, iOS 16.4/17.5/18.6/26.5): add Android 17
and iOS 27, drop iOS 17.5 (very few users). Screen sizes: one large phone (iPhone Pro Max, a
6.7" Android) and one small (iPhone 13 mini or SE size).

### Physical devices (manual finger check)

Emulators do not catch real touch (the 0.7.2 bug: the backdrop ate the finger's small
movements, so taps on X and Send did nothing) nor OEM skins. Keep 2 or 3 devices:

1. **Mid Samsung phone**: Galaxy A55 or A56 on Android 15 or 16. The most common device family.
2. **Samsung tablet**: Galaxy Tab A or S, any recent Android. Tablets are rare in the data,
   but B2B clients use them and the iPad keyboard issue came from one. If we can only have 2
   devices, swap it for a **vivo, OPPO or Redmi** phone: their skins are most Android users.
3. **Older iPhone**: iPhone 11 or 12, kept on iOS 18 (do not update it). Older iPhones cannot
   go back to iOS 16, so iOS 16.4 stays on the simulator.

## Manual finger check (about 10 minutes, before each native release)

On every physical device above, with the release candidate in the NORDLY test app
(`test-apps/`, DEV, `-PsdkVersion=local` or the candidate version). Use your finger, not a
stylus, not adb or Maestro taps. Write the result in the release pull request (device, OS,
pass or what failed).

1. Launch the app, go to the screen with a popup. The popup opens without a grey flash or
   empty panel.
2. **X closes on the first tap**, also with a slightly sloppy tap (finger moves a bit).
3. Open it again. Tap the backdrop: nothing breaks (it closes or stays, as configured).
4. Answer a rating: one tap selects, auto advance works, no double answer.
5. Choice question: tap two options quickly. The one shown is the one sent.
6. Open text: tap the field, the keyboard opens and **does not cover Send or Next**. Type,
   then **Send works on the first tap** with the keyboard open.
7. With the keyboard open, tap X: the popup closes and the keyboard goes away.
8. Scroll inside a long survey with the finger: it scrolls, it does not select an option.
9. Back: the Android back button or gesture closes the popup (not the app); on iOS a swipe
   from the left edge does not leave a half closed popup.
10. Rotate once and switch the system to dark mode: the popup stays readable and usable.
11. Send the app to the background with the popup open and come back: still usable or cleanly
    closed, never frozen.
12. The thank you screen shows and closes.

Anything that fails blocks the release, same as a red gate (see [VERSIONING.md](VERSIONING.md)).
