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

import lombok.Getter;
import sonia.scm.mail.api.ScmMail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
class MailSummaryQueue {
  private final Map<String, List<ScmMail>> mailQueueByCategory = new HashMap<>();
  private final Map<String, Map<String, List<ScmMail>>> mailQueueByCategoryAndEntityId = new HashMap<>();

  MailSummaryQueue() {
  }

  public void add(String category, ScmMail mail) {
    addToMailQueue(this.mailQueueByCategory, category, mail);
  }

  public void add(String category, String entityId, ScmMail mail) {
    Map<String, List<ScmMail>> entityMailQueues = this.mailQueueByCategoryAndEntityId.computeIfAbsent(
      category, key -> new HashMap<>()
    );
    addToMailQueue(entityMailQueues, entityId, mail);
  }

  private void addToMailQueue(Map<String, List<ScmMail>> mailQueues, String key, ScmMail mail) {
    List<ScmMail> currentMailQueue = mailQueues.computeIfAbsent(key, k -> new ArrayList<>());
    currentMailQueue.add(mail);
  }
}
