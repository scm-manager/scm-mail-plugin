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

import org.junit.jupiter.api.Test;
import sonia.scm.mail.api.ScmMail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MailSummaryQueueTest {

  @Test
  void addToCategory() {
    String pullRequestCategory = "pull requests";
    ScmMail firstPullRequestEmail = new ScmMail();
    ScmMail secondPullRequestEmail = new ScmMail();

    String mirrorCategory = "mirrors";
    ScmMail firstMirrorEmail = new ScmMail();

    MailSummaryQueue mailSummaryQueue = new MailSummaryQueue();

    mailSummaryQueue.add(pullRequestCategory, firstPullRequestEmail);
    mailSummaryQueue.add(pullRequestCategory, secondPullRequestEmail);
    mailSummaryQueue.add(mirrorCategory, firstMirrorEmail);

    assertThat(mailSummaryQueue.getMailQueueByCategoryAndEntityId()).isEqualTo(new HashMap<>());
    assertThat(mailSummaryQueue.getMailQueueByCategory()).isEqualTo(
      Map.of(
        pullRequestCategory, List.of(firstPullRequestEmail, secondPullRequestEmail),
        mirrorCategory, List.of(firstMirrorEmail)
      )
    );
  }

  @Test
  void addToCategoryAndEntity() {
    String pullRequestCategory = "pull requests";

    String firstPullRequestId = "1";
    ScmMail firstPullRequestEmail = new ScmMail();
    ScmMail secondPullRequestEmail = new ScmMail();

    String secondPullRequestId = "2";
    ScmMail thirdPullRequestEmail = new ScmMail();

    String mirrorCategory = "mirrors";
    String firstMirrorId = "1";
    ScmMail firstMirrorEmail = new ScmMail();

    MailSummaryQueue mailSummaryQueue = new MailSummaryQueue();

    mailSummaryQueue.add(pullRequestCategory, firstPullRequestId, firstPullRequestEmail);
    mailSummaryQueue.add(pullRequestCategory, firstPullRequestId, secondPullRequestEmail);
    mailSummaryQueue.add(pullRequestCategory, secondPullRequestId, thirdPullRequestEmail);
    mailSummaryQueue.add(mirrorCategory, firstMirrorId, firstMirrorEmail);

    assertThat(mailSummaryQueue.getMailQueueByCategory()).isEqualTo(new HashMap<>());
    assertThat(mailSummaryQueue.getMailQueueByCategoryAndEntityId()).isEqualTo(
      Map.of(
        pullRequestCategory, Map.of(
          firstPullRequestId, List.of(firstPullRequestEmail, secondPullRequestEmail),
          secondPullRequestId, List.of(thirdPullRequestEmail)
        ),
        mirrorCategory, Map.of(firstMirrorId, List.of(firstMirrorEmail))
      )
    );
  }
}
