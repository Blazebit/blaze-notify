package com.blazebit.notify.template.freemarker;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MessageFormatterMethod implements TemplateMethodModelEx {
    private final ResourceBundle messages;
    private final Locale locale;

    public MessageFormatterMethod(Locale locale, ResourceBundle messages) {
        this.locale = locale;
        this.messages = messages;
    }

    @Override
    public Object exec(List list) throws TemplateModelException {
        if (list.size() >= 1) {
            // resolve any remaining ${} expressions
            List<Object> resolved = resolve(list.subList(1, list.size()));
            String key = list.get(0).toString();
            return new MessageFormat(messages.getString(key),locale).format(resolved.toArray());
        } else {
            return null;
        }
    }

    private List<Object> resolve(List<Object> list) {
        ArrayList<Object> result = new ArrayList<>();
        for (Object item: list) {
            if (item instanceof SimpleScalar) {
                item = ((SimpleScalar) item).getAsString();
            }
            if (item instanceof String) {
                result.add(TemplatingUtil.resolveVariables((String) item, messages));
            } else {
                result.add(item);
            }
        }
        return result;
    }
}