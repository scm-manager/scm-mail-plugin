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
import { AvailableTopics } from "./MailConfiguration";
import { divideTopicsIntoColumns } from "./divideTopicsIntoColumns";

describe("divide topics into columns", () => {
  it("should put larger category in first column", () => {
    const availableTopics: AvailableTopics = {
      topics: [
        { category: { name: "hitchhikers guide" }, name: "releases" },
        { category: { name: "deep thought" }, name: "answer" },
        { category: { name: "hitchhikers guide" }, name: "updates" },
        { category: { name: "deep thought" }, name: "question" },
        { category: { name: "hitchhikers guide" }, name: "amendments" }
      ]
    };

    const topicInCategories = divideTopicsIntoColumns(availableTopics);

    expect(topicInCategories[0]).toStrictEqual({
      "hitchhikers guide": [
        { category: { name: "hitchhikers guide" }, name: "releases" },
        { category: { name: "hitchhikers guide" }, name: "updates" },
        { category: { name: "hitchhikers guide" }, name: "amendments" }
      ]
    });
    expect(topicInCategories[1]).toStrictEqual({
      "deep thought": [
        { category: { name: "deep thought" }, name: "answer" },
        { category: { name: "deep thought" }, name: "question" }
      ]
    });
  });
  it("should take header into account", () => {
    const availableTopics: AvailableTopics = {
      topics: [
        { category: { name: "hitchhikers guide" }, name: "releases" },
        { category: { name: "heart of gold" }, name: "probability level" },
        { category: { name: "hitchhikers guide" }, name: "updates" },
        { category: { name: "marvin" }, name: "mourning" }
      ]
    };

    const topicInCategories = divideTopicsIntoColumns(availableTopics);

    expect(topicInCategories[0]).toStrictEqual({
      "heart of gold": [{ category: { name: "heart of gold" }, name: "probability level" }],
      marvin: [{ category: { name: "marvin" }, name: "mourning" }]
    });
    expect(topicInCategories[1]).toStrictEqual({
      "hitchhikers guide": [
        { category: { name: "hitchhikers guide" }, name: "releases" },
        { category: { name: "hitchhikers guide" }, name: "updates" }
      ]
    });
  });
});
