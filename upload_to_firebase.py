import requests
import json
import sys

# ==============================================================================
#             SKRIP UNGGAH FILE JSON SPESIFIK KE FIREBASE
# ==============================================================================
# PERINGATAN: Menjalankan skrip ini akan MENGGANTI SELURUH data di Firebase
# dengan konten dari file JSON yang Anda tentukan. Gunakan dengan hati-hati.
# ==============================================================================

# --- KONFIGURASI ---
# URL root database Firebase Anda.
FIREBASE_URL = "https://final-ca080-default-rtdb.firebaseio.com"

def upload_database(json_file_path):
    """Membaca file JSON yang ditentukan dan mengunggahnya ke Firebase."""
    
    print("===================================================")
    print("   Memulai Proses Unggah Database ke Firebase    ")
    print("===================================================\n")
    
    # Langkah 1: Baca data dari file JSON lokal
    try:
        with open(json_file_path, 'r') as f:
            data_to_upload = json.load(f)
        print(f"Berhasil membaca data dari '{json_file_path}'.")
    except FileNotFoundError:
        print(f"ERROR: File '{json_file_path}' tidak ditemukan.")
        return
    except json.JSONDecodeError:
        print(f"ERROR: Gagal mem-parsing JSON dari '{json_file_path}'.")
        return

    # Langkah 2: Kirim data ke Firebase menggunakan permintaan PUT
    url = f"{FIREBASE_URL}/.json"
    print(f"\nMengirim data ke: {url}")
    print("PERINGATAN: Operasi ini akan menimpa semua data yang ada di Firebase.")
    
    # Konfirmasi dari pengguna
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
    # Pastikan nama file diberikan sebagai argumen command-line
    if len(sys.argv) < 2:
        print("Cara penggunaan: python upload_to_firebase.py <nama_file_json>")
        print("Contoh: python upload_to_firebase.py database_with_balance.json")
    else:
        file_path = sys.argv[1]
        upload_database(file_path)
