# Fit AI Tracker

Repository created by GitHub Copilot on user request.

**Une application Android de suivi fitness avec IA, synchronisée bidirectionnellement avec Health Connect et vos autres apps de santé.**

## 🎯 Fonctionnalités

- **Tracking métabolique en direct** : Suivi du poids, TDEE, trend weight
- **Scanner IA (Gemini)** : Analysez vos repas/boissons avec l'IA
- **Synchronisation Health Connect** : Lisez ET écrivez dans Health Connect
- **Interopérabilité multi-apps** : Partagez vos données avec Fit, Health, Strava, etc.
- **Stockage local** : Base de données Room pour une synchronisation robuste
- **Permissions granulaires** : Contrôle total sur vos données de santé

## 📱 Architecture

```
app/
├── src/main/java/com/fitai/tracker/
│   ├── MainActivity.kt              # UI Compose principale
│   ├── TrackerViewModel.kt          # Logique métier
│   ├── HealthConnectHelper.kt       # Lecture/Écriture Health Connect (READ + WRITE)
│   ├── NotificationHelper.kt        # Notifications
│   ├── PreferencesManager.kt        # Préférences utilisateur
│   ├── db/
│   │   ├── HealthDataDatabase.kt    # Room Database
│   │   ├── HealthDataEntity.kt      # Modèle de données
│   │   └── HealthDataDao.kt         # Accès aux données
│   ├── provider/
│   │   └── HealthDataProvider.kt    # ContentProvider pour partage inter-apps
│   └── sync/
│       └── HealthDataSyncManager.kt # Synchronisation bidirectionnelle
├── AndroidManifest.xml             # Permissions READ/WRITE + ContentProvider
└── build.gradle.kts                # Dépendances (Room, Coroutines, Health Connect)
```

## 🔄 Flux de Synchronisation Bidirectionnelle

### ENTRANT (In)
Les autres apps → Health Connect → Votre app
```
Fit App / Strava / autre app
    ↓
Health Connect
    ↓
HealthConnectHelper.readWeight/Steps/HeartRate/etc()
    ↓
Stocké dans Room Database
    ↓
Utilisé pour analyses Gemini
```

### SORTANT (Out)
Votre app → Health Connect → Autres apps
```
Données utilisateur / IA
    ↓
Ajoutées à Room (synced=false)
    ↓
HealthDataSyncManager.syncToHealthConnect()
    ↓
HealthConnectHelper.write*()
    ↓
Health Connect
    ↓
Visibles dans Fit, Health, Strava, etc.
```

## 🚀 Installation & Configuration

### Prérequis
- Android Studio 2023.1+
- Android 9+ (API 28+)
- Gemini API Key (from [Google AI Studio](https://aistudio.google.com))

### Setup

1. **Cloner le repo**
```bash
git clone https://github.com/rmambulancier-pixel/fit-ai-tracker.git
cd fit-ai-tracker
```

2. **Créer `local.properties`**
```properties
GEMINI_API_KEY=your_actual_api_key_here
```

3. **Build & Run**
```bash
./gradlew build
./gradlew installDebug
```

## 📊 Utilisation

### Depuis l'UI
- **"Enregistrer la pesée"** : Ajoute le poids localement ET dans Health Connect
- **"Autoriser Health Connect"** : Demande les permissions pour lire/écrire
- **"Scanner repas (Gemini)"** : Analyse la photo et sauvegarde les calories

### Depuis le Code

```kotlin
// Initialiser le sync manager
val syncManager = HealthDataSyncManager(context)

// Ajouter une donnée (auto-sync vers Health Connect)
syncManager.addHealthData(
    dataType = "weight",
    value = 75.5,
    unit = "kg",
    source = "user_input"
)

// Lire l'historique
val weightHistory = syncManager.getHistory("weight", limit = 30)

// Sync bidirectionnelle manuelle
syncManager.fullSync()

// Sync automatique toutes les 30 min
syncManager.startPeriodicSync(intervalMinutes = 30)
```

## 🔐 Permissions

### Health Connect (READ)
- `android.permission.health.READ_WEIGHT`
- `android.permission.health.READ_STEPS`
- `android.permission.health.READ_HEART_RATE`
- `android.permission.health.READ_NUTRITION`
- `android.permission.health.READ_HYDRATION`
- `android.permission.health.READ_DISTANCE`
- `android.permission.health.READ_CALORIES_BURNED`

### Health Connect (WRITE)
- `android.permission.health.WRITE_WEIGHT`
- `android.permission.health.WRITE_STEPS`
- `android.permission.health.WRITE_HEART_RATE`
- `android.permission.health.WRITE_NUTRITION`
- `android.permission.health.WRITE_HYDRATION`
- `android.permission.health.WRITE_DISTANCE`
- `android.permission.health.WRITE_CALORIES_BURNED`

### Autres
- `android.permission.INTERNET`
- `android.permission.CAMERA`
- `android.permission.POST_NOTIFICATIONS`

## 📦 Dépendances Clés

```kotlin
// Health Connect
androidx.health.connect:connect-client:1.1.0-alpha11

// Room Database
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1

// Jetpack Compose
androidx.compose:compose-bom:2024.05.00

// Google Generative AI (Gemini)
com.google.ai.client.generativeai:generativeai:0.9.0

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3
```

## 🔄 Flux de Données

```
┌────────────────────────────────────────────────────────────────[...]
│                    OTHER HEALTH APPS                            │
│  (Fit, Health, Strava, WeWard, Mi Fitness, ICE smart, etc.)     │
└────────────────────┬───────────────────────────────────────────[...]
                     │
        ┌────────────┴──────────────┐
        ↓                           ↓
    [READ]                      [WRITE]
        ↓                           ↓
┌────────────────────────────────────────────────────────────────[...]
│              HEALTH CONNECT (Central Hub)                       │
└────────────────────┬───────────────────────────────────────────[...]
                     │
        ┌────────────┴──────────────┐
        ↓                           ↓
┌──────────────────────┐   ┌──────────────────────┐
│ HealthConnectHelper  │   │ HealthDataProvider   │
│  (Read operations)   │   │ (ContentProvider)    │
└──────────┬───────────┘   └──────────┬───────────┘
           │                          │
           └────────────┬─────────────┘
                        ↓
          ┌──────────────────────────┐
          │   Room Database          │
          │  (HealthDataEntity)      │
          └──────────────────────────┘
                        ↑
                        │
          ┌─────────────┴──────────────┐
          ↓                            ↓
   [MainActivity]          [HealthDataSyncManager]
   [UI Updates]            [Sync Logic]
          │                            │
          └─────────────┬──────────────┘
                        ↓
              ┌──────────────────────┐
              │ TrackerViewModel     │
              │ (Business Logic)     │
              └──────────────────────┘
```

## 🛠️ Développement

### Ajouter un nouveau type de donnée

1. Ajouter dans `HealthConnectHelper.kt` :
```kotlin
@Suppress("UNCHECKED_CAST")
suspend fun readMyData(context: Context): List<MyRecord> {
    val timeRangeFilter = TimeRangeFilter.between(...)
    val request = ReadRecordsRequest(
        recordType = MyRecord::class as KClass<Record>,
        timeRangeFilter = timeRangeFilter
    ) as ReadRecordsRequest<MyRecord>
    return client.readRecords(request).records
}

suspend fun writeMyData(context: Context, value: Double) { ... }
```

2. Ajouter dans `HealthDataSyncManager.kt` :
```kotlin
"my_data" -> {
    HealthConnectHelper.writeMyData(context, data.value)
    dao.markAsSynced(data.id, "health_connect")
}
```

3. Ajouter permissions dans `AndroidManifest.xml` :
```xml
<uses-permission android:name="android.permission.health.READ_MY_DATA" />
<uses-permission android:name="android.permission.health.WRITE_MY_DATA" />
```

### Tests
```bash
./gradlew connectedAndroidTest
```

## 📝 Changelog

### v1.0 (Initial)
- ✅ Synchronisation Health Connect (READ + WRITE)
- ✅ ContentProvider pour inter-app sharing
- ✅ Room Database pour stockage local
- ✅ Sync bidirectionnelle automatique
- ✅ Scanner IA Gemini
- ✅ Tracking métabolique
- ✅ Corrections des erreurs de compilation Kotlin (type variance)

### v1.1 (Bug Fixes)
- ✅ Correction des erreurs de type dans `HealthPermission` avec casts explicites
- ✅ Correction de `ReadRecordsRequest` avec suppression des avertissements UNCHECKED_CAST
- ✅ Suppression des imports inutilisés dans `HealthDataSyncManager`

## 🤝 Contribution

Les PRs sont bienvenues ! Pour les changements majeurs, ouvrez une issue en premier.

## 📄 Licence

Non spécifiée

## 🆘 Support

**Question** : "Health ne voit toujours pas mon application"
**Réponse** : 
- Vérifiez les permissions dans `AndroidManifest.xml`
- Redémarrez l'application après avoir accordé les permissions
- Vérifiez que Health Connect est installé (Android 13+)
- Utilisez le `ContentProvider` déclaré pour permettre le partage

**Question** : "Erreurs de compilation Kotlin avec Health Connect"
**Réponse** :
- Assurez-vous d'utiliser `@Suppress("UNCHECKED_CAST")` pour les casts de type variance
- Les record types (`WeightRecord`, etc.) doivent être castés en `KClass<Record>` pour `HealthPermission` et `ReadRecordsRequest`
- Consulter : [Health Connect Type Variance Guide](https://developer.android.com/guide/health-connect/read-write-data)

---

**Créé avec ❤️ par GitHub Copilot pour rmambulancier-pixel**
