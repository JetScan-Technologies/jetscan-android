# 🛠️ App Architecture

The app is built using the MVVM architecture pattern. The app is divided into three main modules:

## App

The `app` module is the main module of the app. It is sectioned into `data` and `presentation` folders.

### Data layer

This folder contains the data sources for the app. It contains the auth, document and platform logic. Each folder follows a pattern

```cmd
folder
├── datasource
│   ├── disk
│   │   ├── dao, database, entity, di, utils
│   │
│   ├── network
│       ├── api, service, model, di, utils
│
│── di
│
│── manager
│   ├── di, model, utils
│
├── repository
│   │── di, utils, (repository and impl)
│
├── utils
```

This pattern is followed for each core component (if possible).

- The `datasource` folder contains the data sources for the app which can be from `Room` db (local) or from network api (remote).
- The `di` folder contains all the dependency injection logic for this component.
- The `manager` folder contains the business logic for the app. - The `repository` folder contains the repository pattern for the app.
- The `utils` folder contains the utility classes for the app.

The `platform` folder contains the platform logic for the app. It contains the platform specific logic for the app.

### Presentation layer

This folder contains the ui logic for the app. All the ui logic is divided into `feature` folders. Each feature may folder follows a pattern

```cmd
screen
├── components
├── FeatureScreen.kt
├── FeatureViewModel.kt
├── FeatureNavigation.kt
```

#### Components

This folder contains the ui components for the feature. If the feature screen is complex, it is divided into smaller components and placed in this folder.

#### FeatureScreen

This file contains the ui logic for the feature. It contains the ui logic for the feature screen. It is a `composable` function which returns the ui for the feature. It can be represented as follows

```kotlin

@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    JetScanScaffold(
        topBar = {
            // Top bar logic
        },
        content = {
            // Content logic
        },
    ){ padding, windowSize ->
        // Scaffold logic

    }
}
```

The `FeatureScreen` composable function should start with the `JetScanScaffold` composable which is a custom scaffold for the app. It contains the top bar, floating action bar, inner padding and current window size of the phone.

#### FeatureViewModel

This file contains the viewmodel logic for the feature. Every viewmodel must be inherited from the [`BaseViewModel`](..\app\src\main\java\io\github\dracula101\jetscan\presentation\platform\base\BaseViewModel.kt) class. It is represented as follows

```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val repository: FeatureRepository
) : BaseViewModel<FeatureState, FeatureEffect, FeatureAction>(

) {
    override fun handleEvent(event: FeatureEvent) {
        // Event logic
    }
}

@Parcelize
sealed class FeatureState(
    // State logic
) : Parcelable

@Parcelize
sealed class FeatureEvent(
    // Event logic
) : Parcelable

@Parcelize
sealed class FeatureEffect(
    // Effect logic
) : Parcelable
```

The `FeatureState`, `FeatureEvent` and `FeatureEffect` are the state, event and effect classes for the feature. They are `sealed` classes which contain the state, event and effect logic for the feature. The `handleEvent` function is used to handle the events for the feature.

#### FeatureNavigation

The `FeatureNavigation` file contains the navigation logic for the feature. It contains the navigation logic for the feature. It is represented as follows

```kotlin
const val FEATURE_SCREEN_ROUTE = "feature_screen"

fun NavGraphBuilder.createScreenNavigation() {
    composable(FEATURE_SCREEN_ROUTE) {
        FeatureScreen()
    }
}

fun NavHostController.navigateToFeatureScreen() {
    navigate(FEATURE_SCREEN_ROUTE)
}
```

Credits to **Bitwarden** app for the architecture pattern.

## OpenCv

The `opencv` module contains version 4.1.0 of the OpenCV library. It is used for image processing in the app.

## Pdf

The `pdf` module contains the pdf library for the app. It is used for pdf processing in the app. It uses the `itextg` library for pdf processing.

**ITextG Version**: 5.5.10

Currently, the ItextG library is only used to generate pdfs in the app. The pdf-tools functionality uses custom backend server, but it can be extended to use the ItextG library (future idea).

## Common used classes

### Data Layer

- [**BaseDiskSource**](../app/src/main/java/io/github/dracula101/jetscan/data/platform/datasource/disk/BaseDiskSource.kt): This class is the base class for all the disk sources in the app which uses shared preferences, to store key value pairs. A variant of this class [BaseEncryptedDiskSource](../app/src/main/java/io/github/dracula101/jetscan/data/platform/datasource/disk/BaseEncryptedDiskSource.kt) which uses encrypted shared preferences.

- [**OpenCvManager**](../app/src/main/java/io/github/dracula101/jetscan/data/platform/manager/opencv/OpenCvManager.kt): This class is the bridge between OpenCV and the app. It has common functionality like rotate, crop, etc.

- [**AuthRepository**](../app/src/main/java/io/github/dracula101/jetscan/data/auth/repository/AuthRepository.kt): This class is the repository for the auth logic in the app. It contains the logic for login, register, etc.

- [**ConfigRepository**](../app/src/main/java/io/github/dracula101/jetscan/data/platform/repository/config/ConfigRepository.kt): This class is the repository for the config logic in the app. It uses [ConfigDiskSource](../app/src/main/java/io/github/dracula101/jetscan/data/platform/datasource/disk/config/ConfigDiskSource.kt) to store the config info in shared preferences.

- [**PdfManager**](../pdf/src/main/java/io/github/dracula101/pdf/manager/PdfManager.kt): This class is the bridge between the pdf library and the app. It has common functionality like create, merge, etc.

- [**DocumentRepository**](../app/src/main/java/io/github/dracula101/jetscan/data/document/repository/DocumentRepository.kt): This class is the main repository for the document state logic in the app. The [DocumentDao](..app/src/main/java/io/github/dracula101/jetscan/data/document/datasource/disk/database/DocumentDatabase.kt) is used to store document info in Room DB and [DocumentManager](../app/src/main/java/io/github/dracula101/jetscan/data/document/manager/DocumentManager.kt) is used to manage files stored locally.

#### How is a Document stored?

A document can be either be imported or scanned from the scanner screen. Currently, the documents are only stored locally in the following way:

1. The [DocumentRepository](../app/src/main/java/io/github/dracula101/jetscan/data/document/repository/DocumentRepository.kt) takes in bitmaps(scanner) or imported pdf and passes to [DocumentManager](../app/src/main/java/io/github/dracula101/jetscan/data/document/manager/DocumentManager.kt) to store the document locally.

2. The [DocumentManager](../app/src/main/java/io/github/dracula101/jetscan/data/document/manager/DocumentManager.kt) generates `Scanned Documents` folder to store the file and creates a folder with encoded file name to store document files.

3. Folder structure to store efficiently:

   ```cmd
   Scanned Documents
   │
   ├────14npnr8h78o9hb7o89uli97o8
   │   ├── Original Images
   │   │   ├── Image_0.jpg
   │   │   ├── Image_1.jpg
   │   │   ├── Image_2.jpg
   │   │
   │   ├── Scanned Images
   │   │   ├── Image_0.jpg
   │   │   ├── Image_1.jpg
   │   │   ├── Image_2.jpg
   │   │
   │   ├── Document.pdf
   ```

   The `Original Images` folder contains the original images taken from the scanner screen. The `Scanned Images` folder contains the processed images after applying filters. The `Document.pdf` is the final pdf generated from the scanned images.

4. After the previous action is successfully, [DocumentDao](../app/src/main/java/io/github/dracula101/jetscan/data/document/datasource/disk/database/DocumentDatabase.kt) is used to store the document info in Room DB. The document info contains the document name, document path, document type, document size, etc.

5. Finally the pdf is either generated by the [PdfManager](../pdf/src/main/java/io/github/dracula101/pdf/manager/PdfManager.kt) or the copied from the imported file.

### Presentation Layer

- [**BaseViewModel**](../app/src/main/java/io/github/dracula101/jetscan/presentation/platform/base/BaseViewModel.kt): This class is the base class for all the viewmodels in the app. It contains the common logic for the viewmodels.

- [**ImportBaseViewModel**](../app/src/main/java/io/github/dracula101/jetscan/presentation/platform/base/ImportBaseViewModel.kt): This class is the base class to import documents more efficiently.

- [**JetScanScaffold**](../app/src/main/java/io/github/dracula101/jetscan/presentation/platform/component/scaffold/JetScanScaffold.kt): This class is the custom scaffold for the app. It contains the top bar, floating action bar, inner padding and current window size of the phone.

- [**JetScanScaffoldWithFlexAppBar**](../app/src/main/java/io/github/dracula101/jetscan/presentation/platform/component/scaffold/JetScanScaffoldWithFlexAppBar.kt): This class is similar to `JetScanScaffold` but with a flexible app bar.

- [**JetScanTopBar**](../app/src/main/java/io/github/dracula101/jetscan/presentation/platform/component/appbar/JetScanTopAppbar.kt): This class is the custom top bar for the app consistent with current app theme.
