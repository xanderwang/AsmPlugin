package io.github.xander.plugin.lib

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.tasks.Workers
import com.android.ide.common.workers.ExecutorServiceAdapter
import com.android.ide.common.workers.WorkerExecutorFacade
import com.google.common.io.Files
import groovy.lang.Reference
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.io.Serializable

/**
 * 基础的 transform ，封装了基本操作，后续只需要实现 IWeaverManager 就可以了
 */
abstract class BaseTransform(private val project: Project) : Transform() {

    companion object {
        private val APP_SCOPES: MutableSet<QualifiedContent.Scope> = HashSet()
        private val LIB_SCOPES: MutableSet<QualifiedContent.Scope> = HashSet()

        init {
            APP_SCOPES.add(QualifiedContent.Scope.PROJECT)
            APP_SCOPES.add(QualifiedContent.Scope.SUB_PROJECTS)
            APP_SCOPES.add(QualifiedContent.Scope.EXTERNAL_LIBRARIES)

            LIB_SCOPES.add(QualifiedContent.Scope.PROJECT)
        }
    }

    private var isAppType: Boolean

    /* 插件配置 */
    private var pluginConfig = BasePluginConfig.debug

    /* 编织器 */
    private var iWeaverManager: IWeaverManager

    /* 线程池 */
    private var executorFacade: WorkerExecutorFacade? = null

    /* 获取插件 name */
    override fun getName(): String {
        return this.javaClass.simpleName
    }

    /* 可以接受的类型 */
    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope>? {
        return if (isAppType) APP_SCOPES else LIB_SCOPES
    }

    /* 是否支持增量模式 */
    override fun isIncremental(): Boolean {
        return true
    }

    /* 是否可以缓存 */
    override fun isCacheable(): Boolean {
        return true
    }

    open fun createWorkerExecutorFacade(): WorkerExecutorFacade {
        // return ExecutorServiceAdapter(name, name, ForkJoinPool.commonPool())
        return Workers.withThreads(name, name)
    }

    /* 创建插件配置 */
    private fun initPluginConfig(): BasePluginConfig {
        return project.extensions.getByName(getConfigName()) as? BasePluginConfig ?: BasePluginConfig.debug
    }

    init {
        try {
            isAppType = project.extensions.getByType(AppExtension::class.java) != null
        } catch (e: Exception) {
            isAppType = false
        }
        // executorFacade = createWorkerExecutorFacade()
        iWeaverManager = createWeaverManager()
        project.extensions.create(getConfigName(), BasePluginConfig::class.java)
    }

    abstract fun createWeaverManager(): BaseWeaverManager
    abstract fun getConfigName(): String

    private fun applyConfig(config: BasePluginConfig) {
        BaseWeaverManager.pluginConfig = config
    }

    override fun transform(transformInvocation: TransformInvocation) {
        println("$name start transform ============================")
        // super.transform(transformInvocation)
        val skip = Reference(false)
        pluginConfig = initPluginConfig()
        println("$name config:$pluginConfig")
        applyConfig(pluginConfig)
        val variantName = transformInvocation.context.variantName.lowercase()
        if ("debug" == variantName) {
            skip.set(pluginConfig.debugSkip)
        } else if ("release" == variantName) {
            skip.set(pluginConfig.releaseSkip)
        }
        println("name: $name, variant:$variantName, skip:${skip.get()}")
        val startTime = System.currentTimeMillis()
        val outputProvider = transformInvocation.outputProvider
        if (!transformInvocation.isIncremental) {
            // 不支持增量，就全删除
            outputProvider.deleteAll()
        }
        val inputs = transformInvocation.inputs
        val referencedInputs = transformInvocation.referencedInputs
        var flagForCleanDexBuilderFolder = false

        for (input in inputs) {
            for (jarInput in input.jarInputs) {
                val dest = outputProvider.getContentLocation(jarInput.file.absolutePath, jarInput.contentTypes,
                        jarInput.scopes, Format.JAR)
                if (pluginConfig.log) {
                    println("jarInput:${jarInput.file.absolutePath} ,dest:${dest.absolutePath}")
                }
                if (skip.get() || pluginConfig.skipJar) {
                    println("skip transform jar: ${jarInput.file.absolutePath}")
                    FileUtils.copyFile(jarInput.file, dest)
                    continue
                }
                val status = jarInput.status
                if (transformInvocation.isIncremental) {
                    when (status) {
                        Status.NOTCHANGED -> {}
                        Status.ADDED, Status.CHANGED -> transformJar(jarInput.file, dest)
                        Status.REMOVED -> if (dest.exists()) FileUtils.forceDelete(dest)
                    }
                } else {
                    // Forgive me!, Some project will store 3rd-party aar for serveral copies
                    // in dexbuilder folder,unknown issue.
                    if (inDuplicatedClassSafeMode() && !flagForCleanDexBuilderFolder) {
                        cleanDexBuilderFolder(dest)
                        flagForCleanDexBuilderFolder = true
                    }
                    transformJar(jarInput.file, dest)
                }
            }

            for (directoryInput in input.directoryInputs) {
                val dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes,
                        directoryInput.scopes, Format.DIRECTORY)
                if (pluginConfig.log) {
                    println("directoryInput:${directoryInput.file.absolutePath},dest:${dest.absolutePath}")
                }
                FileUtils.forceMkdir(dest)
                if (skip.get()) {
                    println("skip transform dir:${directoryInput.file.absolutePath}")
                    FileUtils.copyDirectory(directoryInput.file, dest)
                    continue
                }
                if (transformInvocation.isIncremental) {
                    val srcDirPath = directoryInput.file.absolutePath
                    val destDirPath = dest.absolutePath
                    val fileStatusMap = directoryInput.changedFiles
                    for ((inputFile, status) in fileStatusMap) {
                        val destFilePath = inputFile.absolutePath.replace(srcDirPath, destDirPath)
                        val destFile = File(destFilePath)
                        when (status) {
                            Status.NOTCHANGED -> {}
                            Status.REMOVED -> if (destFile.exists()) {
                                destFile.delete()
                            }
                            Status.ADDED, Status.CHANGED -> {
                                try {
                                    FileUtils.touch(destFile)
                                } catch (e: IOException) { //maybe mkdirs fail for some strange reason, try again.
                                    Files.createParentDirs(destFile)
                                }
                                transformSingleFile(inputFile, destFile, srcDirPath)
                            }
                        }
                    }
                } else {
                    transformDir(directoryInput.file, directoryInput.file.absolutePath, dest.absolutePath)
                }
            }
        }
        if (pluginConfig.useExecutor) {
            try {
                executorFacade?.await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val costTime = System.currentTimeMillis() - startTime
        println("plugin: $name, cost time: $costTime ms")
        println("${getConfigName()} end transform ============================")
    }

    protected fun transformDir(sourceDir: File, inputDirPath: String, outputDirPath: String) {
        if (sourceDir.isDirectory) {
            val files = sourceDir.listFiles()
            if (null == files || files.isEmpty()) {
                return
            }
            val childFiles = ArrayList<File>()
            for (sourceFile in files) {
                if (sourceFile.isDirectory) {
                    transformDir(sourceFile, inputDirPath, outputDirPath)
                } else {
                    childFiles.add(sourceFile)
                }
            }
            if (childFiles.isNotEmpty()) {
                transformFileList(childFiles, inputDirPath, outputDirPath)
            }
        }
    }

    protected fun transformFileList(sourceList: ArrayList<File>, inputDirPath: String, outputDirPath: String) {
        if (pluginConfig.log) {
            println("transformFileList inputDirPath:${inputDirPath}")
            println("transformFileList outputDirPath:${outputDirPath}")
        }
        for (sourceFile in sourceList) {
            val sourceFilePath = sourceFile.absolutePath
            val outputFile = File(sourceFilePath.replace(inputDirPath, outputDirPath))
            if (pluginConfig.log) {
                println("transformFileList sourceFile:${sourceFile.absolutePath}")
                println("transformFileList outputFile:${outputFile.absolutePath}")
            }
            iWeaverManager.weaveSingleClass(sourceFile, outputFile, inputDirPath)
        }
    }

    protected fun transformSingleFile(inputFile: File, outputFile: File, srcBaseDir: String) {
        if (pluginConfig.log) {
            println("transformSingleFile inputFile:${inputFile.absolutePath}")
            println("transformSingleFile outputFile:${outputFile.absolutePath}")
        }
        if (pluginConfig.useExecutor) {
            executorFacade?.submit(WeaveSingleClassTask::class.java,
                    WeaveSingleClassTask.Params(iWeaverManager, inputFile, outputFile, srcBaseDir))
        } else {
            iWeaverManager.weaveSingleClass(inputFile, outputFile, srcBaseDir)
        }
    }

    protected fun transformJar(srcJar: File, destJar: File) {
        if (pluginConfig.log) {
            println("transformJar srcJar:${srcJar.absolutePath}")
            println("transformJar destJar:${destJar.absolutePath}")
        }
        if (pluginConfig.useExecutor) {
            executorFacade?.submit(WeaveJarTask::class.java, WeaveJarTask.Params(iWeaverManager, srcJar, destJar))
        } else {
            iWeaverManager.weaveJar(srcJar, destJar)
        }
    }

    protected open fun cleanDexBuilderFolder(dest: File) {
        try {
            val dexBuilderDir = replaceLastPart(dest.absolutePath, name, "dexBuilder")
            //intermediates/transforms/dexBuilder/debug
            val file = File(dexBuilderDir).parentFile
            println("clean dexBuilder folder:${file.absolutePath}")
            if (file.exists() && file.isDirectory) {
                FileUtils.deleteDirectory(file)
                //com.android.utils.FileUtils.deleteDirectoryContents(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected open fun replaceLastPart(originString: String, replacement: String, toReplace: String): String {
        val start = originString.lastIndexOf(replacement)
        val builder = StringBuilder()
        builder.append(originString.substring(0, start))
        builder.append(toReplace)
        builder.append(originString.substring(start + replacement.length))
        return builder.toString()
    }

    protected open fun inDuplicatedClassSafeMode(): Boolean {
        return false
    }

    class WeaveSingleClassTask(private val params: Params) : Runnable {
        /**
         * task 业务逻辑
         */
        override fun run() {
            params.weaver.weaveSingleClass(params.inputFile, params.outputFile, params.inputBaseDir)
        }

        /**
         * 需要的参数
         */
        data class Params(val weaver: IWeaverManager, val inputFile: File, val outputFile: File,
            val inputBaseDir: String) : Serializable

    }

    class WeaveJarTask(private val params: Params) : Runnable {

        /**
         * task 业务逻辑
         */
        override fun run() {
            params.weaver.weaveJar(params.srcJar, params.destJar)
        }

        /**
         * 需要的参数
         */
        data class Params(val weaver: IWeaverManager, val srcJar: File, val destJar: File) : Serializable

    }
}