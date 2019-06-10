package com.accenture;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.StringType;

import java.util.List;

/**
 *
 */
public class TypeUtils {
    private static StringType stringType = StringType.String;
    private static final Schema NULL_SCHEMA = Schema.create(Schema.Type.NULL);

    /**
     * Retrives the string type
     *
     * @return String type
     */
    private static String getStringType() {
        switch (stringType) {
            case String:        return "java.lang.String";
            case Utf8:          return "org.apache.dto.util.Utf8";
            case CharSequence:  return "java.lang.CharSequence";
            default: throw new RuntimeException("Unknown string type: " + stringType);
        }
    }

    /**
     * Retrieves the java type
     *
     * @param schema Schema (type)
     * @return Java type
     */
    public static String javaType(Schema schema) {
        switch (schema.getType()) {
            case RECORD:
                return schema.getName();
            case ENUM:
            case FIXED:
                return schema.getFullName();
            case ARRAY:
                return "java.util.List<" + javaType(schema.getElementType()) + ">";
            case MAP:
                return "java.util.Map<" + getStringType() + ","
                        + javaType(schema.getValueType()) + ">";
            case UNION:
                List<Schema> types = schema.getTypes(); // elide unions with null
                if ((types.size() == 2) && types.contains(NULL_SCHEMA))
                    return javaType(types.get(types.get(0).equals(NULL_SCHEMA) ? 1 : 0));
                return "java.lang.Object";
            case STRING:
                return getStringType();
            case BYTES:
                return "java.nio.ByteBuffer";
            case INT:
                return "java.lang.Integer";
            case LONG:
                return "java.lang.Long";
            case FLOAT:
                return "java.lang.Float";
            case DOUBLE:
                return "java.lang.Double";
            case BOOLEAN:
                return "java.lang.Boolean";
            case NULL:
                return "java.lang.Void";
            default:
                throw new RuntimeException("Unknown type: " + schema);
        }
    }
}
