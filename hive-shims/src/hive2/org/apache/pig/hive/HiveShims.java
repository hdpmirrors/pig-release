/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pig.hive;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.io.orc.OrcFile;
import org.apache.hadoop.hive.ql.io.sarg.PredicateLeaf;
import org.apache.hadoop.hive.ql.io.sarg.SearchArgument;
import org.apache.hadoop.hive.ql.udf.generic.Collector;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.AbstractSerDe;
import org.apache.hadoop.hive.serde2.io.DateWritable;
import org.apache.hadoop.hive.serde2.io.HiveDecimalWritable;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.shims.HadoopShimsSecure;
import org.apache.hadoop.hive.shims.ShimLoader;
import org.apache.orc.OrcFile.Version;
import org.joda.time.DateTime;

import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;

public class HiveShims {
    public static String normalizeOrcVersionName(String version) {
        return Version.byName(version).getName();
    }

    public static void addLessThanOpToBuilder(SearchArgument.Builder builder,
            String columnName, PredicateLeaf.Type columnType, Object value) {
        builder.lessThan(columnName, columnType, value);
    }

    public static void addLessThanEqualsOpToBuilder(SearchArgument.Builder builder,
            String columnName, PredicateLeaf.Type columnType, Object value) {
        builder.lessThanEquals(columnName, columnType, value);
    }

    public static void addEqualsOpToBuilder(SearchArgument.Builder builder,
            String columnName, PredicateLeaf.Type columnType, Object value) {
        builder.equals(columnName, columnType, value);
    }

    public static void addBetweenOpToBuilder(SearchArgument.Builder builder,
            String columnName, PredicateLeaf.Type columnType, Object low, Object high) {
        builder.between(columnName, columnType, low, high);
    }

    public static void addIsNullOpToBuilder(SearchArgument.Builder builder,
            String columnName, PredicateLeaf.Type columnType) {
        builder.isNull(columnName, columnType);
    }

    public static Class[] getOrcDependentClasses(Class hadoopVersionShimsClass) {
        return new Class[] {OrcFile.class, HiveConf.class, AbstractSerDe.class,
                org.apache.hadoop.hive.shims.HadoopShims.class, HadoopShimsSecure.class, DateWritable.class,
                hadoopVersionShimsClass, Input.class, org.apache.orc.OrcFile.class,
                com.esotericsoftware.minlog.Log.class};
    }

    public static Class[] getHiveUDFDependentClasses(Class hadoopVersionShimsClass) {
        return new Class[] {GenericUDF.class,
                PrimitiveObjectInspector.class, HiveConf.class, Serializer.class, ShimLoader.class, 
                hadoopVersionShimsClass, HadoopShimsSecure.class, Collector.class, HiveDecimalWritable.class};
    }

    public static Object getSearchArgObjValue(Object value) {
        if (value instanceof Integer) {
            return new Long((Integer)value);
        } else if (value instanceof Float) {
            return new Double((Float)value);
        } else if (value instanceof BigInteger) {
            return new HiveDecimalWritable(HiveDecimal.create((BigInteger)value));
        } else if (value instanceof BigDecimal) {
            return new HiveDecimalWritable(HiveDecimal.create((BigDecimal)value));
        } else if (value instanceof DateTime) {
            return new java.sql.Date(((DateTime)value).getMillis());
        } else {
            return value;
        }
    }
}
