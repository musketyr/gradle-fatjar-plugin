package eu.appsatori.gradle.fatjar.tasks

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.testfixtures.ProjectBuilder;

import eu.appsatori.gradle.fatjar.TempDir;

import spock.lang.Specification

class PrepareFilesTaskSpec extends Specification {

    def 'Test precompile task'() {
        given:
            Project project = ProjectBuilder.builder().build()

            def dir = TempDir.createNew('prepare-task')
            
            File classesDir = new File(dir, 'classes')
            classesDir.mkdirs()
            new File(classesDir, 'fake.class').append('0123456789')
            
            File resourcesDir = new File(dir, 'resources')
            resourcesDir.mkdirs()
            File resourcesDirServiceDir =  new File(resourcesDir.absolutePath + '/META-INF/services/')
            resourcesDirServiceDir.mkdirs()
            new File(resourcesDirServiceDir, 'a.b.c.Service').append('a.b.c.ServiceImpl')
            
            File fakeClasspath = new File(dir, 'classpath')
            fakeClasspath.mkdirs()
            new File(fakeClasspath, 'fake2.class').append('9876543210')
            File fakeClasspathServiceDir = new File(fakeClasspath.absolutePath + '/META-INF/services/')
            fakeClasspathServiceDir.mkdirs()
            new File(fakeClasspathServiceDir, 'a.b.c.Service').append('e.f.g.ServiceImpl')
            
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
    }
    
}
