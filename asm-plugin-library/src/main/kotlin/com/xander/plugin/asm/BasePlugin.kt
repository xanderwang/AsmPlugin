package com.xander.plugin.asm

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 * @Description //TODO
 *
 * @author Xander Wang
 * Created on 2020/11/27.
 */
open abstract class BasePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val android = target.extensions.getByType(AppExtension::class.java)
        registerTransform(android, target)
    }

    abstract fun registerTransform(android: AppExtension, target: Project)
}