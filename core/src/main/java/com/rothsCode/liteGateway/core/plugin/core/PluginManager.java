package com.rothsCode.liteGateway.core.plugin.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author roths
 * @Description:插件管理器
 * @date 2023/9/5 15:08
 */
public class PluginManager {

  public static final PluginManager pluginManager = new PluginManager();
  private Map<String, Plugin> pluginMap = new HashMap<>();
  private List<Plugin> pluginList = new ArrayList<>();

  public PluginManager() {
    ServiceLoader<Plugin> plugins = ServiceLoader.load(Plugin.class);
    for (Plugin plugin : plugins) {
      if (plugin.checkInit()) {
        pluginMap.put(plugin.pluginName(), plugin);
        pluginList.add(plugin);
      }
    }
  }

  public static PluginManager getInstance() {
    return pluginManager;
  }

  public Plugin getPluginByName(String pluginName) {
    return pluginMap.get(pluginName);
  }

  public List<Plugin> getPlugins() {
    return pluginList;
  }
}
