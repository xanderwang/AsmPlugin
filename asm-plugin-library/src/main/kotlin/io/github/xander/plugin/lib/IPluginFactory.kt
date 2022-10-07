package io.github.xander.plugin.lib

import com.android.build.api.transform.Transform
import org.gradle.api.Project

/**
 * 插件 factory ，用于创建创建实例
 */
open interface IPluginFactory {

    /**
     * 创建一个 factory
     */
    fun createTransform(project: Project): Transform
}