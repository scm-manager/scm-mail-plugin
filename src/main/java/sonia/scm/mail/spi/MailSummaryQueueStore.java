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

package sonia.scm.mail.spi;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.codec.binary.Base16;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.mail.api.ScmMail;
import sonia.scm.mail.api.ScmUserMails;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import java.util.HashMap;
import java.util.Map;

@Singleton
class MailSummaryQueueStore {
  private static final Logger LOG = LoggerFactory.getLogger(MailSummaryQueueStore.class);
  private static final String STORE_NAME = "mail-queue";

  private final Base16 base16 = new Base16();
  private final DataStore<ScmUserMails> store;

  @Inject
  MailSummaryQueueStore(DataStoreFactory storeFactory) {
    store = storeFactory.withType(ScmUserMails.class).withName(STORE_NAME).build();
  }

  public void addUserMailsByCategory(String userId, String category, ScmMail mail) {
    LOG.trace("Store mail with subject {} for user {} and category {}", mail.getSubject(), userId, category);
    ScmUserMails userMails = getWithEncodedUserId(userId);
    userMails.getUserMailsByCategory().add(mail);
    putWithEncodedUserId(userId, userMails);
  }

  public void addUserMailsByCategoryAndEntity(String userId, String category, String entityId, ScmMail mail) {
    LOG.trace("Store mail with subject {} for user {} category {} and {}", mail.getSubject(), userId, category, entityId);
    ScmUserMails userMails = getWithEncodedUserId(userId);
    userMails.getUserMailsByCategoryAndEntity().add(mail);
    putWithEncodedUserId(userId, userMails);
  }

  private ScmUserMails getWithEncodedUserId(String userId) {
    return store.getOptional(encodeUserId(userId)).orElseGet(ScmUserMails::new);
  }

  private void putWithEncodedUserId(String userId, ScmUserMails userMails) {
    store.put(encodeUserId(userId), userMails);
  }

  public void removeAllMailsOfUser(String userId) {
    LOG.trace("Clear mails for user {}", userId);
    store.remove(encodeUserId(userId));
  }

  public Map<String, MailSummaryQueue> getAll() {
    LOG.trace("Load all summary queues of all users");
    Map<String, MailSummaryQueue> result = new HashMap<>();

    store.getAll().forEach((encodedUserId, userMails) -> {
      MailSummaryQueue summaryQueue = new MailSummaryQueue();
      result.put(decodeUserId(encodedUserId), summaryQueue);

      userMails.getUserMailsByCategory().forEach(
        scmMail -> summaryQueue.add(scmMail.getCategory(), scmMail)
      );

      userMails.getUserMailsByCategoryAndEntity().forEach(
        scmMail -> summaryQueue.add(scmMail.getCategory(), scmMail.getEntityId(), scmMail)
      );
    });

    return result;
  }

  private String encodeUserId(String userId) {
    return base16.encodeAsString(userId.getBytes());
  }

  private String decodeUserId(String encodedUserId) {
    return new String(base16.decode(encodedUserId));
  }
}
