@Component
• loan-lookup...
E VaultConfig....
public class GraphQLWiring {
E VaultHealthl...
" LoanServicel...D LoanReposito...
Quick Access
© DateTimeSca.
@Autowired
GraphQLDataFetchers graphQLDataFetchers;
private static final Logger LOGGER = LoggerFactory. getLogger (GraphQLDataFetchers.class);
public GraphQLWiring ()
graphQLDataFetchers = new GraphQLDataFetchers () ;
public RuntimeWiring buildGraphoLWiring() {
if (graphQLDatafetchers == null)
LOGGER. info("graphOLDataFetchers is null in GraphQLWiring" );
return RuntimeWiring.newRuntimeWiring()
• scalar (DateScalar .DATE_SCALAR)
.scalar (DateTimeScalarType.DATE_TIME)
• type ("Query", typeWiring -> typewiring
dataFetcher("getServicingLoanViewByStratusLastUpdateDate", graphQLDataFetchers.getServicingLoanViewByStratusLastUpdateDate())
dataFetcher("getServicingLoanViewByLoanIdentifiers",graphQLDataFetchers.getServicingLoanViewByLoanIdentifiers())
dataFetcher("get", environment -> {
handle "get" query, which selects the appropriate method based on the "type" argument
String type = environment. getArgument ("type");
if ("getServicingLoanViewByStratusLastUpdateDate".equals(type)) {
return graphQLDatafetchers.getServicingLoanViewByStratusLastUpdateDate();
} else if ("authors". equals (type)) {
return graphQLDataFetchers-getServicingLoanViewByLoanIdentifiers();
} else {
throw new RuntimeException("Invalid type:
" + type);
}))
.build ();
}}