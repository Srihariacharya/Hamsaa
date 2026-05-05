# Hamsaa - Advanced Business Intelligence & Relationship Management

**Hamsaa** is an elite, professional-grade relationship intelligence suite. It is designed to modernize professional network tracking using deep ingestion architectures, predictive analytics, and performance-driven scaling.

## 🚀 Key Stabilized Features (v1.2.0)

- 📊 **Intelligence Dashboard**: Real-time interaction algorithms that map outreach trends with 100% data integrity (deduplicated).
- 🔔 **Task Automation**: Smart task scheduling with Material 3 Date Picking and automated 9:00 AM push notifications.
- ⚡ **High-Scale Performance**: Optimized database indexing supporting 15,000+ nodes with zero-latency searching.
- 🛡️ **Safe Classification**: Manual-first gender logic ensures 100% accuracy in relationship tracking without predictive errors.
- 🌙 **Midnight Slate Theme**: Premium modern design schema using Jetpack Compose and Material 3 design tokens.

## 💎 Technical Innovations

- **Intelligent Deduplication Engine**: A custom-built backend algorithm that merges incoming phone logs with existing cloud records using fuzzy matching and timestamp normalization, preventing data inflation.
- **Self-Healing Data Architecture**: Implemented a "Refresh Intelligence" protocol that allows users to trigger server-side re-processing of contact metadata to correct historical inaccuracies.
- **Lazy-Load Synchronization**: Optimized Android sync engine that handles 15,000+ contact records by chunking data transfers, ensuring the UI remains responsive during ingestion.

## 🛠️ Integrated Ecosystem

### Mobile Client (Android)
- **Language**: Kotlin
- **Toolkit**: Jetpack Compose (Material 3)
- **Networking**: Retrofit & OkHttp
- **Architecture**: MVVM + StateFlow + Coroutines
- **Features**: AlarmManager Reminders, Local Sync Engine

### Service Engine (Backend)
- **Framework**: Spring Boot 3+ (Java)
- **Persistence**: JPA / Hibernate (Indexed Performance Schema)
- **Security**: JWT & Secure DTO Mapping
- **Deployment**: Render

## 📂 Project Structure

```text
.
├── ContactProApp/       # Mobile Application Client (Android Compose)
├── Backend/             # Service Middleware Engine (Java Spring)
└── README.md            # Execution Manual
```

## 🚀 Setup & Launch

### Prerequisites
- Android Studio (Ladybug+)
- JDK 17
- Gradle 8+

### Execution Protocol
1. **Backend**: Launch the Spring Boot application using `mvn spring-boot:run`.
2. **Mobile**: Open `ContactProApp` in Android Studio and deploy to a device with Notification Permissions enabled.

## ✍️ Authors
- **Srihari Acharya**
- **Srivishnu Pejathaya**
