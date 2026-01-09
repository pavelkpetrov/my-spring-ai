# Splunk Sandbox - Quick Start Guide

Get up and running with Splunk in 3 minutes!

## Step 1: Start Splunk (1 minute)

```bash
cd mcp-server/docker
docker-compose up -d
```

Wait for the container to start. You'll see:
```
✔ Container splunk-sandbox  Started
```

## Step 2: Wait for Splunk to Initialize (1-2 minutes)

Check if Splunk is ready:

```bash
docker-compose logs -f splunk
```

Look for this message:
```
Ansible playbook complete, will begin streaming var/log/splunk/splunkd_stderr.log
```

Press `Ctrl+C` to exit logs.

## Step 3: Load Tutorial Data (30 seconds)

```bash
./load-tutorial-data.sh
```

This will:
- Create sample log files (web access, application logs, JSON events)
- Load them into Splunk
- Configure appropriate sourcetypes

## Step 4: Access Splunk Web

Open your browser and navigate to:

**http://localhost:8000**

Login credentials:
- **Username**: `admin`
- **Password**: `Admin123!`

## Step 5: Run Your First Search

In the Splunk search bar, try:

```spl
index=main | stats count by sourcetype
```

You should see your tutorial data!

---

## Next Steps

### Explore Your Data

```spl
# View all events
index=main

# Search for errors
index=main error OR ERROR OR failed

# View JSON events
index=main sourcetype=_json

# Create a timechart
index=main | timechart count by sourcetype
```

### Add More Data

1. **Via Web UI**: Settings > Add Data > Upload
2. **Via HEC (HTTP)**:
   ```bash
   curl -k https://localhost:8088/services/collector/event \
     -H "Authorization: Splunk 12345678-1234-1234-1234-123456789012" \
     -d '{"event": "Hello from API", "sourcetype": "api_data"}'
   ```
3. **Add files to tutorial-data/**: Place files in `tutorial-data/` and run `./load-tutorial-data.sh` again

### Install Apps

1. Download apps from [Splunkbase](https://splunkbase.splunk.com/)
2. Extract to `splunk-apps/` directory
3. Restart: `docker-compose restart splunk`

---

## Troubleshooting

**Can't access http://localhost:8000?**
- Wait 2-3 minutes for initial setup
- Check status: `docker-compose ps`
- View logs: `docker-compose logs splunk`

**Password not working?**
- Default password is `Admin123!`
- Change it in `docker-compose.yml` under `SPLUNK_PASSWORD`

**Port already in use?**
- Check what's using port 8000: `lsof -i :8000`
- Change ports in `docker-compose.yml`

---

## Useful Commands

```bash
# Stop Splunk
docker-compose stop

# Start Splunk
docker-compose start

# Restart Splunk
docker-compose restart

# View logs
docker-compose logs -f splunk

# Access container shell
docker exec -it splunk-sandbox bash

# Stop and remove (keeps data)
docker-compose down

# Complete reset (deletes all data)
docker-compose down -v
```

---

## API Access Example

### REST API

```bash
# Search via API
curl -k -u admin:Admin123! \
  https://localhost:8089/services/search/jobs \
  -d search="search index=main | head 10"
```

### Python Example

```python
import requests
from requests.auth import HTTPBasicAuth

# Disable SSL warning for self-signed cert
requests.packages.urllib3.disable_warnings()

# Create search
response = requests.post(
    'https://localhost:8089/services/search/jobs',
    auth=HTTPBasicAuth('admin', 'Admin123!'),
    data={'search': 'search index=main | head 10'},
    verify=False
)

search_id = response.text
print(f"Search ID: {search_id}")
```

---

## Available Ports

| Port | Service |
|------|---------|
| 8000 | Web UI |
| 8089 | REST API |
| 8088 | HTTP Event Collector |
| 9997 | Forwarder Receiver |
| 514/udp | Syslog |

---

## Resources

- Full documentation: See `README.md`
- Splunk Docs: https://docs.splunk.com/
- Splunk Apps: https://splunkbase.splunk.com/
- Community: https://community.splunk.com/

---

**Happy Splunking! 🎉**