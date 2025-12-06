import json

# Definisikan path file input dan output
input_filename = 'final-ca080-default-rtdb-export (2).json'
output_filename = 'database_with_balance.json'

try:
    # Buka dan baca file JSON asli
    with open(input_filename, 'r') as f:
        data = json.load(f)

    # Periksa apakah ada node 'users'
    if 'users' in data and isinstance(data['users'], dict):
        users_node = data['users']
        
        # Iterasi melalui setiap user di dalam node 'users'
        for user_id, user_data in users_node.items():
            # Periksa apakah user_data adalah dictionary dan belum memiliki 'balance'
            if isinstance(user_data, dict) and 'balance' not in user_data:
                print(f"Adding 'balance: 0.0' to user: {user_id}")
                user_data['balance'] = 0.0
    
    # Periksa apakah ada node 'user' (jika ada yang typo)
    elif 'user' in data and isinstance(data['user'], dict):
        users_node = data['user']
        
        # Iterasi melalui setiap user di dalam node 'user'
        for user_id, user_data in users_node.items():
            # Periksa apakah user_data adalah dictionary dan belum memiliki 'balance'
            if isinstance(user_data, dict) and 'balance' not in user_data:
                print(f"Adding 'balance: 0.0' to user: {user_id}")
                user_data['balance'] = 0.0
    else:
        print("Warning: Node 'users' or 'user' not found or not in expected format.")

    # Tulis data yang sudah dimodifikasi ke file JSON baru
    with open(output_filename, 'w') as f:
        # Gunakan indent=4 agar file JSON mudah dibaca
        json.dump(data, f, indent=4)
        
    print(f"\nSuccessfully processed the file.")
    print(f"Updated data has been saved to: {output_filename}")

except FileNotFoundError:
    print(f"Error: The file '{input_filename}' was not found.")
except json.JSONDecodeError:
    print(f"Error: The file '{input_filename}' is not a valid JSON file.")
except Exception as e:
    print(f"An unexpected error occurred: {e}")
