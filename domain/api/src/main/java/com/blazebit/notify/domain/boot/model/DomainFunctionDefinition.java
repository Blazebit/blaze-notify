package com.blazebit.notify.domain.boot.model;

import java.util.List;

public interface DomainFunctionDefinition extends MetadataDefinitionHolder<DomainFunctionDefinition> {

    public String getName();

    public int getMinArgumentCount();

    public int getArgumentCount();

    public List<DomainFunctionArgumentDefinition> getArgumentDefinitions();

    public DomainTypeDefinition<?> getResultTypeDefinition();
}
