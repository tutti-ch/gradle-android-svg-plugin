package ch.tutti.svg;

import java.io.File;
import java.io.IOException;

public class SvgConverter {

    private final File mInkscapeExecutable;

    public SvgConverter(File inkscapeExecutable) {
        this.mInkscapeExecutable = inkscapeExecutable;

        if (!mInkscapeExecutable.exists()) {
            throw new IllegalStateException(
                    "Inkscape executable does not exist at "
                            + mInkscapeExecutable.getAbsolutePath());
        }
    }

    public Task convert(String svgPath) {
        return convert(new File(svgPath));
    }

    public Task convert(File svgPath) {
        return new Task(svgPath);
    }

    private void execute(Task task) throws IOException, InterruptedException {
        File targetFolder = task.targetFile.getParentFile();
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        int widthPixel = (int) (task.width * task.multiplier);
        int heightPixel = (int) (task.height * task.multiplier);

        String command =
                mInkscapeExecutable.getAbsolutePath()
                        + (widthPixel > 0 ? " -w" + widthPixel : "")
                        + (heightPixel > 0 ? " -h" + heightPixel : "")
                        + " --export-background-opacity=0"
                        + " --export-" + task.type + "=" + task.targetFile.getAbsolutePath()
                        + " " + task.svgFile.getAbsolutePath()
                        + " > /dev/null";

        Runtime.getRuntime()
                .exec(command)
                .waitFor();
    }

    public class Task {

        private final File svgFile;

        private int width;

        private int height;

        private float multiplier;

        private String type;

        private File targetFile;

        public Task(File svgFile) {
            this.svgFile = svgFile;

            if (!svgFile.exists()) {
                throw new IllegalStateException(
                        "svgFile does not exist: " + this.svgFile.getAbsolutePath());
            }
        }

        public Task width(int width) {
            return width(width, 1);
        }

        public Task width(int width, float multiplier) {
            this.width = width;
            this.multiplier = multiplier;
            return this;
        }

        public Task height(int height) {
            return height(height, 1);
        }

        public Task height(int height, float multiplier) {
            this.height = height;
            this.multiplier = multiplier;
            return this;
        }

        public Task type(String type) {
            this.type = type;
            return this;
        }

        public Task to(String targetFile) {
            return to(new File(targetFile));
        }

        public Task to(File targetFile) {
            this.targetFile = targetFile;
            if (type == null || type.length() == 0) {
                type(getFileExtension(targetFile.getName()));
            }
            return this;
        }

        private String getFileExtension(String fileName) {
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                return fileName.substring(i + 1);
            }
            return null;
        }

        public void execute() throws IOException, InterruptedException {
            SvgConverter.this.execute(this);
        }
    }
}
