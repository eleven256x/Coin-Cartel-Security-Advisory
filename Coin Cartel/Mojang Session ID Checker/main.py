import requests

print(f"[-] Minecraft Session ID Checker, Checks if the inputted ID is valid.")
print(f"[-] Powered by Deathwish Software, Developed by eleven256x (599131954767462572)")

user_input = input("[+] Please enter a Session ID: ")
if user_input != "":
    print(f"[-] Checking Session ID, Please wait for a few moments.")
    response_information = requests.post("https://authserver.mojang.com/validate", json={"accessToken": user_input}, headers={"Content-Type": "application/json"})
    if response_information.status_code == 204:
        print(f"[+] Session ID is valid ({user_input})")
    else:
        print(f"[-] Session ID isn't valid({user_input})")
else:
    print(f"[-] Wasn't pasted in anything to search for, Ending...")
