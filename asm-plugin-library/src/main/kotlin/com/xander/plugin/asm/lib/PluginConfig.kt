package com.xander.plugin.asm.lib

open class PluginConfig {
  var log = true
  var debugSkip = false
  var releaseSkip = false
  var skipJar = true
  override fun toString(): String {
    return "PluginConfig(log=$log, debugSkip=$debugSkip, releaseSkip=$releaseSkip, skipJar=$skipJar)"
  }

  companion object {
    val debug = PluginConfig()
  }

}