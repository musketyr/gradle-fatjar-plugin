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
            // force: true, overwrite:true because we want the user's project to always win
            // failOnerror: false to be compatible with case-insentive file systems
            ant.copy(todir: stageDir, force: true, overwrite: true, failOnError: false) {
                fileset(dir: resourcesDir, excludes: filter.excludes.join(','))
            }
        }

        if(classesDir?.exists()){
            // force: true, overwrite:true because we want the user's project to always win
            // failOnerror: false to be compatible with case-insentive file systems
            ant.copy(todir: stageDir, force: true, overwrite: true, failOnError: false) {
                fileset(dir: classesDir, excludes: filter.excludes.join(','))
            }
        }
        
        FileTree filesToMerge = files.asFileTree.matching filter


        Set<String> toDelete = [] as Set
        toDelete.addAll(filter.includes)
        toDelete.addAll(filter.excludes)
        
        ant.delete {
            fileset dir: stageDir, includes: toDelete.join(',')
        }

        filesToMerge.visit { FileTreeElement file ->
            if(file.isDirectory()) {
                return
            }
                
            File theFile = new File(stageDir, file.relativePath.toString())

            if(!theFile.exists()){
                theFile.createNewFile()
            }

            theFile.append file.file.text.trim() + '\n'
        }
        
        handleExtensionModules(stageDir)
        
    }

    private handleExtensionModules(File stageDir) {
        File extModuleFile = new File(stageDir, 'META-INF/services/org.codehaus.groovy.runtime.ExtensionModule')
        if(!extModuleFile.exists()){
            return
        }
        def modules = [extensionClasses: [], staticExtensionClasses: []]
        extModuleFile.eachLine {
            def line = it.trim()
            if(line){
                int indexOfEqualSign = line.indexOf('=')
                if(indexOfEqualSign < 0 || indexOfEqualSign == line.size() - 1){
                    return
                }
                String key = line[0..(indexOfEqualSign - 1)].trim()
                String value = line[(indexOfEqualSign + 1)..-1].trim()
                switch(key){
                    case 'moduleName': 
                       if(modules.name) {
                           modules.name = 'MergedByFatJar'
                       } else {
                           modules.name = value
                       }
                       break
                    case 'moduleVersion':
                       if(modules.version) {
                           modules.version = project.version
                       } else {
                           modules.version = value
                       }
                       break
                   case 'extensionClasses':
                       modules.extensionClasses.addAll value.split(/\s*,\s*/)
                       break
                   case 'staticExtensionClasses':
                       modules.staticExtensionClasses.addAll value.split(/\s*,\s*/)
                       break
                }
            }
        }
        extModuleFile.withWriter { writer ->
           writer.println "moduleName=$modules.name"
           writer.println "moduleVersion=$modules.version"
           writer.println "staticExtensionClasses=${modules.staticExtensionClasses.join(',')}"
           writer.println "extensionClasses=${modules.extensionClasses.join(',')}"
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
