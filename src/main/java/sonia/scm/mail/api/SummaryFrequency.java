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

import lombok.Getter;

@Getter
public enum SummaryFrequency {
  MINUTES_15("0 0/15 * ? * * *"),
  HOURS_2("0 0 0/2 ? * * *"),
  HOURS_8("0 0 0/8 ? * * *");

  SummaryFrequency(String cronExpression) {
    this.cronExpression = cronExpression;
  }

  private final String cronExpression;
}
