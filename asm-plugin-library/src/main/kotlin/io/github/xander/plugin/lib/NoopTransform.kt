package io.github.xander.plugin.lib

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.io.IOException
import java.util.*

open class NoopTransform(var project: Project) : Transform() {

    override fun getName(): String {
        return "NoopTransform"
    }

    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope>? {
        return SCOPES
    }

    override fun isIncremental(): Boolean { // 开启增量编译
        return true
    }

    override fun isCacheable(): Boolean {
        return true
    }

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope>? {
        return TransformManager.EMPTY_SCOPES
    }

    @Throws(TransformException::class, InterruptedException::class, IOException::class)
    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        println("transform:----------------------------------------start $name") // 这里是关键代码，就是如何处理字节码的，处理自己码我们需要 asm 工具
        // 当前是否是增量编译
        val isIncremental = transformInvocation.isIncremental
        println("transform:----------------------------------------isIncremental $isIncremental") // 消费型输入，可以从中获取 jar 包和 class 文件夹路径。需要输出给下一个任务
        val inputs = transformInvocation.inputs // 引用型输入，无需输出。
        val referencedInputs = transformInvocation.referencedInputs // OutputProvider 管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
        val outputProvider = transformInvocation.outputProvider
        println("outputProvider: $outputProvider")
        if (!isIncremental) { // 如果不是增量编译，那么就清空旧的输出内容
            outputProvider.deleteAll()
        }
        for (input in inputs) {
            for (jarInput in input.jarInputs) {
                val dest = outputProvider.getContentLocation(jarInput.file.absolutePath, jarInput.contentTypes,
                        jarInput.scopes, Format.JAR)
                println("transform jar:${jarInput.file.absolutePath} dest:${dest.absolutePath}") // 将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
                FileUtils.copyFile(jarInput.file, dest)
            }
            for (directoryInput in input.directoryInputs) {
                val dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes,
                        directoryInput.scopes, Format.DIRECTORY)
                println("transform directory:${directoryInput.file.absolutePath} dest:${dest.absolutePath}") //将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }
        println(":----------------------------------------:end $name")
    }

    companion object {
        @JvmStatic
        private val SCOPES: MutableSet<QualifiedContent.Scope> = HashSet()
    }
}