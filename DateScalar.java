sublic final class DateScalar {
Quick Access: isles
※印
public static final GraphQLScalarType DATE_SCALAR = GraphQLScalarType.newScalar() .name ("Date"). description("Date scalar type") .coercing(getCoercing()) .build
private DateScalar ( )
throw new IllegalStateException("Utility class");
}
T
public static Coercing getCoercing() {
return new Coercing() {
@Override
public Object serialize (Object datafetcherResult) throws CoercingSerializeException {
return serializeDate (dataFetcherResult);
}
@Override
public Object parseValue(Object input) throws CoercingSerializeException {
return input;
}
@Override
public Object parseLiteral (Object input) throws CoercingSerializeException {
return input;
}
g
B
PP o  
};
}
private static Object serializeDate (Object dataFetcherResult) ‹
SimpleDateFormat simpleFormat = new SimpleDateFormat ("MM/dd/yyyy HH:mm:ss");
if (datafetcherResult instanceof Date) {
return simpleFormat.format (dataFetcherResult);
} else
return null;
}
}