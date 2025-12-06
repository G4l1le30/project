import requests
import json

# ==============================================================================
#                      SKRIP UNGGAH DATABASE LENGKAP
# ==============================================================================
# PERINGATAN: Menjalankan skrip ini akan MENGGANTI SELURUH data di Firebase
# dengan konten dari file JSON lokal. Gunakan dengan hati-hati.
# ==============================================================================

# --- KONFIGURASI ---
# URL root database Firebase Anda.
FIREBASE_URL = "https://final-ca080-default-rtdb.firebaseio.com"
# Path ke file JSON yang akan diunggah.
JSON_FILE_PATH = "umkm.json"

def upload_database():
    """Membaca file JSON lokal dan mengunggahnya ke Firebase, menimpa semua data."""
    
    print("===================================================")
    print("   Memulai Proses Unggah Database ke Firebase    ")
    print("===================================================\n")
    
    # Langkah 1: Baca data dari file JSON lokal
    try:
        with open(JSON_FILE_PATH, 'r') as f:
            data_to_upload = json.load(f)
        print(f"Berhasil membaca data dari '{JSON_FILE_PATH}'.")
    except FileNotFoundError:
        print(f"ERROR: File '{JSON_FILE_PATH}' tidak ditemukan.")
        print("Pastikan file tersebut ada di direktori yang sama dengan skrip ini.")
        return
    except json.JSONDecodeError:
        print(f"ERROR: Gagal mem-parsing JSON dari '{JSON_FILE_PATH}'.")
        print("Pastikan file tersebut berisi format JSON yang valid.")
        return

    # Langkah 2: Kirim data ke Firebase menggunakan permintaan PUT
    # Permintaan PUT ke root URL akan menimpa seluruh database.
    url = f"{FIREBASE_URL}/.json"
    print(f"\nMengirim data ke: {url}")
    print("PERINGATAN: Operasi ini akan menimpa semua data yang ada di Firebase.")
    
    # Konfirmasi dari pengguna sebelum melanjutkan
    confirmation = input("Apakah Anda yakin ingin melanjutkan? (y/n): ")
    if confirmation.lower() != 'y':
        print("\nProses dibatalkan oleh pengguna.")
        return
        
    print("\nMelanjutkan proses unggah...")
    response = requests.put(url, data=json.dumps(data_to_upload))
    
    # Langkah 3: Periksa hasil respons
    if response.status_code == 200:
        print("\n=============================================")
        print("   BERHASIL! Database telah diperbarui.      ")
        print("=============================================")
        print("Silakan cek Firebase console Anda untuk memverifikasi perubahan.")
    else:
        print("\n=============================================")
        print(f"   GAGAL! Terjadi kesalahan saat mengunggah. ")
        print("=============================================")
        print(f"Status Code: {response.status_code}")
        print(f"Response: {response.text}")


# ==============================================================================
# EKSEKUSI SKRIP
# ==============================================================================

if __name__ == "__main__":
    upload_database()

# ==============================================================================
# CARA MENJALANKAN SKRIP INI:
# ==============================================================================
# 1. Pastikan Anda memiliki Python terinstal.
# 2. Instal library 'requests': pip install requests
# 3. Tempatkan skrip ini di direktori yang sama dengan file 'umkm.json'.
# 4. Jalankan skrip ini dari terminal: python upload_database.py
# 5. Anda akan diminta konfirmasi sebelum data diunggah. Ketik 'y' lalu Enter.
# ==============================================================================
