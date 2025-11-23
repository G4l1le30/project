from docx2pdf import convert
import os

def main():
    docx_path = input("Masukkan path file DOCX: ").strip()
    pdf_path = input("Masukkan path output PDF: ").strip()

    if not os.path.isfile(docx_path):
        print("Error: File DOCX tidak ditemukan.")
        return

    try:
        convert(docx_path, pdf_path)
        print("Berhasil convert ke PDF:", pdf_path)
    except Exception as e:
        print("Gagal convert:", e)

if __name__ == "__main__":
    main()
