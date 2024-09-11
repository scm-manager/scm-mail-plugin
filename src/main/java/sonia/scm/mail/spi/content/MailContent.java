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

package sonia.scm.mail.spi.content;

public class MailContent {

  private final String text;
  private final String html;

  private MailContent(String text, String html) {
    this.text = text;
    this.html = html;
  }

  public String getHtml() {
    return html;
  }

  public String getText() {
    return text;
  }

  public static MailContent html(String content) {
    return new MailContent(null, content);
  }

  public static MailContent text(String content) {
    return new MailContent(content, null);
  }

  public static MailContent textAndHtml(String text, String html) {
    return new MailContent(text, html);
  }
}
