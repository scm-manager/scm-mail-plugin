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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.mail.internal.LocalDateTimeAdapter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "scmMail")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScmMail {

  private String category;
  private String entityId;
  private ScmRecipient from;
  private ScmRecipient to;
  private String subject;
  private String plainText;

  @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
  private LocalDateTime createdAt = LocalDateTime.now();

  public ScmMail(String category, String entityId, ScmRecipient from, ScmRecipient to, String subject, String text) {
    this(category, entityId, from, to, subject, text, LocalDateTime.now());
  }
}
