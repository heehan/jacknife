package kr.co.jacknife.framework;

import javax.persistence.AttributeConverter;

public class EnumCode {

    public enum YnType {
        Y(1), N(0);
        private Integer digitCode;
        YnType(Integer digitCode)
        {
            this.digitCode = digitCode;
        }

        public static YnType fromDigitCode(Integer digitCode)
        {
            if (digitCode.equals(1)) return YnType.Y;
            else if (digitCode.equals(2)) return YnType.N;
            else return YnType.N;
        }

        public static class IntegerConverter implements AttributeConverter<YnType, Integer> {

            @Override
            public Integer convertToDatabaseColumn(YnType attribute) {
                return attribute.digitCode;
            }

            @Override
            public YnType convertToEntityAttribute(Integer dbData) {
                return YnType.fromDigitCode(dbData);
            }
        }

        public static class StringConverter implements AttributeConverter<YnType, String> {

            @Override
            public String convertToDatabaseColumn(YnType attribute) {
                return attribute.name();
            }

            @Override
            public YnType convertToEntityAttribute(String dbData) {
                if (dbData == null)
                    return YnType.N;
                return YnType.valueOf(dbData);
            }
        }
    }
}
