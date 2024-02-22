---
title: Configuration
---
Für das SCM-Mail-Plugin gibt es zwei unterschiedliche Konfigurationen.

### Globale Konfiguration

Die globale Konfiguration kann unter 'Administration', 'Einstellungen' und dann 'E-Mail' gefunden werden.
Mit der entsprechenden Berechtigung kann hier der Transport von E-Mails mittels eines SMTP Servers konfiguriert werden.
Zu Beginn kann der Host und der Port des SMTP Servers konfiguriert werden.
Anschließend kann die Versender E-Mail-Adresse und ein Betreff Prefix festgelegt werden, welche für jede versendete E-Mail genutzt wird.
Falls der SMTP Server einen Benutzernamen und Passwort zur Authentifizierung benötigt, können diese als nächstes konfiguriert werden.
Alle diese Werte werden jeweils durch ein Text-Input gesetzt.
Danach kann noch die Standard-Sprache für E-Mails mittels Dropdown gesetzt werden.
Abschließend kann noch die Transportstrategie für die E-Mails festgelegt werden mithilfe eines Dropdowns.
Die verfügbaren Optionen lauten SMTP, SMTPS und SMTP via TLS.
Die getätigten Änderungen müssen durch einen Klick auf den 'Speichern'-Button bestätigt werden.

Die SMTP Konfiguration lässt sich hier auch direkt testen, indem eine Test-E-Mail versendet wird.
Hierfür muss eine E-Mail-Adresse in einem Text-Input angegeben werden und anschließend der 'Konfiguration testen'-Button geklickt werden.
Das Ergebnis des Tests wird in einem neu geöffneten Modal angezeigt.

### Benutzer-spezifische Konfiguration

Die benutzer-spezifische Konfiguration kann im Footer unter 'E-Mail' gefunden werden.
Hier werden ihre persönlichen Einstellungen für E-Mail-Benachrichtigungen festgelegt.
Zunächst können sie die Sprache via Dropdown festlegen, welche für die E-Mail-Benachrichtigungen verwendet werden soll.
Anschließend kann die Zusammenfassung zu einer Sammel-E-Mail durch eine Checkbox eingestellt werden.
Falls die Sammel-E-Mail aktiviert ist, können weitere Details konfiguriert werden.

Die E-Mail-Benachrichtigungen können entweder nach Kategorie zu einer E-Mail zusammengefasst werden oder nach einer Entität.
Wenn zum Beispiel das SCM-Review-Plugin installiert ist, dann sind E-Mail-Benachrichtigungen bezüglich Pull Requests verfügbar.
Entweder können alle E-Mails, die sich auf einen bestimmten Pull-Request beziehen, zu einer E-Mail zusammengefasst werden,
sodass eine E-Mail pro Pull Request gesendet wird.
Alternativ können alle E-Mails zu beliebigen Pull-Requests in einer E-Mail zusammengefasst werden, sodass eine E-Mail für
alle Pull-Requests gesammelt versendet wird.
Die gewünschte Strategie kann durch eine Checkbox gewählt werden.

Anschließend ist noch die Frequenz, in der die zusammengefassten E-Mails versendet werden sollen, zu wählen.
Wichtig zu beachten ist, dass nicht alle E-Mails-Benachrichtigungen zusammengefasst werden.
Manche Themen gelten als priorisiert. Diese werden stets direkt und einzeln versendet.
Die Frequenz kann mithilfe eines Dropdowns gesetzt werden.

Als Letztes kann gewählt werden, zu welchen Kategorien und Themen E-Mail-Benachrichtigungen gesendet werden sollen.
Ob eine Kategorie / Thema ein- oder ausgeschlossen werden soll, wird jeweils mit einer Checkbox festgelegt.
Um Änderungen zu speichern, muss der 'Speichern'-Button betätigt werden.
