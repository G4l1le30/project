import requests
import json

# ==============================================================================
#                      SKRIP PEMBARUAN DATABASE (VERSI AMAN)
# ==============================================================================
# Versi ini telah diperbaiki untuk tidak menghapus data yang tidak seharusnya.
# Metode penambahan data diubah dari satu PATCH besar ke beberapa PUT yang
# lebih spesifik untuk memastikan hanya data yang dituju yang diperbarui.
# ==============================================================================

# KONFIGURASI
FIREBASE_URL = "https://final-ca080-default-rtdb.firebaseio.com"
IDS_TO_DELETE = ["umkm5", "umkm9"]
NEW_DATA = {
    "umkm": {
        "umkm5": { "id": "umkm5", "name": "Cetak Cepat Digital Printing", "category": "Jasa", "description": "Layanan print, jilid, dan desain grafis. Solusi cepat untuk kebutuhan tugas dan skripsi.", "address": "Jl. Sumbersari, Malang", "lat": -7.9625, "lng": 112.617, "imageUrl": "https://upload.wikimedia.org/wikipedia/commons/8/89/Photocopy_shop.jpg" },
        "umkm9": { "id": "umkm9", "name": "Seblak Jeletot Teh Eni", "category": "Makanan", "description": "Seblak pedas dengan berbagai topping, dari ceker sampai sosis. Level pedas bisa diatur.", "address": "Jl. Bendungan Sutami, Malang", "lat": -7.965, "lng": 112.614, "imageUrl": "https://upload.wikimedia.org/wikipedia/commons/a/a2/Seblak_Ceker.jpg" }
    },
    "umkm_menu": {
        "umkm9": [ { "name": "Seblak Original", "price": 12000 }, { "name": "Seblak Ceker", "price": 15000 }, { "name": "Seblak Sosis Bakso", "price": 17000 } ]
    },
    "umkm_services": {
        "umkm5": [ { "service": "Print A4 Hitam Putih", "price": 500 }, { "service": "Jilid Spiral Kawat", "price": 10000 }, { "service": "Cetak Banner (per meter)", "price": 25000 } ]
    },
    "reviews": {
        "umkm5": { "r1": "Hasil print di Cetak Cepat bagus dan tidak luntur.", "r2": "Pelayanannya ramah dan prosesnya cepat.", "r3": "Harga jilidnya terjangkau untuk mahasiswa." },
        "umkm9": { "r1": "Seblaknya nampol, pedasnya pas!", "r2": "Banyak pilihan topping, tempatnya juga bersih.", "r3": "Harga murah, porsi banyak, mantap." }
    }
}
NODES = ["umkm", "umkm_menu", "umkm_services", "reviews"]

# ==============================================================================
# FUNGSI-FUNGSI (YANG SUDAH DIPERBAIKI)
# ==============================================================================

def delete_old_umkm_data():
    """Menghapus data UMKM lama dari semua node terkait."""
    print("Memulai proses penghapusan data lama...")
    for umkm_id in IDS_TO_DELETE:
        for node in NODES:
            url = f"{FIREBASE_URL}/{node}/{umkm_id}.json"
            print(f"  Menghapus {node}/{umkm_id}...")
            response = requests.delete(url)
            if response.status_code == 200:
                print(f"    -> Berhasil dihapus.")
            else:
                print(f"    -> Tidak ada data untuk dihapus atau terjadi error (Status: {response.status_code}).")
    print("Proses penghapusan selesai.\n")

def add_new_umkm_data_safely():
    """
    (FUNGSI BARU YANG AMAN) Menambahkan data baru menggunakan PUT ke path spesifik.
    Ini memastikan tidak ada data lain yang tertimpa.
    """
    print("Memulai proses penambahan data baru (metode aman)...")
    
    # Iterasi melalui setiap node (umkm, umkm_menu, etc.) dalam data baru
    for node, umkm_entries in NEW_DATA.items():
        # Iterasi melalui setiap entri umkm (umkm5, umkm9) dalam node
        for umkm_id, data_payload in umkm_entries.items():
            # Buat URL spesifik untuk setiap data yang akan ditambahkan/diperbarui
            url = f"{FIREBASE_URL}/{node}/{umkm_id}.json"
            
            print(f"  Menambahkan/memperbarui data untuk {node}/{umkm_id}...")
            # Gunakan PUT pada path spesifik. Ini aman.
            response = requests.put(url, data=json.dumps(data_payload))
            
            if response.status_code == 200:
                print(f"    -> Berhasil ditambahkan/diperbarui.")
            else:
                print(f"    -> Gagal menambahkan data untuk {node}/{umkm_id} (Status: {response.status_code}).")
                print(f"       Response: {response.text}")

    print("Proses penambahan data baru selesai.\n")


# ==============================================================================
# EKSEKUSI SKRIP
# ==============================================================================

if __name__ == "__main__":
    print("======================================================")
    print("  Memulai Skrip Pembaruan Database Firebase (V2 AMAN) ")
    print("======================================================\n")
    
    # Langkah 1: Hapus data lama (fungsi ini sudah aman)
    delete_old_umkm_data()
    
    # Langkah 2: Tambah data baru dengan metode yang aman
    add_new_umkm_data_safely()
    
    print("=============================================")
    print("   Skrip Selesai Dijalankan.               ")
    print("=============================================")
    print("Silakan cek Firebase console Anda untuk memverifikasi perubahan.")

# ==============================================================================
# CARA MENJALANKAN SKRIP INI:
# ==============================================================================
# 1. Pastikan Anda memiliki Python terinstal.
# 2. Instal library 'requests': pip install requests
# 3. Jalankan skrip ini dari direktori proyek: python update_firebase.py
# ==============================================================================