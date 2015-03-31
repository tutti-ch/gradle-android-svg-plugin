package ch.tutti.gradle.android.svg

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidSvgPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create("androidsvg", AndroidSvgPluginExtension)

        project.configure(project) {
            if (it.plugins.hasPlugin("com.android.application")) {
                project.android.applicationVariants.all { variant ->
                    def resFolder = file("${project.buildDir}/generated/res/svg/${variant.dirName}")
                    def generateResourcesTask = project.("generate${variant.name.capitalize()}Resources")
                    def svgTaskName = "generate${variant.name.capitalize()}SvgResources"

                    // variant flavorDebug includes:
                    // src/main/svg
                    // src/debug/svg
                    // src/flavor/svg
                    // src/flavorDebug/svg
                    List<String> folders = []
                    folders.add("src/main/svg")
                    folders.add("src/${variant.buildType.name}/svg")
                    for (flavor in variant.productFlavors) {
                        folders.add("src/${flavor.name}/svg")
                    }
                    folders.add("src/${variant.name}/svg")

                    List<File> svgFiles = []
                    HashSet<String> alreadyAdded = new HashSet<>()
                    def filter = { dir, file -> file ==~ /.*?\.svg/ } as FilenameFilter
                    // reverse because last one overwrites previous ones
                    for (folderName in folders.reverse()) {
                        def folder = new File(project.projectDir, folderName)
                        if (folder.exists()) {
                            for (file in folder.listFiles(filter)) {
                                // TODO this check should be done removing -w40 or -h29...
                                if (!alreadyAdded.contains(file.name)) {
                                    svgFiles.add(file)
                                    alreadyAdded.add(file.name)
                                }
                            }
                        }
                    }

                    def svgTask = project.task(svgTaskName, type: SvgConvertTask) {
                        inputFiles = files({ svgFiles })
                        outputDir = resFolder
                        defaultWidthDp = project.androidsvg.defaultWidthDp
                        defaultHeightDp = project.androidsvg.defaultHeightDp
                    }

                    variant.registerResGeneratingTask(svgTask, resFolder)
                    generateResourcesTask.dependsOn(svgTaskName)
                }
            }
        }
    }
}

class AndroidSvgPluginExtension {

    def int defaultWidthDp = SvgConstants.DEFAULT_WIDTH_DP

    def int defaultHeightDp = SvgConstants.DEFAULT_HEIGHT_DP
}