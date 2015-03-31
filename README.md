Gradle: Android SVG to PNG plugin
=================================

This plugin automatically converts SVGs located in src/main/svg/* to pngs. No need anymore to manually convert and place them in a drawables folder. It converts them to mdpi, hdpi, xhdpi, xxhdpi and xxxhdpi.

## !! NOTICE !!
Currently Android Studio doesn't pick up the generated drawables inside the XML editor. Generated drawables will be highlighted red (not found) in the xml editor and code completion in xml does not work for them. In the code they are working but without drawable preview.

Compiling/running the app works fine on the other hand.

The issue for getting this fixed in Android Studio can be found here (please star it): [https://code.google.com/p/android/issues/detail?id=160646](https://code.google.com/p/android/issues/detail?id=160646)


## How it works
This uses [Inkscape](https://inkscape.org/en/download/) to convert the SVGs. It does not require XQuartz on OSX because it only uses the command line.

By default it assumes that the width of the resulting image should be 32dp and generates the correct images in mdpi, hdpi, xhdpi, xxhdpi and xxxhdpi.

## Requirements
- Inkscape
- ```com.android.tools.build:gradle``` >= 1.1

## Usage
build.gradle

```
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.0.0'
        classpath 'ch.tutti.gradle.android.svg:android-svg:1.0.0'
    }
}

apply plugin: 'com.android.application'
apply plugin: 'android-svg'

androidsvg {
    defaultWidthDp = 40 // defaults to 32
    // defaultHeightDp = 40 // will be used if defaultWidthDp is 0
}
```

This is all the setup needed. Now you can place your SVGs in one or more of the following folders:
- src/main/svg
- src/release/svg
- src/flavor/svg
- src/flavorDebug/svg

An SVG in src/flavorDebug/svg will take preference over the image in src/main/svg.

## TODOs
- Support Linux and Windows (probably supported already if you adjust the inkscape path in local.poperties)
- Allow enabling/disabling specific mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi

## License
```
Copyright (c) 2015 tutti.ch AG

Permission to use, copy, modify, and distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
```
