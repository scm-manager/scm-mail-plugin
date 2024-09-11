/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.mail.internal;

import com.github.legman.Subscribe;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import jakarta.inject.Inject;
import sonia.scm.EagerSingleton;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.mail.api.Category;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.mail.api.Topic;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.HealthCheckEvent;
import sonia.scm.repository.Repository;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.UserDisplayManager;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.util.ResourceBundle.getBundle;

@Extension
@EagerSingleton
public class HealthCheckFailedHook {

  public static final String HEALTH_CHECK_FAILED_EVENT_DISPLAY_NAME = "healthCheckFailed";
  private static final Category CATEGORY = new Category("scm-manager-core");
  static final Topic TOPIC_HEALTH_CHECK_FAILED = new Topic(CATEGORY, HEALTH_CHECK_FAILED_EVENT_DISPLAY_NAME);
  protected static final String HEALTH_CHECK_FAILED_TEMPLATE_PATH = "sonia/scm/mail/emailnotification/healthcheck_failed.mustache";
  private static final String SCM_REPOSITORY_URL_PATTERN = "{0}/repo/{1}/{2}/settings/general/";

  private static final String SUBJECT_PATTERN = "{0}/{1} {2}";

  private static final Map<Locale, ResourceBundle> SUBJECT_BUNDLES = Maps
    .asMap(new HashSet<>(Arrays.asList(ENGLISH, GERMAN)), locale -> getBundle("sonia.scm.mail.emailnotification.Subjects", locale));

  private final MailService mailService;
  private final ScmConfiguration scmConfiguration;
  private final UserDisplayManager userDisplayManager;

  @Inject
  public HealthCheckFailedHook(MailService mailService, ScmConfiguration scmConfiguration, UserDisplayManager userDisplayManager) {
    this.mailService = mailService;
    this.scmConfiguration = scmConfiguration;
    this.userDisplayManager = userDisplayManager;
  }

  @Subscribe
  public void handle(HealthCheckEvent event) throws MailSendBatchException {
    if (event.getCurrentFailures() != event.getPreviousFailures() && !event.getCurrentFailures().isEmpty()) {
      Set<String> emergencyContacts = scmConfiguration.getEmergencyContacts();
      MailService.EnvelopeBuilder envelopeBuilder = mailService.emailTemplateBuilder();
      for (String user : emergencyContacts) {
        addAddressForUser(envelopeBuilder, user);
      }

      envelopeBuilder
        .onTopic(TOPIC_HEALTH_CHECK_FAILED)
        .withSubject(getMailSubject(event, ENGLISH))
        .withSubject(GERMAN, getMailSubject(event, GERMAN))
        .withTemplate(HEALTH_CHECK_FAILED_TEMPLATE_PATH, MailTemplateType.MARKDOWN_HTML)
        .andModel(getTemplateModel(event))
        .send();
    }
  }

  private void addAddressForUser(MailService.EnvelopeBuilder envelopeBuilder, String user) {
    Optional<DisplayUser> displayUser = userDisplayManager.get(user);
    if (displayUser.isPresent()) {
      String mail = displayUser.get().getMail();
      if (!Strings.isNullOrEmpty(mail)) {
        envelopeBuilder.toAddress(mail);
      }
    }
  }

  private Map<String, Object> getTemplateModel(HealthCheckEvent event) {
    Map<String, Object> result = Maps.newHashMap();
    result.put("namespace", event.getRepository().getNamespace());
    result.put("name", event.getRepository().getName());
    result.put("link", getRepositoryLink(event.getRepository()));
    return result;
  }

  private String getRepositoryLink(Repository repository) {
    String baseUrl = scmConfiguration.getBaseUrl();
    return MessageFormat.format(SCM_REPOSITORY_URL_PATTERN, baseUrl, repository.getNamespace(), repository.getName());
  }

  private String getMailSubject(HealthCheckEvent event, Locale locale) {
    Repository repository = event.getRepository();
    String displayEventName = SUBJECT_BUNDLES.get(locale).getString(HEALTH_CHECK_FAILED_EVENT_DISPLAY_NAME);
    return MessageFormat.format(SUBJECT_PATTERN, repository.getNamespace(), repository.getName(), displayEventName);
  }
}
