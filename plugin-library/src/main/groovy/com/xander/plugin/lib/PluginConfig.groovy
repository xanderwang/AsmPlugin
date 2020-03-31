package com.xander.plugin.lib;

class PluginConfig {
  Boolean log = true
  Boolean debugSkip = false
  Boolean releaseSkip = false
  Boolean skipJar = true

  @Override
  String toString() {
    return "PluginConfig{" + "log=" +
      log +
      ", debugSkip=" +
      debugSkip +
      ", releaseSkip=" +
      releaseSkip +
      ", skipJar=" +
      skipJar +
      '}';
  }
}
