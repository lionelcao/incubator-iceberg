/*
 * Copyright 2017 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.iceberg.types;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.netflix.iceberg.types.Type.NestedType;
import com.netflix.iceberg.types.Type.PrimitiveType;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Types {
  private static final ImmutableMap<String, PrimitiveType> TYPES = ImmutableMap
      .<String, PrimitiveType>builder()
      .put(BooleanType.get().toString(), BooleanType.get())
      .put(IntegerType.get().toString(), IntegerType.get())
      .put(LongType.get().toString(), LongType.get())
      .put(FloatType.get().toString(), FloatType.get())
      .put(DoubleType.get().toString(), DoubleType.get())
      .put(DateType.get().toString(), DateType.get())
      .put(TimeType.withZone().toString(), TimeType.withZone())
      .put(TimeType.withoutZone().toString(), TimeType.withoutZone())
      .put(TimestampType.withZone().toString(), TimestampType.withZone())
      .put(TimestampType.withoutZone().toString(), TimestampType.withoutZone())
      .put(StringType.get().toString(), StringType.get())
      .put(UUIDType.get().toString(), UUIDType.get())
      .put(BinaryType.get().toString(), BinaryType.get())
      .build();

  private static final Pattern FIXED = Pattern.compile("fixed\\[(\\d+)\\]");
  private static final Pattern DECIMAL = Pattern.compile("decimal\\((\\d+),\\s+(\\d+)\\)");

  public static PrimitiveType fromPrimitiveString(String typeString) {
    String lowerTypeString = typeString.toLowerCase(Locale.ENGLISH);
    if (TYPES.containsKey(lowerTypeString)) {
      return TYPES.get(lowerTypeString);
    }

    Matcher fixed = FIXED.matcher(lowerTypeString);
    if (fixed.matches()) {
      return FixedType.ofLength(Integer.parseInt(fixed.group(1)));
    }

    Matcher decimal = DECIMAL.matcher(lowerTypeString);
    if (decimal.matches()) {
      return DecimalType.of(
          Integer.parseInt(decimal.group(1)),
          Integer.parseInt(decimal.group(2)));
    }

    throw new IllegalArgumentException("Cannot parse type string to primitive: " + typeString);
  }

  public static class BooleanType extends PrimitiveType {
    private static final BooleanType INSTANCE = new BooleanType();

    public static BooleanType get() {
      return INSTANCE;
    }

    @Override
    public TypeID typeId() {
      return TypeID.BOOLEAN;
    }

    @Override
    public String toString() {
      return "boolean";
    }
  }

  public static class IntegerType extends PrimitiveType {
    private static final IntegerType INSTANCE = new IntegerType();

    public static IntegerType get() {
      return INSTANCE;
    }

    @Override
    public TypeID typeId() {
      return TypeID.INTEGER;
    }

    @Override
    public String toString() {
      return "int";
    }
  }

  public static class LongType extends PrimitiveType {
    private static final LongType INSTANCE = new LongType();

    public static LongType get() {
      return INSTANCE;
    }

    @Override
    public TypeID typeId() {
      return TypeID.LONG;
    }

    @Override
    public String toString() {
      return "long";
    }
  }

  public static class FloatType extends PrimitiveType {
    private static final FloatType INSTANCE = new FloatType();

    public static FloatType get() {
      return INSTANCE;
    }

    @Override
    public TypeID typeId() {
      return TypeID.FLOAT;
    }

    @Override
    public String toString() {
      return "float";
    }
  }

  public static class DoubleType extends PrimitiveType {
    private static final DoubleType INSTANCE = new DoubleType();

    public static DoubleType get() {
      return INSTANCE;
    }

    @Override
    public TypeID typeId() {
      return TypeID.DOUBLE;
    }

    @Override
    public String toString() {
      return "double";
    }
  }

  public static class DateType extends PrimitiveType {
    private static final DateType INSTANCE = new DateType();

    public static DateType get() {
      return INSTANCE;
    }

    @Override
    public TypeID typeId() {
      return TypeID.DATE;
    }

    @Override
    public String toString() {
      return "date";
    }
  }

  public static class TimeType extends PrimitiveType {
    private static final TimeType INSTANCE_WITH_ZONE = new TimeType(true);
    private static final TimeType INSTANCE_WITHOUT_ZONE = new TimeType(false);

    public static TimeType withZone() {
      return INSTANCE_WITH_ZONE;
    }

    public static TimeType withoutZone() {
      return INSTANCE_WITHOUT_ZONE;
    }

    private final boolean adjustToUTC;

    private TimeType(boolean adjustToUTC) {
      this.adjustToUTC = adjustToUTC;
    }

    public boolean shouldAdjustToUTC() {
      return adjustToUTC;
    }

    @Override
    public TypeID typeId() {
      return TypeID.TIME;
    }

    @Override
    public String toString() {
      if (shouldAdjustToUTC()) {
        return "timetz";
      } else {
        return "time";
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TimeType timeType = (TimeType) o;
      return adjustToUTC == timeType.adjustToUTC;
    }

    @Override
    public int hashCode() {
      return Objects.hash(TimeType.class, adjustToUTC);
    }
  }

  public static class TimestampType extends PrimitiveType {
    private static final TimestampType INSTANCE_WITH_ZONE = new TimestampType(true);
    private static final TimestampType INSTANCE_WITHOUT_ZONE = new TimestampType(false);

    public static TimestampType withZone() {
      return INSTANCE_WITH_ZONE;
    }

    public static TimestampType withoutZone() {
      return INSTANCE_WITHOUT_ZONE;
    }

    private final boolean adjustToUTC;

    private TimestampType(boolean adjustToUTC) {
      this.adjustToUTC = adjustToUTC;
    }

    public boolean shouldAdjustToUTC() {
      return adjustToUTC;
    }

    @Override
    public TypeID typeId() {
      return TypeID.TIMESTAMP;
    }

    @Override
    public String toString() {
      if (shouldAdjustToUTC()) {
        return "timestamptz";
      } else {
        return "timestamp";
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TimestampType timestampType = (TimestampType) o;
      return adjustToUTC == timestampType.adjustToUTC;
    }

    @Override
    public int hashCode() {
      return Objects.hash(TimestampType.class, adjustToUTC);
    }
  }

  public static class StringType extends PrimitiveType {
    private static final StringType INSTANCE = new StringType();

    public static StringType get() {
      return INSTANCE;
    }

    @Override
    public TypeID typeId() {
      return TypeID.STRING;
    }

    @Override
    public String toString() {
      return "string";
    }
  }

  public static class UUIDType extends PrimitiveType {
    private static final UUIDType INSTANCE = new UUIDType();

    public static UUIDType get() {
      return INSTANCE;
    }

    @Override
    public TypeID typeId() {
      return TypeID.UUID;
    }

    @Override
    public String toString() {
      return "uuid";
    }
  }

  public static class FixedType extends PrimitiveType {
    public static FixedType ofLength(int length) {
      return new FixedType(length);
    }

    private final int length;

    private FixedType(int length) {
      this.length = length;
    }

    public int length() {
      return length;
    }

    @Override
    public TypeID typeId() {
      return TypeID.FIXED;
    }

    @Override
    public String toString() {
      return String.format("fixed[%d]", length);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o == null || getClass() != o.getClass()) {
        return false;
      }

      FixedType fixedType = (FixedType) o;
      return length == fixedType.length;
    }

    @Override
    public int hashCode() {
      return Objects.hash(FixedType.class, length);
    }
  }

  public static class BinaryType extends PrimitiveType {
    private static final BinaryType INSTANCE = new BinaryType();

    public static BinaryType get() {
      return INSTANCE;
    }

    @Override
    public TypeID typeId() {
      return TypeID.BINARY;
    }

    @Override
    public String toString() {
      return "binary";
    }
  }

  public static class DecimalType extends PrimitiveType {
    public static DecimalType of(int precision, int scale) {
      return new DecimalType(precision, scale);
    }

    private final int scale;
    private final int precision;

    private DecimalType(int precision, int scale) {
      this.scale = scale;
      this.precision = precision;
    }

    public int scale() {
      return scale;
    }

    public int precision() {
      return precision;
    }

    @Override
    public TypeID typeId() {
      return TypeID.DECIMAL;
    }

    @Override
    public String toString() {
      return String.format("decimal(%d, %d)", precision, scale);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o == null || getClass() != o.getClass()) {
        return false;
      }

      DecimalType that = (DecimalType) o;
      if (scale != that.scale) {
        return false;
      }
      return precision == that.precision;
    }

    @Override
    public int hashCode() {
      return Objects.hash(DecimalType.class, scale, precision);
    }
  }

  public static class NestedField implements Serializable {
    public static NestedField optional(int id, String name, Type type) {
      return new NestedField(true, id, name, type);
    }

    public static NestedField required(int id, String name, Type type) {
      return new NestedField(false, id, name, type);
    }

    private final boolean isOptional;
    private final int id;
    private final String name;
    private final Type type;

    private NestedField(boolean isOptional, int id, String name, Type type) {
      Preconditions.checkNotNull(name, "Name cannot be null");
      Preconditions.checkNotNull(type, "Type cannot be null");
      this.isOptional = isOptional;
      this.id = id;
      this.name = name;
      this.type = type;
    }

    public boolean isOptional() {
      return isOptional;
    }

    public boolean isRequired() {
      return !isOptional;
    }

    public int fieldId() {
      return id;
    }

    public String name() {
      return name;
    }

    public Type type() {
      return type;
    }

    @Override
    public String toString() {
      return String.format("%d: %s: %s %s",
          id, name, isOptional ? "optional" : "required", type);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o == null || getClass() != o.getClass()) {
        return false;
      }

      NestedField that = (NestedField) o;
      if (isOptional != that.isOptional) {
        return false;
      } else if (id != that.id) {
        return false;
      } else if (!name.equals(that.name)) {
        return false;
      }
      return type.equals(that.type);
    }

    @Override
    public int hashCode() {
      return Objects.hash(NestedField.class, id, isOptional, name, type);
    }
  }

  public static class StructType extends NestedType {
    private static final Joiner FIELD_SEP = Joiner.on(", ");

    public static StructType of(NestedField... fields) {
      return of(Arrays.asList(fields));
    }

    public static StructType of(List<NestedField> fields) {
      return new StructType(fields);
    }

    private final List<NestedField> fields;

    // lazy indexes
    private transient Map<String, NestedField> fieldsByName = null;
    private transient Map<Integer, NestedField> fieldsById = null;

    private StructType(List<NestedField> fields) {
      Preconditions.checkNotNull(fields, "Field list cannot be null");
      this.fields = ImmutableList.copyOf(fields);
    }

    @Override
    public List<NestedField> fields() {
      return fields;
    }

    public NestedField field(String name) {
      return lazyFieldsByName().get(name);
    }

    @Override
    public Type fieldType(String name) {
      NestedField field = field(name);
      if (field != null) {
        return field.type();
      }
      return null;
    }

    @Override
    public NestedField field(int id) {
      return lazyFieldsById().get(id);
    }

    @Override
    public TypeID typeId() {
      return TypeID.STRUCT;
    }

    @Override
    public boolean isStructType() {
      return true;
    }

    @Override
    public Types.StructType asStructType() {
      return this;
    }

    @Override
    public String toString() {
      return String.format("struct<%s>", FIELD_SEP.join(
          fields.stream()
              .map(NestedField::toString)
              .collect(Collectors.toList())));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o == null || getClass() != o.getClass()) {
        return false;
      }

      StructType that = (StructType) o;
      return fields.equals(that.fields);
    }

    @Override
    public int hashCode() {
      return Objects.hash(NestedField.class, fields);
    }

    private Map<String, NestedField> lazyFieldsByName() {
      if (fieldsByName == null) {
        indexFields();
      }
      return fieldsByName;
    }

    private Map<Integer, NestedField> lazyFieldsById() {
      if (fieldsById == null) {
        indexFields();
      }
      return fieldsById;
    }

    private void indexFields() {
      ImmutableMap.Builder<String, NestedField> byNameBuilder = ImmutableMap.builder();
      ImmutableMap.Builder<Integer, NestedField> byIdBuilder = ImmutableMap.builder();
      for (NestedField field : fields) {
        byNameBuilder.put(field.name(), field);
        byIdBuilder.put(field.fieldId(), field);
      }
      this.fieldsByName = byNameBuilder.build();
      this.fieldsById = byIdBuilder.build();
    }
  }

  public static class ListType extends NestedType {
    public static ListType ofOptional(int elementId, Type elementType) {
      Preconditions.checkNotNull(elementType, "Element type cannot be null");
      return new ListType(NestedField.optional(elementId, "element", elementType));
    }

    public static ListType ofRequired(int elementId, Type elementType) {
      Preconditions.checkNotNull(elementType, "Element type cannot be null");
      return new ListType(NestedField.required(elementId, "element", elementType));
    }

    private final NestedField elementField;
    private final List<NestedField> fields;

    private ListType(NestedField elementField) {
      this.elementField = elementField;
      this.fields = ImmutableList.of(elementField);
    }

    public Type elementType() {
      return elementField.type();
    }

    @Override
    public Type fieldType(String name) {
      if ("element".equals(name)) {
        return elementType();
      }
      return null;
    }

    @Override
    public NestedField field(int id) {
      if (elementField.fieldId() == id) {
        return elementField;
      }
      return null;
    }

    @Override
    public List<NestedField> fields() {
      return fields;
    }

    public int elementId() {
      return elementField.fieldId();
    }

    public boolean isElementOptional() {
      return elementField.isOptional;
    }

    @Override
    public TypeID typeId() {
      return TypeID.LIST;
    }

    @Override
    public boolean isListType() {
      return true;
    }

    @Override
    public Types.ListType asListType() {
      return this;
    }

    @Override
    public String toString() {
      return String.format("list<%s>", elementField.type());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ListType listType = (ListType) o;
      return elementField.equals(listType.elementField);
    }

    @Override
    public int hashCode() {
      return Objects.hash(ListType.class, elementField);
    }
  }

  public static class MapType extends NestedType {
    public static MapType ofOptional(int keyId, int valueId, Type valueType) {
      Preconditions.checkNotNull(valueType, "Value type cannot be null");
      return new MapType(
          NestedField.required(keyId, "key", StringType.get()),
          NestedField.optional(valueId, "value", valueType));
    }

    public static MapType ofRequired(int keyId, int valueId, Type valueType) {
      Preconditions.checkNotNull(valueType, "Value type cannot be null");
      return new MapType(
          NestedField.required(keyId, "key", StringType.get()),
          NestedField.required(valueId, "value", valueType));
    }

    private final NestedField keyField;
    private final NestedField valueField;
    private final List<NestedField> fields;

    private MapType(NestedField keyField, NestedField valueField) {
      this.keyField = keyField;
      this.valueField = valueField;
      this.fields = ImmutableList.of(keyField, valueField);
    }

    public Type keyType() {
      return keyField.type();
    }

    public Type valueType() {
      return valueField.type();
    }

    @Override
    public Type fieldType(String name) {
      if ("key".equals(name)) {
        return keyField.type();
      } else if ("value".equals(name)) {
        return valueField.type();
      }
      return null;
    }

    @Override
    public NestedField field(int id) {
      if (keyField.fieldId() == id) {
        return keyField;
      } else if (valueField.fieldId() == id) {
        return valueField;
      }
      return null;
    }

    @Override
    public List<NestedField> fields() {
      return fields;
    }

    public int keyId() {
      return keyField.fieldId();
    }

    public int valueId() {
      return valueField.fieldId();
    }

    public boolean isValueOptional() {
      return valueField.isOptional;
    }

    @Override
    public TypeID typeId() {
      return TypeID.MAP;
    }

    @Override
    public boolean isMapType() {
      return true;
    }

    @Override
    public Types.MapType asMapType() {
      return this;
    }

    @Override
    public String toString() {
      return String.format("map<%s, %s>",
          keyField.type(), valueField.type());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o == null || getClass() != o.getClass()) {
        return false;
      }

      MapType mapType = (MapType) o;
      if (!keyField.equals(mapType.keyField)) {
        return false;
      }
      return valueField.equals(mapType.valueField);
    }

    @Override
    public int hashCode() {
      return Objects.hash(MapType.class, keyField, valueField);
    }
  }
}