package eu.appsatori.gradle.fatjar.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileTreeElement
import org.gradle.api.file.FileCollection.AntType
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet



class PrepareFiles extends DefaultTask {

    @InputDirectory @Optional File resourcesDir
    @InputDirectory @Optional File classesDir
    @InputFiles @Optional FileCollection compileClasspath

    @OutputDirectory File stageDir

    @Delegate PatternFilterable filter = new PatternSet()

    PrepareFiles(){
        super()
        filter.include 'META-INF/services/*'
    }

    @TaskAction prepareFiles(){
        File resourcesDir = getResourcesDir()
        File classesDir = getClassesDir()
        FileCollection compileClasspath = getCompileClasspath()
        File stageDir = getStageDir()


        FileCollection files = getFatJarFiles()
        if(!files.isEmpty()){
            ant.copy(todir: stageDir, failonerror: false){
                files.addToAntBuilder(ant, 'fileset', AntType.FileSet)
            }
        }

        if(resourcesDir?.exists()){
            ant.copy(todir: stageDir, failonerror: false) { fileset(dir: resourcesDir) }
        }

        if(classesDir?.exists()){
            ant.copy(todir: stageDir, failonerror: false){ fileset(dir: classesDir) }
        }

        FileTree filesToMerge = files.asFileTree.matching filter


        ant.delete {
            fileset dir: stageDir, includes: filter.includes.join(','), excludes: filter.excludes.join(',')
        }

        filesToMerge.visit { FileTreeElement file ->
            if(file.isDirectory()) return
                File theFile = new File(stageDir, file.relativePath.toString())
            if(!theFile.exists()){
                theFile.createNewFile()
            }
            theFile.append file.file.text.trim() + '\n'
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
