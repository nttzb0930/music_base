# Music Base: Features And Workflow

Tài liệu này mô tả đầy đủ các tính năng và luồng vận hành hiện có trong ứng dụng `Music Base`, bám theo codebase Android hiện tại.

## 1. Mục tiêu sản phẩm

`Music Base` là ứng dụng nghe nhạc Android viết bằng Kotlin + Jetpack Compose, tập trung vào 3 nhóm năng lực chính:

- Trải nghiệm nghe nhạc hiện đại cho người dùng cuối.
- Hệ thống tài khoản, thư viện cá nhân và đồng bộ dữ liệu qua API.
- Bộ công cụ quản trị để ingest, quản lý và kiểm soát nội dung.

## 2. Các nhóm người dùng

### 2.1 Guest

- Có thể vào ứng dụng, duyệt giao diện và dùng một phần trải nghiệm khám phá.
- Không có thư viện cá nhân, playlist riêng, lịch sử cá nhân hoặc dữ liệu đồng bộ.
- Được dẫn vào luồng `Login` hoặc `Register` khi cần tính năng cá nhân hóa.

### 2.2 User đã đăng nhập

- Có đầy đủ khả năng nghe nhạc, quản lý thư viện và dữ liệu cá nhân.
- Có thể like bài hát, tạo playlist, theo dõi nghệ sĩ, xem lịch sử nghe và insights.
- Có thể chỉnh sửa hồ sơ, đổi mật khẩu và gửi yêu cầu bài hát bị thiếu.

### 2.3 Admin

- Kế thừa toàn bộ khả năng của user thường.
- Có thêm `Admin Mode` để truy cập Dashboard, Ingest và Database.
- Có thể upload track, sửa metadata, xóa track, quản lý artist và theo dõi số liệu hệ thống.

## 3. Kiến trúc tính năng tổng thể

Ứng dụng được tổ chức theo `MVVM + Repository Pattern + Unidirectional Data Flow`.

### 3.1 Tầng UI

- `MainActivity` điều phối toàn bộ điều hướng kiểu single-activity.
- Các màn hình chính gồm: `Home`, `Search`, `Library`, `Profile`, `Player`, `Settings`.
- Các overlay detail gồm: `Album Detail`, `Artist Detail`, `Playlist Detail`, `Liked Songs`, `Playback History`.

### 3.2 Tầng ViewModel

- `AuthViewModel` quản lý trạng thái xác thực, hồ sơ và thao tác tài khoản.
- `MusicViewModel` quản lý dữ liệu âm nhạc, thư viện, lịch sử, insights, admin operation và toast state.

### 3.3 Tầng dữ liệu

- `AuthRepository` xử lý đăng nhập, đăng ký, refresh token, hồ sơ cá nhân.
- `MusicRepository` xử lý tracks, artists, albums, playlists, likes, playback, admin endpoints.
- `StashRepository` xử lý dữ liệu dashboard cho admin.

### 3.4 Tầng player

- `MusicPlayerManager` là playback engine trung tâm.
- Dùng `MediaPlayer`, `MediaSessionCompat`, notification playback và background service.
- State player được expose qua `StateFlow` để đồng bộ UI theo thời gian thực.

## 4. Authentication và bảo mật

### 4.1 Cơ chế xác thực

- Sử dụng `JWT access token` và `refresh token`.
- Token được lưu qua `TokenManager`.
- `AuthInterceptor` tự chèn access token vào request cần xác thực.
- `TokenAuthenticator` tự làm mới token khi access token hết hạn.

### 4.2 Luồng khởi động ứng dụng

1. App mở lên và tạo `AuthViewModel`.
2. `checkAuthStatus()` gọi `getMe()`.
3. Nếu token hợp lệ:
   - App vào trạng thái `Authenticated`.
   - Nạp playlists, liked tracks, playback history, followed artists.
4. Nếu token không hợp lệ:
   - App vào trạng thái `Unauthenticated`.
   - Hiển thị gateway `Login/Register`.

### 4.3 Luồng đăng nhập và đăng ký

- `Login` gửi email/password lên API.
- Thành công thì lưu token và gọi lại `getMe()`.
- `Register` gửi thông tin đăng ký, sau đó chuyển về flow phù hợp.
- `Logout` xóa token, reset phiên và dừng phát nhạc đang chạy.

## 5. Trải nghiệm người dùng chính

### 5.1 Home

Màn hình Home là trung tâm khám phá nội dung:

- Tải suggested tracks từ API và lọc theo tiêu chí có thể phát.
- Hiển thị ranking tracks.
- Hiển thị top albums và top artists.
- Có phần `Continue Listening` lấy từ playback history, loại trùng theo track gần nhất.
- Cho phép phát nhanh, mở menu track, like nhanh, follow artist nhanh.
- Có `Show All` để tải thêm theo phân trang.

### 5.2 Search

- Tìm kiếm đồng thời track và artist theo query.
- Debounce/cancel request cũ bằng `searchJob`.
- Hiển thị loading state rõ ràng khi query thay đổi.
- Cho phép mở player, like bài, thêm vào playlist, share và mở artist detail.
- Có luồng `syncTrackFromUrl` để gửi URL bài hát bị thiếu lên hệ thống.

Lưu ý:

- Trong code hiện tại, `syncTrackFromUrl()` ở `MusicViewModel` đang là logic mô phỏng UI flow, chưa gọi API thật.

### 5.3 Library

Thư viện cá nhân hỗ trợ:

- Xem `Liked Songs`.
- Xem danh sách playlist cá nhân.
- Xem danh sách nghệ sĩ đang follow.
- Lọc theo `All`, `Playlists`, `Artists`, `Albums`.
- Sắp xếp theo `Recents`, `Alphabetical`, `Creator`.
- Chuyển đổi giữa `list view` và `grid view`.
- Tạo playlist mới.
- Sửa playlist.
- Xóa playlist.
- Share playlist ra ngoài app.

### 5.4 Profile

Profile dành cho user đã đăng nhập gồm:

- Thông tin người dùng.
- Số lượng nghệ sĩ đang follow.
- `Listening Insights`.
- `Top Artists` lấy từ lịch sử nghe thực tế.
- Public playlists của user.
- Shortcut tới `Playback History`.

Nếu chưa đăng nhập, tab `Profile` hoạt động như cổng vào `Settings + Login/Register`.

### 5.5 Settings

Settings hiện có các nhóm:

- Quản lý hồ sơ: username, email.
- Đổi mật khẩu.
- Đăng xuất.
- `Track Request Portal` để gửi yêu cầu đồng bộ bài hát thiếu.
- Tùy chọn giao diện/settings hiển thị về chất lượng âm thanh và data saver.
- Thông tin version, terms, privacy.

## 6. Trải nghiệm player

### 6.1 Khả năng playback

- Phát bài từ Home, Search, Album, Artist, Playlist, Liked Songs, Dashboard admin.
- Có `play/pause`, `next`, `previous`, `seek`.
- Có `repeat off`, `repeat all`, `repeat one`.
- Có `shuffle`.
- Có mini-player và full-screen player.

### 6.2 Smart shuffle

Shuffle không chỉ random đơn giản:

- Giữ track hiện tại ở đầu hàng đợi.
- Tách track đã like và track thường thành các pool riêng.
- Trộn lại nhưng cố tránh phát liên tiếp nhiều bài cùng nghệ sĩ.
- Khi tắt shuffle, queue quay về thứ tự ban đầu.

### 6.3 Playback nền

- Hỗ trợ phát nhạc nền qua `MusicService`.
- Có `MediaSessionCompat` để nhận control từ hệ thống.
- Hiển thị notification playback.
- Hỗ trợ control từ lock screen, notification và thiết bị Bluetooth tương thích.

### 6.4 Đồng bộ trạng thái player

- `currentTrack`, `isPlaying`, `currentPosition`, `duration`, `repeatMode`, `isShuffle` đều được phát qua `StateFlow`.
- Timer cập nhật vị trí phát mỗi 500ms để UI mượt hơn.

## 7. Social và hành động nội dung

### 7.1 Track actions

Từ nhiều màn hình, user có thể:

- Like / unlike bài hát.
- Thêm bài vào playlist.
- Share bài hát.
- Mở artist liên quan.

### 7.2 Artist actions

- Follow artist.
- Unfollow artist.
- Mở artist detail để xem danh sách track.

### 7.3 Playlist actions

- Tạo playlist.
- Cập nhật tên, mô tả, trạng thái public/private.
- Xóa playlist.
- Thêm track vào playlist.
- Xóa track khỏi playlist.
- Mở playlist detail và phát toàn bộ.

## 8. Playback history và insights

### 8.1 Ghi nhận lịch sử nghe

Hệ thống không spam request liên tục:

- Chỉ ghi playback khi có đủ tiến trình nghe đáng kể.
- Mặc định chỉ ghi lại khi track đổi hoặc tăng thêm khoảng 10 giây.
- Không ghi khi mới phát chưa đủ ngưỡng tối thiểu.

### 8.2 Lịch sử nghe

- Lấy từ API `playback/history`.
- Nhóm theo ngày.
- Có tìm kiếm trong lịch sử.
- Có xóa từng bài khỏi lịch sử.
- Có xóa toàn bộ lịch sử.

### 8.3 Insights sinh từ lịch sử

Từ playback history, app tính:

- `Listening streak`: số ngày nghe liên tục.
- `Preferred time of day`: khoảng thời gian nghe nhiều nhất.
- `Weekly stats`: tổng thời gian nghe 7 ngày gần nhất.
- `Top artists from history`: nghệ sĩ được nghe nhiều nhất.
- `Continue Listening`: danh sách track gần đây nhất, loại trùng.

## 9. Quản lý nội dung dành cho admin

Khi tài khoản có `isAdmin = true`, app bật được `Admin Mode`.

### 9.1 Admin Dashboard

Tab `Command` hiển thị:

- `Overview` tổng quan hệ thống.
- `Recent activity` cho user, artist, track mới.
- `Top tracks`.
- Refresh dashboard theo yêu cầu.
- Có thể mở track từ dashboard để nghe thử nhanh.

### 9.2 Admin Ingest

Tab `Ingest` hỗ trợ đưa nội dung mới vào hệ thống:

- Upload theo metadata + file audio local.
- Upload theo metadata + nguồn YouTube.
- Liên kết với `artistId`, `albumId`, thumbnail, video id, source type.
- Sau khi ingest thành công sẽ refresh dashboard và danh sách track admin.

### 9.3 Admin Database

Tab `Database` hỗ trợ vận hành nội dung:

- Load danh sách track toàn hệ thống.
- Load danh sách artist toàn hệ thống.
- Tìm kiếm nội dung.
- Chỉnh sửa metadata track.
- Xóa track.
- Tạo artist.
- Cập nhật artist.
- Xóa artist.
- Đồng bộ artist theo YouTube channel.

## 10. Workflow chi tiết theo vai trò

### 10.1 Workflow của guest

1. Mở app.
2. Xem Home hoặc đi tới Search.
3. Khi cần lưu dữ liệu cá nhân, app điều hướng sang Login/Register.
4. Sau đăng nhập thành công, dữ liệu cá nhân được nạp lại tự động.

### 10.2 Workflow nghe nhạc của user

1. User mở Home, Search, Album, Artist, Playlist hoặc Liked Songs.
2. Chọn một track.
3. App lấy track detail đầy đủ nếu cần, đặc biệt là `audioUrl`.
4. `MusicPlayerManager` set queue và bắt đầu phát.
5. Full player hoặc mini player đồng bộ trạng thái tức thời.
6. Trong lúc nghe, app ghi playback theo chu kỳ phù hợp.
7. Lịch sử và insights được cập nhật ở những lần tải sau.

### 10.3 Workflow quản lý thư viện

1. User like bài hát trong Home/Search/Player/History.
2. Track xuất hiện trong `Liked Songs`.
3. User có thể tạo playlist mới.
4. User thêm track vào playlist bất kỳ từ action sheet hoặc player.
5. Playlist có thể sửa, xóa hoặc share từ Library/Playlist Detail.

### 10.4 Workflow theo dõi nghệ sĩ

1. User mở artist card hoặc artist detail.
2. Thực hiện `Follow`.
3. Nghệ sĩ xuất hiện trong Library và Profile.
4. User có thể `Unfollow` bất kỳ lúc nào.

### 10.5 Workflow yêu cầu bài hát thiếu

1. User không tìm thấy bài hát mong muốn.
2. Mở `Track Request Portal` ở Settings hoặc Search flow.
3. Dán URL nguồn.
4. App hiển thị trạng thái xử lý và toast feedback.

Lưu ý:

- Trong code hiện tại, workflow này đang được mô phỏng trên UI và chưa dùng repository call thật.

### 10.6 Workflow admin ingest

1. Admin bật `Admin Mode`.
2. Vào tab `Ingest`.
3. Chọn nguồn dữ liệu: local file hoặc YouTube metadata.
4. Điền metadata bắt buộc.
5. Gửi request upload.
6. Khi thành công:
   - Hiển thị toast.
   - Refresh dashboard.
   - Refresh danh sách track admin.

### 10.7 Workflow admin database

1. Admin mở tab `Database`.
2. Tải danh sách track và artist.
3. Tìm track/artist cần xử lý.
4. Chỉnh metadata hoặc xóa nội dung.
5. Hệ thống refresh lại danh sách và dashboard để phản ánh thay đổi.

## 11. Tích hợp API hiện có

Ứng dụng đang tích hợp các nhóm API sau:

Tổng số endpoint hiện đang được khai báo và tích hợp trong app là `49 API`, gồm:

- `AuthApiService`: 8 endpoint
- `MusicApiService`: 38 endpoint
- `StashApiService`: 3 endpoint

- Auth: register, login, logout, refresh, getMe, updateProfile, changePassword.
- Tracks: list, detail, ranking, tăng views, audio tracks.
- Artists: list, detail, tracks theo artist, tạo/sửa/xóa, sync artist.
- Albums: list, detail, album theo artist.
- Playlists: tạo, danh sách của tôi, detail, sửa, xóa, thêm/xóa track.
- Likes: toggle like, danh sách liked tracks, clear all.
- Playback: record listen, history, xóa từng item, clear history.
- Admin tracks: upload, update metadata, delete.
- Stash dashboard: overview, recent, top tracks.

## 12. Các giới hạn hiện tại cần biết

Những điểm dưới đây đang tồn tại trong code hiện tại và nên được ghi rõ để tránh hiểu sai phạm vi:

- `syncTrackFromUrl()` trong `MusicViewModel` đang là luồng demo bằng `delay`, chưa gọi API thật dù repository đã có endpoint.
- `uploadAudio(title, artist)` trong `MusicViewModel` cũng đang là luồng mô phỏng.
- Một số text trong UI còn pha trộn Anh - Việt.
- Tài liệu cũ bị lỗi encoding; file này đã được viết lại để dùng UTF-8 chuẩn.

## 13. Tóm tắt nhanh phạm vi tính năng hiện có

`Music Base` hiện đã có:

- Auth hoàn chỉnh với token flow.
- Home khám phá nội dung và phân trang.
- Search track và artist.
- Player nền với notification và media session.
- Liked Songs, playlist, follow artist, library cá nhân.
- Playback history và listening insights.
- Settings cho tài khoản và request portal.
- Admin dashboard, ingest và database management.

Đây là bộ tính năng đủ để ứng dụng hoạt động như một music platform client có cả user flow lẫn admin operation flow.
