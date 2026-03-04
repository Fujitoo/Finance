# Campus Expense Tracker

A simple offline Android app for undergraduate students to track daily expenses and split trip expenses with friends.

## Features

- **Track Personal Expenses**: Quickly add and categorize daily expenses
- **Categorize Expenses**: Create custom categories with emoji icons
- **Create Trips**: Set up trips with friends for shared expenses
- **Split Expenses**: Record who paid each expense and calculate balances
- **Attach Receipts**: Take or attach photos of receipts
- **Share Summary**: Generate shareable image cards for trip expense summaries
- **Offline First**: Works completely offline, no internet required
- **Indian Rupees (₹)**: All amounts displayed in INR

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM
- **Database**: Room
- **Navigation**: Navigation Compose
- **Design**: Material 3

## Project Structure

```
app/
├── src/main/
│   ├── java/com/campus/expensetracker/
│   │   ├── data/
│   │   │   ├── dao/           # Room DAOs
│   │   │   ├── database/      # Database setup
│   │   │   ├── entity/        # Room entities
│   │   │   └── repository/    # Repository layer
│   │   ├── navigation/        # Navigation setup
│   │   ├── theme/             # Material 3 theme
│   │   ├── ui/
│   │   │   ├── components/    # Reusable UI components
│   │   │   └── screens/       # App screens
│   │   ├── utils/             # Utility classes
│   │   ├── viewmodel/         # ViewModels
│   │   └── ExpenseTrackerApp.kt
│   ├── res/
│   └── AndroidManifest.xml
```

## Build Instructions

### Prerequisites

- Android SDK (API 26+)
- Gradle 8.2+
- JDK 17+

### Building with CLI

```bash
# Navigate to project directory
cd /path/to/Finance

# Make gradlew executable (Linux/Mac)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# The APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

### Building with GitHub Actions

Every push to the `master` or `main` branch automatically builds an APK using GitHub Actions.

1. Push your code to GitHub
2. Go to the **Actions** tab in your repository
3. Click on the "Build Android APK" workflow run
4. Download the APK from the artifacts section

### Installing on Device

```bash
# Install via ADB
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Data Flow

```
UI → ViewModel → Repository → Room Database
```

## Database Entities

- **Expense**: id, amount, categoryId, note, date, imagePath, tripId, paidByFriendId
- **Category**: id, name, icon
- **Trip**: id, name, dateCreated
- **Friend**: id, name, tripId
- **Split**: id, expenseId, friendId, amount

## Screens

1. **HomeScreen**: Monthly summary, recent expenses, trips overview
2. **ExpensesScreen**: List of all expenses with edit/delete
3. **AddExpenseScreen**: Add/edit expense with amount, category, note, trip, friend
4. **CategoriesScreen**: Manage expense categories
5. **TripsScreen**: List of trips with totals
6. **TripDetailScreen**: Trip details, friends, expenses, balance summary

## Permissions

- `READ_MEDIA_IMAGES` (Android 13+): For attaching receipt photos
- `READ_EXTERNAL_STORAGE` (Android 12 and below): For attaching receipt photos

## Security

- Safe file storage using internal app storage
- FileProvider for secure file sharing
- Input validation for amounts
- No arbitrary file access

## License

This project is open source and available for educational purposes.
