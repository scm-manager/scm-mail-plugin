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

import { Links } from "@scm-manager/ui-types";

export type Category = {
  name: string;
};

export type Topic = {
  category: Category;
  name: string;
};

export type AvailableTopics = {
  topics: Topic[];
};

export type UserMailConfiguration = {
  language: string;
  summarizeMails: boolean;
  summarizeByEntity: boolean;
  summaryFrequency: string;
  excludedTopics?: Topic[];
  _links: Links;
};

export type MailConfiguration = {
  host: string;
  port: number;
  from: string;
  fromAddressAsSender: boolean;
  username: string;
  subjectPrefix: string;
  transportStrategy: string;
  language: string;
  _links: Links;
};
