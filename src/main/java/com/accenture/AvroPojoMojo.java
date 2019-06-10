package com.accenture;

import org.apache.avro.Schema;
import org.apache.commons.text.CaseUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Maven plugin to generate DTOs from Avro schema.
 */
@Mojo(name = "pojo", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class AvroPojoMojo extends AbstractMojo {

    @Parameter(property = "pojo.sourceDirectory", defaultValue = "${project.basedir}/src/main/resources", required = true)
    private File sourceDirectory;

    @Parameter(property = "pojo.outputDirectory", defaultValue = "${project.build.directory}/generated-sources")
    private File outputDirectory;

    @Parameter(property = "pojo.packageName", defaultValue = "")
    private String packageName;

    @Parameter(property = "pojo.fieldAccessor", defaultValue = "private")
    private String fieldAccessor;

    @Parameter
    private String[] includes = { "**/*.avsc" };

    @Parameter
    private String[] excludes = { };

    @Parameter
    private File[] imports = { };

    @Parameter(property = "pojo.getters", defaultValue = "true")
    private Boolean getters;

    @Parameter(property = "pojo.getters", defaultValue = "true")
    private Boolean setters;

    @Parameter(property = "pojo.fieldsCamelCase", defaultValue = "true")
    private Boolean fieldsCamelCase;

    @Parameter(property = "pojo.methodsCamelCase", defaultValue = "true")
    private Boolean methodsCamelCase;

    @Parameter
    private String[] delimiters = { "_" };


    private Map<String, Schema> schemas = new HashMap<>();

    /**
     * Convert string array to char array
     *
     * @param stringArray Array to be converted
     * @return Converted array
     */
    private char[] convertArray(String[] stringArray) {
        char[] charArray = new char[stringArray.length];

        for (Integer i=0; i < stringArray.length; i++) {
            charArray[i] = stringArray[i].charAt(0);
        }

        return charArray;
    }

    /**
     * Get the included files
     *
     * @param dir Directory to be scanned
     * @param includeFiles Files to be included
     * @param excludeFiles Files to be excluded
     * @return Included files
     */
    private String[] getIncludedFiles(String dir, String[] includeFiles, String[] excludeFiles, File[] imports) {
        FileSetManager fsm = new FileSetManager();
        FileSet fs = new FileSet();
        fs.setDirectory(dir);
        fs.setFollowSymlinks(false);

        for (String include : includeFiles) {
            fs.addInclude(include);
        }

        for (String exclude : excludeFiles) {
            fs.addExclude(exclude);
        }

        for (File fileName : imports) {
            fs.addExclude(fileName.getName());
        }

        return fsm.getIncludedFiles(fs);
    }

    /**
     * Scans the schema recursively to define the correct generation order
     *
     * @param stack Stack with the correct generation order
     * @param schema Schema to be scanned
     */
    private void scan(Stack<Schema> stack, Schema schema) {
        for (Schema.Field field : schema.getFields()) {
            Schema fieldSchema = field.schema();

            if (fieldSchema.getType() == Schema.Type.UNION) {
                List<Schema> types = fieldSchema.getTypes();

                if ((types.size() == 2) && (types.get(0).getType() == Schema.Type.NULL)) {
                    fieldSchema = types.get(1);
                }
            }

            if (fieldSchema.getType() == Schema.Type.ARRAY) {
                fieldSchema = fieldSchema.getElementType();
            }

            if (fieldSchema.getType() == Schema.Type.RECORD) {
                stack.push(fieldSchema);
                scan(stack, fieldSchema);
            }
        }
    }

    /**
     * Creates package directory if it not exists
     *
     * @return Package absolute path
     */
    private String createPackageDirectory() {
        String packagePath = packageName.replaceAll("\\.", "/");
        String packageAbsolutePath = outputDirectory.getAbsolutePath() + "/" + packagePath;
        File dir = new File(packageAbsolutePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return packageAbsolutePath;
    }

    /**
     * Generates java file based on schema
     *
     * @param schema Schema to be converted
     */
    private void generate(Schema schema) {
        List<Schema.Field> fields = schema.getFields();

        if (packageName == null || packageName.equals("")) {
            packageName = schema.getNamespace();
        }
        String className = schema.getName();

        VelocityEngine engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

        VelocityContext context = new VelocityContext();
        context.put("packageName", packageName);
        context.put("className", className);
        context.put("fieldAccessor", fieldAccessor);
        context.put("fields", fields);
        context.put("getters", getters);
        context.put("setters", setters);
        context.put("fieldsCamelCase", fieldsCamelCase);
        context.put("methodsCamelCase", methodsCamelCase);
        context.put("delimiters", convertArray(delimiters));
        context.put("CaseUtils", CaseUtils.class);
        context.put("TypeUtils", TypeUtils.class);

        Template template = engine.getTemplate("pojo.vm");
        String packageAbsolutePath = createPackageDirectory();
        String fileName = packageAbsolutePath + "/" + className + ".java";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            template.merge(context, bw);
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads file content
     *
     * @param file Avro file
     * @return File content
     * @throws IOException
     */
    private String readFile(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            StringBuffer buffer = new StringBuffer();
            String line = null;

            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }

            return buffer.toString();
        }
    }

    /**
     * Parses the avro files
     *
     * @param file Avro file
     * @return Parsed schema
     * @throws IOException
     */
    private Schema parse(File file) throws IOException {
        Schema.Parser parser = new Schema.Parser();
        parser.addTypes(schemas);

        return parser.parse(file);
    }

    /**
     * Load the import files and register them on parser
     *
     * @param dir Input directory
     * @param imports Files to be imported
     * @throws IOException
     */
    private void registerImports(String dir, File[] imports) throws IOException {
        for (File file : imports) {
            Schema schema = parse(file);

            schemas.put(schema.getFullName(), schema);
        }
    }

    /**
     * Plugin execution
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Stack<Schema> stack = new Stack<>();
            String[] files = getIncludedFiles(sourceDirectory.getAbsolutePath(), includes, excludes, imports);
            registerImports(sourceDirectory.getAbsolutePath(), imports);

            for (String fileName : files) {
                File file = new File(sourceDirectory.getAbsolutePath() + "/" + fileName);
                Schema schema = parse(file);
                stack.clear();
                scan(stack, schema);

                while (!stack.empty()) {
                    generate(stack.pop());
                }

                schemas.put(schema.getName(), schema);
                generate(schema);
            }
        } catch (IOException e) {
            getLog().error(e);
        }
    }
}
