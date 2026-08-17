# Come generare l'APK di TLCField

Il tuo assistente AI (Claude) non ha accesso ai server Google/Gradle necessari
per compilare un APK Android, quindi non può farlo direttamente in chat.
Ecco due modi semplici per ottenerlo comunque.

## Opzione A — GitHub Actions (consigliata, automatica, gratuita)

1. Crea un repository su GitHub (può essere privato) e carica tutto il
   contenuto di questa cartella (compresa la cartella `.github/workflows`).
   - Via web: trascina i file nella pagina "Add file > Upload files".
   - Via terminale:
     ```bash
     cd TLCField_0.9
     git init
     git add .
     git commit -m "Initial commit"
     git branch -M main
     git remote add origin https://github.com/TUO_USERNAME/TLCField.git
     git push -u origin main
     ```
2. Vai sulla tab **Actions** del repository: partirà automaticamente il
   workflow "Build APK".
3. A fine build (2-5 minuti), apri il job completato e scarica l'artifact
   **TLCField-debug-apk** dalla sezione "Artifacts": conterrà il file
   `app-debug.apk`, installabile su qualunque Android (minSdk 26 = Android 8+).
4. Se non parte in automatico, vai su Actions > Build APK > "Run workflow".

⚠️ Nota: è un **APK di debug**, non firmato per il Play Store — va bene per
installarlo direttamente sul telefono (dovrai abilitare "Origini sconosciute"
o confermare l'installazione da file), ma non per pubblicarlo su Google Play.

## Opzione B — Android Studio (in locale)

1. Installa [Android Studio](https://developer.android.com/studio).
2. Apri la cartella `TLCField_0.9` come progetto esistente.
3. Al primo avvio, Android Studio rigenera automaticamente i file mancanti
   del wrapper Gradle (`gradlew`) e scarica le dipendenze.
4. Menu **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. L'APK compare in `app/build/outputs/apk/debug/app-debug.apk`.

## Requisiti del progetto (per riferimento)
- compileSdk / targetSdk: 37
- minSdk: 26 (Android 8.0+)
- Kotlin 2.3.21, Jetpack Compose
- Gradle 9.4.1
