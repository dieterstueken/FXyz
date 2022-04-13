/**
 * SampleScanner.java
 *
 * Copyright (c) 2013-2018, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package org.fxyz3d.util;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.fxyz3d.FXyzSample;
import org.fxyz3d.FXyzSamplerProject;
import org.fxyz3d.model.Project;

/**
 * All the code related to classpath scanning, etc for samples.
 */
public class SampleScanner {

    private static final Map<String, FXyzSamplerProject> packageToProjectMap = new HashMap<>();
    static {
        System.out.println("Initialising FXyz-Sampler sample scanner...");
        System.out.println("\tDiscovering projects...");
        // find all projects on the classpath that expose a FXyzSamplerProject
        // service. These guys are our friends....
        ServiceLoader<FXyzSamplerProject> loader = ServiceLoader.load(FXyzSamplerProject.class);
        for (FXyzSamplerProject project : loader) {
            final String projectName = project.getProjectName();
            final String basePackage = project.getSampleBasePackage();
            packageToProjectMap.put(basePackage, project);
            System.out.println("\t\tFound project '" + projectName + 
                    "', with sample base package '" + basePackage + "'");
        }
        
        if (packageToProjectMap.isEmpty()) {
            System.out.println("\tError: Did not find any projects!");
        }
    }
    
    private final Map<String, Project> projectsMap = new HashMap<>();
    
    /**
     * Gets the list of sample classes to load
     *
     * @return The classes
     */
    public Map<String, Project> discoverSamples() {

        // find all provided samples on module path.
        ServiceLoader.load(FXyzSample.class).stream()
                .map(ServiceLoader.Provider::get)
                .forEach(this::registerSample);
        
        return projectsMap;
    }

    private void registerSample(FXyzSample sample) {
        final String packageName = sample.getClass().getPackage().getName();

        for (String key : packageToProjectMap.keySet()) {
            if (packageName.contains(key)) {
                final String prettyProjectName = packageToProjectMap.get(key).getProjectName();

                Project project;
                if (! projectsMap.containsKey(prettyProjectName)) {
                    project = new Project(prettyProjectName, key);
                    project.setWelcomePage(packageToProjectMap.get(key).getWelcomePage());
                    projectsMap.put(prettyProjectName, project);
                } else {
                    project = projectsMap.get(prettyProjectName);
                }

                project.addSample(packageName, sample);
            }
        }
    }
}