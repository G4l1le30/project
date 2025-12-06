import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
import json
import os

# --- KONFIGURASI ---
# Akan membaca dari environment variable GOOGLE_APPLICATION_CREDENTIALS
# Jika tidak diset, akan mencari di path default yang bisa diatur di sini
SERVICE_ACCOUNT_KEY_ENV_VAR = "GOOGLE_APPLICATION_CREDENTIALS"
DEFAULT_SERVICE_ACCOUNT_KEY_PATH = "path/to/your/serviceAccountKey.json" # Fallback/contoh

# Ganti dengan URL database Anda
DATABASE_URL = "https://final-ca080-default-rtdb.firebaseio.com/"
# Nama file aturan Anda
RULES_FILE = "database.rules.json"
# -----------------

def update_firebase_rules():
    """
    Menginisialisasi Firebase Admin SDK dan memperbarui aturan Realtime Database.
    """
    service_account_path = os.getenv(SERVICE_ACCOUNT_KEY_ENV_VAR)

    if not service_account_path:
        print(f"Peringatan: Environment variable '{SERVICE_ACCOUNT_KEY_ENV_VAR}' tidak diset.")
        print(f"Mencoba menggunakan path default: '{DEFAULT_SERVICE_ACCOUNT_KEY_PATH}'.")
        service_account_path = DEFAULT_SERVICE_ACCOUNT_KEY_PATH
        # Jika Anda tidak ingin menggunakan path default, Anda bisa exit di sini
        # sys.exit(1)

    try:
        # Inisialisasi Firebase Admin
        # 'ApplicationDefault' akan mencari kredensial di GOOGLE_APPLICATION_CREDENTIALS
        # atau di lokasi default lainnya.
        cred = credentials.ApplicationDefault()
        firebase_admin.initialize_app(cred, {
            'databaseURL': DATABASE_URL
        })

        print(f"Berhasil menginisialisasi Firebase App.")

        # Baca file aturan
        with open(RULES_FILE, 'r') as f:
            rules = json.load(f)
            print(f"Berhasil membaca aturan dari {RULES_FILE}.")

        # Dapatkan referensi ke root database dan perbarui aturan
        ref = db.reference('/')
        ref.update_rules(rules)

        print("\n>>> Aturan Firebase Realtime Database berhasil diperbarui! <<<")
        print("Silakan periksa Firebase Console Anda untuk memverifikasi perubahan.")

    except FileNotFoundError:
        print(f"ERROR: File '{service_account_path}' atau '{RULES_FILE}' tidak ditemukan.")
        print(f"Pastikan '{SERVICE_ACCOUNT_KEY_ENV_VAR}' diset dengan benar atau file '{RULES_FILE}' ada.")
    except Exception as e:
        print(f"Terjadi error: {e}")

if __name__ == "__main__":
    update_firebase_rules()
