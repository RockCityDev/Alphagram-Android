# Alphagram

## About

Alphagram, the Web3 Telegram.

Creates a secure 3rd party Web3 Telegram for worldwide users！ 

## Features - Provides More Convenience

1. Comfortable - Incognito Mode, The other party can't see the message read double checkmark.
2. Fast - Unlimited Pinned Messages, Help you find important messages quickly.
3. Instantly - Translate every message with one click, Help you manage the international communication and information.
4. Helpful - Practical tools, Scan the code to add friends/Easy to clear up storage.
5. Safe - Crypto Wallet, Fast Web3 Asset Transaction.
6. Auto-sync - NFT Profile Verification, Give you immersive Web3 experience.
7. Advanced - Crypto Transfer, Transactions will be done in a chat box.
8. Statistics - Web3 Profile, Check your crypto public data easily.

## Web3

1. Web3j https://github.com/web3j/web3j
2. TSS Wallet https://docs.particle.network/
3. Wallet-Connect https://docs.walletconnect.com/1.0/
4. OpenSeaAPI https://docs.opensea.io/reference/api-overview

# Telegram

## Telegram messenger for Android

[Telegram](https://telegram.org) is a messaging app with a focus on speed and security. It’s superfast, simple and free.
This repo contains the official source code for [Telegram App for Android](https://play.google.com/store/apps/details?id=org.telegram.messenger).

## Creating your Telegram Application

We welcome all developers to use our API and source code to create applications on our platform.
There are several things we require from **all developers** for the moment.

1. [**Obtain your own api_id**](https://core.telegram.org/api/obtaining_api_id) for your application.
2. Please **do not** use the name Telegram for your app — or make sure your users understand that it is unofficial.
3. Kindly **do not** use our standard logo (white paper plane in a blue circle) as your app's logo.
3. Please study our [**security guidelines**](https://core.telegram.org/mtproto/security_guidelines) and take good care of your users' data and privacy.
4. Please remember to publish **your** code too in order to comply with the licences.

## API, Protocol documentation

Telegram API manuals: https://core.telegram.org/api

MTproto protocol manuals: https://core.telegram.org/mtproto

## Compilation Guide

**Note**: In order to support [reproducible builds](https://core.telegram.org/reproducible-builds), this repo contains dummy release.keystore,  google-services.json and filled variables inside BuildVars.java. Before publishing your own APKs please make sure to replace all these files with your own.

You will require Android Studio 3.4, Android NDK rev. 20 and Android SDK 8.1

1. Download the Telegram source code from https://github.com/DrKLO/Telegram ( git clone https://github.com/DrKLO/Telegram.git )
2. Copy your release.keystore into TMessagesProj/config
3. Fill out RELEASE_KEY_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_STORE_PASSWORD in gradle.properties to access your  release.keystore
4.  Go to https://console.firebase.google.com/, create two android apps with application IDs org.telegram.messenger and org.telegram.messenger.beta, turn on firebase messaging and download google-services.json, which should be copied to the same folder as TMessagesProj.
5. Open the project in the Studio (note that it should be opened, NOT imported).
6. Fill out values in TMessagesProj/src/main/java/org/telegram/messenger/BuildVars.java – there’s a link for each of the variables showing where and which data to obtain.
7. You are ready to compile Telegram.

## Localization

We moved all translations to https://translations.telegram.org/en/android/. Please use it.
