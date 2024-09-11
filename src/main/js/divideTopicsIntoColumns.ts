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
