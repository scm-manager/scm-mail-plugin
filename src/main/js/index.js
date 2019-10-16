// @flow
import React from "react";
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import GlobalMailConfiguration from "./GlobalMailConfiguration";
import UserMailConfigurationNavLink from "./UserMailConfigurationNavLink";
import type {Me} from "@scm-manager/ui-types";
import {binder} from "@scm-manager/ui-extensions";
import { Route } from "react-router-dom";
import UserMailConfigurationComponent from "./UserMailConfigurationComponent";

cfgBinder.bindGlobal("/mail", "scm-mail-plugin.nav-link", "mailConfig", GlobalMailConfiguration);

type ExtensionProps = {
  me: Me,
  url: string
};

const predicate = (props: ExtensionProps) => {
  return props.me && props.me._links.mailConfig;
};

const UserMailConfigNavLink = ({ url }) => {
  return <UserMailConfigurationNavLink url={url} />;
};

binder.bind(
  "profile.setting",
  UserMailConfigNavLink,
  predicate
);

const UserMailConfigurationRoute = (props) => {
  return (
    <Route
      path={`${props.url}/settings/mail-configuration`}
      render={() => <UserMailConfigurationComponent link={ props.me._links.mailConfig.href} /> }
    />
  );
};

binder.bind("profile.route", UserMailConfigurationRoute, predicate);
