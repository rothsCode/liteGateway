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

import com.rothsCode.liteGateway.core.config.GatewayConfigLoader;
import com.rothsCode.liteGateway.core.config.ServerConfig;
import com.rothsCode.liteGateway.core.plugin.core.Plugin;
import com.rothsCode.liteGateway.core.plugin.core.PluginEnum;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prometheus metric register.
 */

public final class PrometheusMetricsRegister implements MetricsRegister, Plugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusMetricsRegister.class);
  private static final Map<String, Counter> COUNTER_MAP = new ConcurrentHashMap<>();

  private static final Map<String, Gauge> GAUGE_MAP = new ConcurrentHashMap<>();

  private static final Map<String, Histogram> HISTOGRAM_MAP = new ConcurrentHashMap<>();
  private final AtomicBoolean registered = new AtomicBoolean(false);
  private HTTPServer server;
  private String prometheusHost;
  private int prometheusPort;
  private ServerConfig serverConfig;

  @Override
  public void registerCounter(final String name, final String[] labelNames, final String document) {
    if (!COUNTER_MAP.containsKey(name)) {
      Counter.Builder builder = Counter.build().name(name).help(document);
      if (null != labelNames) {
        builder.labelNames(labelNames);
      }
      COUNTER_MAP.putIfAbsent(name, builder.register());
    }
  }

  @Override
  public void registerGauge(final String name, final String[] labelNames, final String document) {
    if (!GAUGE_MAP.containsKey(name)) {
      Gauge.Builder builder = Gauge.build().name(name).help(document);
      if (null != labelNames) {
        builder.labelNames(labelNames);
      }
      GAUGE_MAP.putIfAbsent(name, builder.register());
    }
  }

  @Override
  public void registerHistogram(final String name, final String[] labelNames,
      final String document) {
    if (!HISTOGRAM_MAP.containsKey(name)) {
      Histogram.Builder builder = Histogram.build().name(name).help(document);
      if (null != labelNames) {
        builder.labelNames(labelNames);
      }
      HISTOGRAM_MAP.putIfAbsent(name, builder.register());
    }
  }

  @Override
  public void counterIncrement(final String name, final String[] labelValues, final long count) {
    Counter counter = COUNTER_MAP.get(name);
    if (Objects.isNull(counter)) {
      return;
    }
    if (null != labelValues) {
      counter.labels(labelValues).inc(count);
    } else {
      counter.inc(count);
    }
  }

  @Override
  public void gaugeIncrement(final String name, final String[] labelValues) {
    Gauge gauge = GAUGE_MAP.get(name);
    if (Objects.isNull(gauge)) {
      return;
    }
    if (null != labelValues) {
      gauge.labels(labelValues).inc();
    } else {
      gauge.inc();
    }
  }

  @Override
  public void gaugeDecrement(final String name, final String[] labelValues) {
    Gauge gauge = GAUGE_MAP.get(name);
    if (Objects.isNull(gauge)) {
      return;
    }
    if (null != labelValues) {
      gauge.labels(labelValues).dec();
    } else {
      gauge.dec();
    }
  }

  @Override
  public void recordTime(final String name, final String[] labelValues, final long duration) {
    Histogram histogram = HISTOGRAM_MAP.get(name);
    if (Objects.isNull(histogram)) {
      return;
    }
    if (null != labelValues) {
      histogram.labels(labelValues).observe(duration);
    } else {
      histogram.observe(duration);
    }
  }

  @Override
  public boolean checkInit() {
    serverConfig = GatewayConfigLoader.getInstance().getServerConfig();
    return serverConfig.getMetricsEnabled() && pluginName().equals(serverConfig.getMetricsType());
  }

  @Override
  public String pluginName() {
    return PluginEnum.PROMETHEUS.getCode();
  }

  @Override
  public void init() {
    prometheusHost = serverConfig.getPrometheusHost();
    prometheusPort = serverConfig.getPrometheusPort();
    if (serverConfig.getJvmEnabled()) {
      // 注册默认的JVM指标
      DefaultExports.initialize();
    }
    MetricsReporter.register(this);
  }

  @Override
  public void start() {
    if (!registered.compareAndSet(false, true)) {
      return;
    }
    if (prometheusPort > 0) {
      try {
        server = new HTTPServer(prometheusHost, prometheusPort);
      } catch (IOException e) {
        LOGGER.error("prometheus start error:{}", e);
        registered.set(false);
      }
    }
  }

  @Override
  public void shutDown() {
    if (server != null) {
      server.stop();
    }
    COUNTER_MAP.clear();
    GAUGE_MAP.clear();
    HISTOGRAM_MAP.clear();
    registered.set(false);
  }
}
