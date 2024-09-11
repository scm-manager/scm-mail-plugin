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
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import GlobalMailConfiguration from "./GlobalMailConfiguration";
import UserMailConfigurationNavLink from "./UserMailConfigurationNavLink";
import { Me } from "@scm-manager/ui-types";
import { binder } from "@scm-manager/ui-extensions";
import { Route } from "react-router-dom";
import UserMailConfigurationComponent from "./UserMailConfigurationComponent";

cfgBinder.bindGlobal("/mail", "scm-mail-plugin.navLink", "mailConfig", GlobalMailConfiguration, "email");

type ExtensionProps = {
  me: Me;
  url: string;
};

const predicate = (props: ExtensionProps) => {
  return props.me && props.me._links.mailConfig;
};

const UserMailConfigNavLink = ({ url }) => {
  return <UserMailConfigurationNavLink url={url} />;
};

binder.bind("profile.setting", UserMailConfigNavLink, predicate);

const UserMailConfigurationRoute = props => {
  return (
    <Route
      path={`${props.url}/settings/mail-configuration`}
      render={() => <UserMailConfigurationComponent link={props.me._links.mailConfig.href} />}
    />
  );
};

binder.bind("profile.route", UserMailConfigurationRoute, predicate);
