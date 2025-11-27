import pika
import json
import time
import random
from datetime import datetime



# Load config from file
try:
    with open('config.json', 'r') as f:
        config = json.load(f)
except FileNotFoundError:
    print("  config.json not found! Using default values.")
    config = {
        "device_id": "device-test-001",
        "rabbitmq_host": "localhost",
        "rabbitmq_port": 5672,
        "rabbitmq_user": "guest",
        "rabbitmq_pass": "guest",
        "interval_seconds": 600,
        "test_mode": False,
        "max_measurements": 0
    }

DEVICE_ID = config.get('device_id', 'device-test-001')
RABBITMQ_HOST = config.get('rabbitmq_host', 'localhost')
RABBITMQ_PORT = config.get('rabbitmq_port', 5672)
RABBITMQ_USER = config.get('rabbitmq_user', 'guest')
RABBITMQ_PASS = config.get('rabbitmq_pass', 'guest')
INTERVAL_SECONDS = config.get('interval_seconds', 600)  # 10 minutes = 600 seconds
TEST_MODE = config.get('test_mode', False)  # If True, sends every 10 seconds for testing
MAX_MEASUREMENTS = config.get('max_measurements', 0)  # 0 = infinite, >0 = stop after N measurements

QUEUE_NAME = 'sensor_data_queue'

# ==================== FUNCTIONS ====================

def generate_measurement():
    """
    Generează o valoare realistă de consum energetic (kWh în 10 minute)
    
    Logica:
    - Ziua (6:00 - 22:00): consum mai mare (2.0 - 4.0 kWh)
    - Noaptea (22:00 - 6:00): consum mai mic (0.5 - 1.5 kWh)
    - Adaugă fluctuații random pentru realism
    """
    hour = datetime.now().hour
    
    # Determină consumul de bază în funcție de ora din zi
    if 6 <= hour < 9:  # Dimineața - consum mare (mic dejun, pregătire)
        base_load = random.uniform(3.0, 4.5)
    elif 9 <= hour < 12:  # Dimineața târziu - consum mediu
        base_load = random.uniform(2.0, 3.0)
    elif 12 <= hour < 14:  # Prânz - consum mare
        base_load = random.uniform(3.5, 5.0)
    elif 14 <= hour < 18:  # După-amiază - consum mediu
        base_load = random.uniform(2.5, 3.5)
    elif 18 <= hour < 22:  # Seară - consum mare (cină, TV, lumini)
        base_load = random.uniform(3.0, 4.5)
    else:  # Noapte (22:00 - 6:00) - consum mic
        base_load = random.uniform(0.5, 1.5)
    
    # Adaugă fluctuații mici pentru realism
    fluctuation = random.uniform(-0.3, 0.3)
    
    # Returnează valoarea rotunjită la 2 zecimale
    return round(base_load + fluctuation, 2)

def send_to_rabbitmq(data):
    """
    Trimite date la RabbitMQ în queue-ul 'sensor_data_queue'
    
    Parametri:
    - data: dict cu timestamp, device_id, measurement_value
    """
    try:
        # Creează conexiune la RabbitMQ
        credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
        parameters = pika.ConnectionParameters(
            host=RABBITMQ_HOST,
            port=RABBITMQ_PORT,
            credentials=credentials,
            heartbeat=600,
            blocked_connection_timeout=300
        )
        connection = pika.BlockingConnection(parameters)
        channel = connection.channel()
        
        # Declară queue-ul (dacă nu există, îl creează; dacă există, nu face nimic)
        channel.queue_declare(queue=QUEUE_NAME, durable=True)
        
        # Publică mesajul
        channel.basic_publish(
            exchange='',  # Default exchange
            routing_key=QUEUE_NAME,
            body=json.dumps(data),
            properties=pika.BasicProperties(
                delivery_mode=2,  
            )
        )
        
        connection.close()
        print(f"✓ Sent: {data}")
        return True
        
    except Exception as e:
        print(f" Error sending to RabbitMQ: {e}")
        return False

def main():
    """
    Loop principal - trimite date la fiecare interval configurat
    """
    print("=" * 70)
    print(" Device Data Simulator Started")
    print("=" * 70)
    print(f"Device ID: {DEVICE_ID}")
    print(f"RabbitMQ: {RABBITMQ_HOST}:{RABBITMQ_PORT}")
    print(f"Queue: {QUEUE_NAME}")
    
    if TEST_MODE:
        print(f"  TEST MODE: Sending every 10 seconds (instead of {INTERVAL_SECONDS}s)")
        interval = 10
    else:
        print(f"Interval: {INTERVAL_SECONDS} seconds ({INTERVAL_SECONDS // 60} minutes)")
        interval = INTERVAL_SECONDS
    
    if MAX_MEASUREMENTS > 0:
        print(f"  AUTO-STOP: Will stop after {MAX_MEASUREMENTS} measurements")
    else:
        print(f"  CONTINUOUS: Will run until stopped manually (CTRL+C)")
    
    print("=" * 70)
    print()
    
    measurement_count = 0
    
    try:
        while True:
            measurement_count += 1
            
            # Generează măsurătoare
            measurement_value = generate_measurement()
            
            # Creează mesajul
            message = {
                "timestamp": datetime.now().isoformat(),
                "device_id": DEVICE_ID,
                "measurement_value": measurement_value
            }
            
            # Trimite la RabbitMQ
            success = send_to_rabbitmq(message)
            
            if success:
                print(f"[{measurement_count}] {datetime.now().strftime('%Y-%m-%d %H:%M:%S')} - "
                      f"Device: {DEVICE_ID} - Value: {measurement_value} kWh")
            
            # Verifică dacă am ajuns la limita de măsurători
            if MAX_MEASUREMENTS > 0 and measurement_count >= MAX_MEASUREMENTS:
                print("\n" + "=" * 70)
                print(f"  Reached maximum measurements limit: {MAX_MEASUREMENTS}")
                print(f" Total measurements sent: {measurement_count}")
                print(" Simulator stopped automatically")
                print("=" * 70)
                break
            
            # Așteaptă intervalul configurat
            print(f"    Wait {interval} seconds until next measurement...\n")
            time.sleep(interval)
            
    except KeyboardInterrupt:
        print("\n" + "=" * 70)
        print(" Simulator stopped by user")
        print(f"Total measurements sent: {measurement_count}")
        print("=" * 70)



if __name__ == "__main__":
    main()
