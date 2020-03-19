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
import React from "react";
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import GlobalMailConfiguration from "./GlobalMailConfiguration";
import UserMailConfigurationNavLink from "./UserMailConfigurationNavLink";
import { Me } from "@scm-manager/ui-types";
import { binder } from "@scm-manager/ui-extensions";
import { Route } from "react-router-dom";
import UserMailConfigurationComponent from "./UserMailConfigurationComponent";

cfgBinder.bindGlobal("/mail", "scm-mail-plugin.navLink", "mailConfig", GlobalMailConfiguration);

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
