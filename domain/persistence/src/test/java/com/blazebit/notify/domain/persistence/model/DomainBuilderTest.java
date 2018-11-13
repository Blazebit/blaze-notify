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

package com.blazebit.notify.domain.persistence.model;

import com.blazebit.notify.domain.Domain;
import com.blazebit.notify.domain.boot.model.DomainBuilder;
import com.blazebit.notify.domain.runtime.model.DomainFunction;
import com.blazebit.notify.domain.runtime.model.DomainFunctionArgument;
import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.notify.domain.runtime.model.DomainType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainBuilderTest {

    private DomainModel domainModel;
    private DomainType stringType;
    private DomainType integerType;
    private DomainType decimalType;

    @Before
    public void before() {
        if (domainModel == null) {
            DomainBuilder domainBuilder = Domain.getDefaultProvider().createDefaultBuilder();
            domainModel = domainBuilder.build();
            stringType = domainModel.getType(String.class);
            integerType = domainModel.getType(Integer.class);
            decimalType = domainModel.getType(BigDecimal.class);
        }
    }

    private DomainType resolveFunctionType(String functionName, DomainType... argumentTypes) {
        Map<DomainFunctionArgument, DomainType> argumentDomainTypeMap = new HashMap<>(argumentTypes.length);
        DomainFunction function = domainModel.getFunction(functionName);
        List<DomainFunctionArgument> arguments = function.getArguments();
        for (int i = 0; i < argumentTypes.length; i++) {
            argumentDomainTypeMap.put(arguments.get(i), argumentTypes[i]);
        }
        return domainModel.getFunctionTypeResolver(functionName).resolveType(domainModel, function, argumentDomainTypeMap);
    }

    @Test
    public void testSubstring() {
        DomainFunction substring = domainModel.getFunction("substring");
        Assert.assertEquals("SUBSTRING", substring.getName());
        Assert.assertEquals(2, substring.getMinArgumentCount());
        Assert.assertEquals(-1, substring.getArgumentCount());
        Assert.assertEquals(stringType, substring.getResultType());

        Assert.assertEquals(3, substring.getArguments().size());
        Assert.assertEquals("string", substring.getArguments().get(0).getName());
        Assert.assertEquals(stringType, substring.getArguments().get(0).getType());
        Assert.assertEquals("start", substring.getArguments().get(1).getName());
        Assert.assertEquals(integerType, substring.getArguments().get(1).getType());
        Assert.assertEquals("end", substring.getArguments().get(2).getName());
        Assert.assertEquals(integerType, substring.getArguments().get(2).getType());
        Assert.assertEquals(stringType, resolveFunctionType("substring", stringType, integerType, integerType));
    }

    @Test
    public void testAbs() {
        DomainFunction substring = domainModel.getFunction("abs");
        Assert.assertEquals("ABS", substring.getName());
        Assert.assertEquals(1, substring.getMinArgumentCount());
        Assert.assertEquals(1, substring.getArgumentCount());
        Assert.assertNull(substring.getResultType());

        Assert.assertEquals(1, substring.getArguments().size());
        Assert.assertNull(substring.getArguments().get(0).getName());
        Assert.assertEquals(integerType, resolveFunctionType("abs", integerType));
        Assert.assertEquals(decimalType, resolveFunctionType("abs", decimalType));
    }
}
