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

import React from "react";
import { Subtitle, Configuration } from "@scm-manager/ui-components";
import { withTranslation, WithTranslation } from "react-i18next";
import UserMailConfigurationForm from "./UserMailConfigurationForm";

type Props = WithTranslation & {
  link: string;
};

class UserMailConfigurationComponent extends React.Component<Props> {
  render() {
    const { t, link } = this.props;
    return (
      <>
        <Subtitle subtitle={t("scm-mail-plugin.form.header")} />
        <Configuration link={link} render={props => <UserMailConfigurationForm {...props} />} />
      </>
    );
  }
}

export default withTranslation("plugins")(UserMailConfigurationComponent);
