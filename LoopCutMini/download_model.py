import os
import requests
from tqdm import tqdm

def download_model():
    # Whisper-tinyモデルのURL
    model_url = "https://openaipublic.azureedge.net/whisper/models/345ae4da62f91d01cab486d1ce3438cfb4e5126f0b2325e326e0a93258962e7b/20230330/ggml-tiny.bin"
    
    # ダウンロード先のパス
    download_dir = os.path.join(os.path.dirname(__file__), "app", "src", "main", "assets")
    os.makedirs(download_dir, exist_ok=True)
    model_path = os.path.join(download_dir, "whisper-tiny-model.bin")
    
    # ダウンロード
    print("Whisper-tinyモデルをダウンロード中...")
    response = requests.get(model_url, stream=True)
    total_size = int(response.headers.get('content-length', 0))
    
    with open(model_path, 'wb') as f:
        for data in tqdm(response.iter_content(chunk_size=4096), 
                        total=total_size//4096, 
                        unit='KB'):
            f.write(data)
    
    print(f"ダウンロード完了: {model_path}")

if __name__ == "__main__":
    download_model()
