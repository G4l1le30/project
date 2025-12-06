# Presentasi Proyek: UMKMami - Memberdayakan Bisnis Lokal

---

### Pembukaan

Selamat pagi/siang Bapak Agi Putra Kharisma.

Perkenalkan, saya dari kelompok Hoshira, pada kesempatan ini akan mempresentasikan hasil proyek, yaitu sebuah aplikasi Android bernama **UMKMami**.

Tujuan kami adalah menciptakan sebuah platform yang menjembatani antara pelaku Usaha Mikro, Kecil, dan Menengah (UMKM) dengan masyarakat luas di era digital ini.

---

### Latar Belakang

Kita mulai dari sebuah fakta yang sangat penting. Berdasarkan data dari Kementerian Koperasi dan UKM, lebih dari **60% Produk Domestik Bruto (PDB) Indonesia** disumbang oleh aktivitas UMKM. Angka ini menunjukkan betapa vitalnya peran UMKM sebagai tulang punggung sejati ekonomi negara kita.

Namun, di balik kontribusi masif tersebut, banyak dari mereka yang masih menghadapi tantangan besar, terutama dalam hal **visibilitas di era digital**. Di satu sisi, konsumen sering kesulitan menemukan bisnis lokal yang berkualitas di sekitar mereka. Di sisi lain, para pemilik UMKM, yang seringkali fokus pada kualitas produk, tidak memiliki sumber daya atau keahlian teknis untuk memasarkan bisnis mereka secara online.

Melihat kesenjangan ini, kami mengembangkan **UMKMami**. UMKMami adalah sebuah direktori digital berbasis mobile yang dirancang untuk menjadi etalase bagi UMKM lokal, sehingga mereka mudah ditemukan, dihubungi, dan didukung oleh masyarakat.

---

### Persona Pengguna

Dalam merancang aplikasi ini, kami berfokus pada dua persona pengguna utama:

1.  **Pengguna Aplikasi (Konsumen)**
    *   Kita sebut saja **Budi**, seorang mahasiswa atau pekerja muda. Budi aktif menggunakan smartphone, gemar menjelajahi kuliner, dan memiliki keinginan untuk mendukung bisnis-bisnis kecil di sekitarnya.
    *   **Kebutuhan Budi**: "Saya ingin cara cepat untuk menemukan tempat makan sate yang enak dan murah di dekat kosan," atau "Saya butuh jasa reparasi elektronik yang terpercaya."
    *   **Solusi dari UMKMami**: Aplikasi kami menyediakan daftar, filter pencarian, dan tampilan peta untuk Budi agar bisa menemukan UMKM yang ia butuhkan dengan mudah.

2.  **Pemilik UMKM**
    *   Kita sebut saja **Ibu Siti**, seorang pemilik warung bakso yang sudah berdiri selama 5 tahun. Rasa baksonya enak, tetapi pelanggannya hanya dari lingkungan sekitar. Ibu Siti tidak terlalu familier dengan teknologi, apalagi pemasaran digital.
    *   **Kebutuhan Ibu Siti**: "Saya ingin warung saya lebih dikenal orang," dan "Saya mau punya 'profil online' tapi tidak tahu cara buatnya dan tidak mau ribet."
    *   **Solusi dari UMKMami**: Kami menyediakan dashboard khusus untuk pemilik UMKM yang sangat mudah digunakan, di mana Ibu Siti bisa mendaftarkan bisnisnya hanya dalam beberapa langkah.

---

### Demo Penggunaan Aplikasi dengan Penjelasan Teknis

#### Alur Konsumen (Budi):

1.  **Halaman Utama & Rekomendasi**
    Saat Budi membuka aplikasi, ia melihat daftar UMKM dan rekomendasi. **Secara teknis, data ini diambil secara *real-time* dari Firebase Realtime Database. Fitur rekomendasi kami bangun dengan menganalisis kategori yang paling sering dilihat oleh pengguna, di mana data preferensi ini juga kami simpan di Firebase.**

2.  **Pencarian & Tampilan Peta**
    Budi bisa mencari UMKM atau beralih ke tampilan Peta. **Tampilan peta ini kami render menggunakan *Maps SDK for Android for Compose*, yang memungkinkan kami mengintegrasikan peta Google yang interaktif secara native. Setiap pin atau `Marker` di peta ditempatkan berdasarkan data latitude dan longitude yang kami tarik dari Firebase.**

3.  **Melihat Detail UMKM**
    Setelah mengklik UMKM, Budi masuk ke halaman detail. **Semua informasi di sini—termasuk menu, harga, dan ulasan—lagi-lagi merupakan data dinamis dari Firebase, memastikan informasi selalu yang paling baru.**

#### Alur Pemilik UMKM (Ibu Siti):

1.  **Registrasi & Login**
    Ibu Siti mendaftar dan login ke akunnya. **Proses otentikasi pengguna ini kami kelola sepenuhnya menggunakan *Firebase Authentication*, yang menyediakan sistem login yang aman dan mudah diimplementasikan.**

2.  **Mengisi Profil di Dashboard**
    Ia mulai mengisi profil bisnisnya di Owner Dashboard. Semua data yang ia masukkan, seperti nama, deskripsi, dan menu, akan disimpan dalam sebuah *state* di dalam **OwnerDashboardViewModel** sebelum diunggah.

3.  **Verifikasi Alamat Otomatis (Geocoding)**
    Nah, ini bagian pentingnya. Ibu Siti mengetikkan alamat warungnya. Tanpa perlu tombol verifikasi, sistem kami secara otomatis mengkonversi alamat tersebut menjadi titik koordinat. **Secara teknis, kami menggunakan kelas `Geocoder` bawaan Android yang kami jalankan di *background thread* menggunakan *Kotlin Coroutines*. Ini memastikan aplikasi tetap responsif. Metode ini jauh lebih aman dan stabil dibandingkan pemanggilan API web manual, karena tidak ada API Key yang terekspos di dalam kode.**

4.  **Menyimpan Profil**
    Terakhir, ia menekan tombol 'Simpan Profil'. Data dari ViewModel kemudian diunggah ke **Firebase Realtime Database**. **Berkat sifat *real-time* dari Firebase, setelah data berhasil disimpan, profil 'Warung Bakso Bu Siti' akan langsung tersedia dan bisa ditemukan oleh semua pengguna seperti Budi tanpa perlu menunggu lama.**

---

### Penutup

Sebagai kesimpulan, UMKMami bukan hanya sekadar aplikasi, tetapi sebuah ekosistem digital yang kami bangun untuk memberdayakan bisnis lokal yang menyumbang lebih dari 60% PDB negara kita. Dengan mempermudah UMKM untuk tampil secara online dan mempermudah masyarakat untuk menemukan mereka, kami berharap dapat memberikan kontribusi positif bagi perekonomian lokal.

Sekian presentasi dari kami. Terima kasih atas perhatian Bapak/Ibu Dosen dan teman-teman semua.