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
