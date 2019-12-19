android-web-payment
===================

This is an Android payment app sample that works with Web PaymentRequest API.

- SamplePay
  - An Android payment app
  - A web payment app (The implementation is empty; it can only delegate to the Android app)
- SampleMerchant
  - A web merchant site

## SamplePay (Web)

The project can be deployed to [Firebase Hosting](https://firebase.google.com/docs/hosting).

1. Install [Firebase CLI](https://firebase.google.com/docs/cli#install_the_firebase_cli).
2. Create a new Firebase project.
3. Edit `SamplePay/.firebaserc` and change the project ID to yours.
4. Edit `SamplePay/public/manifest.json` and change the domain nams to yours. Also, change the fingerprint SHA256 hash to yours.
5. Edit `SamplePay/public/payment-manifest.json` and change the domain name to yours.
6. Run `$ firebase deploy`.

## SamplePay (Android)

1. Import the project path (`SamplePay/`) to Android Studio.
2. Modify these files and change the domain name to yours.
   - SamplePay/app/src/main/AndroidManifest.xml
   - SamplePay/app/src/main/res/values/arrays.xml
   - SamplePay/app/src/main/java/com/example/android/samplepay/PaymentActivity.kt
   - SamplePay/app/src/main/java/com/example/android/samplepay/SampleIsReadyToPayService.kt
3. Run

## SampleMerchant

The project can be deployed to [Firebase Hosting](https://firebase.google.com/docs/hosting).

1. Install [Firebase CLI](https://firebase.google.com/docs/cli#install_the_firebase_cli).
2. Create a new Firebase project.
3. Edit `SamplePay/.firebaserc` and change the project ID to yours.
4. Edit `SamplePay/public/index.html` and change the `supportedMethods` to your domain.
5. Run `$ firebase deploy`.
