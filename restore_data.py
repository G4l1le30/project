import requests
import json

# ==============================================================================
# SKRIP PEMULIHAN DATA
# ==============================================================================
# Tujuan: Skrip ini akan mengembalikan data UMKM (selain umkm5 dan umkm9)
# yang tidak sengaja terhapus oleh skrip sebelumnya.
# ==============================================================================

# KONFIGURASI
FIREBASE_URL = "https://final-ca080-default-rtdb.firebaseio.com"

# Data yang akan dipulihkan
RESTORE_DATA = {
    "umkm": {
        "umkm0": { "id": "umkm0", "name": "Warung Bakso Mantep", "category": "Makanan", "description": "Bakso enak dekat kampus UB, terkenal dengan kuah gurih dan bakso uratnya.", "address": "Jl. Veteran, Lowokwaru, Malang", "lat": -7.956, "lng": 112.615, "imageUrl": "https://github.com/G4l1le30/project/blob/master/assets/images/umkm0/Bakso_mi_bihun.jpg?raw=true" },
        "umkm1": { "id": "umkm1", "name": "Kedai Kopi Sore", "category": "Minuman", "description": "Kedai kopi lokal dengan suasana tenang, cocok untuk nugas dan nongkrong.", "address": "Jl. Sigura-gura, Malang", "lat": -7.957, "lng": 112.612, "imageUrl": "https://github.com/G4l1le30/project/blob/master/assets/images/umkm1/A_small_cup_of_coffee.JPG?raw=true" },
        "umkm2": { "id": "umkm2", "name": "Roti Bakar 88", "category": "Makanan", "description": "Roti bakar legendaris dengan topping melimpah dan harga terjangkau.", "address": "Jl. Soekarno-Hatta, Malang", "lat": -7.951, "lng": 112.626, "imageUrl": "https://github.com/G4l1le30/project/blob/master/assets/images/umkm2/Resep-Bolu-Bakar-Bandung-Sederhana-Lezat-Teksturnya-Empuk-Banget.jpg?raw=true" },
        "umkm3": { "id": "umkm3", "name": "Laundry Kilat Express", "category": "Jasa", "description": "Layanan laundry cepat 4 jam selesai dengan harga mahasiswa.", "address": "Jl. MT Haryono, Malang", "lat": -7.952, "lng": 112.621, "imageUrl": "https://github.com/G4l1le30/project/blob/master/assets/images/umkm4/ayam%20geprek.png?raw=true" },
        "umkm4": { "id": "umkm4", "name": "Ayam Geprek Pak D", "category": "Makanan", "description": "Ayam geprek dengan level sambal bervariasi, favorit mahasiswa UB.", "address": "Jl. Kerto Leksono, Malang", "lat": -7.958, "lng": 112.618, "imageUrl": "https://github.com/G4l1le30/project/blob/master/assets/images/umkm5/toko%20kaos%20arema.png?raw=true" },
        "umkm6": { "id": "umkm6", "name": "Sate Ayu", "category": "Makanan", "description": "Sate ayam bumbu khas Madura dengan harga mahasiswa.", "address": "Jl. Jakarta, Malang", "lat": -7.964, "lng": 112.633, "imageUrl": "https://github.com/G4l1le30/project/blob/master/assets/images/umkm6/sate.png?raw=true" },
        "umkm7": { "id": "umkm7", "name": "Foto Copy Murah Jaya", "category": "Jasa", "description": "Fotocopy dan print murah, dekat kampus dan buka sampai malam.", "address": "Jl. Mayjen Panjaitan, Malang", "lat": -7.953, "lng": 112.620, "imageUrl": "https://upload.wikimedia.org/wikipedia/commons/8/89/Photocopy_shop.jpg" },
        "umkm8": { "id": "umkm8", "name": "Es Teh Jumbo 5K", "category": "Minuman", "description": "Es teh jumbo berbagai rasa, harga ramah kantong mahasiswa.", "address": "Jl. Kalpataru, Malang", "lat": -7.949, "lng": 112.619, "imageUrl": "https://upload.wikimedia.org/wikipedia/commons/1/1b/Teh_bunga_telaga.jpg" }
    },
    "umkm_menu": {
        "umkm0": [ { "name": "Bakso Urat", "price": 15000 }, { "name": "Bakso Halus", "price": 12000 }, { "name": "Mie Bakso", "price": 13000 } ],
        "umkm1": [ { "name": "Kopi Hitam", "price": 10000 }, { "name": "Cappuccino", "price": 18000 }, { "name": "Latte", "price": 20000 } ],
        "umkm2": [ { "name": "Roti Bakar Coklat", "price": 12000 }, { "name": "Roti Bakar Keju", "price": 15000 }, { "name": "Roti Bakar Mix", "price": 17000 } ],
        "umkm4": [ { "name": "Ayam Geprek Original", "price": 14000 }, { "name": "Ayam Geprek Keju", "price": 17000 }, { "name": "Ayam Geprek Level 5", "price": 16000 } ],
        "umkm6": [ { "name": "Sate Ayam", "price": 15000 }, { "name": "Sate Kulit", "price": 12000 }, { "name": "Lontong Sate", "price": 18000 } ],
        "umkm8": [ { "name": "Es Teh Original", "price": 5000 }, { "name": "Es Teh Lemon", "price": 7000 }, { "name": "Es Teh Lychee", "price": 8000 } ]
    },
    "umkm_services": {
        "umkm3": [ { "service": "Laundry Reguler", "price": 7000 }, { "service": "Laundry Kilat 4 Jam", "price": 15000 }, { "service": "Cuci Kering Setrika", "price": 10000 } ],
        "umkm7": [ { "service": "Fotocopy", "price": 200 }, { "service": "Print Hitam Putih", "price": 500 }, { "service": "Print Warna", "price": 1500 }, { "service": "Jilid Spiral", "price": 6000 } ]
    },
    "reviews": {
        "umkm0": { "r1": "Warung Bakso Mantep mantap, kualitas sangat bagus.", "r2": "Pelayanan di Warung Bakso Mantep cepat dan ramah.", "r3": "Harga terjangkau, Warung Bakso Mantep recommended." },
        "umkm1": { "r1": "Kedai Kopi Sore mantap, kualitas sangat bagus.", "r2": "Pelayanan di Kedai Kopi Sore cepat dan ramah.", "r3": "Harga terjangkau, Kedai Kopi Sore recommended." },
        "umkm2": { "r1": "Roti Bakar 88 mantap, kualitas sangat bagus.", "r2": "Pelayanan di Roti Bakar 88 cepat dan ramah.", "r3": "Harga terjangkau, Roti Bakar 88 recommended." },
        "umkm3": { "r1": "Laundry Kilat Express mantap, kualitas sangat bagus.", "r2": "Pelayanan di Laundry Kilat Express cepat dan ramah.", "r3": "Harga terjangkau, Laundry Kilat Express recommended." },
        "umkm4": { "r1": "Ayam Geprek Pak D mantap, kualitas sangat bagus.", "r2": "Pelayanan di Ayam Geprek Pak D cepat dan ramah.", "r3": "Harga terjangkau, Ayam Geprek Pak D recommended." },
        "umkm6": { "r1": "Sate Ayu mantap, kualitas sangat bagus.", "r2": "Pelayanan di Sate Ayu cepat dan ramah.", "r3": "Harga terjangkau, Sate Ayu recommended." },
        "umkm7": { "r1": "Foto Copy Murah Jaya mantap, kualitas sangat bagus.", "r2": "Pelayanan di Foto Copy Murah Jaya cepat dan ramah.", "r3": "Harga terjangkau, Foto Copy Murah Jaya recommended." },
        "umkm8": { "r1": "Es Teh Jumbo 5K mantap, kualitas sangat bagus.", "r2": "Pelayanan di Es Teh Jumbo 5K cepat dan ramah.", "r3": "Harga terjangkau, Es Teh Jumbo 5K recommended." }
    }
}


def restore_data():
    """Menggunakan PATCH untuk memulihkan data tanpa menimpa data lain."""
    print("Memulai proses pemulihan data...")
    url = f"{FIREBASE_URL}/.json"
    
    response = requests.patch(url, data=json.dumps(RESTORE_DATA))
    
    if response.status_code == 200:
        print("  -> Data berhasil dipulihkan!")
    else:
        print(f"  -> Gagal memulihkan data (Status: {response.status_code}).")
        print(f"     Response: {response.text}")
    print("Proses pemulihan selesai.\n")


if __name__ == "__main__":
    print("=======================================")
    print("  Memulai Skrip Pemulihan Data Firebase  ")
    print("=======================================\n")
    
    restore_data()
    
    print("=======================================")
    print("   Skrip Pemulihan Selesai.            ")
    print("=======================================")
    print("Database Anda seharusnya sudah kembali normal (plus data baru dari skrip sebelumnya).")
    print("Selanjutnya, saya akan perbaiki skrip 'update_firebase.py'.")

# CARA MENJALANKAN:
# 1. Buka terminal/command prompt di direktori proyek.
# 2. Jalankan skrip ini dengan perintah:
#    python restore_data.py
