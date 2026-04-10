# Music Base Android
Hệ thống ứng dụng nghe nhạc Android hiện đại — bao gồm Player engine, UI Components (Compose), và API Integration.

## Tech Stack
### App
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Modern Declarative UI)
*   **Asynchronous Content**: Kotlin Coroutines & Flow
*   **Dependency Injection**: Manual DI (Scalable architecture)
*   **Networking**: Retrofit & OkHttp
*   **Local Storage**: Jetpack DataStore (Encrypted for sensitive data)
*   **Media Handling**: Android MediaPlayer / MediaSessionCompat
*   **Image Loading**: Coil

### Shared Packages (Logic layering)
*   **`data/model`**: Định nghĩa Domain models và POJO classes sử dụng cho cả API và Database.
*   **`data/api`**: Hạ tầng kết nối mạng, interceptors (AuthInterceptor) và Retrofit services.
*   **`ui/theme`**: Hệ thống design system (colors, typography, shapes) dựa trên Material Design 3.
*   **`ui/components`**: Thư viện UI widgets dùng chung (Buttons, Cards, Lists).

## Project Structure
```text
music_base/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/music_base/
│   │   │   ├── data/           # Data layer (Repositories, API, Local storage)
│   │   │   │   ├── api/        # Retrofit services & Network logic
│   │   │   │   ├── local/      # DataStore (User settings, Auth tokens)
│   │   │   │   ├── model/      # Data models
│   │   │   │   ├── player/     # Cơ chế phát nhạc Core Engine (Media3)
│   │   │   │   └── repository/ # Nguồn dữ liệu duy nhất (Single Source of Truth)
│   │   │   ├── ui/             # UI layer (Compose)
│   │   │   │   ├── components/ # Widgets tái sử dụng
│   │   │   │   ├── screens/    # Full-screen Composables (Home, Player, Library)
│   │   │   │   ├── theme/      # Material Design 3 configuration
│   │   │   │   └── viewmodel/  # Quản lý UI State & Logic
│   │   │   ├── MainActivity.kt # Entry point activity
│   │   │   └── MusicApp.kt     # Application class
│   │   └── res/                # Android resources (Drawables, XML)
├── gradle/                     # Build scripts & Wrapper
├── build.gradle.kts           # Root configuration
└── settings.gradle.kts        # Module management
```

## Getting Started
### Prerequisites
*   Android SDK ≥ 35 (Compile SDK)
*   JDK 17 hoặc cao hơn (Khuyên dùng JDK 17 cho các tính năng mới nhất)
*   Android Studio Ladybug (2024.2.1) hoặc mới hơn

### Setup
1.  **Clone the repository**
    ```bash
    git clone https://github.com/nttzb0930/music_base.git
    cd music_base
    ```

2.  **Environment Setup**
    *   Mở dự án bằng **Android Studio**.
    *   Đợi Gradle đồng bộ hóa (Sync project).
    *   Cấu hình `API_BASE_URL` trong file cấu hình (nếu có).

3.  **Build & Run**
    *   Chọn thiết bị (Emulator hoặc Physical device).
    *   Click **Run** (biểu tượng Play) hoặc sử dụng lệnh:
        ```bash
        ./gradlew assembleDebug
        ```

## Core Functionalities
Ứng dụng được thiết kế để mang lại trải nghiệm nghe nhạc liên tục và mượt mà:

*   **Background Playback**: Cho phép phát nhạc dưới nền thông qua `MediaPlayer` duy trì trạng thái bằng `MediaSessionCompat` và System Notification.
*   **Notification Controls**: Điều khiển nhạc trực tiếp từ thanh thông báo và màn hình khóa (Play/Pause, Skip, Seek) thông qua BroadcastReceiver.
*   **Smart Playback Queue**: Hỗ trợ lặp lại (Repeat) và phát ngẫu nhiên (Shuffle) chuyên sâu, giữ nguyên context phát nhạc.


## API Integration
Ứng dụng kết nối với **Music Platform REST API** để quản lý dữ liệu và trải nghiệm người dùng.

*   **Base URL**: `https://api-music-player.up.railway.app/api/v1`
*   **Swagger Documentation**: [View API Docs](https://api-music-player.up.railway.app/api/docs)
*   **Tài liệu dự án**: [music-platform-api-docs.md](music-platform-api-docs.md)

### Core API Modules
*   **Authentication**: Đăng ký, đăng nhập và xử lý JWT flow (Access/Refresh token).
*   **Users**: Quản lý hồ sơ, danh sách bài hát yêu thích và tính năng Follow Artist.
*   **Music (Artists & Tracks)**: Lấy danh sách nghệ sĩ và bài hát đồng bộ từ YouTube.
*   **Playlists**: Tạo, quản lý và thêm nhạc vào danh sách phát cá nhân/cộng đồng.
*   **Audio**: Stream dữ liệu âm thanh trực tiếp với chất lượng cao và upload file audio.

## Architecture Patterns
*   **MVVM (Model-View-ViewModel)**: Tách biệt hoàn toàn Logic và UI, giúp code dễ test và bảo trì.
*   **Repository Pattern**: Quản lý tập trung các luồng dữ liệu, xử lý logic cache và network.
*   **UDF (Unidirectional Data Flow)**: State truyền xuống các Composable, và Events (user actions) truyền ngược lên ViewModel để xử lý.

## Data Processing
Hệ thống xử lý dữ liệu được thiết kế tối ưu để đảm bảo tính nhất quán và hiệu suất:

*   **Data Transformation**: Chuyển đổi dữ liệu từ API layer sang UI layer. Ví dụ: Chuyển `duration` (giây) sang `durationMs` (mili giây) cho ExoPlayer, format lượt xem (`viewCount`) sang định dạng K/M (1.2M, 500K).
*   **Fallback Strategy**: Tự động xử lý các trường hợp thiếu dữ liệu hình ảnh bằng các placeholder thông minh (YouTube HQ thumbnails cho bài hát, Picsum cho nghệ sĩ/album).
*   **Unified Error Handling**: Bóc tách lỗi từ JSON response của server để hiển thị thông báo tiếng Việt/tiếng Anh thân thiện cho người dùng.
*   **Interceptors & Security**: 
    *   **AuthInterceptor**: Tự động tiêm Token vào header cho các request cần xác thực.
    *   **TokenAuthenticator**: Tự động thực hiện luồng Refresh Token khi Access Token hết hạn, đảm bảo phiên làm việc không bị gián đoạn.
*   **Local Persistence**: Sử dụng Jetpack DataStore để lưu trữ an toàn các cài đặt người dùng và thông tin xác thực (kế thừa tính năng encryption).

### Playback History Processing
Hệ thống xử lý lịch sử nghe nhạc được xây dựng thông minh để cung cấp phân tích sâu (Insights):

*   **Smart Recording**: Cơ chế ghi nhận lịch sử (`recordPlayback`) tích hợp thuật toán chống spam, chỉ ghi nhận sau mỗi 10 giây hoặc khi thay đổi trạng thái phát đáng kể.
*   **Continue Listening**: Tự động trích xuất danh sách bài hát gần đây nhất (đã loại trùng) để người dùng có thể tiếp tục nghe nhanh từ màn hình chính.
*   **Listening Insights**:
    *   **Streak**: Tính toán số ngày nghe nhạc liên tục.
    *   **Active Hours**: Phân tích thời điểm nghe nhạc phổ biến trong ngày (Sáng, Chiều, Tối, Đêm).
    *   **Weekly Stats**: Thống kê tổng thời gian nghe nhạc trong 7 ngày gần nhất.
*   **Top Artists Aggregation**: Tự động tổng hợp và xếp hạng các nghệ sĩ được nghe nhiều nhất dựa trên lịch sử cá nhân.
*   **Date Grouping**: Dữ liệu lịch sử được nhóm theo ngày để dễ dàng theo dõi và quản lý.

## Media Player Implementation
Hệ thống phát nhạc lõi được xây dựng sử dụng **MediaPlayer** chuẩn của Android, tối ưu hóa qua các lớp xử lý custom:

*   **Smart Queue & Shuffle**: Thuật toán trộn bài (Shuffle) thông minh, tách riêng tỷ lệ bài Liked/Others và luân phiên nghệ sĩ (interleaving) để tránh phát trùng nghệ sĩ liên tiếp.
*   **State Synchronization**: Tối ưu bộ định thời (`fixedRateTimer`) liên tục đồng bộ tiến trình (current position) lên Rx/Flow layer để cập nhật UI mượt mà, không gặp độ trễ.
*   **Session Management**: Sử dụng `MusicNotificationManager` kết hợp `MediaSessionCompat` để duy trì quyền điều khiển bài hát trọn vẹn từ bên ngoài ứng dụng. Cập nhật cover art linh hoạt qua `Coil` ImageLoader.

## Command Reference
### Build Scripts
| Command | Description |
| :--- | :--- |
| `./gradlew build` | Build toàn bộ dự án |
| `./gradlew clean` | Xóa các file build cũ |
| `./gradlew assembleDebug` | Build file APK debug |
| `./gradlew test` | Chạy bộ Unit tests |
| `./gradlew connectedAndroidTest` | Chạy UI tests trên thiết bị |

## Git Conventions
Dự án áp dụng quy chuẩn **Conventional Commits** để quản lý lịch sử thay đổi sạch sẽ.

```text
<type>: <subject>
```

| Type | Mô tả |
| :--- | :--- |
| **feat** | Tính năng mới (New feature) |
| **fix** | Sửa lỗi (Bug fix) |
| **docs** | Cập nhật tài liệu kỹ thuật |
| **ui** | Thay đổi hoặc cải thiện giao diện |
| **refactor** | Cấu trúc lại mã nguồn mà không đổi tính năng |
| **test** | Thêm hoặc cập nhật bộ kiểm thử |
| **chore** | Các tác vụ bảo trì, build cấu hình |

## Roadmap
- [ ] **Offline Mode**: Tích hợp tính năng tải nhạc và nghe ngoại tuyến.
- [ ] **Real-time Lyrics**: Hiển thị lời bài hát chạy theo nhạc.
- [ ] **Localization**: Hỗ trợ đa ngôn ngữ (Tiếng Việt, Tiếng Anh).
- [ ] **Home Widget**: Điều khiển nhạc nhanh từ màn hình chính điện thoại.
