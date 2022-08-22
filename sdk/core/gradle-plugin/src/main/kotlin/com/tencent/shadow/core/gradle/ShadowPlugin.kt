/*
 * Tencent is pleased to support the open source community by making Tencent Shadow available.
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.tencent.shadow.core.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.sdklib.AndroidVersion.VersionCodes
import com.tencent.shadow.core.gradle.extensions.PackagePluginExtension
import com.tencent.shadow.core.manifest_parser.generatePluginManifest
import com.tencent.shadow.core.transform.ShadowTransform
import com.tencent.shadow.core.transform_kit.AndroidClassPoolBuilder
import com.tencent.shadow.core.transform_kit.ClassPoolBuilder
import org.gradle.api.*
import java.io.File

class ShadowPlugin : Plugin<Project> {

    private lateinit var androidClassPoolBuilder: ClassPoolBuilder
    private lateinit var contextClassLoader: ClassLoader
    private lateinit var agpCompat: AGPCompat

    override fun apply(project: Project) {
        agpCompat = buildAgpCompat(project)
        val baseExtension = project.extensions.getByName("android") as BaseExtension

        //在这里取到的contextClassLoader包含运行时库(classpath方式引入的)shadow-runtime
        contextClassLoader = Thread.currentThread().contextClassLoader
        val lateInitBuilder = object : ClassPoolBuilder {
            override fun build() = androidClassPoolBuilder.build()
        }

        val shadowExtension = project.extensions.create("shadow", ShadowExtension::class.java)
        if (!project.hasProperty("disable_shadow_transform")) {
            baseExtension.registerTransform(ShadowTransform(
                    project,
                    lateInitBuilder,
                    { shadowExtension.transformConfig.useHostContext }
            ))
        }

        addFlavorForTransform(baseExtension)

        project.extensions.create("packagePlugin", PackagePluginExtension::class.java, project)

        project.afterEvaluate {
            initAndroidClassPoolBuilder(baseExtension, project)

            createPackagePluginTasks(project)

            onEachPluginVariant(project) { pluginVariant ->
                checkAaptPackageIdConfig(pluginVariant)

                val appExtension: AppExtension =
                        project.extensions.getByType(AppExtension::class.java)
                createGeneratePluginManifestTasks(project, appExtension, pluginVariant)
            }
        }

        checkKotlinAndroidPluginForPluginManifestTask(project)
    }

    /**
     * GeneratePluginManifestTask会向android DSL添加新的java源码目录，
     * 而kotlin-android会在syncKotlinAndAndroidSourceSets中接管java的源码目录，
     * 从而使后添加到android DSL中的java目录失效。
     */
    private fun checkKotlinAndroidPluginForPluginManifestTask(project: Project) {
        if (project.plugins.hasPlugin("kotlin-android")) {
            throw Error("必须在kotlin-android之前应用com.tencent.shadow.plugin")
        }
    }

    private fun createPackagePluginTasks(project: Project) {
        val packagePlugin = project.extensions.findByName("packagePlugin")
        val extension = packagePlugin as PackagePluginExtension
        val buildTypes = extension.buildTypes

        val tasks = mutableListOf<Task>()
        for (i in buildTypes) {
            project.logger.info("buildTypes = " + i.name)
            val task = createPackagePluginTask(project, i)
            tasks.add(task)
        }
        if (tasks.isNotEmpty()) {
            project.tasks.create("packageAllPlugin") {
                it.group = "plugin"
                it.description = "打包所有插件"
            }.dependsOn(tasks)
        }
    }

    private fun onEachPluginVariant(project: Project, actions: (ApplicationVariant) -> Unit) {
        val appExtension: AppExtension = project.extensions.getByType(AppExtension::class.java)
        val pluginVariants = appExtension.applicationVariants.filter { variant ->
            variant.productFlavors.any { flavor ->
                flavor.dimension == ShadowTransform.DimensionName &&
                        flavor.name == ShadowTransform.ApplyShadowTransformFlavorName
            }
        }

        checkPluginVariants(pluginVariants, appExtension, project.name)

        pluginVariants.forEach(actions)
    }

    /**
     * 创建生成PluginManifest.java的任务
     */
    private fun createGeneratePluginManifestTasks(
            project: Project,
            appExtension: AppExtension,
            pluginVariant: ApplicationVariant
    ) {
        val output = pluginVariant.outputs.first()

        val processManifestTask = agpCompat.getProcessManifestTask(output)
        val manifestFile = agpCompat.getManifestFile(processManifestTask)
        val variantName = pluginVariant.name
        val outputDir = File(project.buildDir, "generated/source/pluginManifest/$variantName")

        // 添加生成PluginManifest.java任务
        val task = project.tasks.register("generate${variantName.capitalize()}PluginManifest") {
            it.dependsOn(processManifestTask)
            it.inputs.file(manifestFile)
            it.outputs.dir(outputDir).withPropertyName("outputDir")

            val packageForR = agpCompat.getPackageForR(project, variantName)

            it.doLast {
                generatePluginManifest(
                        manifestFile,
                        outputDir,
                        "com.tencent.shadow.core.manifest_parser",
                        packageForR
                )
            }
        }
        project.tasks.getByName("compile${variantName.capitalize()}JavaWithJavac")
                .dependsOn(task)

        // 把PluginManifest.java添加为源码
        appExtension.sourceSets.getByName(variantName).java.srcDir(outputDir)
    }

    /**
     * 检查插件是否修改了资源ID分区
     *
     * 因为CreateResourceBloc在为插件创建Resources对象时，
     * 将宿主和插件的apk都放进去了，所以不能让宿主和插件的资源ID冲突。详见CreateResourceBloc注释。
     *
     * 此任务只是检查任务，对构建无影响。
     */
    private fun checkAaptPackageIdConfig(pluginVariant: ApplicationVariant) {
        val output = pluginVariant.outputs.first()
        val minSdkVersion = agpCompat.getMinSdkVersion(pluginVariant)
        val processResourcesTask = agpCompat.getProcessResourcesTask(output)

        processResourcesTask.doFirst {
            val parameterList = agpCompat.getAaptAdditionalParameters(processResourcesTask)
            var foundPackageIdParameter = false
            parameterList.forEachIndexed { index, parameter ->
                if (parameter == "--package-id" && parameterList.size >= index + 2) {
                    val packageIdSetting = parameterList[index + 1]
                    val packageIdValue = Integer.decode(packageIdSetting)

                    if (minSdkVersion > VersionCodes.O) {
                        if (packageIdValue <= 0x7f) {
                            throw Error("minSdkVersion大于26时--package-id必须大于0x7f")
                        } else {
                            foundPackageIdParameter = true
                        }
                    } else {
                        if (packageIdValue >= 0x7f) {
                            /*
                            为了兼容minSDK小于26，且packageId大于0x7f时Android系统的bug，aapt对id进行了修改，
                            导致Resources中记录的id值和layout中使用的id值不一致。
                            但是minSDK小于26时可以使用--allow-reserved-package-id选项使用小于0x7f的值。
                            https://android.googlesource.com/platform/frameworks/base/+/master/tools/aapt2/readme.md#version-2_14
                            https://developer.android.com/studio/command-line/aapt2#link_options
                             */
                            throw Error(
                                    "minSdkVersion小于26时--package-id必须小于0x7f，" +
                                            "同时使用--allow-reserved-package-id选项。"
                            )
                        } else {
                            foundPackageIdParameter = true
                        }
                    }
                }
            }
            if (!foundPackageIdParameter) {
                val example1 = "aaptOptions {\n" +
                        "    additionalParameters \"--package-id\", \"0x80\"\n" +
                        "}"
                val example2 = "aaptOptions {\n" +
                        "    additionalParameters \"--package-id\", \"0x7E\", \"--allow-reserved-package-id\"\n" +
                        "}"
                val example = if (minSdkVersion > VersionCodes.O) example1 else example2
                throw Error(
                        "插件需要利用aapt2的修改资源ID前缀的选项使其与宿主不同。\n" +
                                "没有找到--package-id参数。示例：\n" + example
                )
            }
        }
    }

    private fun checkPluginVariants(
            pluginVariants: List<ApplicationVariant>,
            appExtension: AppExtension,
            projectName: String
    ) {
        if (pluginVariants.isEmpty()) {
            val errorMessage = StringBuilder()
            errorMessage.appendLine("在${projectName}中找不到Shadow所添加的Dimension flavor")
            errorMessage.appendLine("当前所有flavor打印如下：")
            appExtension.applicationVariants.forEach { variant ->
                errorMessage.appendLine("variant.name：${variant.name}")
                variant.productFlavors.forEach { flavor ->
                    errorMessage.appendLine(
                            "flavor.name：${flavor.name} flavor.dimension：${flavor.dimension} "
                    )
                }
            }
            errorMessage.appendLine("提示：添加flavorDimension时，不要覆盖已有flavorDimension")
            errorMessage.appendLine("示例：flavorDimensions(*flavorDimensionList, 'new')")
            throw Error(errorMessage.toString())
        }
    }

    private fun addFlavorForTransform(baseExtension: BaseExtension) {
        agpCompat.addFlavorDimension(baseExtension, ShadowTransform.DimensionName)
        try {
            baseExtension.productFlavors.create(ShadowTransform.NoShadowTransformFlavorName) {
                it.dimension = ShadowTransform.DimensionName
                agpCompat.setProductFlavorDefault(it, true)
            }
            baseExtension.productFlavors.create(ShadowTransform.ApplyShadowTransformFlavorName) {
                it.dimension = ShadowTransform.DimensionName
                agpCompat.setProductFlavorDefault(it, false)
            }
        } catch (e: InvalidUserDataException) {
            throw Error("请在android{} DSL之前apply plugin: 'com.tencent.shadow.plugin'", e)
        }
    }

    private fun initAndroidClassPoolBuilder(
            baseExtension: BaseExtension,
            project: Project
    ) {
        val sdkDirectory = baseExtension.sdkDirectory
        val compileSdkVersion =
                baseExtension.compileSdkVersion
                        ?: throw IllegalStateException("compileSdkVersion获取失败")
        val androidJarPath = "platforms/${compileSdkVersion}/android.jar"
        val androidJar = File(sdkDirectory, androidJarPath)

        androidClassPoolBuilder = AndroidClassPoolBuilder(project, contextClassLoader, androidJar)
    }

    open class ShadowExtension {
        var transformConfig = TransformConfig()
        fun transform(action: Action<in TransformConfig>) {
            action.execute(transformConfig)
        }
    }

    class TransformConfig {
        var useHostContext: Array<String> = emptyArray()
    }

    companion object {
        private fun buildAgpCompat(project: Project): AGPCompat {
            return AGPCompatImpl()
        }
    }

}