package ru.tn;

import org.primefaces.application.resource.PrimeResourceHandler;
import ru.tn.model.ArchiveGridDataValueM;

import javax.el.BeanELResolver;
import javax.el.ELContext;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;

public class ExtendedBeanELResolver extends BeanELResolver {

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (property == null || base == null || base instanceof ResourceBundle || base instanceof Map
                || base instanceof Collection || base instanceof PrimeResourceHandler) {
            return null;
        }

        String propertyString = property.toString();

        if (propertyString.contains(".")) {
            String[] properties = propertyString.split("\\.");

            Object value = super.getValue(context, base, properties[0]);
            if (value instanceof ArchiveGridDataValueM[]) {
                int index = Integer.valueOf(properties[1]);
                if (properties.length == 2) {
                    return ((ArchiveGridDataValueM[]) value).length <= index ? null :
                            (((ArchiveGridDataValueM[]) value)[index] == null ? null : ((ArchiveGridDataValueM[]) value)[index].getValue());
                } else {
                    return ((ArchiveGridDataValueM[]) value).length <= index ? null :
                            (((ArchiveGridDataValueM[]) value)[index] == null ? null : ((ArchiveGridDataValueM[]) value)[index].getColor());
                }
            }
        }

        return super.getValue(context, base, property);
    }
}
