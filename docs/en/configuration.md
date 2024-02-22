---
title: Configuration
---
There are two different configurations for the SCM-Mail-Plugin.

### Global configuration

The global configuration can be found under 'Administration', 'Settings' and then 'Email'.
With this administrators can configure the transportation of emails by an SMTP server.
At first the administrator can configure the host and the port of the SMTP server.
Then the 'from' email address and a subject prefix can be configured for every email, sent by the SMTP server.
If credentials are needed to authenticate against the SMTP server, then they can be configured next.
All those values are set by a text input each.
After that, the default language of the emails sent by the SCM-Server can be configured by a dropdown.
Lastly the transport strategy can also be configured by a dropdown.
The available options are SMTP, SMTPS and SMTP via TLS.
Changes need to be saved by clicking the 'Submit' button at the end of the page.

At the end of the configuration page the administrators can check if the current configuration is valid, 
by sending an email to a specified address.
That specified email address can be set by a text input.
By clicking the button 'Test Configuration' the email gets send out.
The result of sending that email, will be displayed in a newly opened modal.

### User specific configuration

The user specific configuration can be found under 'Email' in the footer.
Users can configure their personal settings for notifications via email.
First the user can set the language used in the email notifications with a dropdown.

Then the user can activate / deactivate the summarization of their email notifications, by clicking a checkbox.
If the summarization is activated, the user can further configure the summary mails.
The emails can either be summarized by category or by a specific entity.
For example, if the SCM-Review-Plugin is also installed, then email notifications regarding pull requests are available.
Either every email notification will be summarized by a specific pull request and therefore the user gets a summary email for each pull request.
Or the summarization is done by the pull request category, which means every email notification regarding any pull request
will be summarized into one email notification.
By clicking a checkbox the user can decide which summarization strategy should be used.

After that the user can select the frequency of the emails  with a dropdown.
Important to note is, that not all email notifications get summarized.
Some topics are deemed prioritized.
Those topics are always send out directly and individually.

Lastly the user can select the topics for which notifications will be sent.
For each topic and its category a checkbox is available, to include / exclude the topic / category.
To save those settings, the user must click the 'Submit' button at the end of the page.
