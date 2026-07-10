import os
import glob

directory = r"d:\skmfmfvlrm\java_project\stockGame_spring\src\main\java\com\skfkfkvlrm\stockgame_spring\domain"
pattern = "@SessionAttribute(name = \"studentId\", required = false) String studentId"
replacement = "@org.springframework.web.bind.annotation.RequestAttribute(name = \"studentId\", required = false) String studentId"

for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith(".java"):
            filepath = os.path.join(root, file)
            with open(filepath, "r", encoding="utf-8") as f:
                content = f.read()
            if pattern in content:
                content = content.replace(pattern, replacement)
                with open(filepath, "w", encoding="utf-8") as f:
                    f.write(content)
                print(f"Updated {filepath}")
