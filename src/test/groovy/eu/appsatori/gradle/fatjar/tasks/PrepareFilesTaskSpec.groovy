package eu.appsatori.gradle.fatjar.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification

class PrepareFilesTaskSpec extends Specification {
    
    @Rule TemporaryFolder folder

    def 'Test prepare task'() {
        given:
            Project project = ProjectBuilder.builder().build()
            project.version = '1.0.1'

            def dir = folder.newFolder('prepare-task')
            
            File classesDir = new File(dir, 'classes')
            classesDir.mkdirs()
            new File(classesDir, 'fake.class').append('0123456789')
            
            File resourcesDir = new File(dir, 'resources')
            resourcesDir.mkdirs()
            File resourcesDirServiceDir =  new File(resourcesDir.absolutePath + '/META-INF/services/')
            resourcesDirServiceDir.mkdirs()
            new File(resourcesDirServiceDir, 'a.b.c.Service').append('a.b.c.ServiceImpl')
            new File(resourcesDir.absolutePath + '/META-INF/generic.file').append('xyz')
            new File(resourcesDir.absolutePath + '/META-INF/donot.copy').append('xyz')
            new File(resourcesDirServiceDir, 'org.codehaus.groovy.runtime.ExtensionModule').append(PrepareFilesTaskSpecData.EXAMPLE_MODULE_A)
            
            File fakeClasspath = new File(dir, 'classpath')
            fakeClasspath.mkdirs()
            new File(fakeClasspath, 'fake2.class').append('9876543210')
            File fakeClasspathServiceDir = new File(fakeClasspath.absolutePath + '/META-INF/services/')
            fakeClasspathServiceDir.mkdirs()
            new File(fakeClasspathServiceDir, 'a.b.c.Service').append('e.f.g.ServiceImpl')
            new File(fakeClasspath.absolutePath + '/META-INF/generic.file').append('abc')
            new File(fakeClasspath.absolutePath + '/META-INF/donot.copy').append('abc')
            new File(resourcesDirServiceDir, 'org.codehaus.groovy.runtime.ExtensionModule').append(PrepareFilesTaskSpecData.EXAMPLE_MODULE_B)
            
            
            File libDir = new File(dir, 'lib')
            libDir.mkdirs()
            
            File libFile = new File(new File('./src/test/resources/a.jar').absolutePath)
            
            project.ant.copy(todir: libDir, file: libFile)
            
            File stageDir = new File(dir, 'stage')

            PrepareFiles task = project.task('prepareFatJar', type: PrepareFiles)
            task.classesDir = classesDir
            task.resourcesDir = resourcesDir
            task.compileClasspath = project.files(fakeClasspath) + project.fileTree(libDir)
            task.stageDir = stageDir
            
            task.configure {
                include 'META-INF/generic.file'
                exclude 'META-INF/donot.copy'
            }
            
        when:
            task.prepareFiles()

        then:
            new File(stageDir, 'fake.class').exists()
            new File(stageDir, 'fake.class').text == '0123456789'
            new File(stageDir, 'fake2.class').exists()
            new File(stageDir, 'fake2.class').text == '9876543210'
            new File(stageDir.absolutePath + '/com/example/a/Whatever.class').exists()
            new File(stageDir.absolutePath + '/META-INF/services/a.b.c.Service').exists()
            new File(stageDir.absolutePath + '/META-INF/services/a.b.c.Service').text.contains('a.b.c.ServiceImpl')
            new File(stageDir.absolutePath + '/META-INF/services/a.b.c.Service').text.contains('e.f.g.ServiceImpl')
            new File(stageDir.absolutePath + '/META-INF/generic.file').exists()
            new File(stageDir.absolutePath + '/META-INF/generic.file').text.contains('xyz')
            new File(stageDir.absolutePath + '/META-INF/generic.file').text.contains('abc')
            new File(stageDir.absolutePath + '/META-INF/services/org.codehaus.groovy.runtime.ExtensionModule').exists()
            new File(stageDir.absolutePath + '/META-INF/services/org.codehaus.groovy.runtime.ExtensionModule').text == PrepareFilesTaskSpecData.EXAMPLE_MODULE_RESULT
            !new File(stageDir.absolutePath + '/META-INF/donot.copy').exists()
    }
    
}
