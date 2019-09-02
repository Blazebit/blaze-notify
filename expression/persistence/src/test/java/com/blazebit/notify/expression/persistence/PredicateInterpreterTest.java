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

public class PredicateInterpreterTest {

    private final ExpressionServiceFactory expressionServiceFactory;
    private Instant instant;

    public PredicateInterpreterTest() {
        DomainModel domainModel = Domain.getDefaultProvider().createDefaultBuilder().build();
        this.expressionServiceFactory = Expressions.forModel(domainModel);
    }

    private Boolean testPredicate(String expr) {
        ExpressionCompiler compiler = expressionServiceFactory.createCompiler();
        ExpressionInterpreter interpreter = expressionServiceFactory.createInterpreter();
        Predicate expression = compiler.createPredicate(expr);
        ExpressionInterpreter.Context context = interpreter.createContext(Collections.emptyMap(), Collections.emptyMap());
        if (instant != null) {
            context.setProperty(CurrentTimestampFunction.INSTANT_PROPERTY, instant);
            instant = null;
        }
        return interpreter.evaluate(expression, context);
    }

    @Test
    public void testBasic() {
        Assert.assertEquals(false, testPredicate("1 > 2"));
    }

    @Test
    public void testBasic2() {
        Assert.assertEquals(true, testPredicate("1 + 2.0 = 3"));
    }

    @Test
    public void testBasic3() {
        Assert.assertEquals(true, testPredicate("CURRENT_TIMESTAMP() = CURRENT_TIMESTAMP()"));
    }
}
