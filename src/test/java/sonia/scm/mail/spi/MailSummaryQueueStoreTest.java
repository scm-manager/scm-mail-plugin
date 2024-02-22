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
