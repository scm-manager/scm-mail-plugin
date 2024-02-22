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

package sonia.scm.mail.api;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SummarizeMailConfigChangedEventTest {

  @ParameterizedTest
  @CsvSource(value = {"false,false,false", "false,true,false", "true,false,true", "true,true,false"})
  void shouldEvalIsSummaryDisabled(String oldSummarizeMails, String newSummarizeMails, String isSummaryDisabled) {
    UserMailConfiguration previousConfig = new UserMailConfiguration();
    previousConfig.setSummarizeMails(Boolean.parseBoolean(oldSummarizeMails));

    UserMailConfiguration newConfig = new UserMailConfiguration();
    newConfig.setSummarizeMails(Boolean.parseBoolean(newSummarizeMails));

    SummarizeMailConfigChangedEvent event = new SummarizeMailConfigChangedEvent(
      "userId", previousConfig, newConfig
    );

    boolean result = event.isSummarizeMailsDisabled();

    assertThat(result).isEqualTo(Boolean.parseBoolean(isSummaryDisabled));
  }

  @ParameterizedTest
  @CsvSource(value = {"MINUTES_15,HOURS_2,true", "MINUTES_15,MINUTES_15,false"})
  void shouldEvalIsFrequencyChanged(String oldFrequency, String newFrequency, String isChanged) {
    UserMailConfiguration previousConfig = new UserMailConfiguration();
    previousConfig.setSummaryFrequency(SummaryFrequency.valueOf(oldFrequency));

    UserMailConfiguration newConfig = new UserMailConfiguration();
    newConfig.setSummaryFrequency(SummaryFrequency.valueOf(newFrequency));

    SummarizeMailConfigChangedEvent event = new SummarizeMailConfigChangedEvent(
      "userId", previousConfig, newConfig
    );

    boolean result = event.isFrequencyChanged();

    assertThat(result).isEqualTo(Boolean.parseBoolean(isChanged));
  }
}
