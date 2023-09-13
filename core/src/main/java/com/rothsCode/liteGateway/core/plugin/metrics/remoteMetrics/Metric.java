/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rothsCode.liteGateway.core.plugin.metrics.remoteMetrics;

import java.util.List;

/**
 * Metric.
 */
public final class Metric {

  private final MetricType type;

  /**
   * 指标名
   */
  private final String name;

  /**
   * 指标描述
   */
  private final String document;

  /**
   * 指标值
   */
  private final List<String> labels;

  /**
   * Instantiates a new Metric.
   *
   * @param type     the type
   * @param name     the name
   * @param document the document
   * @param labels   the labels
   */
  public Metric(final MetricType type, final String name, final String document,
      final List<String> labels) {
    this.type = type;
    this.name = name;
    this.document = document;
    this.labels = labels;
  }

  /**
   * Gets type.
   *
   * @return the type
   */
  public MetricType getType() {
    return type;
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets document.
   *
   * @return the document
   */
  public String getDocument() {
    return document;
  }

  /**
   * Gets labels.
   *
   * @return the labels
   */
  public List<String> getLabels() {
    return labels;
  }
}
