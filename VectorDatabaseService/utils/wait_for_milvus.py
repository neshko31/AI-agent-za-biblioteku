import time
from pymilvus import connections, utility

def wait_for_milvus(host: str, port: int, timeout: int = 120):
    print(">>> Waiting for Milvus to be ready...")
    deadline = time.time() + timeout
    
    while time.time() < deadline:
        try:
            connections.connect("default", host=host, port=port)
            # Pokušaj stvarne operacije, ne samo konekciju
            utility.list_collections()
            print(">>> Milvus is ready.")
            return True
        except Exception as e:
            print(f">>> Not ready yet: {e}")
            try:
                connections.disconnect("default")
            except Exception:
                pass
            time.sleep(3)
    
    raise TimeoutError("Milvus did not become ready in time.")