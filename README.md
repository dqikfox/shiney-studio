# ShinyVerse (Android)

A native Android client for **ShinyVerse / Kid Mode**, the kid-friendly persona of the
same self-hosted Ultra Studio generation server. Same account system and backend as
[Ultra Studio](https://github.com/dqikfox/ULTRA-STUDIO), reskinned and simplified for
younger users, and shipped as its own separate app rather than a mode switch — it has
its own icon, its own install, and no admin/queue surfaces a kid doesn't need.

This app is a client only — it talks to an existing Ultra Studio / SHInEyVErSE server
over HTTP and does not include or run any generation backend itself.

## What's different from Ultra Studio

- **No Live Queue, no Admin** — the Kid Mode web page never exposes either, so neither
  does this app. Bottom nav is just Create, Gallery, Settings.
- **Pip** instead of Nyx — the same ElevenLabs voice/text companion architecture,
  pointed at Kid Mode's own agent, with the same client tools (list/create/move/copy/
  delete gallery items and folders, kick off a generation).
- **Pastel theme** — colors pulled straight from the Kid Mode web page's own palette,
  always light (Kid Mode has no dark variant on the web either).
- **Starter prompts, "Surprise Me!", and "Random Pokemon!"** — inspiration chips fill
  the prompt field; Random Pokemon fills it and generates immediately, with a 1-in-50
  chance of a "shiny" reveal, matching the original web behavior.
- Advanced generation settings are still there, just tucked under "Grown-up settings".

## Tech stack

Same as Ultra Studio Android: Kotlin + Jetpack Compose, Hilt, Retrofit/OkHttp with a
runtime-configurable server address, cookie-based session auth, Coil + Media3 wired
through an authenticated client, a foreground `Service` for batch generation, and the
ElevenLabs Conversational AI SDK for Pip.

## Building

Requires JDK 21 and the Android SDK.

```bash
JAVA_HOME=/path/to/jdk-21 ./gradlew assembleDebug
```

The resulting APK is at `app/build/outputs/apk/debug/app-debug.apk`.

## Server address

On first launch the app asks for the server's address (e.g. a Tailscale hostname or
LAN IP with port) — the same server Ultra Studio connects to. This can be changed
later from Settings without reinstalling.
