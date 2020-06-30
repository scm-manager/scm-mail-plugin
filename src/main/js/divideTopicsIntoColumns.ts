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

import { AvailableTopics, Topic } from "./MailConfiguration";

export type TopicInCategories = {
  [name: string]: Topic[];
};

const groupTopics = (availableTopics: AvailableTopics) => {
  const categories: TopicInCategories = {};
  new Set(availableTopics.topics.map(topic => topic.category.name)).forEach(
    categoryName =>
      (categories[categoryName] = availableTopics.topics.filter(topic => topic.category.name === categoryName))
  );
  return categories;
};

export const divideTopicsIntoColumns = (availableTopics: AvailableTopics) => {
  // We will create two 'column' objects containing the categories with their
  // corresponding topics.
  // To get two columns with a more or less equal number of topics, first we
  // fill up the second column unless we have not put more than the half of
  // all topics into it. The rest of the topics will go into the first column.
  // Doing this we will assure that we utilize the columns best and that the
  // second column is not longer than the first.
  // To take the category headings into account, we count them as topics.
  const firstColumn: TopicInCategories = {};
  const secondColumn: TopicInCategories = {};
  const groupedTopics = groupTopics(availableTopics);
  let lineCount = 0;
  Object.entries(groupedTopics).forEach(categoryWithTopics => (lineCount += categoryWithTopics[1].length + 1));

  let currentCount = 0;
  Object.entries(groupedTopics).forEach(categoryWithTopics => {
    if (currentCount + categoryWithTopics[1].length + 1 <= lineCount / 2) {
      currentCount += categoryWithTopics[1].length + 1;
      secondColumn[categoryWithTopics[0]] = categoryWithTopics[1];
    }
  });
  Object.entries(groupedTopics).forEach(categoryWithTopics => {
    if (!secondColumn[categoryWithTopics[0]]) {
      firstColumn[categoryWithTopics[0]] = categoryWithTopics[1];
    }
  });

  return [firstColumn, secondColumn];
};
