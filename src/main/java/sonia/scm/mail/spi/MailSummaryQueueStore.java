/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
