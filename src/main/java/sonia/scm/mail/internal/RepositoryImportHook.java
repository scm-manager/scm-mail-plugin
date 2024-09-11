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
import com.google.common.collect.Maps;
import jakarta.inject.Inject;
import org.apache.shiro.SecurityUtils;
import sonia.scm.EagerSingleton;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.mail.api.Category;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.mail.api.Topic;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryImportEvent;
import sonia.scm.user.User;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.util.ResourceBundle.getBundle;

@EagerSingleton
@Extension
public class RepositoryImportHook {

  public static final String IMPORT_SUCCESS_EVENT_DISPLAY_NAME = "importSuccess";
  public static final String IMPORT_FAILED_EVENT_DISPLAY_NAME = "importFailed";
  private static final Category CATEGORY = new Category("scm-manager-core");
  private static final Topic TOPIC_IMPORT_SUCCESS = new Topic(CATEGORY, IMPORT_SUCCESS_EVENT_DISPLAY_NAME);
  private static final Topic TOPIC_IMPORT_FAILED = new Topic(CATEGORY, IMPORT_FAILED_EVENT_DISPLAY_NAME);
  protected static final String IMPORT_SUCCESS_TEMPLATE_PATH = "sonia/scm/mail/emailnotification/import_success.mustache";
  protected static final String IMPORT_FAILED_TEMPLATE_PATH = "sonia/scm/mail/emailnotification/import_failed.mustache";
  private static final String SCM_REPOSITORY_URL_PATTERN = "{0}/repo/{1}/{2}/code/sources/";
  private static final String SCM_LOG_URL_PATTERN = "{0}/importlog/{1}/";

  private static final String SUBJECT_PATTERN = "{0}/{1} {2}";

  private static final Map<Locale, ResourceBundle> SUBJECT_BUNDLES = Maps
    .asMap(new HashSet<>(Arrays.asList(ENGLISH, GERMAN)), locale -> getBundle("sonia.scm.mail.emailnotification.Subjects", locale));

  private final MailService mailService;
  private final ScmConfiguration scmConfiguration;

  @Inject
  public RepositoryImportHook(MailService mailService, ScmConfiguration scmConfiguration) {
    this.mailService = mailService;
    this.scmConfiguration = scmConfiguration;
  }

  @Subscribe
  public void handleEvent(RepositoryImportEvent event) throws MailSendBatchException {
    User currentUser = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);

    mailService.emailTemplateBuilder()
      .fromCurrentUser()
      .toUser(currentUser.getId())
      .onTopic(evaluateTopic(event.isFailed()))
      .withSubject(getMailSubject(event, ENGLISH))
      .withSubject(GERMAN, getMailSubject(event, GERMAN))
      .withTemplate(evaluateTemplatePath(event.isFailed()), MailTemplateType.MARKDOWN_HTML)
      .andModel(getTemplateModel(event))
      .send();
  }

  private Topic evaluateTopic(boolean failed) {
    return failed ? TOPIC_IMPORT_FAILED : TOPIC_IMPORT_SUCCESS;
  }

  private String evaluateTemplatePath(boolean failed) {
    return failed ? IMPORT_FAILED_TEMPLATE_PATH : IMPORT_SUCCESS_TEMPLATE_PATH;
  }

  private String getMailSubject(RepositoryImportEvent event, Locale locale) {
    Repository repository = event.getItem();
    String displayEventKeyName = event.isFailed() ? IMPORT_FAILED_EVENT_DISPLAY_NAME : IMPORT_SUCCESS_EVENT_DISPLAY_NAME;
    String displayEventName = SUBJECT_BUNDLES.get(locale).getString(displayEventKeyName);
    return MessageFormat.format(SUBJECT_PATTERN, repository.getNamespace(), repository.getName(), displayEventName);
  }

  private Map<String, Object> getTemplateModel(RepositoryImportEvent event) {
    Map<String, Object> result = Maps.newHashMap();
    result.put("namespace", event.getItem().getNamespace());
    result.put("name", event.getItem().getName());
    result.put("link", getRepositoryLink(event.getItem()));
    result.put("logLink", getImportLogLink(event.getLogId()));
    return result;
  }

  private String getRepositoryLink(Repository repository) {
    String baseUrl = scmConfiguration.getBaseUrl();
    return MessageFormat.format(SCM_REPOSITORY_URL_PATTERN, baseUrl, repository.getNamespace(), repository.getName());
  }

  private String getImportLogLink(String logId) {
    String baseUrl = scmConfiguration.getBaseUrl();
    return MessageFormat.format(SCM_LOG_URL_PATTERN, baseUrl, logId);
  }
}
