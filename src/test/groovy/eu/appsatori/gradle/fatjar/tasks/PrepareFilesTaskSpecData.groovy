package eu.appsatori.gradle.fatjar.tasks

final class PrepareFilesTaskSpecData {

    public static final String EXAMPLE_MODULE_A = '''moduleName = DatomicExtension
moduleVersion = 1.0
staticExtensionClasses = groovy.datomic.extension.DatomicPeerExtension
extensionClasses = groovy.datomic.extension.EntityMapExtension
faultyKey =
'''

    public static final String EXAMPLE_MODULE_B = '''moduleName = GBenchExtension
moduleVersion = 1.0
staticExtensionClasses = groovyx.gbench.BenchmarkStaticExtension
extensionClasses = groovyx.gbench.BenchmarkExtension
faultyKey
'''

    public static final def EXAMPLE_MODULE_RESULT = ['moduleName=MergedByFatJar',
                                                     'moduleVersion=1.0.1',
                                                     'staticExtensionClasses=groovy.datomic.extension.DatomicPeerExtension,groovyx.gbench.BenchmarkStaticExtension',
                                                     'extensionClasses=groovy.datomic.extension.EntityMapExtension,groovyx.gbench.BenchmarkExtension']
}
