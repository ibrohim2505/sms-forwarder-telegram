# SMS Forwarder to Telegram

Android ilovasi - uy telefoning zaryad tutmasa va uyda zaryadda turgan bo'lsa, barcha kiruvchi SMS xabarlarni iPhone yoki boshqa qurilmangizga Telegram bot orqali yuboradi.

## 🎯 Xususiyatlar

- ✅ Kiruvchi SMS xabarlarni avtomatik yuborish
- ✅ Telegram bot integratsiyasi
- ✅ Fonda ishlash (telefon zaryadda turganida ham)
- ✅ Telefon qayta yoqilganda avtomatik ishga tushish
- ✅ Oson sozlash interfeysi
- ✅ Xavfsiz konfiguratsiya saqlash

## 📱 Talablar

- Android 7.0 (API 24) yoki yuqori
- Telegram bot tokeni
- Telegram Chat ID

## 🚀 O'rnatish

### 1. Telegram Bot Yaratish

1. Telegram'da [@BotFather](https://t.me/BotFather) botini oching
2. `/newbot` buyrug'ini yuboring
3. Bot nomi va username kiriting
4. BotFather sizga **Bot Token** beradi (masalan: `123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11`)
5. Token'ni nusxalab oling

### 2. Chat ID Olish

1. Yaratgan botingiz bilan chat boshlang (birinchi xabar yuboring)
2. Telegram'da [@userinfobot](https://t.me/userinfobot) botini oching
3. Bot sizga **Chat ID** beradi (masalan: `123456789`)
4. Chat ID'ni nusxalab oling

### 3. Ilovani O'rnatish

1. Android Studio'da proyektni oching
2. Telefon yoki emulyatorda ishga tushiring
3. Ilova ochilganda:
   - **Bot Token**'ni kiriting
   - **Chat ID**'ni kiriting
   - "Save Configuration" tugmasini bosing
4. SMS ruxsatlarini bering (ilova so'raydi)
5. "SMS Forwarding Service" kalitini yoqing

## 📖 Foydalanish

1. Ilova sozlangandan keyin, telefonda konfiguratsiya qilishingiz shart
2. Service yoqilgandan keyin barcha kiruvchi SMS'lar avtomatik yuboriladi
3. Telefon zaryadda turishi mumkin va xabarlar kelaveradi
4. Telefon o'chirib yoqilganda ham service avtomatik davom etadi

## 🔧 Build Qilish

```bash
# Proyektni build qilish
./gradlew build

# Debug APK yaratish
./gradlew assembleDebug

# Release APK yaratish (signing kerak)
./gradlew assembleRelease
```

## 🏗️ Proyekt Tuzilmasi

```
app/
├── src/main/
│   ├── java/com/smsforwarder/app/
│   │   ├── MainActivity.kt              # Asosiy ekran
│   │   ├── receiver/
│   │   │   ├── SmsReceiver.kt          # SMS qabul qiluvchi
│   │   │   └── BootReceiver.kt         # Boot receiver
│   │   ├── service/
│   │   │   └── SmsForwardingService.kt # Fon service
│   │   ├── telegram/
│   │   │   └── TelegramBot.kt          # Telegram API
│   │   └── utils/
│   │       └── PreferenceHelper.kt      # Settings saqlash
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml       # UI layout
│   │   └── values/
│   │       ├── strings.xml
│   │       ├── colors.xml
│   │       └── themes.xml
│   └── AndroidManifest.xml
├── build.gradle.kts
└── proguard-rules.pro
```

## 🔐 Ruxsatlar

Ilova quyidagi ruxsatlarni so'raydi:

- `RECEIVE_SMS` - SMS xabarlarni qabul qilish
- `READ_SMS` - SMS xabarlarni o'qish
- `INTERNET` - Telegram API ga ulanish
- `FOREGROUND_SERVICE` - Fonda ishlash
- `RECEIVE_BOOT_COMPLETED` - Telefon yoqilganda avtomatik ishga tushish
- `POST_NOTIFICATIONS` - Bildirishnomalar ko'rsatish (Android 13+)

## 🛠️ Texnologiyalar

- **Kotlin** - Dasturlash tili
- **Android SDK 24+** - Minimum Android versiya
- **OkHttp** - HTTP client (Telegram API uchun)
- **Coroutines** - Asinxron operatsiyalar
- **ViewBinding** - UI binding
- **Material Components** - UI dizayni

## ⚠️ Muhim Eslatmalar

1. Bot Token va Chat ID maxfiy saqlang
2. Faqat ishonchli qurilmalarda ishlating
3. Telefon zaryadda turishi kerak (zaryad tutmasa)
4. Internet ulanishi bo'lishi shart
5. Telefon qayta yoqilganda service avtomatik yoqiladi

## 🐛 Muammolarni Hal Qilish

### SMS yuborilmayapti
- Bot Token va Chat ID to'g'ri ekanligini tekshiring
- SMS ruxsatlari berilganligini tekshiring
- Service yoqilganligini tekshiring
- Internet ulanishini tekshiring

### Service to'xtab qolmoqda
- Telefon sozlamalarida batareya optimallashtirish o'chirilganligini tekshiring
- Background restrictions o'chirilganligini tekshiring

### Bot javob bermayapti
- Bot Token to'g'ri ekanligini tekshiring
- Bot bilan birinchi xabar yuborganingizni tekshiring
- Chat ID to'g'ri ekanligini tekshiring

## 📄 Litsenziya

Bu proyekt shaxsiy foydalanish uchun yaratilgan.

## 👨‍💻 Muallif

SMS Forwarder - Android ilova, SMS'larni Telegram botga yuboruvchi

## 🤝 Hissa Qo'shish

Pull request'lar qabul qilinadi. Katta o'zgarishlar uchun avval issue oching.

---

**Eslatma:** Bu ilova faqat shaxsiy foydalanish uchun. Boshqalarga taratishdan oldin barcha maxfiy ma'lumotlarni o'chiring.
