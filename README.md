# avro-pojo-maven-plugin
Maven plugin to generate pojo files from avro schema files

## Getting started

### Prerequites
* Maven

### Installing

```
mvn clean install
```

### Usage
Parameters:
* phase - generate-sources
* goal - pojo
* sourceDirectory - Absolute path of directory containing avro files (optional: no)
* outputDirectory - Absolute path where the pojo files will be generated (optional: yes)
* packageName - Package name (optional: yes, default: avro record namespace)
* fieldAcessor - Field acessor (optional: yes, default: private)
* includes - List of avro files to be included (optional: yes, default: **/*avsc)
* excludes - List of avro files to be excluded (optional: yes, default: generated-sources)
* getters - Generate getter methods (optional: yes, default: true)
* setters - Generate setter methods (optional: yes, default: true)
* fieldsCamelCase - Convert fields to camel case (optional: yes, default: false)
* methodsCamelCase - Convert methods to camel case (optional: yes, default: false)
* delimiters - List of delimiter to be consideren when converting to camel case (optional: yes, default: _)
* imports - List of import files. Required to external record files (optional: yes)

Example:

```xml
<plugins>
    ...
    <plugin>
        <groupId>org.toscano</groupId>
        <artifactId>avro-pojo-maven-plugin</artifactId>
        <version>1.0.0</version>

        <executions>
            <execution>
                <phase>generate-sources</phase>
                <goals>
                    <goal>pojo</goal>
                </goals>
                <configuration>
                    <sourceDirectory>${project.basedir}/src/main/avro</sourceDirectory>
                    <outputDirectory>${project.basedir}/src/main/java</outputDirectory>
                    <packageName>org.toscano.pojo</packageName>
                    <fieldAccessor>private</fieldAccessor>
                    <includes>
                        <include>**/*.avsc</include>
                    </includes>
                    <imports>
                        <import>${project.basedir}/src/main/avro/action.avsc</import>
                    </imports>
                    <fieldsCamelCase>false</fieldsCamelCase>
               </configuration>
           </execution>
       </executions>
   </plugin>
   ...
</plugins>
```

## Built with

* Maven - https://maven.apache.org
* Avro - https://avro.apache.org
* Velocity - https://velocity.apache.org

## Versioning

## Authors

* Luiz Toscano - Initial work

See also the list of contributors who participated in this project.

## License

This project is licensed under the MIT License - see the LICENSE.md file for details
