package com.xander.plugin.asm

import com.android.build.api.transform.Transform
import org.gradle.api.Project

open interface IPluginFactory {
  fun createTransform(project: Project): Transform
}