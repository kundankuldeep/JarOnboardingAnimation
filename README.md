# Android Project Setup - MVVM with Hilt, Retrofit & Compose

This project has been configured with modern Android development dependencies compatible with AGP 8.5.0.

## 🔧 Dependencies Added

### Core Dependencies
- **Jetpack Compose** - Modern UI toolkit
- **Hilt** (v2.51.1) - Dependency injection
- **Coroutines** (v1.9.0) - Asynchronous programming
- **Retrofit** (v2.11.0) - HTTP client
- **MVVM Architecture** - ViewModel and LiveData

### Additional Libraries
- **OkHttp** (v4.12.0) - HTTP client with logging interceptor
- **Gson** (v2.10.1) - JSON parsing (primary)
- **Moshi** (v1.15.1) - JSON parsing (alternative option)
- **Navigation Compose** (v2.8.4) - Navigation component for Compose
- **Hilt Navigation Compose** (v1.2.0) - Hilt integration with Navigation

## 📱 Architecture Implemented

### MVVM Pattern
- `MainViewModel` - Manages UI state with StateFlow
- `UserRepository` - Handles data operations
- `ApiService` - Retrofit interface for network calls

### Dependency Injection
- `@HiltAndroidApp` - Application class annotation
- `@AndroidEntryPoint` - Activity injection
- `@HiltViewModel` - ViewModel injection
- `NetworkModule` - Provides network dependencies

### Key Features Demonstrated
- State management with StateFlow
- Coroutines for async operations
- Retrofit for API calls (JSONPlaceholder API)
- Gson for JSON parsing (with Moshi as alternative)
- Compose UI with ViewModel integration
- Loading states and error handling

### JSON Parsing Options
The project is configured with both **Gson** and **Moshi** for flexibility:
- **Current setup**: Uses Gson with `@SerializedName` annotations
- **Alternative**: Switch to Moshi by uncommenting the alternative Retrofit provider in `NetworkModule`
- **Utility**: `JsonUtils` class provides common Gson operations

## 🚀 Getting Started

1. Sync the project - All dependencies should download automatically
2. Run the app - You'll see a demo MVVM screen
3. The app demonstrates:
   - ViewModel state management
   - Coroutines with loading states
   - Dependency injection with Hilt
   - Ready for Retrofit integration

## 📁 Project Structure

```
app/src/main/java/com/app/jaronboardinganimation/
├── JarOnboardingApplication.kt          # Application class with Hilt
├── MainActivity.kt                      # Main activity with @AndroidEntryPoint
├── ui/
│   ├── main/
│   │   └── MainViewModel.kt            # MVVM ViewModel
│   └── theme/                          # Compose theming
├── data/
│   ├── api/
│   │   └── ApiService.kt               # Retrofit interface
│   ├── model/
│   │   └── User.kt                     # Data model with Gson annotations
│   └── repository/
│       └── UserRepository.kt           # Repository pattern
├── di/
│   └── NetworkModule.kt                # Dependency injection module
└── utils/
    └── JsonUtils.kt                    # Gson utility functions
```

## ✅ Compatibility

All dependencies are compatible with AGP 8.5.0 and target Android API 35.

Happy coding! 🎉
