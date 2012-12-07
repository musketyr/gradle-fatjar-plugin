package eu.appsatori.gradle.fatjar

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.War

import eu.appsatori.gradle.fatjar.tasks.PrepareFiles

class FatJarPlugin implements Plugin<Project>{

    static final String FATJAR_GROUP = "Fat JAR"
    static final String FATJAR_PREPARE_FILES = "fatJarPrepareFiles"
    static final String FATJAR_PREPARE_FILES_DESC = "Prepare files for the fat JAR"
    static final String FATJAR_FAT_JAR = "fatJar"
    static final String FATJAR_FAT_JAR_DESC = "Assembles the fat JAR"
    static final String FATJAR_SLIM_WAR = "slimWar"
    static final String FATJAR_SLIM_WAR_DESC = "Creates war file with all dependencies and classes bundled inside one fat JAR"
    static final String FATJAR_STAGE_DIR = "/tmp/fatjar-stage"

    @Override
    public void apply(Project project) {
        project.plugins.apply JavaPlugin

        File stageDir = new File(project.buildDir.absolutePath + FATJAR_STAGE_DIR)

        project.tasks.withType(PrepareFiles).whenTaskAdded { PrepareFiles prepareFiles ->
            prepareFiles.conventionMapping.map("classesDir") { 
                File ret = project.sourceSets.main.output.classesDir
                if(ret?.exists()){
                    return ret
                }
                null
            }
            
            prepareFiles.conventionMapping.map("resourcesDir") {
                File ret = project.sourceSets.main.output.resourcesDir 
                if(ret?.exists()){
                    return ret
                }
                null
            }
            
            prepareFiles.conventionMapping.map("compileClasspath") {
                def excluded = []
                project.logger.debug("FatJar: Building compile classpath")
                def classpath = project.configurations.runtime.copyRecursive { 
                    if(!it.ext.has('fatJarExclude') || !it.ext.get('fatJarExclude')){
                        project.logger.debug("FatJar: $it.group:$it.name is INCLUDED ($it)")
                        return true
                    }
                    project.logger.debug("FatJar: $it.group:$it.name:$it.version is EXCLUDED ($it)")
                    excluded << it
                    false
                }
                for(Dependency d in excluded){
                    classpath.exclude group: d.group, module: d.name                        
                }
                project.logger.debug("FatJar: classpath contains following files -  ${classpath.resolve()}")
                classpath
            }
            prepareFiles.conventionMapping.map("stageDir") { stageDir }
        }

        PrepareFiles prepareFiles = project.tasks.add(FATJAR_PREPARE_FILES, PrepareFiles)
        prepareFiles.description = FATJAR_PREPARE_FILES_DESC
        prepareFiles.group = FATJAR_GROUP
        prepareFiles.dependsOn project.tasks.classes
        
        Jar fatJar = project.tasks.add(FATJAR_FAT_JAR, Jar)
        fatJar.description = FATJAR_FAT_JAR_DESC
        fatJar.group = FATJAR_GROUP
        fatJar.dependsOn prepareFiles
        fatJar.from stageDir
        
        if(project.plugins.hasPlugin(WarPlugin)){
            War slimWar = project.tasks.add(FATJAR_SLIM_WAR, War)
            slimWar.description = FATJAR_SLIM_WAR_DESC
            slimWar.group = FATJAR_GROUP
            slimWar.dependsOn fatJar
            
            slimWar.conventionMapping.map("classpath") { 
                project.files(fatJar.archivePath) +  project.configurations.runtime.copyRecursive {
                    it.ext.has('fatJarExclude') && it.ext.get('fatJarExclude')
                }
            }
            
        }
    }
}
