// @flow

import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import GlobalMailConfiguration from "./GlobalMailConfiguration";

cfgBinder.bindGlobal("/mail", "scm-mail-plugin.nav-link", "mailConfig", GlobalMailConfiguration);
