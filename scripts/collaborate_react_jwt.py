import urllib.request
import json
import sys

def query_ollama(model, prompt):
    url = "http://localhost:11434/api/generate"
    payload = {
        "model": model,
        "prompt": prompt,
        "stream": False
    }
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json"})
    
    try:
        with urllib.request.urlopen(req) as response:
            result = json.loads(response.read().decode())
            return result.get("response", "")
    except Exception as e:
        return f"Error: {str(e)}"

# Read files
with open(r"d:\skmfmfvlrm\java_project\stockGame_react\src\features\core\api\axiosConfig.js", "r", encoding="utf-8") as f:
    axios_code = f.read()

with open(r"d:\skmfmfvlrm\java_project\stockGame_react\src\features\auth\store\useAuthStore.js", "r", encoding="utf-8") as f:
    auth_code = f.read()

prompt = f"""
We are migrating our React app from Session to JWT auth. 
Please rewrite the following two files to support JWT.
1. Save the token to localStorage upon login (the token is returned in `response.data.message` temporarily based on backend change).
2. Attach the token as `Bearer ${{token}}` in axiosConfig interceptors.
3. Remove token on logout.

--- axiosConfig.js ---
{axios_code}

--- useAuthStore.js ---
{auth_code}

Provide only the updated code blocks, wrapped in markdown.
"""

print("Querying qwen2.5:1.5b for JWT frontend refactoring...")
response = query_ollama("qwen2.5:1.5b", prompt)
print("\n=== Qwen 2.5 Response ===")
print(response)
