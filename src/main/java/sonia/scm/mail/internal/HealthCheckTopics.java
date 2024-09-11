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

package sonia.scm.mail.internal;

import sonia.scm.mail.api.Topic;
import sonia.scm.mail.api.TopicProvider;
import sonia.scm.plugin.Extension;

import java.util.Collection;
import java.util.Collections;

import static sonia.scm.mail.internal.HealthCheckFailedHook.TOPIC_HEALTH_CHECK_FAILED;

@Extension
public class HealthCheckTopics implements TopicProvider {

  @Override
  public Collection<Topic> topics() {
    return Collections.singletonList(
      TOPIC_HEALTH_CHECK_FAILED
    );
  }
}
