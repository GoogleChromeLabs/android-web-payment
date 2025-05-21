
This is an Android payment app sample that works with Web PaymentRequest API.

- SamplePay
  - An Android payment app
  - A web payment app (The implementation is empty; it can only delegate to the Android app)
- SampleMerchant
  - A web merchant site

## Test the Android payment App

1. Build and run the Android app in the `SamplePay` folder using Android Studio (or other tools or IDEs using Gradle) or run the `app-debug.apk` file included in the repo.
2. Open the Chrome browser and navigate to  https://sample-pay-ss.web.app
3. Click on "PAY" button.


<table>
  <tr>
    <td style="vertical-align: top; border-right: 30px solid transparent">

## SampleMerchant

The project can be deployed to [Firebase Hosting](https://firebase.google.com/docs/hosting).

1. Install [Firebase CLI](https://firebase.google.com/docs/cli#install_the_firebase_cli).
2. Create a new Firebase project.
3. Edit `SampleMerchant/.firebaserc` and change the project ID to yours.
4. Edit `SampleMerchant/public/index.html` and change the `supportedMethods` to your SamplePay's domain.
5. Run `$ firebase deploy`.

## SamplePay (Android)

1. Import the project path (`SamplePay/`) to Android Studio.
2. Modify `SamplePay/gradle.properties` and change the domain name to yours.
3. Run



> **Note:** This app generates invalid responses to show how the Payment Request API responds to incomplete payloads (e.g.: an empty shipping address). Make sure that your payment objects are well formed.

  </td>
  <td width="280">
    <img src="payment-app-repo.gif" alt=""/>
  </td>
</tr>
</table>

## SamplePay (Web)

The project can be deployed to [Firebase Hosting](https://firebase.google.com/docs/hosting).

1. Install [Firebase CLI](https://firebase.google.com/docs/cli#install_the_firebase_cli).
2. Create a new Firebase project.
3. Edit `SamplePay/.firebaserc` and change the project ID to yours.
4. Edit `SamplePay/public/manifest.json` and change the domain nams to yours. Also, change the fingerprint SHA256 hash to your SamplePay's app.
5. Edit `SamplePay/public/payment-manifest.json` and change the domain name to yours.
6. Edit `SamplePay/firebase.json` and change the domain name to yours.
6. Run `$ firebase deploy`.


