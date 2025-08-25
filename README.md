# DesirePaths

DesirePaths is an Android mobile application for collecting geolocated public input for community projects. People can log in with their Facebook or Google account or remain anonymous (anonymous login is handled in `LoginActivity`) and add ideas, comments or warnings on a map. Each entry can include a title, snippet, photo and category (idea, comment or warning) defined in the `Universals` constants. The app allows others to view these entries, rate and comment on them, forming a collaborative map of “desire paths” for urban‑planning projects.

## Features

- **Map‑based public input:** The main `MapActivity` displays project boundaries and user submissions on a Google map. It uses clustering and heatmaps to represent many points and lets users drop markers at specific locations. Submitted entries are represented by custom markers whose icon reflects the sentiment category (idea/comment/warning).

- **Add new public input:** In `PublicInputAddActivity` users can select or capture a photo, choose the category, answer project questions and upload the entry. The activity handles image sampling and sending photos via FTP.

- **View entries and comments:** `PublicInputViewActivity` shows the details of a single public input item, including the image, title, snippet, submitter, date and a list of comments. Users can reply with comments and rate existing comments. Comments are loaded via the `CommentsAdapter` and are tied to each entry by foreign keys.

- **User authentication:** The app supports Facebook and Google sign‑in using the Facebook SDK and Google Play services. The `LoginActivity` sets up anonymous login as well as Facebook and Google authentication flows.

- **Local storage and sync:** Entries, users, comments and projects are stored in a SQLite database via `DatabaseHelper`. The tables define columns for each entity: `PIEntryTable` stores image URL, title, snippet, category, location, user and timestamp; `CommentsTable` stores comment text, rating and author; `UserTable` stores user IDs, names and rating counts; and `ProjectTable` stores project metadata including name, description, questions and coordinates. PHP scripts in `src/main/assets` (e.g. `AddPublicInput.php`, `GetAllPublicInput.php`) mediate synchronisation with a remote server.

- **Image caching:** A memory cache in `Universals` stores downloaded images; the class also provides helper methods for scaling and downloading bitmaps.

- **Offline support:** The local database allows the app to function without network connectivity. Data is synced with the server when connectivity is available.

## Project Structure

The repository is organised as an Android Gradle project:

- `build.gradle` (root) – top‑level build file with common configuration for all modules.
- `app/` – Android application module containing source code and resources.
  - `build.gradle` – module build file. It sets `compileSdkVersion`, `minSdkVersion` and declares dependencies such as Google Maps, Firebase, Facebook SDK, Parse and Glide.
  - `src/main/java/com/pdceng/www/desirepaths/` – Java source code, including:
    - `MapActivity` – main map screen for viewing and interacting with public input.
    - `PublicInputAddActivity` – UI for adding new public input with photo upload.
    - `PublicInputViewActivity` – detailed view of a specific public input item.
    - `LoginActivity` – manages Facebook/Google/anonymous authentication.
    - `DatabaseHelper` – SQLite helper that creates and manages tables, inserts and retrieves data and handles synchronisation.
    - Model and table classes (`Project`, `PublicInput`, `User`, `PIEntryTable`, `CommentsTable`, `UserTable`, `ProjectTable`) that define the schema for each entity.
    - `CommentsAdapter` – adapter for displaying comments in a list.
    - Various helper classes (`Universals`, `MyItem`, etc.) for caching, mapping and UI utilities.
  - `src/main/res/` – Android resources (layouts, drawables, strings). Layouts define activities such as `activity_add_public_input.xml` and `activity_public_input_view.xml`.
  - `src/main/assets/` – Assets bundled into the app. Includes PHP scripts (`AddPublicInput.php`, `GetAllPublicInput.php`, `FilterPublicInput.php`, `create_web_service.php`) used by the app’s synchronisation layer.
  - `libs/` – Third‑party JARs such as `simpleftp.jar` and `okhttp-3.8.1.jar`.
  - `google-services.json` – Firebase configuration file (contains keys specific to your project and is not checked into version control).

## Building and Running

Prerequisites: Android Studio 3.x or later, JDK 8 and Android SDK platform 25. The app is configured to compile with `compileSdkVersion` 25 and requires a minimum SDK of 21.

1. **Clone the repository:**
   ```
   git clone https://github.com/awlondon/DesirePaths.git
   ```
2. **Import into Android Studio:** Choose *Open an existing Android Studio project* and select the DesirePaths directory.
3. **Configure Firebase/Parse:** Provide a valid `google-services.json` file for Firebase in `app/`. Edit `res/values/strings.xml` to include your Parse application keys and any other secrets referenced by the code (e.g. `parse_app_id` used during initialisation).
4. **Build & run:** Use Android Studio’s *Run* button or execute `./gradlew assembleDebug` to build the APK.

## Data Model

The app uses a local SQLite database to cache data and a remote server for synchronisation. Key tables include:

| Table | Purpose | Key Columns (see code) |
| --- | --- | --- |
| PIEntryTable | Stores each public input entry | `url`, `title`, `snippet`, `sentiment` (idea/comment/warning), `latitude`, `longitude`, `user`, `timestamp`, `project_id` |
| CommentsTable | Stores comments on public input | `pientry_id` (foreign key), `comment` text, `rating`, `social_media_id`, `timestamp` |
| UserTable | Stores user accounts and rating counts | `social_media_id`, `photo_url`, `name`, `registered_timestamp`, `positive_ratings`, `negative_ratings`, `pi_agree`, `pi_disagree` |
| ProjectTable | Stores project definitions | `name`, `location`, `description`, `questions`, `website`, `latitude`, `longitude`, `zoom` |

These models are represented by Java classes (`PublicInput`, `User`, `Project`) and corresponding table classes that define constants for column names and lists of fields to insert into SQLite.

## Dependencies

The project relies on several libraries, declared in `app/build.gradle`:

- Google Play Services: Maps, Auth and Location APIs for map display, login and geolocation.
- Google Maps Utils: For clustering markers and heatmaps.
- Firebase: Core and Authentication for user accounts.
- Facebook Android SDK: Enables Facebook login.
- Parse Android SDK: Back-end storage and push notifications.
- Glide: Image loading and caching.
- Gson: JSON parsing for model classes.
- Commons Net/SimpleFTP: Uploading images via FTP.
- Android Support libraries: AppCompat, Design, CardView, RecyclerView, ConstraintLayout etc. See `app/build.gradle` for the full list.

## Contributing

Contributions and bug reports are welcome! If you find an issue or have a feature request, please open an issue on GitHub. Pull requests should:

1. Fork the repository and create your feature branch.
2. Include clear commit messages and comments where necessary.
3. Follow the existing code style and organise new classes within the appropriate packages.
4. Update documentation as needed.

## License

This repository does not include an explicit license. If you intend to use or modify the code, please contact the repository owner.

