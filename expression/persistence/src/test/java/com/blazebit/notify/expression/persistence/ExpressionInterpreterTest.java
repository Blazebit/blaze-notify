package com.blazebit.notify.expression.persistence;

import com.blazebit.notify.domain.Domain;
import com.blazebit.notify.domain.runtime.model.DomainModel;
import com.blazebit.notify.expression.*;
import com.blazebit.notify.expression.persistence.function.CurrentTimestampFunction;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

public class ExpressionInterpreterTest {

    private final ExpressionServiceFactory expressionServiceFactory;
    private Instant instant;

    public ExpressionInterpreterTest() {
        DomainModel domainModel = Domain.getDefaultProvider().createDefaultBuilder().build();
        this.expressionServiceFactory = Expressions.forModel(domainModel);
    }

    private Object testExpression(String expr) {
        ExpressionCompiler compiler = expressionServiceFactory.createCompiler();
        ExpressionInterpreter interpreter = expressionServiceFactory.createInterpreter();
        Expression expression = compiler.createExpression(expr);
        ExpressionInterpreter.Context context = interpreter.createContext(Collections.emptyMap(), Collections.emptyMap());
        if (instant != null) {
            context.setProperty(CurrentTimestampFunction.INSTANT_PROPERTY, instant);
            instant = null;
        }
        return interpreter.evaluate(expression, context);
    }

    @Test
    public void testBasic() {
        Assert.assertEquals(3, testExpression("1 + 2"));
    }

    @Test
    public void testBasic2() {
        Assert.assertEquals(new BigDecimal("3.0"), testExpression("1 + 2.0"));
    }

    @Test
    public void testBasic3() {
        instant = Instant.ofEpochMilli(0);
        Assert.assertEquals(instant, testExpression("CURRENT_TIMESTAMP"));
    }
}
