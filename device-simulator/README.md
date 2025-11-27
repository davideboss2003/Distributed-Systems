# Device Data Simulator

Simulează un smart meter care trimite date de consum energetic la fiecare 10 minute.

## Funcționalitate

- **Generare date realiste**: Consum mai mare ziua, mai mic noaptea
- **Publicare în RabbitMQ**: Trimite mesaje în `sensor_data_queue`
- **Configurabil**: Device ID și interval prin `config.json`
- **Test mode**: Trimite la fiecare 10 secunde pentru testing rapid

## Instalare

```bash
# Instalează dependințe
pip install -r requirements.txt
```

## Configurare

Editează `config.json`:

```json
{
  "device_id": "device-test-001",        // ID-ul device-ului (trebuie să existe în Device MS)
  "rabbitmq_host": "localhost",          // Host RabbitMQ
  "rabbitmq_port": 5672,                 // Port RabbitMQ
  "rabbitmq_user": "guest",              // Username RabbitMQ
  "rabbitmq_pass": "guest",              // Password RabbitMQ
  "interval_seconds": 600,               // Interval între măsurători (600s = 10 min)
  "test_mode": true                      // true = trimite la 10s, false = trimite la interval_seconds
}
```

## Rulare

```bash
python simulator.py
```

**Output așteptat:**
```
======================================================================
🔌 Device Data Simulator Started
======================================================================
Device ID: device-test-001
RabbitMQ: localhost:5672
Queue: sensor_data_queue
⚠️  TEST MODE: Sending every 10 seconds (instead of 600s)
======================================================================
Press CTRL+C to stop

✓ Sent: {'timestamp': '2025-11-13T10:30:00.123456', 'device_id': 'device-test-001', 'measurement_value': 3.45}
[1] 2025-11-13 10:30:00 - Device: device-test-001 - Value: 3.45 kWh
    Waiting 10 seconds until next measurement...
```

## Logica de Generare Date

Simulatorul generează valori realiste bazate pe ora din zi:

| Interval Orar | Consum (kWh/10min) | Descriere |
|---------------|-------------------|-----------|
| 06:00 - 09:00 | 3.0 - 4.5 | Dimineața (mic dejun, pregătire) |
| 09:00 - 12:00 | 2.0 - 3.0 | Dimineața târziu |
| 12:00 - 14:00 | 3.5 - 5.0 | Prânz (consum maxim) |
| 14:00 - 18:00 | 2.5 - 3.5 | După-amiază |
| 18:00 - 22:00 | 3.0 - 4.5 | Seară (cină, TV, lumini) |
| 22:00 - 06:00 | 0.5 - 1.5 | Noapte (consum minim) |

## Format Mesaj

Mesajele trimise în RabbitMQ au formatul:

```json
{
  "timestamp": "2025-11-13T10:30:00.123456",
  "device_id": "device-test-001",
  "measurement_value": 3.45
}
```

## Testing

### 1. Test Mode (rapid)
Setează în `config.json`:
```json
"test_mode": true
```
Trimite date la fiecare 10 secunde → în 1 minut ai 6 măsurători → vezi agregarea în DB.

### 2. Production Mode (real)
Setează în `config.json`:
```json
"test_mode": false,
"interval_seconds": 600
```
Trimite date la fiecare 10 minute → în 1 oră ai 6 măsurători → vezi agregarea în DB.

## Oprire

Apasă `CTRL+C` pentru a opri simulatorul.

## Troubleshooting

**Eroare: "Connection refused"**
- Verifică că RabbitMQ rulează: `docker ps | grep rabbitmq`
- Verifică host și port în `config.json`

**Eroare: "Authentication failed"**
- Verifică username și password în `config.json`

**Nu văd date în Monitoring DB**
- Verifică că Monitoring MS rulează: `docker ps | grep monitoring`
- Verifică logs: `docker logs monitoring-microservice-app`
- Așteaptă 6 măsurători (60 secunde în test mode, 1 oră în production mode)
