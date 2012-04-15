/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.appsatori.gradle.fatjar

/**
 * Utility class for creating temporary directories.
 *
 * @author Vladimir Orany
 */
class TempDir {
    @Lazy static tempDir = new File(System.getProperty('java.io.tmpdir'))

    static File createNew(prefix, suffix = new Random().nextInt()){
        File where = findDir(prefix, suffix)
        if(!where.exists()) {
            where.mkdir()
        }
        where
    }

    static File findDir(prefix, suffix){
        new File(tempDir, "$prefix-$suffix")
    }

    static withTempDir(prefix, suffix = new Random().nextInt(), Closure closure){
        File where = findDir(prefix, suffix)
        try{
            closure.call(where)
        } finally {
            new AntBuilder().delete dir: where.path
        }
    }
}
