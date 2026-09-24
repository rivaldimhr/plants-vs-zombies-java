# Plants vs Zombies (Java)

Game tower defense bergaya *Plants vs Zombies* yang dibuat dengan Java Swing, tanpa library game eksternal.

Pemain memilih 6 tanaman ke dalam deck, lalu menanamnya di halaman (darat dan kolam) untuk menahan zombie. Jika ada zombie yang sampai ke rumah, pemain kalah. Pemain menang jika bertahan sampai waktu habis (240 detik) dan semua zombie sudah mati.

## Cara Menjalankan

### 1. Persiapan

Butuh **JDK 17** atau lebih baru (bukan hanya JRE, karena perlu `javac`). Cek di terminal:

```
java -version
javac -version
```

Kalau perintah tidak dikenali, install JDK (misalnya [Microsoft Build of OpenJDK](https://learn.microsoft.com/java/openjdk/download) atau [Adoptium](https://adoptium.net)) lalu pastikan folder `bin`-nya ada di `PATH`.

### 2. Ambil kode

```
git clone https://github.com/rivaldimhr/plants-vs-zombies-java.git
cd plants-vs-zombies-java
```

Semua perintah di bawah dijalankan dari **folder yang berisi `src/`, `image/`, dan `run.bat`** (folder README ini). Gambar dibaca relatif terhadap folder ini, jadi game yang dijalankan dari folder lain akan tampil tanpa gambar.

### 3. Jalankan game

| | Windows | Linux / macOS / Git Bash |
| --- | --- | --- |
| Main game | double-click `run.bat` (atau ketik `run.bat` di CMD) | `./run.sh` |
| Unit test | `test.bat` | `./test.sh` |

Script akan compile semua kode ke folder `bin/` lalu membuka jendela game.

### 4. Menjalankan manual (tanpa script)

**Git Bash / Linux / macOS**
```
javac -encoding UTF-8 -d bin $(find src -name '*.java')
java -cp bin game.Game
```

**Windows CMD**
```
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
java -cp bin game.Game
```

**Windows PowerShell**
```
Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName } | Set-Content sources.txt
javac -encoding UTF-8 -d bin "@sources.txt"
java -cp bin game.Game
```

**Unit test manual** (Git Bash)
```
javac -encoding UTF-8 -cp lib/junit-platform-console-standalone-1.10.2.jar -d bin-test $(find src test -name '*.java')
java -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path bin-test --scan-class-path
```

### 5. Menjalankan dari IDE

- **IntelliJ IDEA**: *Open* folder ini. Tandai `src` sebagai *Sources Root* dan `test` sebagai *Test Sources Root* (klik kanan folder → *Mark Directory as*). Tambahkan `lib/junit-platform-console-standalone-1.10.2.jar` sebagai library. Jalankan `game.Game`, dan pastikan *Working directory* di Run Configuration adalah folder ini.
- **VS Code** (Extension Pack for Java): buka folder ini, lalu klik *Run* di atas `main` pada `src/game/Game.java`. Kalau gambar tidak muncul, set `"cwd": "${workspaceFolder}"` di `launch.json`.

### Troubleshooting

| Masalah | Solusi |
| --- | --- |
| Jendela muncul tapi gambar kosong / abu-abu, terminal menampilkan `Gambar tidak ditemukan: ...` | Game dijalankan dari folder yang salah. Jalankan dari folder README ini |
| `'javac' is not recognized` | JDK belum terinstall atau belum ada di `PATH` |
| `error: invalid source release` / fitur Java tidak dikenal | Versi JDK di bawah 17 |
| Sprite ingin diganti | Ubah gambar di `image/`, lalu jalankan `python tools/prepare_sprites.py` (butuh `pip install pillow`) untuk membuat ulang `image/sprites/` |

## Cara Bermain

1. **Main Menu**: *Play*, *Plants List*, *Zombies List*, *Help*, *Quit*.
2. **Inventory**: pilih 6 tanaman untuk deck, lalu *Play*. *Clear* mengosongkan deck.

| Aksi | Cara |
| --- | --- |
| Masukkan / keluarkan tanaman | Klik kartu di inventory |
| **Swap** posisi di deck | Klik kartu deck (border kuning), lalu klik kartu deck lain |
| **Ganti** kartu deck dengan tanaman lain | Klik kartu deck, lalu klik tanaman di inventory |
| Keluarkan dari deck | Klik kanan kartu deck |
| **Drag and drop** | Seret kartu deck ke slot lain (swap) atau ke luar baris deck (keluarkan); seret tanaman inventory ke slot deck (ganti / isi slot kosong) |

   Urutan deck menentukan tombol 1–6 saat bermain (nomornya tampil di pojok kartu).
3. **Bermain**:

| Aksi | Mouse | Keyboard |
| --- | --- | --- |
| Menanam | Klik kartu, lalu klik tile | Panah + Enter pilih tile, lalu 1–6 |
| Mencabut tanaman | Klik sekop, lalu klik tanaman | Pilih tile, lalu 7 |
| Batal memilih | Klik kanan | – |
| Pause | Tombol *Pause* | P / Esc |

- Saat memilih kartu, tile di bawah mouse menampilkan **bayangan tanaman**: hijau = bisa ditanam, merah = tidak bisa.
- Kartu yang sedang **cooldown** digelapkan dengan sisa detik; kartu yang **sun-nya tidak cukup** berwarna abu-abu.
- Tanaman dan zombie yang terkena damage menampilkan **bar HP**; Wall-nut dan Tall-nut terlihat retak.
- Menu pause berisi *Lanjut* dan *Menu Utama*.

Aturan tanam:
- Kolom paling kiri (rumah) dan paling kanan (tempat zombie muncul) tidak bisa ditanami.
- Baris 3–4 adalah kolam. Di kolam hanya bisa ditanam tanaman air (Lily Pad, Tangle Kelp).
- Tanaman darat bisa ditanam di kolam jika di atas Lily Pad.
- Menanam butuh sun yang cukup, dan setiap slot deck punya cooldown setelah dipakai.

## Aturan Waktu

Game berjalan 60 tick per detik.

| Hal | Nilai |
| --- | --- |
| Durasi game | 240 detik |
| Siang / malam | Siklus 200 detik: 0–99 siang, 100–199 malam (transisi fade 20 detik) |
| Sun awal | 50 |
| Sun otomatis | +25 tiap 5–10 detik, **hanya siang hari** |
| Zombie mulai muncul | Detik 20 (20 detik pertama untuk persiapan) |
| Peluang zombie muncul | 30% tiap detik, maksimal 10 zombie di papan |
| **Huge wave** | Detik 200–239: langsung 1 zombie di tiap baris, lalu peluang 70% tiap detik, maksimal 15 zombie |
| Zombie berhenti muncul | Detik 240 |
| Kecepatan zombie | ±5,5 px/detik (±11 detik per tile) |

## Tanaman

Stat mengikuti dokumentasi di layar *Plants List*.

| Tanaman | Cost | HP | Damage | Serang tiap | Range | Cooldown | Keterangan |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Sunflower | 50 | 100 | – | – | – | 10 s | +25 sun tiap 3 detik |
| Peashooter | 100 | 100 | 25 | 4 s | 1 baris | 10 s | |
| Repeater | 200 | 100 | 25 ×2 | 2 s | 1 baris | 10 s | Menembak 2 kacang sekaligus |
| Snow Pea | 175 | 100 | 25 | 4 s | 1 baris | 10 s | Memperlambat zombie 3 detik (jalan & makan 2x lebih lambat) |
| Puff-shroom | 0 | 100 | 15 | 4 s | 3 tile | 7 s | **Jamur: tidur saat siang**, aktif saat malam |
| Wall-nut | 50 | 1000 | – | – | – | 20 s | Penahan |
| Tall-nut | 125 | 2000 | – | – | – | 30 s | Penahan, **tidak bisa dilompati** Pole Vaulting |
| Squash | 50 | 100 | 5000 | – | 1 tile | 20 s | Sekali pakai |
| Lily Pad | 25 | 100 | – | – | – | 10 s | Tanaman air, alas tanaman darat |
| Tangle Kelp | 25 | 100 | 2000 | – | 1 tile | 15 s | Tanaman air, sekali pakai |

## Zombie

Stat mengikuti dokumentasi di layar *Zombies List*. Semua zombie menyerang 100 damage tiap 1 detik.

| Zombie | HP | Area | Muncul mulai | Kemampuan |
| --- | --- | --- | --- | --- |
| Normal Zombie | 125 | Darat | detik 20 | – |
| Conehead Zombie | 250 | Darat | detik 20 | – |
| Newspaper Zombie | 200 | Darat | detik 30 | Koran menahan 100 damage pertama; setelah koran hancur, jalan & makan **2x lebih cepat** |
| Pole Vaulting Zombie | 175 | Darat | detik 40 | Berlari 2x lebih cepat dan **melompati tanaman pertama** (kecuali Tall-nut) |
| Buckethead Zombie | 300 | Darat | detik 60 | – |
| Football Zombie | 300 | Darat | detik 90 | Berlari 2x lebih cepat |
| Ducky Tube Zombie | 100 | Kolam | detik 20 | – |
| Ducky Tube Conehead Zombie | 250 | Kolam | detik 30 | – |
| Snorkel Zombie | 100 | Kolam | detik 40 | **Menyelam**: tidak bisa kena peluru sampai muncul untuk memakan tanaman |
| Dolphin Rider Zombie | 175 | Kolam | detik 60 | ±2x lebih cepat, tanaman pertama yang ditemui **langsung mati** |

## Struktur Project

```
src/
  game/                     layar, game loop, dan aturan permainan
    Game                    main class: JFrame + game loop (60 UPS, 120 FPS), menyimpan layar aktif
    GameScreen              JPanel tempat layar aktif digambar
    States, ScreenMethod    daftar layar & interface tiap layar
    BaseScreen              kelas dasar layar menu (background + tombol)
    CatalogScreen           kelas dasar Plants List & Zombies List
    Menu, Help, GameOver, Win, PlantsList, ZombiesList, Inventory
    GameLevel               layar permainan: input, HUD, pause
    Board                   state satu permainan: tanaman, zombie, peluru, sun, waktu, aturan tanam
    ZombieSpawner           aturan kemunculan zombie & huge wave
    Deck, Sun               deck 6 tanaman + cooldown, jumlah sun
    Assets, Toast, MyButton, KeyHandler, MyMouseListener
  entity/
    Entity, Updatable       kelas dasar Plant & Zombie, interface update tiap tick
    plant/                  Plant, ShooterPlant, InstantKillPlant, PlantType + 10 tanaman
    zombie/                 Zombie, ZombieType + 10 zombie
    projectile/             Bullet, PeaBullet, SlowBullet, PuffBullet
test/                       unit test JUnit 5 (Board, Deck, spawner, tanaman, zombie, stat)
lib/                        JUnit (junit-platform-console-standalone)
image/                      aset gambar asli
image/sprites/              sprite yang sudah di-crop (dibuat oleh tools/prepare_sprites.py)
tools/                      script pendukung
```

`image/sprites/` dibuat dari gambar asli dengan `python tools/prepare_sprites.py` (butuh Pillow). Script ini memotong kanvas kosong dan memperbaiki GIF yang tampil berkotak hitam di Java. Jalankan ulang jika gambar asli diganti.

## Konsep OOP yang Digunakan

- **Inheritance**
  - `Entity` → `Plant` / `Zombie`
  - `Plant` → `ShooterPlant` / `InstantKillPlant` → tanaman konkret
  - `Bullet` → `PeaBullet` / `SlowBullet` / `PuffBullet`
  - `BaseScreen` → `CatalogScreen` → `PlantsList` / `ZombiesList`
- **Abstraction**: `Entity`, `Plant`, `Zombie`, `ShooterPlant`, `InstantKillPlant`, `Bullet`, `BaseScreen`, dan `CatalogScreen` adalah abstract class.
- **Interface**: `Updatable` (update tiap tick) dan `ScreenMethod` (render & input tiap layar, dengan default method).
- **Polymorphism**
  - `Board` memanggil `update()` pada semua tanaman, zombie, dan peluru tanpa tahu jenis konkretnya.
  - Kemampuan khusus dibuat dengan override: `PoleVaultingZombie.update`, `NewspaperZombie.takeDamage`, `SnorkelZombie.isTargetable`, `SlowBullet.onHit`, `WallNut.getImagePath`, `PuffShroom.isNocturnal`.
- **Encapsulation**: state permainan ada di `Board` (bukan variabel `static`), atribut entity `protected`/`private` dan diakses lewat getter/setter.
- **Design pattern**
  - *Factory*: `PlantType` dan `ZombieType` membuat objek dari enum.
  - *Template Method*: `Plant.update()` → `act()`, `ShooterPlant.act()` → `shoot()`, `BaseScreen.render()` → `renderContent()`.
  - *State*: `States` dan layar aktif di `Game`.

## Asal Project

Project ini berawal dari Tugas Besar kelompok mata kuliah Pemrograman Berorientasi Objek (IF2212) di [rivaldimhr/TUBES-OOP-IF2212](https://github.com/rivaldimhr/TUBES-OOP-IF2212), lalu dilanjutkan sebagai project pribadi: struktur kode ditulis ulang, bug diperbaiki, dan fitur ditambah (semua zombie & kemampuannya, huge wave, kontrol mouse, drag and drop deck, pause, unit test).

Aset gambar *Plants vs Zombies* adalah milik PopCap Games / Electronic Arts dan digunakan untuk keperluan belajar (non-komersial).
