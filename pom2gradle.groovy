def pom = new XmlSlurper().parse(new File('pom.xml' ))

def convert = { String s ->
    s.contains('.version') ? s.replaceAll('\\.','_') : s
}

File buildGradle = new File('build.gradle')


buildGradle.withWriter { w->
    w << """
buildscript {
    repositories {
        mavenLocal()
 
        jcenter() 
    }

    dependencies {
      classpath 'com.github.jruby-gradle:jruby-gradle-plugin:0.1.0-SNAPSHOT'
      classpath 'com.jfrog.bintray.gradle:gradle-bintray-plugin:0.5'
    }
}

// We temporarily set buildDir to somewhere else fo rhte sake of this experiment
buildDir = new File(projectDir,'buildGradle')

apply plugin: 'java'
apply plugin: 'maven'
apply plugin: 'com.github.jruby-gradle.base'
apply plugin: 'com.jfrog.bintray'

repositories {
    maven { 
        url  'http://rubygems-proxy.torquebox.org/prereleases'
        name 'rubygems-prerelease'
    }
    
    // WORKAROUND: for broken some POMs on Rubygems
    ivy {
        url 'http://rubygems-proxy.torquebox.org/releases'
        name 'brokenRubyGems'
        layout('pattern') {
            artifact 'rubygems/[module]/[revision]/[artifact]-[revision].[ext]'
            m2compatible = true
        }
    }
}

"""

    w << """
group = '${pom.groupId}'
// module = ${pom.artifactId}
version = '${pom.version}'
"""

    w << "ext {\n"
    pom.properties.children().each { p ->
        String s = p.name()
        if(!s.startsWith('project.') && !s.startsWith('gem.') ) {
            w << "  ${convert(s)} = '${p}'\n"
        }
    }
    w << "}\n"

    w << "dependencies {\n"
    pom.dependencies.children().each { d ->
        
        String cfg='compile'
        if(d.groupId=='rubygems') { cfg='gems' }
        if(d?.scope == 'test') { cfg='testCompile' }
        
        if(d.artifactId=='erubis') {
            w << "  // WORKAROUND: for broken some POMs on Rubygems\n"
            w << "  ${cfg} \"brokenGem:erubis:\${erubis_version}@gem\"\n"
        } else if(d.artifactId=='asciidoctor-epub3') {
            w << "  ${cfg} \"${d.groupId}:${d.artifactId}:${convert(d.version.toString())}-SNAPSHOT\"\n"
        } else if(d.exclusions.size()) {
            w << "  ${cfg} (\"${d.groupId}:${d.artifactId}:${convert(d.version.toString())}\") {\n"
            w << "    exclude module : '${d.exclusions.exclusion.artifactId}'\n"
            w << "  }\n"
        } else {
            w << "  ${cfg} \"${d.groupId}:${d.artifactId}:${convert(d.version.toString())}\"\n"
        }
    }    
    
    w << "  testRuntime  \"org.slf4j:slf4j-simple:\${slf4_version}\"\n"
    w << "}\n"

    pom.profiles.children().each { p ->
        if (p.id == 'bintray-release' ) {
           w << """
if( !hasProperty( 'bintrayUser' ) )
  ext.bintrayUser = ''

if( !hasProperty( 'bintrayKey' ) )
  ext.bintrayKey = ''

jar {
  from (jruby.gemInstallDir) {
      include 'gems/**'
  }
  
  manifest {
      attributes 'Implementation-Version' : project.version
      attributes 'Main-Class': 'org.asciidoctor.cli.AsciidoctorInvoker'    
  }

}

task sourcesJar(type: Jar, dependsOn: classes) {
    classifier = 'sources'
    from sourceSets.main.allSource
}

artifacts {
    archives sourcesJar
}

bintray {
    user = project.bintrayUser
    key = project.bintrayKey
    publish = true
    dryRun = false
    configurations = ['archives']

    pkg {
        repo = '${p.distributionManagement.repository.id}'
        name = '${p.distributionManagement.repository.name}'
        labels = ['asciidoctor','asciidoc','asciidoctorj']

        version {
            name = project.version
            vcsTag = "v\${project.version}"
            desc = '${pom.description}'
        }
    }
}

"""
        }
        
    }
}
