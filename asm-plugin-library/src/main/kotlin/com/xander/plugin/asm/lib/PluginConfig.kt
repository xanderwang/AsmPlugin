package com.xander.plugin.asm.lib

open class PluginConfig {
  var log = true
  var classLog = false
  var methodLog = false
  var debugSkip = false
  var releaseSkip = false
  var skipJar = true
  var timeAnnotation: String = "com.xander.dev.tool.DevTime"

  override fun toString(): String {
    return "PluginConfig(log=$log, debugSkip=$debugSkip, releaseSkip=$releaseSkip, skipJar=$skipJar)"
  }

  companion object {
    val debug = PluginConfig()
  }

}