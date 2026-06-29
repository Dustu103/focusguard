import os

base_dir = r"d:\Prorgram\FreeApp\blocker"

directories = [
    "app/src/main/java/com/focusguard/ui/screens",
    "app/src/main/java/com/focusguard/ui/theme",
    "app/src/main/java/com/focusguard/services",
    "app/src/main/java/com/focusguard/data",
    "app/src/main/java/com/focusguard/admin",
    "app/src/main/java/com/focusguard/utils",
    "app/src/main/res/xml",
    "app/src/main/res/values",
    "app/src/main/res/mipmap-hdpi",
    "app/src/main/res/mipmap-mdpi",
    "app/src/main/res/mipmap-xhdpi",
    "app/src/main/res/mipmap-xxhdpi",
    "app/src/main/res/mipmap-xxxhdpi",
    "gradle/wrapper"
]

for d in directories:
    os.makedirs(os.path.join(base_dir, d), exist_ok=True)

print("Directories created.")
