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
