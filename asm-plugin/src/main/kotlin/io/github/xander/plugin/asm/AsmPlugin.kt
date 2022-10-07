package io.github.xander.plugin.asm

import com.android.build.api.transform.Transform
import io.github.xander.plugin.lib.BasePlugin
import org.gradle.api.Project

class AsmPlugin : BasePlugin() {

    override fun createTransforms(project: Project): List<out Transform> {
        return listOf(AsmTransform(project))
    }

}