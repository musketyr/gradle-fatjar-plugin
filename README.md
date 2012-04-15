# Gradle FatJar Plugin

Gradle FatJar Plugin allows you to create JAR file with all dependecies bundled inside. It handles files in `META-INF/services`
directory gracefully by merging them. It also allows you to create slim WAR file which contains only JAR with dependencies.
Classes are also bundled into the JAR instead of putting them into `WEB-INF/classes`.


## 


Plugin is hosted in Maven Central Repository. You can easily add plugin to your build script using following configuration

## Installation

```groovy
buildscript {
    dependencies {
        classpath 'eu:appsatori:gradle-fatjar-plugin:0.1.1'
    }
    
    apply plugin: 'fatjar'
}
```

## Tasks

### `fatJarPrepareFiles`

Explodes all JARs into the stage directory and merges all files needed such as those in `META-INF/services`.

### `fatJar`

Creates the JAR with all dependencies bundled. 

This is regullar `Jar` task so you can e.g. customize the manifest as
described in [Jar Task DSL](http://gradle.org/docs/current/dsl/org.gradle.api.tasks.bundling.Jar.html).

### `slimWar`

Bundles all output classes into JAR with dependencies and place it into `WEB-INF/lib` directory of the newly created
WAR file.

This is regullar `War` task so you can e.g. customize the `web.xml` file as
described in [War Task DSL](http://gradle.org/docs/current/dsl/org.gradle.api.tasks.bundling.War.html).

## Customization


### File Merge

You can specify additional files to be merged using Ant-style pattern and `include` method on `fatJarPrepareFiles` task.

```groovy
fatJarPrepareFiles {
  include 'META-INF/spring.handlers'
  include 'META-INF/spring.schemas'
}
```

### Exclude JARs

If you need to keep some JARs out of the JAR with dependencies you can specify extendended property `fatJarExclude` on
particular dependency. All excluded JARs will be placed in `WEB-INF\lib` directory if `slimWar` task is called.

```groovy
dependencies {
  compile 'eu.appsatori:pipes:0.6.1', {
    ext {
      fatJarExclude = true
    }
  }
}
```


