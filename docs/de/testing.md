---
title: Testing
---
Sie können dieses Plugin gegen E-Mail Test-Tools wie [MailHog](https://github.com/mailhog/MailHog) testen.

### Testumgebung aufsetzen 

Um eine Testumgebung mit MailHog aufzusetzen, führen Sie folgende Kommandos aus:
```
docker pull mailhog/mailhog
docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

Oder nutzen Sie stattdessen `docker-compose`:
```
docker-compose up
```

Um sich mit diesem Container verbinden zu können, müssen die folgenden Einstellungen in der globalen Konfiguration gesetzt sein:
* Host: `localhost`
* Port: `1025`
* Von and Benutzername dürfen nicht leer sein.

### TLS Tests

Die folgenden Abschnitte beschreiben eine Testumgebung mit selbst signierten Zertifikaten.
Um diese Zertifikate freizugeben und um den Verlauf der Tests zu beobachten, sollte das SCM-SSL-Context-Plugin installiert sein.
Mit folgenden Kommandos kann die Umgebung für die TLS tests gestartet werden:
```
cd tls-test
docker-compose up
```

#### Explizites TLS (STARTTLS)

Konfig:
- Host: `localhost`
- Port: `25`
- Benutzername: `trillian@hitchhiker.org`
- Passwort: `secret`
- Transport Strategie: `SMTP_TLS`

#### SMPTS / Implizites TLS

Konfig:
- Host: `localhost`
- Port: `465`
- Benutzername: `trillian@hitchhiker.org`
- Passwort: `secret`
- Transport Strategie: `SMTPS`
