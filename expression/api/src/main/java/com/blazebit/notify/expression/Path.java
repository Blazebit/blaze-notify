/*
 * Copyright 2018 Blazebit.
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

package com.blazebit.notify.expression;

import com.blazebit.notify.domain.runtime.model.DomainType;
import com.blazebit.notify.domain.runtime.model.EntityDomainTypeAttribute;

import java.util.Collections;
import java.util.List;

public class Path implements ArithmeticExpression {
    private final String alias;
    private final List<EntityDomainTypeAttribute> attributes;
    private final DomainType type;

    public Path(String alias, List<EntityDomainTypeAttribute> attributes, DomainType type) {
        this.alias = alias;
        this.attributes = attributes;
        this.type = type;
    }

    public static List<EntityDomainTypeAttribute> empty() {
        return Collections.emptyList();
    }

    public String getAlias() {
        return alias;
    }

    public List<EntityDomainTypeAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public DomainType getType() {
        return type;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Path)) {
            return false;
        }

        Path path = (Path) o;

        if (!getAlias().equals(path.getAlias())) {
            return false;
        }
        if (!getType().equals(path.getType())) {
            return false;
        }
        return getAttributes().equals(path.getAttributes());
    }

    @Override
    public int hashCode() {
        int result = getAlias().hashCode();
        result = 31 * result + getType().hashCode();
        result = 31 * result + getAttributes().hashCode();
        return result;
    }
}
