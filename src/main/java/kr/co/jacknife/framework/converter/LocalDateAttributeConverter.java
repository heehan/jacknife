package kr.co.jacknife.framework.converter;

import javax.persistence.AttributeConverter;
import java.sql.Timestamp;
import java.time.LocalDate;

public class LocalDateAttributeConverter  implements AttributeConverter<LocalDate, Timestamp> {
    @Override
    public Timestamp convertToDatabaseColumn(LocalDate localDate) {
        return (localDate == null ? null : Timestamp.valueOf(localDate.atStartOfDay()));
    }

    @Override
    public LocalDate convertToEntityAttribute(Timestamp dbData) {
        return (dbData == null ? null : dbData.toLocalDateTime().toLocalDate());
    }
}
