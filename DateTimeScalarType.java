public class DateTimeScalarType
{
public static final GraphQLScalarType DATE_TIME = GraphQLScalarType.newscalar()
. name ("DateTime")
description("A date-time string in ISO-8601 format (e.g. '2007-12-03T10:15:30')")
coercing(new Coercing<LocalDateTime, String> ()
@Override
public String serialize (Object input) {
if (input instanceof LocalDateTime) ‹
}
return ((LocalDateTime) input) .format (DateTimeFormatter. ISO_LOCAL_DATE_TIME);
T
throw new IllegalArgumentException ("Invalid DateTime format: " + input);
}
@Override
public LocalDateTime parseValue (Object input) ‹
if (input instanceof String) {
return LocalDateTime.parse( (String) input, DateTimeFormatter. ISO_LOCAL_DATE_TIME);
}
throw new IllegalArgumentException("Invalid DateTime format: " + input);
}
@Override
public LocalDateTime parseliteral(Object input) {
if (input instanceof Date) {
Instant instant = ( (Date) input). toInstant();
return LocalDateTime.ofInstant(instant, ZoneId.systemDefault ( ));
throw new IllegalArgumentException("Invalid DateTime format: " + input);
}
}
})
build ();
}