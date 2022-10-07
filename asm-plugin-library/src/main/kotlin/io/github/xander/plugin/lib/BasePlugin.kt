package io.github.xander.plugin.lib

import com.android.build.api.transform.Transform
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestedExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 * @author Xander Wang
 * Created on 2020/11/27.
 */
abstract class BasePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val transforms = createTransforms(project)
        registerTransforms(AppExtension::class.java, project, transforms)
        registerTransforms(LibraryExtension::class.java, project, transforms)

    }

    private fun registerTransforms(type: Class<out TestedExtension>, project: Project, transforms: List<Transform>) {
        try {
            val extension: TestedExtension = project.extensions.getByType(type)
            transforms.forEach { transform ->
                extension.registerTransform(transform)
            }
        } catch (e: Exception) {
            println(e)
        }
    }

    abstract fun createTransforms(project: Project): List<out Transform>
}