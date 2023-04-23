@Component
public class GraphQLDataFetchers {
private static final Logger LOGGER = LoggerFactory-getLogger (GraphQLDataFetchers.class);
public static final String MIDAS_LOAN_IDS = "loanIdentifiers"
public static final String START_DATE= "fromDate public static final String END_DATE= "toDate public static final String SERVICER
ACCOUNT IDENTIFIERS="servicerAccountIdentifiers"
@Autowired
ServicingLoanRepository servicingLoanRepository:
@Autowired
LoanRepository loanRepository;
public DataFetcher<List<SingleFamilyservicingLoan>> getServicingLoanViewByStratusLastUpdateDate() {
LOGGER. info("GraphQLDataFetchers - - getLoanViewByStratusLastUpdateDate ()") ;
try {
return dataFetchingEnvironment -> {
List<String> servicerAccountIdentifiers = dataFetchingEnvironment.getArgument(SERVICER_ACCOUNT_IDENTIFIERS);
String startDate2= dataFetchingEnvironment.getArgument(START_DATE);
String endDate2= dataFetchingEnvironment.getArgument(END_DATE) ;
DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault ()) ;
LocalDateTime startDate = LocalDateTime.parse (startDate2,
dtf);
LocalDateTime endDate = LocalDateTime.parse (endDate2, dtf);
LOGGER. info("Fetching Single Family containers for the date starting:
"+ startDate2+ " to date: "tendDat
" servicerAccountIdentifiers:
"'+servicerAccountIdentifiers ) ;
List‹SingleFamilyServicingLoān> servicingLoanResult=null;
if(servicerAccountIdentifiers!=null)(
servicingLoanResult = loanRepository. getBylastUpdatedateServicerAccountIdentifiers (stantDate, endDate,
}
if (servicerAccountIdentifiers=-null){
servicingLoanResult = servicingLoanRepository. getBylastUpdatedate (startate, endate) ;
}
List<SingleFamilyServicingLoan›curatedSingleFamilyServicingLoanResult=null;
if (servicingLoanResult!=null) {
curatedSingleFamilyServicingLoanResult=curatedSingleFamilyServicingLoanResult(servicingLoanResul
}
return curatedSingleFamilyServicingLoanResult;
};
]catch (Exception ex){
LOGGER. error ("Error retrieving list of loans by date", ex.getMessage ());
return null;
}