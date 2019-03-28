// @flow

import type { Links } from "@scm-manager/ui-types";

export type MailConfiguration = {
  host: string,
  port: number,
  from: string,
  username: string,
  subjectPrefix: string,
  transportStrategy: string,
  language: string,
  _links: Links
};
