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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.mail.api.ScmMail;
import sonia.scm.mail.api.ScmRecipient;
import sonia.scm.store.InMemoryByteDataStoreFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({MockitoExtension.class})
class MailSummaryQueueStoreTest {

  private final InMemoryByteDataStoreFactory inMemoryByteDataStoreFactory = new InMemoryByteDataStoreFactory();

  private MailSummaryQueueStore summaryQueueStore;

  @BeforeEach
  void setupMocks() {
    summaryQueueStore = new MailSummaryQueueStore(inMemoryByteDataStoreFactory);
  }

  @Test
  void shouldStoreCategoryMail() {
    String category = "pokemon";
    String userId = "userId";

    ScmMail mail = createTestMail(category, null);
    summaryQueueStore.addUserMailsByCategory(userId, category, mail);
    Map<String, MailSummaryQueue> result = summaryQueueStore.getAll();

    MailSummaryQueue expectedQueue = new MailSummaryQueue();
    expectedQueue.add(category, mail);
    Map<String, MailSummaryQueue> expectedResult = Map.of(userId, expectedQueue);

    assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
  }

  @Test
  void shouldStoreCategoryAndEntityMail() {
    String category = "pokemon";
    String userId = "userId";
    String entityId = "entityId";

    ScmMail mail = createTestMail(category, entityId);
    summaryQueueStore.addUserMailsByCategoryAndEntity(userId, category, entityId, mail);
    Map<String, MailSummaryQueue> result = summaryQueueStore.getAll();

    MailSummaryQueue expectedQueue = new MailSummaryQueue();
    expectedQueue.add(category, entityId, mail);
    Map<String, MailSummaryQueue> expectedResult = Map.of(userId, expectedQueue);

    assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
  }

  private ScmMail createTestMail(String category, String entity) {
    return new ScmMail(
      category,
      entity,
      new ScmRecipient("Trainer Red", "trainer.red@mail.com"),
      new ScmRecipient("Trainer Blue", "trainer.blue@mail.com"),
      "Pokemon Champion",
      "Today"
    );
  }
}
