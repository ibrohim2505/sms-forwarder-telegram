# Ilovani O'rnatish Bo'yicha Qo'llanma

## Usul 1: Android Studio orqali (Tavsiya etiladi)

### Kerakli dasturlar:
1. **Android Studio** - https://developer.android.com/studio dan yuklab oling
2. **USB kabel** - telefoningizni kompyuterga ulash uchun

### Qadamlar:

1. **Developer Mode yoqing (Telefonda):**
   - Settings → About Phone → Build Number ga 7 marta bosing
   - Settings → Developer Options → USB Debugging ni yoqing

2. **Android Studio ochish:**
   - Android Studio ni oching
   - "Open" bosing va `xabarchibot` papkasini tanlang
   - Gradle build tugashini kuting (birinchi marta 5-10 daqiqa)

3. **Telefonni ulash:**
   - USB kabel bilan telefonni kompyuterga ulang
   - Telefoningizda "Allow USB Debugging" ga ruxsat bering

4. **Ilovani o'rnatish:**
   - Android Studio tepasida yashil ▶️ (Run) tugmasini bosing
   - Yoki: Run → Run 'app'
   - Ilova avtomatik o'rnatiladi va ishga tushadi

## Usul 2: APK fayl orqali

### Qadamlar:

1. **APK yaratish:**
   - Android Studio da: Build → Build Bundle(s) / APK(s) → Build APK(s)
   - 2-3 daqiqa kutasiz
   - APK tayyor bo'lgach "locate" havolasini bosing
   - APK fayl: `xabarchibot/app/build/outputs/apk/debug/app-debug.apk`

2. **APK ni telefonga o'tkazish:**
   - USB orqali yoki Google Drive/Telegram orqali telefonga yuboring
   - Yoki kompyuterdan ADB bilan: `adb install app-debug.apk`

3. **Telefoningizda o'rnatish:**
   - APK faylni oching
   - "Install from Unknown Sources" ga ruxsat bering
   - "Install" tugmasini bosing

## O'rnatgandan keyin:

1. Ilovani oching
2. "Enable SMS Forwarding" tugmasini yoqing
3. SMS va Notification ruxsatlarini bering
4. Tayyor! Endi kelgan SMS lar telegram botga yuboriladi

## Muammolar:

- **Gradle build xatosi:** Internet ulanishini tekshiring
- **Telefon ko'rinmaydi:** USB Debugging yoqilganini tekshiring
- **Ruxsat berilmaydi:** Settings → Apps → SMS Forwarder → Permissions dan qo'lda bering

## Yordam:
Agar muammo bo'lsa, xato xabarini ko'rsating.
