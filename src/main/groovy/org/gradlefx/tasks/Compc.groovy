/*
 * Copyright (c) 2011 the original author or authors
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

package org.gradlefx.tasks

import org.gradle.api.tasks.TaskAction

/*
 * Gradle task to execute Flex's Compc compiler.
 */
class Compc extends AbstractCompileTask {

	private static final String ANT_RESULT_PROPERTY = 'compcCompileResult'
	private static final String ANT_OUTPUT_PROPERTY = 'compcCompileOutput'
	
    public Compc() {
        description = 'Compiles Flex component (*.swc) using the compc compiler'
    }

    @TaskAction
    def compileFlex() {
        List compilerArguments = createCompilerArguments()

        ant.java(jar: project.flexHome + '/lib/compc.jar',
                dir: project.flexHome + '/frameworks',
                fork: true,
                resultproperty: ANT_RESULT_PROPERTY,
                outputproperty: ANT_OUTPUT_PROPERTY) {

            compilerArguments.each { compilerArgument ->
                arg(value: compilerArgument)
            }
        }

        handleBuildIfFailed ANT_RESULT_PROPERTY, ANT_OUTPUT_PROPERTY, 'Compc'
		
		showAntOutput ant.properties[ANT_OUTPUT_PROPERTY]
    }

    private List createCompilerArguments() {
        List compilerArguments = []

        //add every source path
        project.srcDirs.each { sourcePath ->
            compilerArguments.add("-source-path+=" + project.file(sourcePath).path)
        }

        addSourceFilesAndDirectories(compilerArguments)

        //add dependencies
        addLibraries(project.configurations.internal, "-include-libraries", compilerArguments)
        addLibraries(project.configurations.external, "-external-library-path", compilerArguments)
        addLibraries(project.configurations.merged, "-library-path", compilerArguments)

        //add all the other user specified compiler options
        project.additionalCompilerOptions.each { compilerOption ->
            compilerArguments.add(compilerOption)
        }

        compilerArguments.add("-output=${project.buildDir.path}/${project.output}.swc")
        return compilerArguments
    }

    private def addSourceFilesAndDirectories(List compilerArguments) {
        if (project.includeClasses == null && project.includeSources == null) {
            project.srcDirs.each { sourcePath ->
                compilerArguments.add("-include-sources+=" + project.file(sourcePath).path)
            }
        } else {
            if (project.includeClasses != null) {
                compilerArguments.add('-include-classes')
                project.includeClasses.each { classToInclude ->
                    compilerArguments.add(classToInclude)
                }
            }

            if (project.includeSources != null) {
                project.includeSources.each { classOrDirectoryToInclude ->
                    compilerArguments.add('-include-sources+=' + project.file(classOrDirectoryToInclude).path)
                }
            }
        }
    }

}