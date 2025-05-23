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

/**
 * The template type defines the rendering method and the content type of the mail.
 */
public enum MailTemplateType {
  /**
   * Renders the given template with a template engine and passes the output to a markdown parser.
   * The final email will contain an html body with predefined styles and a text body.
   */
  MARKDOWN_HTML,

  /**
   * Renders the given template with a template engine and sends the output as html body.
   */
  HTML,

  /**
   * Renders the given template with a template engine and passes the output to a markdown parser.
   * The final email will be an plain text mail.
   */
  MARKDOWN_TEXT,

  /**
   * Renders the given template with a template engine and sends the output as plain text body.
   */
  TEXT
}
