/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package me.bechberger.jfr.query;

import jdk.jfr.consumer.RecordedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for holding rows, their values and textual
 * representation.
 */
public final class Table {
    private final List<Row> rows = new ArrayList<>();
    private final List<Field> fields = new ArrayList<>();

    boolean isEmpty() {
        return rows.isEmpty();
    }

    void addRows(List<Row> rows) {
        this.rows.addAll(rows);
    }
    public List<Row> getRows() {
        return rows;
    }

    void addFields(List<Field> fields) {
        for (int index = 0; index <fields.size(); index++) {
            if (fields.get(index).index != index) {
                throw new InternalError("Field index not in sync. with array position");
            }
        }
        this.fields.addAll(fields);
    }

    public List<Field> getFields() {
        return fields;
    }

    public void add(RecordedEvent event, List<Field> sourceFields) {
        Row row = new Row(fields.size());
        for (Field field : sourceFields) {
            row.putValue(field.index, field.valueGetter.apply(event));
        }
        rows.add(row);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Field field : fields) {
            sb.append(field.name).append("\t");
        }
        sb.append("\n");
        for (Row row : rows) {
            for (int i = 0; i < fields.size(); i++) {
                sb.append(row.getValue(i)).append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}