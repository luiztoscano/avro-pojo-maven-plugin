# avro-pojo-maven-plugin
Maven plugin to generate pojo files from avro schema files

## Getting started

### Prerequites

### Installing

### Usage

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
                    <goal>dto</goal>
                </goals>
                <configuration>
                    <sourceDirectory>${project.basedir}/src/main/avro</sourceDirectory>
                    <outputDirectory>${project.basedir}/src/main/java</outputDirectory>
                    <packageName>com.accenture.tst</packageName>
                    <fieldAccessor>private</fieldAccessor>
                    <includes>
                        <include>**/*.avsc</include>
                    </includes>
                    <imports>
                        <import>${project.basedir}/src/main/avro/action.avsc</import>
                    </imports>
                    <propertiesCamelCase>false</propertiesCamelCase>
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
