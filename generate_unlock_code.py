import hashlib
import sys

def generate_unlock_code(support_id):
    # This must perfectly match the salt in PinManager.kt
    secret_salt = "FocusGuard_Master_Secret_8273"
    
    text_to_hash = support_id + secret_salt
    hash_bytes = hashlib.sha256(text_to_hash.encode('utf-8')).digest()
    
    code = ""
    for b in hash_bytes:
        # In Python 3, iterating over bytes gives integers (0-255), exactly matching Kotlin's (byte.toInt() and 0xFF)
        unsigned = b & 0xFF
        code += str(unsigned)
        if len(code) >= 6:
            break
            
    return code[:6]

if __name__ == "__main__":
    print("========================================")
    print("   FocusGuard Unlock Code Generator     ")
    print("========================================")
    
    if len(sys.argv) > 1:
        support_id = sys.argv[1].strip()
    else:
        support_id = input("Enter the user's Support ID: ").strip()
        
    unlock_code = generate_unlock_code(support_id)
    
    print("\n----------------------------------------")
    print(f"Support ID:  {support_id}")
    print(f"Unlock Code: {unlock_code}")
    print("----------------------------------------")
    print("Send this 6-digit Unlock Code to the parent.")
