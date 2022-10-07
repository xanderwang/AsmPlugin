package io.github.xander.plugin.asm

import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import io.github.xander.plugin.lib.BaseTransform
import io.github.xander.plugin.lib.BaseWeaverManager
import org.gradle.api.Project
import java.io.IOException

class AsmTransform(project: Project) : BaseTransform(project) {

    override fun createWeaverManager(): BaseWeaverManager {
        return BaseWeaverManager()
    }

    override fun getConfigName(): String {
        return CONFIG
    }

    @Throws(TransformException::class, InterruptedException::class, IOException::class)
    override fun transform(transformInvocation: TransformInvocation) {
        try {
            super.transform(transformInvocation)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        var CONFIG = "asmConfig"
    }

}