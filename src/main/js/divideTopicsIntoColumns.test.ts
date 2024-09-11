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
