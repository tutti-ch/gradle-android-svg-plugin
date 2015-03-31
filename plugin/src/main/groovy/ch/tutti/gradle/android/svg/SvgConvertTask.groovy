package ch.tutti.gradle.android.svg

import ch.tutti.svg.SvgConverter
import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.commons.configuration.PropertiesConfigurationLayout
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.logging.ProgressLogger
import org.gradle.logging.ProgressLoggerFactory

import java.util.regex.Pattern

class SvgConvertTask extends DefaultTask {

    private static final String PROPERTIES_INKSCAPE_EXECUTABLE = 'inkscape.executable'

    @InputFiles
    def FileCollection inputFiles

    @OutputDirectory
    def File outputDir

    @Input
    def inputProperty = "original"

    def defaultWidthDp = SvgConstants.DEFAULT_WIDTH_DP

    def defaultHeightDp = SvgConstants.DEFAULT_HEIGHT_DP

    SvgConverter svgConverter

    def pattern = Pattern.compile(".*(\\-[wh][0-9]+)\\.svg\$")

    ProgressLogger progressLogger

    @TaskAction
    def execute(IncrementalTaskInputs inputs) {
        svgConverter = new SvgConverter(new File(getInkscapeExecutablePath()))
        progressLogger = services.get(ProgressLoggerFactory).newOperation(getClass())

        progressLogger.setDescription("Converting svgs to pngs")
        progressLogger.started()

        HashSet<String> changedFiles = new HashSet<>()
        inputs.outOfDate { change ->
            def convertSvgItem = getConvertInformation(change.file)
            progressLogger.progress(convertSvgItem.fileName)
            convert(convertSvgItem)
            changedFiles.add(convertSvgItem.destFileName)
        }

        inputs.removed { change ->
            def convertSvgItem = getConvertInformation(change.file)

            // This is done because a file can change from filename.svg to filename-w40.svg which
            // both results in filename.png. And so filename.png would get generated from
            // filename-w40.svg but because filename.svg was deleted, it would wrongly get deleted.
            if (!changedFiles.contains(convertSvgItem.destFileName)) {
                remove(convertSvgItem)
            }
        }

        // Old non incremental way

//        def files = inputDir.listFiles()
//        def fileCount = files.size()
//        def currentFile = 1;
//        files.each() { file ->
//            def convertSvgItem = getConvertInformation(file)
//            println convertSvgItem.fileName + " -> " + widthDp
//            progressLogger.progress((currentFile++) + "/" + fileCount + " > " + convertSvgItem.fileName)
//            convert(convertSvgItem)
//        }

        progressLogger.completed()
    }

    def ConvertSvgItem getConvertInformation(File file) {
        def convertSvgItem = new ConvertSvgItem()
        convertSvgItem.file = file
        convertSvgItem.fileName = file.getName()

        def matcher = pattern.matcher(convertSvgItem.fileName)
        def sizeFlag = matcher.matches() && matcher[0][1] ? matcher[0][1].toString() : null
        if (sizeFlag != null) {
            def size = Integer.parseInt(sizeFlag.substring(2))
            if (sizeFlag.charAt(0) == 'h') {
                convertSvgItem.heightDp = size
            } else {
                convertSvgItem.widthDp = size
            }
        } else {
            convertSvgItem.widthDp = defaultWidthDp > 0 ? defaultWidthDp : defaultHeightDp
        }
        // remove -[wh]XY flag if existing and switch from .svg to .png
        convertSvgItem.destFileName = sizeFlag != null ?
                convertSvgItem.fileName.
                        substring(0, convertSvgItem.fileName.length() - sizeFlag.length() - 4) +
                        ".png" :
                convertSvgItem.fileName.substring(0, convertSvgItem.fileName.length() - 4) + ".png"
        return convertSvgItem
    }

    def remove(ConvertSvgItem convertSvgItem) {
        new File(outputDir, "drawable-mdpi/" + convertSvgItem.destFileName).delete()
        new File(outputDir, "drawable-hdpi/" + convertSvgItem.destFileName).delete()
        new File(outputDir, "drawable-xhdpi/" + convertSvgItem.destFileName).delete()
        new File(outputDir, "drawable-xxhdpi/" + convertSvgItem.destFileName).delete()
        new File(outputDir, "drawable-xxxhdpi/" + convertSvgItem.destFileName).delete()
    }

    def convert(ConvertSvgItem convertSvgItem) {
        convert(convertSvgItem, 1, "drawable-mdpi")
        convert(convertSvgItem, 1.5f, "drawable-hdpi")
        convert(convertSvgItem, 2, "drawable-xhdpi")
        convert(convertSvgItem, 3, "drawable-xxhdpi")
        convert(convertSvgItem, 4, "drawable-xxxhdpi")
    }

    def convert(ConvertSvgItem convertSvgItem, float multiplier, String destinationFolderName) {
        svgConverter
                .convert(convertSvgItem.file)
                .to(
                "${outputDir.getAbsolutePath()}/$destinationFolderName/${convertSvgItem.destFileName}")
                .width(convertSvgItem.widthDp, multiplier)
                .height(convertSvgItem.heightDp, multiplier)
                .execute();
    }

    def getInkscapeExecutablePath() {
        def propsFile = new File(project.rootDir, 'local.properties')

        def propertiesConfigration = new PropertiesConfiguration()
        def propertiesConfigrationLayout = new PropertiesConfigurationLayout(propertiesConfigration)
        propertiesConfigrationLayout.load(propsFile.newReader())

        propertiesConfigration.setProperty("test", "testValue")

        def inkscapeExecutablePath = propertiesConfigration.
                getProperty(PROPERTIES_INKSCAPE_EXECUTABLE)
        if (inkscapeExecutablePath == null) {
            inkscapeExecutablePath = "/Applications/Inkscape.app/Contents/Resources/bin/inkscape"
            propertiesConfigration.
                    setProperty(PROPERTIES_INKSCAPE_EXECUTABLE, inkscapeExecutablePath)
            propertiesConfigrationLayout.save(propsFile.newWriter())
        }

        if (!new File(inkscapeExecutablePath).exists()) {
            throw new InvalidUserDataException(
                    "Inkscape executable is not found at: " + inkscapeExecutablePath + "\n" +
                            "Please install inkscape and set the correct location in your local.properties file")
        }

        return inkscapeExecutablePath
    }
}

class ConvertSvgItem {

    File file

    String fileName

    String destFileName

    int widthDp

    int heightDp
}