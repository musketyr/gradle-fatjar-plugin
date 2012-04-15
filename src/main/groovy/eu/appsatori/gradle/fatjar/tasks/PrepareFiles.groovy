package eu.appsatori.gradle.fatjar.tasks

import java.io.File

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileCollection.AntType
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar

class PrepareFiles extends DefaultTask{

    @InputDirectory @Optional File resourcesDir
    @InputDirectory @Optional File classesDir
    @InputFiles @Optional FileCollection compileClasspath

    @OutputDirectory File stageDir

    @TaskAction prepareFiles(){
        File resourcesDir = getResourcesDir()
        File classesDir = getClassesDir()
        FileCollection compileClasspath = getCompileClasspath()
        File stageDir = getStageDir()


        FileCollection files = getFatJarFiles()
        if(!files.isEmpty()){
            ant.copy(todir: stageDir){
                files.addToAntBuilder(ant, 'fileset', AntType.FileSet)
            }
        }

        if(resourcesDir?.exists()){
            ant.copy(todir: stageDir) { fileset(dir: resourcesDir) }
        }
        
        if(classesDir?.exists()){
            ant.copy(todir: stageDir){ fileset(dir: classesDir) }
        }

        FileCollection serviceFiles = files.filter{ File it ->
            it.parent?.endsWith('META-INF/services') || it.parent?.endsWith('META-INF/services/')
        }
        File serviceDir = new File(stageDir.absolutePath + '/META-INF/services/')
        serviceDir.deleteDir()
        serviceDir.mkdirs()

        for(File file in serviceFiles.files){
            File serviceFile = new File(serviceDir, file.name)
            if(!serviceFile.exists()){
                serviceFile.createNewFile()
            }
            serviceFile.append file.text.trim() + '\n'
        }
    }

    private FileCollection getFatJarFiles(){
        FileCollection files = project.files([])
        if(getResourcesDir()?.exists()) files += project.fileTree getResourcesDir()
        if(getClassesDir()?.exists()) files += project.fileTree getClassesDir()

        def collected = getCompileClasspath().collect {
            it.isDirectory() ? project.fileTree(it) : project.zipTree(it)
        }
        for(f in collected) {
            files += f
        }
        files
    }
}
