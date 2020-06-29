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
import React, { FC, useEffect, useState } from "react";
import { Link } from "@scm-manager/ui-types";
import { Checkbox, DropDown, ErrorNotification, Help, Loading, apiClient } from "@scm-manager/ui-components";
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

  const topicsEqual = (t1: Topic) => {
    return (t2: Topic) => {
      return t1.category.name === t2.category.name && t1.name === t2.name;
    };
  };

  const languageChangedHandler = (value: string) => {
    const newConfig = { ...config, language: value };
    setConfig(newConfig);
    onConfigurationChange(newConfig, true);
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
                helpText={t("mailTopics." + categoryWithTopics[0] + "." + topic.name + ".helpText")}
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
        <label className="label">
          {t("scm-mail-plugin.topics.header")} <Help message={t("scm-mail-plugin.topics.helpText")} />
        </label>
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
          <DropDown
            options={[t("scm-mail-plugin.language.de"), t("scm-mail-plugin.language.en")]}
            optionValues={["de", "en"]}
            preselectedOption={config.language}
            optionSelected={languageChangedHandler}
            disabled={readOnly}
          />
        </div>
      </div>
      {topics}
    </>
  );
};

export default UserMailConfigurationForm;
