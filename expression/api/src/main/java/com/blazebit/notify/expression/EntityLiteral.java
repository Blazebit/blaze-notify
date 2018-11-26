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

import com.blazebit.notify.domain.runtime.model.EntityDomainType;
import com.blazebit.notify.domain.runtime.model.EntityDomainTypeAttribute;

import java.util.Map;

public class EntityLiteral {

    private final EntityDomainType entityDomainType;
    private final Map<EntityDomainTypeAttribute, Expression> attributeValues;

    public EntityLiteral(EntityDomainType entityDomainType, Map<EntityDomainTypeAttribute, Expression> attributeValues) {
        this.entityDomainType = entityDomainType;
        this.attributeValues = attributeValues;
    }

    public EntityDomainType getType() {
        return entityDomainType;
    }

    public Map<EntityDomainTypeAttribute, Expression> getAttributeValues() {
        return attributeValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityLiteral)) {
            return false;
        }

        EntityLiteral that = (EntityLiteral) o;

        if (!entityDomainType.equals(that.entityDomainType)) {
            return false;
        }
        return getAttributeValues().equals(that.getAttributeValues());
    }

    @Override
    public int hashCode() {
        int result = entityDomainType.hashCode();
        result = 31 * result + getAttributeValues().hashCode();
        return result;
    }
}
