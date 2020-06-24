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
import { Checkbox, DropDown, ErrorNotification, Loading, apiClient } from "@scm-manager/ui-components";
import { useTranslation, WithTranslation } from "react-i18next";
import { AvailableTopics, Topic, UserMailConfiguration } from "./MailConfiguration";

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

  type TopicInCategories = {
    [name: string]: Topic[];
  };

  const groupTopics = () => {
    const categories: TopicInCategories = {};
    new Set(availableTopics.topics.map(topic => topic.category.name)).forEach(
      categoryName =>
        (categories[categoryName] = availableTopics.topics.filter(topic => topic.category.name === categoryName))
    );
    return categories;
  };

  const topicSelected = (topic: Topic) => {
    if (!config?.excludedTopics) {
      return true;
    }
    return !config.excludedTopics.find(topicsEqual(topic));
  };

  const divideTopicsIntoColumns = () => {
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
    const groupedTopics = groupTopics();
    const topicCount = Object.entries(groupedTopics)
      .map(categoryWithTopics => categoryWithTopics[1])
      .map(topics => topics.length)
      .reduce((accumulator, n) => n + accumulator);
    const lineCount = topicCount + Object.entries(groupedTopics).length;

    let currentCount = 0;
    Object.entries(groupedTopics).forEach(categoryWithTopics => {
      if (currentCount + categoryWithTopics[1].length <= lineCount / 2) {
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

  let topics;

  const createTopicColumn = (topicsInCategories: TopicInCategories) => {
    return (
      <div className={"column is-half"}>
        {Object.entries(topicsInCategories).map(categoryWithTopics => (
          <>
            <label className="label">{t("mailTopics." + categoryWithTopics[0] + ".label")}</label>
            {categoryWithTopics[1].map(topic => (
              <Checkbox
                name={topic.category.name + "/" + topic.name}
                label={t("mailTopics." + categoryWithTopics[0] + "." + topic.name + ".label")}
                checked={topicSelected(topic)}
                onChange={topicChangedHandler(topic)}
                helpText={t("mailTopics." + categoryWithTopics[0] + "." + topic.name + ".helpText")}
              />
            ))}
          </>
        ))}
      </div>
    );
  };

  if (topicsLoading) {
    topics = <Loading />;
  } else if (availableTopics?.topics?.length > 0) {
    const topicColumns = divideTopicsIntoColumns();
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
