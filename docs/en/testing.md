---
title: Testing
---
You can test this plugin against email testings tools like [MailHog](https://github.com/mailhog/MailHog).

### Setup Test environment

To setup a testing environment with MailHog you may run
```
docker pull mailhog/mailhog
docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

Or simply use `docker-compose`:
```
docker-compose up
```

To connect against this container you must set the following settings in your global email configuration:
* Host to `localhost`
* Port to `1025`
* From and Username to any non-empty value.

### TLS Tests

The following section describes a setup with self-signed certificates.
In order to track the certificate tests, the scm-ssl-context-plugin should be installed.

```
cd tls-test
docker-compose up
```

#### Explicit TLS (STARTTLS)

Config:
- Host: `localhost`
- Port: `25`
- Username: `trillian@hitchhiker.org`
- Password: `secret`
- Transport Strategy: `SMTP_TLS`

#### Implicit TLS

Config:
- Host: `localhost`
- Port: `465`
- Username: `trillian@hitchhiker.org`
- Password: `secret`
- Transport Strategy: `SMTPS`
