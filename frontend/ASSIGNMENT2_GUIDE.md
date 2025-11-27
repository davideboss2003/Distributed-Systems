# Assignment 2 - Monitoring Microservice & Asynchronous Communication

## 📋 Overview

Acest assignment extinde sistemul din Assignment 1 cu:
- **Monitoring Microservice** (Python) - procesează date de consum energetic
- **RabbitMQ** - message broker pentru comunicare asincronă
- **Device Data Simulator** - simulează senzori smart meter
- **Sincronizare evenimente** între microservicii

---

## 🎯 Ce Trebuie Să Faci (Cerințe Minimale - 5p)

### ✅ 1. Monitoring Microservice cu Message Consumer (OBLIGATORIU)
- Microserviciu Python care consumă mesaje din RabbitMQ
- Procesează date de consum energetic de la senzori

### ✅ 2. Message Producer și Message Broker (OBLIGATORIU)
- **Device Data Simulator** - trimite date la RabbitMQ
- **RabbitMQ** - broker pentru queue-uri

### ✅ 3. Insert Hourly Energy Data în Database (OBLIGATORIU)
- Monitoring service agregă datele la nivel de oră
- Salvează în Monitoring Database

### ✅ 4. Readme File (OBLIGATORIU)
- Instrucțiuni build și execuție

### ✅ 5. Răspunsuri Corecte la Întrebări (OBLIGATORIU)
- Message Oriented Middleware
- Queue vs Topic
- Point-to-Point vs Publish-Subscribe

---

## 🏗️ Arhitectura Completă

```
┌─────────────────────────────────────────────────────────────────┐
│                    Visualization Platform                        │
│                      (Next.js Frontend)                          │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                       API Gateway (Traefik)                      │
│                    Reverse Proxy + Validation                    │
└─┬───────────┬───────────────┬──────────────────┬────────────────┘
  │           │               │                  │
  ▼           ▼               ▼                  ▼
┌──────┐  ┌──────┐      ┌──────────┐      ┌─────────────┐
│ Auth │  │ User │      │  Device  │      │ Monitoring  │
│ MS   │  │  MS  │      │    MS    │      │     MS      │
│(Java)│  │(Java)│      │  (Java)  │      │  (Python)   │ ← NOU!
└──┬───┘  └──┬───┘      └────┬─────┘      └──────┬──────┘
   │         │               │                    │
   ▼         ▼               ▼                    ▼
┌──────┐  ┌──────┐      ┌──────────┐      ┌─────────────┐
│Auth  │  │User  │      │ Device   │      │ Monitoring  │
│  DB  │  │  DB  │      │    DB    │      │     DB      │
└──────┘  └──────┘      └────┬─────┘      └─────────────┘
                              │
                              │ publică evenimente
                              ▼
                    ┌──────────────────────┐
                    │      RabbitMQ        │ ← NOU!
                    │  (Message Broker)    │
                    │                      │
                    │ ┌──────────────────┐ │
                    │ │ Sync Queue       │ │ ← Sincronizare
                    │ │ (users/devices)  │ │
                    │ └──────────────────┘ │
                    │                      │
                    │ ┌──────────────────┐ │
                    │ │ Data Queue       │ │ ← Date senzori
                    │ │ (sensor data)    │ │
                    │ └──────────────────┘ │
                    └──────────┬───────────┘
                               │
                               │ consumă mesaje
                               ▼
                    ┌──────────────────────┐
                    │  Device Simulator    │ ← NOU!
                    │  (Python/Desktop)    │
                    │  Generează date      │
                    │  la 10 minute        │
                    └──────────────────────┘
```

---

## 🔄 Flow-ul Complet

### **Scenario 1: Adăugare User Nou**

```
1. Admin creează user în Frontend
   ↓
2. Request → API Gateway (Traefik) → User Microservice
   ↓
3. User MS salvează user în User DB
   ↓
4. User MS publică mesaj în RabbitMQ (Sync Queue):
   {
     "event": "USER_CREATED",
     "userId": "123",
     "username": "john",
     "email": "john@example.com"
   }
   ↓
5. Monitoring MS consumă mesajul din Sync Queue
   ↓
6. Monitoring MS salvează userId în Monitoring DB
   (pentru a ști că acest user există)
```

### **Scenario 2: Adăugare Device Nou**

```
1. Admin creează device în Frontend
   ↓
2. Request → API Gateway → Device Microservice
   ↓
3. Device MS salvează device în Device DB
   ↓
4. Device MS publică mesaj în RabbitMQ (Sync Queue):
   {
     "event": "DEVICE_CREATED",
     "deviceId": "device-456",
     "userId": "123",
     "description": "Smart Meter Living Room"
   }
   ↓
5. Monitoring MS consumă mesajul din Sync Queue
   ↓
6. Monitoring MS salvează deviceId în Monitoring DB
   (pentru a ști că acest device există și cui aparține)
```

### **Scenario 3: Simulare Date Senzor (CORE FUNCTIONALITY)**

```
1. Device Data Simulator (Python app) rulează continuu
   ↓
2. La fiecare 10 minute, generează date random:
   {
     "timestamp": "2025-11-11T14:30:00Z",
     "device_id": "device-456",
     "measurement_value": 2.5  // kWh în 10 minute
   }
   ↓
3. Simulator publică mesaj în RabbitMQ (Data Queue)
   ↓
4. Monitoring MS consumă mesajul din Data Queue
   ↓
5. Monitoring MS procesează datele:
   - Verifică că device-ul există
   - Agregă valorile la nivel de oră (6 măsurători × 10 min = 1 oră)
   - Calculează total: 2.5 + 2.3 + 2.7 + 2.4 + 2.6 + 2.5 = 15 kWh/oră
   ↓
6. Monitoring MS salvează în Monitoring DB:
   {
     "deviceId": "device-456",
     "timestamp": "2025-11-11T14:00:00Z",
     "hourlyConsumption": 15.0  // kWh
   }
   ↓
7. Frontend poate afișa grafice cu consumul pe ore
```

---

## 🛠️ Componente Noi de Implementat

### **1. Device Data Simulator (Python Desktop App)**

**Ce face:**
- Aplicație standalone (nu microserviciu web)
- Generează date random la fiecare 10 minute
- Trimite date la RabbitMQ

**Structură:**
```python
# simulator.py
import pika
import json
import time
import random
from datetime import datetime

# Configurare (din config file)
DEVICE_ID = "device-456"  # Se setează în config
RABBITMQ_HOST = "localhost"
RABBITMQ_QUEUE = "sensor_data_queue"

def generate_measurement():
    """Generează valoare random de consum energetic"""
    # Simulare realistă: mai mult ziua, mai puțin noaptea
    hour = datetime.now().hour
    
    if 6 <= hour <= 22:  # Zi
        base_load = random.uniform(2.0, 4.0)
    else:  # Noapte
        base_load = random.uniform(0.5, 1.5)
    
    # Fluctuații mici
    fluctuation = random.uniform(-0.3, 0.3)
    
    return round(base_load + fluctuation, 2)

def send_to_rabbitmq(data):
    """Trimite date la RabbitMQ"""
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host=RABBITMQ_HOST)
    )
    channel = connection.channel()
    
    # Declară queue
    channel.queue_declare(queue=RABBITMQ_QUEUE, durable=True)
    
    # Publică mesaj
    channel.basic_publish(
        exchange='',
        routing_key=RABBITMQ_QUEUE,
        body=json.dumps(data),
        properties=pika.BasicProperties(
            delivery_mode=2,  # Persistent
        )
    )
    
    connection.close()
    print(f"✓ Sent: {data}")

def main():
    """Loop principal - trimite date la fiecare 10 minute"""
    print(f"Starting simulator for device: {DEVICE_ID}")
    
    while True:
        measurement = {
            "timestamp": datetime.now().isoformat(),
            "device_id": DEVICE_ID,
            "measurement_value": generate_measurement()
        }
        
        send_to_rabbitmq(measurement)
        
        # Așteaptă 10 minute (600 secunde)
        # Pentru testing, poți reduce la 10 secunde
        time.sleep(600)

if __name__ == "__main__":
    main()
```

**Config file (config.json):**
```json
{
  "device_id": "device-456",
  "rabbitmq_host": "localhost",
  "rabbitmq_port": 5672,
  "interval_seconds": 600
}
```

---

### **2. Monitoring Microservice (Python)**

**Ce face:**
- Consumă mesaje din RabbitMQ (2 queue-uri)
- Procesează date de senzori
- Agregă la nivel de oră
- Salvează în Monitoring DB

**Structură:**
```python
# monitoring_service.py
from flask import Flask, jsonify
import pika
import json
import threading
from datetime import datetime
from sqlalchemy import create_engine, Column, String, Float, DateTime
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

app = Flask(__name__)

# Database setup
DATABASE_URL = "mysql+pymysql://root:password@localhost:3310/monitoring_db"
engine = create_engine(DATABASE_URL)
Base = declarative_base()
Session = sessionmaker(bind=engine)

# Models
class HourlyConsumption(Base):
    __tablename__ = 'hourly_consumption'
    
    id = Column(String(36), primary_key=True)
    device_id = Column(String(36), nullable=False)
    timestamp = Column(DateTime, nullable=False)
    consumption_kwh = Column(Float, nullable=False)

class Device(Base):
    __tablename__ = 'devices'
    
    id = Column(String(36), primary_key=True)
    user_id = Column(String(36))
    description = Column(String(255))

Base.metadata.create_all(engine)

# In-memory buffer pentru agregare
consumption_buffer = {}  # {device_id: {hour: [values]}}

def consume_sync_queue():
    """Consumă evenimente de sincronizare (users/devices)"""
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host='localhost')
    )
    channel = connection.channel()
    channel.queue_declare(queue='sync_queue', durable=True)
    
    def callback(ch, method, properties, body):
        message = json.loads(body)
        event = message.get('event')
        
        session = Session()
        
        if event == 'DEVICE_CREATED':
            # Salvează device în DB local
            device = Device(
                id=message['deviceId'],
                user_id=message.get('userId'),
                description=message.get('description')
            )
            session.add(device)
            session.commit()
            print(f"✓ Device synced: {message['deviceId']}")
        
        session.close()
        ch.basic_ack(delivery_tag=method.delivery_tag)
    
    channel.basic_consume(
        queue='sync_queue',
        on_message_callback=callback
    )
    
    print("Listening for sync events...")
    channel.start_consuming()

def consume_data_queue():
    """Consumă date de la senzori"""
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host='localhost')
    )
    channel = connection.channel()
    channel.queue_declare(queue='sensor_data_queue', durable=True)
    
    def callback(ch, method, properties, body):
        data = json.loads(body)
        device_id = data['device_id']
        value = data['measurement_value']
        timestamp = datetime.fromisoformat(data['timestamp'])
        
        # Agregare la nivel de oră
        hour_key = timestamp.replace(minute=0, second=0, microsecond=0)
        
        if device_id not in consumption_buffer:
            consumption_buffer[device_id] = {}
        
        if hour_key not in consumption_buffer[device_id]:
            consumption_buffer[device_id][hour_key] = []
        
        consumption_buffer[device_id][hour_key].append(value)
        
        # Dacă avem 6 măsurători (1 oră completă), salvează
        if len(consumption_buffer[device_id][hour_key]) >= 6:
            total = sum(consumption_buffer[device_id][hour_key])
            
            session = Session()
            consumption = HourlyConsumption(
                id=f"{device_id}_{hour_key.isoformat()}",
                device_id=device_id,
                timestamp=hour_key,
                consumption_kwh=round(total, 2)
            )
            session.add(consumption)
            session.commit()
            session.close()
            
            print(f"✓ Saved hourly consumption: {device_id} - {total} kWh")
            
            # Curăță buffer
            del consumption_buffer[device_id][hour_key]
        
        ch.basic_ack(delivery_tag=method.delivery_tag)
    
    channel.basic_consume(
        queue='sensor_data_queue',
        on_message_callback=callback
    )
    
    print("Listening for sensor data...")
    channel.start_consuming()

# REST API pentru frontend
@app.route('/api/monitoring/devices/<device_id>/consumption', methods=['GET'])
def get_device_consumption(device_id):
    """Returnează consumul pe ore pentru un device"""
    session = Session()
    records = session.query(HourlyConsumption).filter_by(
        device_id=device_id
    ).order_by(HourlyConsumption.timestamp).all()
    
    result = [
        {
            'timestamp': r.timestamp.isoformat(),
            'consumption': r.consumption_kwh
        }
        for r in records
    ]
    
    session.close()
    return jsonify(result)

if __name__ == '__main__':
    # Start consumers în thread-uri separate
    sync_thread = threading.Thread(target=consume_sync_queue, daemon=True)
    data_thread = threading.Thread(target=consume_data_queue, daemon=True)
    
    sync_thread.start()
    data_thread.start()
    
    # Start Flask API
    app.run(host='0.0.0.0', port=8084)
```

---

### **3. Modificări în Device Microservice (Java)**

**Adaugă publicare evenimente la RabbitMQ:**

```java
// DeviceService.java
@Service
public class DeviceService {
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public Device createDevice(CreateDeviceRequest request) {
        Device device = new Device();
        device.setId(UUID.randomUUID().toString());
        device.setDescription(request.getDescription());
        device.setAddress(request.getAddress());
        device.setUserId(request.getUserId());
        
        // Salvează în DB
        device = deviceRepository.save(device);
        
        // Publică eveniment în RabbitMQ
        Map<String, Object> message = new HashMap<>();
        message.put("event", "DEVICE_CREATED");
        message.put("deviceId", device.getId());
        message.put("userId", device.getUserId());
        message.put("description", device.getDescription());
        
        rabbitTemplate.convertAndSend("sync_queue", message);
        
        return device;
    }
}
```

**Configurare RabbitMQ (application.properties):**
```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

---

## 📦 Docker Compose Update

**Adaugă în docker-compose.yml:**

```yaml
services:
  # ... serviciile existente ...
  
  # RabbitMQ
  rabbitmq:
    image: rabbitmq:3-management
    container_name: rabbitmq
    ports:
      - "5672:5672"    # AMQP protocol
      - "15672:15672"  # Management UI
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    networks:
      - app-network
  
  # Monitoring Microservice (Python)
  monitoring-service:
    build: ./monitoring-service
    container_name: monitoring-service
    ports:
      - "8084:8084"
    environment:
      DATABASE_URL: mysql+pymysql://root:password@mysql-monitoring-db:3306/monitoring_db
      RABBITMQ_HOST: rabbitmq
    depends_on:
      - rabbitmq
      - mysql-monitoring-db
    networks:
      - app-network
  
  # Monitoring Database
  mysql-monitoring-db:
    image: mysql:8.0
    container_name: mysql-monitoring-db
    ports:
      - "3310:3306"
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: monitoring_db
    networks:
      - app-network
```

---

## 🚀 Pași de Implementare

### **Pas 1: Setup RabbitMQ**
```bash
# Pornește RabbitMQ
docker-compose up -d rabbitmq

# Verifică Management UI
# http://localhost:15672 (guest/guest)
```

### **Pas 2: Creează Monitoring Microservice (Python)**
```bash
mkdir monitoring-service
cd monitoring-service

# Creează requirements.txt
echo "flask
pika
sqlalchemy
pymysql" > requirements.txt

# Creează monitoring_service.py (codul de mai sus)

# Creează Dockerfile
echo "FROM python:3.9
WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY . .
CMD [\"python\", \"monitoring_service.py\"]" > Dockerfile
```

### **Pas 3: Creează Device Data Simulator**
```bash
mkdir device-simulator
cd device-simulator

# Creează simulator.py (codul de mai sus)
# Creează config.json
```

### **Pas 4: Modifică Device Microservice**
- Adaugă dependency RabbitMQ în pom.xml
- Implementează publicare evenimente
- Testează

### **Pas 5: Testare End-to-End**
```bash
# 1. Pornește toate serviciile
docker-compose up -d

# 2. Creează un device în frontend
# Device ID: device-456

# 3. Pornește simulator
cd device-simulator
python simulator.py

# 4. Verifică în RabbitMQ Management UI că mesajele sunt trimise

# 5. Verifică în Monitoring DB că datele sunt salvate
docker exec -it mysql-monitoring-db mysql -u root -p
USE monitoring_db;
SELECT * FROM hourly_consumption;
```

---

## 📊 Bonus Features (Puncte Extra)

### **User Synchronization (2p)**
- Când se creează user, publică eveniment
- Monitoring MS consumă și salvează userId

### **Device Synchronization (1p)**
- Când se creează device, publică eveniment
- Monitoring MS consumă și salvează deviceId

### **Frontend Charts (2p)**
- Grafice cu consum pe ore (line chart/bar chart)
- Calendar pentru selectare zi
- Afișare OX: hours, OY: kWh

---

## 🎓 Întrebări pentru Evaluare

### **Message Oriented Middleware (MOM)**
Software care permite comunicare asincronă între aplicații prin mesaje (ex: RabbitMQ, Kafka, ActiveMQ).

### **Queue vs Topic**
- **Queue**: Point-to-Point (1 producer → 1 consumer)
- **Topic**: Publish-Subscribe (1 producer → N consumers)

### **Point-to-Point vs Publish-Subscribe**
- **Point-to-Point**: Mesajul e consumat de UN SINGUR consumer (ex: task queue)
- **Publish-Subscribe**: Mesajul e primit de TOȚI subscribers (ex: notifications)

---

## ✅ Checklist Final

- [ ] RabbitMQ instalat și funcțional
- [ ] Monitoring Microservice (Python) implementat
- [ ] Device Data Simulator implementat
- [ ] Sincronizare users/devices via RabbitMQ
- [ ] Date senzori procesate și salvate
- [ ] Agregare la nivel de oră funcțională
- [ ] Docker Compose actualizat
- [ ] Deployment Diagram (PlantUML)
- [ ] README cu instrucțiuni
- [ ] Răspunsuri la întrebări teoretice

---

## 📝 Deliverables

1. **Deployment Diagram** (PlantUML)
2. **README.md** cu build & run instructions
3. **Source code** pe GitLab
4. **Database dump** (opțional)

**Repository name:** `DS2025_Group_LastName_FirstName_Assignment_2`
**Share with:** `utcn_dsrl`
