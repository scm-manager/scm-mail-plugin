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

import React, { FC, useEffect, useState } from "react";
import { Link } from "@scm-manager/ui-types";
import { Checkbox, ErrorNotification, Help, Loading, apiClient, Select } from "@scm-manager/ui-components";
import { useTranslation, WithTranslation } from "react-i18next";
import { AvailableTopics, Topic, UserMailConfiguration } from "./MailConfiguration";
import { divideTopicsIntoColumns } from "./divideTopicsIntoColumns";

type Props = WithTranslation & {
  initialConfiguration: UserMailConfiguration;
  readOnly: boolean;
  onConfigurationChange: (p1: UserMailConfiguration, p2: boolean) => void;
};

const UserMailConfigurationForm: FC<Props> = ({ initialConfiguration, readOnly, onConfigurationChange }) => {
  const [config, setConfig] = useState<UserMailConfiguration>(initialConfiguration);
  const [availableTopics, setAvailableTopics] = useState<AvailableTopics>({ topics: [] });
  const [topicsLoading, setTopicsLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error>();
  const [t] = useTranslation("plugins");

  const getContent = (url: string) => {
    return apiClient.get(url).then(response => response.json());
  };

  useEffect(() => {
    getContent((config._links.availableTopics as Link).href)
      .then(topics => {
        setTopicsLoading(false);
        setAvailableTopics(topics);
      })
      .catch(error => {
        setTopicsLoading(false);
        setError(error);
      });
  }, [initialConfiguration]);

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (topicsLoading) {
    return <Loading />;
  }

  const languageChangedHandler = (value: string) => {
    const newConfig: UserMailConfiguration = { ...config, language: value };
    setConfig(newConfig);
    onConfigurationChange(newConfig, true);
  };

  const summarizeEmailChangedHandler = (value: boolean) => {
    const newConfig: UserMailConfiguration = { ...config, summarizeMails: value };
    setConfig(newConfig);
    onConfigurationChange(newConfig, true);
  };

  const summarizeByEntityChangedHandler = (value: boolean) => {
    const newConfig: UserMailConfiguration = { ...config, summarizeByEntity: value };
    setConfig(newConfig);
    onConfigurationChange(newConfig, true);
  };

  const summaryFrequencyChangedHandler = (value: string) => {
    const newConfig: UserMailConfiguration = { ...config, summaryFrequency: value };
    setConfig(newConfig);
    onConfigurationChange(newConfig, true);
  };

  const mapTranslationKeys = (values: string[], name: string) => {
    return values.map(value => ({
      value,
      label: t(`scm-mail-plugin.${name}.${value}`)
    }));
  };

  const topicsEqual = (t1: Topic) => {
    return (t2: Topic) => {
      return t1.category.name === t2.category.name && t1.name === t2.name;
    };
  };

  const topicChangedHandler = (topic: Topic) => {
    return (value: boolean) => {
      let newExcludedTopics;
      if (value) {
        if (config.excludedTopics) {
          newExcludedTopics = config.excludedTopics.filter(other => !topicsEqual(topic)(other));
        }
      } else {
        newExcludedTopics = config.excludedTopics ? [...config.excludedTopics] : [];
        newExcludedTopics.push(topic);
      }
      const newConfig = { ...config, excludedTopics: newExcludedTopics };
      setConfig(newConfig);
      onConfigurationChange(newConfig, true);
    };
  };

  const categoryChangedHandler = (category: string) => {
    return (value: boolean) => {
      let newExcludedTopics;
      if (value) {
        if (config.excludedTopics) {
          newExcludedTopics = config.excludedTopics.filter(other => other.category.name !== category);
        }
      } else {
        newExcludedTopics = config.excludedTopics ? [...config.excludedTopics] : [];
        availableTopics.topics
          .filter(topic => topic.category.name === category)
          .forEach(topic => newExcludedTopics.push(topic));
      }
      const newConfig = { ...config, excludedTopics: newExcludedTopics };
      setConfig(newConfig);
      onConfigurationChange(newConfig, true);
    };
  };

  type TopicInCategories = {
    [name: string]: Topic[];
  };

  const isTopicSelected = (topic: Topic) => {
    if (!config?.excludedTopics) {
      return true;
    }
    return !config.excludedTopics.find(topicsEqual(topic));
  };

  let topics;

  const createTopicColumn = (topicsInCategories: TopicInCategories) => {
    return (
      <div className={"column is-half"}>
        {Object.entries(topicsInCategories).map(categoryWithTopics => (
          <>
            <label className="label">
              <Checkbox
                label={t("mailTopics." + categoryWithTopics[0] + ".label")}
                name={categoryWithTopics[0]}
                checked={!!categoryWithTopics[1].find(isTopicSelected)}
                indeterminate={
                  categoryWithTopics[1].find(isTopicSelected) &&
                  categoryWithTopics[1].find(topic => !isTopicSelected(topic))
                }
                onChange={categoryChangedHandler(categoryWithTopics[0])}
              />
            </label>
            {categoryWithTopics[1].map(topic => (
              <Checkbox
                name={topic.category.name + "/" + topic.name}
                label={t("mailTopics." + categoryWithTopics[0] + "." + topic.name + ".label")}
                checked={isTopicSelected(topic)}
                onChange={topicChangedHandler(topic)}
                helpText={t("mailTopics." + categoryWithTopics[0] + "." + topic.name + ".helpText", "")}
              />
            ))}
          </>
        ))}
      </div>
    );
  };

  if (availableTopics?.topics?.length > 0) {
    const topicColumns = divideTopicsIntoColumns(availableTopics);
    const firstColumn = topicColumns[0];
    const secondColumn = topicColumns[1];
    topics = (
      <>
        <label className="label">{t("scm-mail-plugin.topics.header")}</label>
        <div className="columns">
          {createTopicColumn(firstColumn)}
          {createTopicColumn(secondColumn)}
        </div>
      </>
    );
  } else {
    topics = null;
  }

  return (
    <>
      <div className="field">
        <label className="label">{t("scm-mail-plugin.form.language")}</label>
        <div className="control">
          <Select
            onChange={languageChangedHandler}
            options={mapTranslationKeys(["de", "en"], "language")}
            disabled={readOnly}
            defaultValue={config.language}
          />
        </div>
        <div className="control">
          <Checkbox
            name="summarizeMails"
            label={t("scm-mail-plugin.form.summarizeMails")}
            onChange={e => summarizeEmailChangedHandler(e.valueOf())}
            checked={config.summarizeMails}
            helpText={t("scm-mail-plugin.form.summarizeMailsHelpText")}
          />
        </div>
        {config.summarizeMails ? (
          <>
            <div className="control">
              <Checkbox
                name="summarizeEmail"
                label={t("scm-mail-plugin.form.summarizeByEntity")}
                onChange={e => summarizeByEntityChangedHandler(e.valueOf())}
                checked={config.summarizeByEntity}
                helpText={t("scm-mail-plugin.form.summarizeByEntityHelpText")}
              />
            </div>
            <label className="label">{t("scm-mail-plugin.form.summaryFrequency")}</label>
            <div className="control">
              <Select
                onChange={summaryFrequencyChangedHandler}
                options={mapTranslationKeys(["MINUTES_15", "HOURS_2", "HOURS_8"], "summaryFrequency")}
                disabled={readOnly}
                defaultValue={config.summaryFrequency}
              />
            </div>
          </>
        ) : null}
      </div>
      {topics}
    </>
  );
};

export default UserMailConfigurationForm;
