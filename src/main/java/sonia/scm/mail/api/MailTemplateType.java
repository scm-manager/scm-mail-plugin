/**
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
