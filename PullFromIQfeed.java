package com.nanoriver.data.subscription;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.nanoriver.data.entities.FutureOptionContract;

import com.nanoriver.data.entities.EquityOptionContract;
import com.nanoriver.utils.CMEContractDay;
import com.nanoriver.utils.CMEContractMonth;
import com.nanoriver.utils.EquityOptionsContractMonthCall;
import com.nanoriver.utils.EquityOptionsContractMonthPut;
import com.nanoriver.utils.TradingUtils;
import com.mathworks.engine.MatlabEngine;

import common.IQFeed_Socket;
import common.Java_Config;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * This class is used to pull live data
 */
public class PullFromIQFeed implements Serializable{

    private static final Logger logger = LogManager.getLogger(PullFromIQFeed.class);
    private static final long serialVersionUID = 1L;
	IQFeed_Socket C_Level1IQFeed_Socket;
	Java_Config config = new Java_Config();
	private int IQFEED_LEVEL1_PORT_DEFAULT = 0; // real port
	//private int IQFEED_LEVEL1_PORT_DEFAULT = 9100; //history port
	//private int IQFEED_LEVEL1_PORT_DEFAULT = 9400; //bw port
	private String ipAddress="localhost";

	private static String stockSymbolUsedLaterAsFileName=null; 
	private final String noData="E,!NO_DATA!,,";
	private final String endOfMsg="!ENDMSG!,";

	public PullFromIQFeed(int portNumber) 
	{
		C_Level1IQFeed_Socket = new IQFeed_Socket();
		//use history port when not using requesting real time data

		IQFEED_LEVEL1_PORT_DEFAULT= portNumber;

		//Attempt to connect our socket

		// requests a socket connection to localhost on port IQFEED_LEVEL1_PORT_DEFAULT, default = localhost and port 5009
		// Port 5009 is configurable in the registry.  See registry settings in the documentation.
		// If False is returned we are not able to connect display an error and exit. 
		if (!C_Level1IQFeed_Socket.ConnectSocket(ipAddress, IQFEED_LEVEL1_PORT_DEFAULT))
		{
			//JOptionPane.showMessageDialog(null, "Did you forget to login first?\nTake a look at the LaunchingTheFeed example app.");
			logger.log(Level.INFO,"Exiting because you are dumb");
			System.exit(1);				
		}

		logger.log(Level.INFO,"Connected to Level 1 port.");
		C_Level1IQFeed_Socket.CreateBuffers();

		//Initialize the protocol, this prepares us for commands to come and verifies that our socket is working as intended.
		try
		{
			C_Level1IQFeed_Socket.brBufferedWriter.write(String.format("S,SET PROTOCOL,%s\r\n",config.most_recent_protocol));
			C_Level1IQFeed_Socket.brBufferedWriter.flush();
			C_Level1IQFeed_Socket.brBufferedReader.readLine();

			//S,REQUEST CURRENT UPDATE FIELDNAMES
			if(IQFEED_LEVEL1_PORT_DEFAULT==5009) {
				C_Level1IQFeed_Socket.brBufferedWriter.write("SS,REQUEST CURRENT UPDATE FIELDNAMES, \n");
				C_Level1IQFeed_Socket.brBufferedWriter.flush();
				C_Level1IQFeed_Socket.brBufferedReader.readLine();

				
			C_Level1IQFeed_Socket.brBufferedWriter.write("S,CURRENT UPDATE FIELDNAMES,Symbol,Most Recent Trade Time,Bid,Bid Size, Ask,Ask Size \r\n");
			C_Level1IQFeed_Socket.brBufferedWriter.flush();
			C_Level1IQFeed_Socket.brBufferedReader.readLine();



			
			C_Level1IQFeed_Socket.brBufferedWriter.write("S,TIMESTAMPSOFF\n\r" + "");
			C_Level1IQFeed_Socket.brBufferedWriter.flush();
			C_Level1IQFeed_Socket.brBufferedReader.readLine();
			}

			logger.log(Level.INFO,"Message Posted, Protocol set.");
		}
		catch (Exception eError) 
		{
			logger.error("Error writing to socket.\n%s "+ eError.toString());
		}

	}
	public void pulldata(String command,String stockSymbolUsedLaterAsFileName,boolean isPullSixMonthsDataAllOption, boolean shouldBeStoredInAFile) {

		/**
		 * Creates new form Level1_Example_Frame
		 */

		String line;
		FileWriter pw =null;
		try {
			if(isPullSixMonthsDataAllOption) 
				C_Level1IQFeed_Socket.brBufferedWriter.write("HTT,"+stockSymbolUsedLaterAsFileName+",20201110 000000\n");
			else 
				C_Level1IQFeed_Socket.brBufferedWriter.write(command);

			C_Level1IQFeed_Socket.brBufferedWriter.flush();
			line = C_Level1IQFeed_Socket.brBufferedReader.readLine();
			if(stockSymbolUsedLaterAsFileName!=null) {
				pw = new FileWriter(new File("/home/ops/tickdata/"+stockSymbolUsedLaterAsFileName+".csv"),true);
				if(IQFEED_LEVEL1_PORT_DEFAULT!=5009) {
					pw.append("Timestamp,last,last size,total volume,bid,Ask, TickID,Basis For Last,Trade Market Center,Trade Conditions,Trade Aggressor,Day Code");
					//port is history
					while (line!=null && !line.contains("ENDMSG!")){
						{
							logger.log(Level.INFO," "+line);
							line =null;
							line=C_Level1IQFeed_Socket.brBufferedReader.readLine();
							if(line!=null) {
								pw.append("\n"+line);
							}

						}
					}
					pw.flush();
					pw.close();
					logger.log(Level.INFO,"DONEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
				}
				else if(shouldBeStoredInAFile){
					//pw.append("Field Name,7 Day Yield,Ask,Ask Change,Ask Market Center,Ask Size,Ask Time,Available Regions,Average Maturity,Bid,Bid Change,Bid Market Center,Bid Size,Bid Time,Change,Change From Open,Close,Close Range 1,Close Range 2,Days to Expiration,Decimal Precision,Delay,Exchange ID,Extended Trade,Extended Trade Date,Extended Trade Market Center,Extended Trade Size,Extended Trade Time,Extended Trading Change,Extended Trading Difference,Financial Status Indicator,Fraction Display Code,High,Last,Last Date,Last Market Center,Last Size,Last Time,Low,Market Capitalization,Market Open,Message Contents,Most Recent Trade,Most Recent Trade Aggressor,Most Recent Trade Conditions,Most Recent Trade Date,Most Recent Trade Day Code,Most Recent Trade Market Center,Most Recent Trade Size,Most Recent Trade Time,Net Asset Value,Number of Trades Today,Open,,Open Interest,Open Range 1,Open Range 2,Percent Change,Percent Off Average Volume,Previous Day Volume,Price-Earnings Ratio,Range,Restricted Code,Settle,Settlement Date,Spread,Symbol,Tick,TickID,Total Volume,Type,Volatility,VWAP");
					while (!line.contains("F,")|| !line.contains("P,@") )
					{
						logger.log(Level.INFO," \n"+line);
						line =null;
						line=C_Level1IQFeed_Socket.brBufferedReader.readLine();
						pw.append("\n"+line);

					}
				}
				else if(!shouldBeStoredInAFile){
					while (!line.contains("F,")|| !line.contains("P,@") )
					{
						logger.log(Level.INFO,line);
						line =null;
						line=C_Level1IQFeed_Socket.brBufferedReader.readLine();

					}
				}
			}

		} catch (Exception e) {
			logger.error(e);
		}
		finally {

			try {

				if(pw!=null) {
					pw.close();
				}
			} catch (IOException e) {
				logger.error(e);
			}
		}

	}



	void pullAllOptionableFromListOfStocksInAFile(){

			File allOptionableStockListFileName=new File("/home/ops/PycharmProjects/Earnings/volumestocks.txt");     //Creation of File Descriptor for input file
			try 
		(	FileReader fr=new FileReader(allOptionableStockListFileName);   //Creation of File Reader object
			BufferedReader br=new BufferedReader(fr)) //Creation of BufferedReader object
				{
			while((stockSymbolUsedLaterAsFileName=br.readLine())!=null)       //Reading the content line by line
			{


				pulldata(null,stockSymbolUsedLaterAsFileName,true,true);
			}
			
		} catch (Exception e) {

			logger.error(e);
		}
	}

	public String tickerHTXline(String tickerOrSingleOptionChain) throws IOException {
		C_Level1IQFeed_Socket.brBufferedWriter.write("HTX,"+tickerOrSingleOptionChain+",1,,,\n");
		C_Level1IQFeed_Socket.brBufferedWriter.flush();
		String htxLine = C_Level1IQFeed_Socket.brBufferedReader.readLine();
		return htxLine;
	}
	/**
	 * return daily data between two dates for a ticker HDT,GOOG,20080919,20080930,,,TESTREQUEST,2500,0
	 * @param ticker
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws IOException
	 */
	public String hDTline(String ticker, String startDate, String endDate) throws IOException {
		String hdtLine = null;
		C_Level1IQFeed_Socket.brBufferedWriter.write("HDT,"+ticker+","+startDate+","+endDate+",,,TESTREQ,2500,0 \n");
		C_Level1IQFeed_Socket.brBufferedWriter.flush();
		hdtLine = C_Level1IQFeed_Socket.brBufferedReader.readLine();
		return hdtLine;
	}

	public List<String> hDTBarData(String ticker, String startDate, String endDate) throws IOException {
		List<String> bars= new ArrayList<>();
		String hdtLine = null;
		C_Level1IQFeed_Socket.brBufferedWriter.write("HIX,AAPL,900,4 \n");

		C_Level1IQFeed_Socket.brBufferedWriter.flush();
		hdtLine = C_Level1IQFeed_Socket.brBufferedReader.readLine();
		while(!hdtLine.equals(endOfMsg)) {
			hdtLine = C_Level1IQFeed_Socket.brBufferedReader.readLine();
			bars.add(hdtLine);
			logger.log(Level.INFO,hdtLine);
		}
		return bars;
	}
	/**
	 * return ask price for option or such as @ESM24696900 or @ES
	 * @param htxLine
	 * @return
	 * @throws IOException 
	 */
	public double tickerHTXAskPrice(String htxLine) throws IOException {
		double htxSingle = Double.parseDouble(htxLine.split(",")[5]);
		return htxSingle;
	}

	/**
	 * return ask price for option or such as @ESM24696900 or @ES
	 * @param htxLine
	 * @return
	 * @throws IOException 
	 */
	public double tickerHTXBidPrice(String htxLine) throws IOException {
		double htxSingle = Double.parseDouble(htxLine.split(",")[4]);
		return htxSingle;
	}

	/**
	 * 
	 * @param ticker
	 * @param ask
	 * @param right 
	 * @return
	 */
	public EquityOptionContract equityOptionsChains(String ticker,double ask, MatlabEngine eng, String right){
		ArrayList<Double> deltaSelector=new ArrayList<>();
		HashMap<Double, Double> deltaDetStrikemap = new HashMap<>();
		HashMap<Double, Double> deltaOptionsPricemap = new HashMap<>();
		String[] strikes=null;
		Object[] results=null;
		EquityOptionContract equityOptionContract=null;
		try {

			//Get Near month @ESM22 for example from @ES input
		
			C_Level1IQFeed_Socket.brBufferedWriter.write("CEO,"+ticker+",,2,1,\r\n");
			C_Level1IQFeed_Socket.brBufferedWriter.flush();
			String nearMonth = C_Level1IQFeed_Socket.brBufferedReader.readLine();

			if(nearMonth.equals(endOfMsg)||nearMonth.equals(noData)) {
				C_Level1IQFeed_Socket.brBufferedWriter.write("CEO,"+nearMonth+",pc,,2,1,\r\n");
				C_Level1IQFeed_Socket.brBufferedWriter.flush();
				nearMonth = C_Level1IQFeed_Socket.brBufferedReader.readLine();
			}
			logger.log(Level.INFO,"Before break");  



			Pattern pattern = Pattern.compile(".+([A-Z].+)$");
			
			
			if(right.equalsIgnoreCase("call")) {
				strikes=nearMonth.split(",");
			}
			if (right.equalsIgnoreCase("put")) {
				int indexOfColon=nearMonth.indexOf(":")+1;
				nearMonth=nearMonth.substring(indexOfColon+1, nearMonth.length());
				strikes=nearMonth.split(",");

			}
			nearMonth=nearMonth.split(",")[1];
			equityOptionContract=getDeltaEquityStrikeExpirationOptimized(right,strikes,ticker);
			for (String singleStrike: strikes) {
				String formattedStrike=singleStrike.replace(",", "").trim();

				if (formattedStrike.startsWith(equityOptionContract.getFilterStrike())) {
					Matcher matcher = pattern.matcher(formattedStrike);
					if(matcher.find()) {
						String strikeUnclean =matcher.group(1);
						String strike = strikeUnclean.substring(1, strikeUnclean.length());
						double det=Double.parseDouble(strike)/100;
						if((strikeUnclean.startsWith("C")&&(Math.abs(det-ask)<(0.02*ask))&& det>ask)||(strikeUnclean.startsWith("P")&&(Math.abs(det-ask)<(0.02*ask))&& det<ask)){

							int daysUntilExpiration = 7;

							if(daysUntilExpiration<13 && daysUntilExpiration>=3) {
								PullFromIQFeed pfiqFeed =new PullFromIQFeed(9100);

								double optionPrice=getOptionsAskPrice(formattedStrike,pfiqFeed);//Double.valueOf(tickerHTXAskPrice(optionsAsk));
								if(!(optionPrice ==0)) {
									double timeUntilExpiration= (double)daysUntilExpiration/365;
									MatlabEngine eng2=MatlabEngine.startMatlab();

									double impliedVolatility = eng2.feval("blsimpv", ask, det, 0.0529,timeUntilExpiration,optionPrice,0);
									if(!Double.isNaN(impliedVolatility)){
										//TODO make sure to use dividend when applicable
									results =eng2.feval(2,"blsdelta", ask,det,0.0758,timeUntilExpiration,impliedVolatility,0.01);
									double callDeltaCalculated=(Double) results[0];
									double putDeltaCalculated=(Double) results[1];

									if(strikeUnclean.startsWith("C")) {
										deltaDetStrikemap.put( callDeltaCalculated,det);
										deltaOptionsPricemap.put(callDeltaCalculated, optionPrice);
										deltaSelector.add( callDeltaCalculated);
									}
									else if(strikeUnclean.startsWith("P")) {
										deltaDetStrikemap.put(putDeltaCalculated,det);
										deltaOptionsPricemap.put(putDeltaCalculated, optionPrice);
										deltaSelector.add( putDeltaCalculated);
									}
									}

								}
							}
						}
					}
				}
			}
			double closestStrikeDelta=0;
			if(deltaSelector!=null) {
				if(right.equalsIgnoreCase("call")) {
					 closestStrikeDelta=TradingUtils.closest(0.33,deltaSelector );
				}
				if(right.equalsIgnoreCase("put")) {
					 closestStrikeDelta=TradingUtils.closest(-0.33,deltaSelector );

				}
			Double closestdet= deltaDetStrikemap.get(closestStrikeDelta);
			double closetOptionPrice = deltaOptionsPricemap.get(closestStrikeDelta);
			equityOptionContract.setStrike(closestdet);
			equityOptionContract.setDeltaOptimizedOptionPrice(closetOptionPrice);
			}
		} catch (IOException e) {
			logger.error(e);
		} catch (RejectedExecutionException | InterruptedException | ExecutionException e) {
			logger.error(e);
		    Thread.currentThread().interrupt();
		}
		return equityOptionContract;
	}

	private EquityOptionContract getDeltaEquityStrikeExpirationOptimized(String right, String[] strikes, String ticker) {
		// TODO Auto-generated method stub
		Set<String> myset = new HashSet<>();
		for(String strike:strikes) {
			if(strike.startsWith(ticker)) {
			myset.add(strike.substring(ticker.length(), ticker.length()+5));
			}
		}
		getEquityDaysUntilExpiration(ticker, right, myset);
		return null;
		
	}
	private int getEquityDaysUntilExpiration(String ticker, String right,Set<String> uniqueStrikes) {
		// AAPL2224F139
		int tickerLength=ticker.length();
		String formattedStrike="todo";
		int expirationDayOfMonth=  Integer.parseInt(formattedStrike.substring(tickerLength+3 ,tickerLength+4));
		ZonedDateTime zd= ZonedDateTime.now(); 
		LocalDate localDateToday=zd.toLocalDate();
		String strikeUnclean="todo";
		String monthOptionChainExpires=EquityOptionsContractMonthCall.valueOf(String.valueOf(strikeUnclean.charAt(0))).getValue();
		if(monthOptionChainExpires==null)
			monthOptionChainExpires=EquityOptionsContractMonthPut.valueOf(String.valueOf(strikeUnclean.charAt(0))).getValue();

		LocalDate dayOptionChainExpires= LocalDate.of(localDateToday.getYear(),Month.valueOf(monthOptionChainExpires.toUpperCase()).getValue() , expirationDayOfMonth);
		int numDays = Period.between(localDateToday, dayOptionChainExpires).getDays();

		//get days between after retrieving month code. MAke sure short year is always current year like 22
		return numDays ;
	}

	public int getFuturesDaysUntilExpiration(String ticker, String formattedStrike,String strikeUnclean, FutureOptionContract futureOptionContract) {
		// AAPL2224F139

		int numDays=0;
		String holidayAdustedExpiration=futureOptionContract.getExpirationDate();
		if(holidayAdustedExpiration!=null) {
			ZonedDateTime zd= ZonedDateTime.now(); 
			LocalDate localDateToday=zd.toLocalDate();
			
			 numDays= Period.between(localDateToday, LocalDate.parse(holidayAdustedExpiration)).getDays();

		} 
		
		else	
			 numDays=getDayofExpiration(formattedStrike);


		return numDays ;
	}
	private int getDayofExpiration(String formattedStrike ) {
		try {
			Month mofStrike=null;
			int weekNumber=0;
			DayOfWeek doWeek=null;
			int year =0;
			int numDays = 0;
			year =Integer.valueOf("20"+formattedStrike.substring(5, 7));

			if(formattedStrike.substring(2, 3).equals("1")	||
					formattedStrike.substring(2, 3).equals("2") ||
					formattedStrike.substring(2, 3).equals("3") ||
					formattedStrike.substring(2, 3).equals("4") ||
					formattedStrike.substring(2, 3).equals("5")) {
				//week as char 2
				weekNumber= Integer.parseInt(formattedStrike.substring(2, 3));
				logger.log(Level.INFO,"substring lesson "+formattedStrike.substring(2, 3));
				if (formattedStrike.substring(3, 4).equals("A"))
					doWeek=DayOfWeek.MONDAY;
				if(formattedStrike.substring(3, 4).equals("B"))
					doWeek =DayOfWeek.TUESDAY;
				if(formattedStrike.substring(3, 4).equals("C"))
					doWeek =DayOfWeek.WEDNESDAY;
				if(formattedStrike.substring(3, 4).equals("D")){
					doWeek =DayOfWeek.THURSDAY;
				}
				String cmeMonth=CMEContractMonth.valueOf(formattedStrike.substring(4, 5)).getValue();
				if(cmeMonth!=null) {
					//End of month expiration
					mofStrike=Month.valueOf(cmeMonth.toUpperCase());

				}

			}
			if(formattedStrike.substring(2, 3).equals("W")) {
				//friday things
				if(formattedStrike.substring(3, 4).equals("1")	||
						formattedStrike.substring(3, 4).equals("2") ||
						formattedStrike.substring(3, 4).equals("3") || 
						formattedStrike.substring(3, 4).equals("4") || 
						formattedStrike.substring(3, 4).equals("5")){
					weekNumber=Integer.valueOf(formattedStrike.substring(3, 4));
					doWeek =DayOfWeek.FRIDAY;
					String cmeMonth=CMEContractMonth.valueOf(String.valueOf(formattedStrike.substring(4, 5)).toString()).getValue();

					if(cmeMonth!=null) {
						mofStrike=Month.valueOf(cmeMonth.toUpperCase());

					}
					year =Integer.valueOf("20"+formattedStrike.substring(5, 7));
				}
				else {

					String cmeMonth=CMEContractMonth.valueOf(formattedStrike.substring(3, 4)).getValue();
					if(cmeMonth!=null) {
						mofStrike=Month.valueOf(cmeMonth.toUpperCase());
						weekNumber=4;
						year =Integer.valueOf("20"+formattedStrike.substring(4, 6));
						doWeek=LocalDate.now().withYear(year).with(mofStrike).with(TemporalAdjusters.lastDayOfMonth()).getDayOfWeek();
					}
					else {

					}
				}

			}
			if(formattedStrike.substring(2, 3).equals("Y")&&(formattedStrike.substring(3, 4).equals("C"))) {

			}

			LocalDate optionsChainsExpirationsDate = 
					YearMonth.of( year , mofStrike )
					.atDay( 1 )
					.with( TemporalAdjusters.dayOfWeekInMonth( weekNumber , doWeek ) );
			ZonedDateTime zd= ZonedDateTime.now(); 
			LocalDate localDateToday=zd.toLocalDate();
			numDays = Period.between(localDateToday, optionsChainsExpirationsDate).getDays();
			return numDays;
		}
		catch(Exception e) {
			logger.error(e);
			return 0;
		}
	}
	/**
	 * futures options ticker(example @ES)
	 * @param ticker
	 * @return
	 */ 
	public String getNearMonth(String ticker) {
		String nearMonth = null;

		//Get Near month @ESM22 for example from @ES input
		try {
            LocalDate current_date = LocalDate.now();
            String current_year_last_char = String.valueOf(current_date.getYear());
            String next_year_last_char = String.valueOf(current_date.getYear()+1);
			C_Level1IQFeed_Socket.brBufferedWriter.write("CFU,"+ticker+",,"+current_year_last_char.charAt(current_year_last_char.length()-1)+""+(next_year_last_char+1)+",1,\r\n");
			C_Level1IQFeed_Socket.brBufferedWriter.flush();
			nearMonth = C_Level1IQFeed_Socket.brBufferedReader.readLine();

			String erroneousNearMonth=nearMonth.toString();
			if(erroneousNearMonth.equals(endOfMsg)||erroneousNearMonth.equals(noData))  
				getNearMonth(ticker);

			nearMonth=nearMonth.split(",")[1];
		}
		catch (Exception e) {
			logger.error(e);
		}
		return nearMonth;

	}
	public FutureOptionContract futuresOptionsChains(String nearMonth,double ask, MatlabEngine eng, String right){
		//use @ESM22 to get list of options chains like @ESM22C314000		
		ConcurrentLinkedDeque<Double> deltaSelector=new ConcurrentLinkedDeque<>();
		ConcurrentHashMap<Double, Double> deltaDetStrikemap = new ConcurrentHashMap<>();
		ConcurrentHashMap<Double, Double> deltaOptionsPricemap = new ConcurrentHashMap<>();
		Stream<String> strikes=null;
		Future<FutureOptionContract> future=null;
		try {
			FutureOptionContract futureOptionContract=getDeltaStrikeExpirationOptimized(right,ask);
			C_Level1IQFeed_Socket.brBufferedWriter.write("CFO,"+nearMonth+",pc,,23,1,\r\n");

			C_Level1IQFeed_Socket.brBufferedWriter.flush();
			String chainStrng = null;
			chainStrng = C_Level1IQFeed_Socket.brBufferedReader.readLine();
			//retry on end message EOM
			if(chainStrng.equals(endOfMsg)||chainStrng.equals(noData)) {
				C_Level1IQFeed_Socket.brBufferedWriter.write("CFO,"+nearMonth+",pc,,2,1,\r\n");
				C_Level1IQFeed_Socket.brBufferedWriter.flush();
				chainStrng = C_Level1IQFeed_Socket.brBufferedReader.readLine();
			}

			Pattern pattern = Pattern.compile(".+([A-Z].+)$");
			if(right.equalsIgnoreCase("call")) {
				//strikes=chainStrng.split(",");
				strikes=Stream.of(chainStrng.split(","));
				String filterStrike=futureOptionContract.getFilterStrike().concat(right.substring(0,1).toString());
				strikes=strikes.parallel().filter(cs->cs.startsWith(filterStrike));
			}
			if (right.equalsIgnoreCase("put")) {
				int indexOfColon=chainStrng.indexOf(":");
				chainStrng=chainStrng.substring(indexOfColon+1, chainStrng.length());
				//strikes=Stream.of(chainStrng.split(","));
				//String filterStrike=futureOptionContract.getFilterStrike().concat(right.substring(0,1).toString());
				//strikes=strikes.parallel().filter(cs->cs.startsWith(filterStrike));
			}
		
			long startTime= System.currentTimeMillis();
			ForkJoinPool excr = ForkJoinPool.commonPool();//newFixedThreadPool(Runtime.getRuntime().availableProcessors()  , ThreadFactory);
			String[] striker=strikes.toArray(String[]::new);
			String lastToken = striker[striker.length-1];
			 CountDownLatch startSignal = new CountDownLatch(1);
		     CountDownLatch doneSignal = new CountDownLatch(striker.length);
			for (String singleStrike: striker) {
				
			 future=excr.submit(new Threadimpl(startSignal,doneSignal, singleStrike, futureOptionContract, eng,deltaDetStrikemap,deltaOptionsPricemap,deltaSelector,ask,pattern, right, lastToken));
			}
			
			excr.shutdown();
			try {
			     // Wait a while for existing tasks to terminate
			     if (!excr.isTerminated()) {
			    	 excr.shutdownNow(); // Cancel currently executing tasks
			       // Wait a while for tasks to respond to being cancelled
			       if (!excr.awaitTermination(60, TimeUnit.SECONDS))
			           logger.error("Pool did not terminate");
			     }
			   } catch (InterruptedException ie) {
			     // (Re-)Cancel if current thread also interrupted
				   excr.shutdownNow();
			     // Preserve interrupt status
			     Thread.currentThread().interrupt();
			   }
			logger.log(Level.INFO,"took approximately: "+(System.currentTimeMillis()-startTime));

			return future.get();

		} catch (Exception e) {
			logger.error(e);
		}
		return null;

	}	
	
	public FutureOptionContract getDeltaStrikeExpirationOptimized(String right, double ask) {
		
		FutureOptionContract futureOptionContract=new FutureOptionContract();
		LocalDate date=TradingUtils.nextOptionsExpirationDateCalculator();
		Month month=date.getMonth();
		int year=date.getYear();
		DayOfWeek dayOfWeek=date.getDayOfWeek();
		StringBuffer buf=new StringBuffer();
		buf.append("@E");
		LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());
		boolean isRolloverDate=rolloverDateCalculator(date);
		int fridayOrdinal=DayOfWeek.FRIDAY.getValue();
		int dayOfWeekOrdinal=dayOfWeek.getValue();

		
		ZonedDateTime zonedDateTime = date.atStartOfDay(ZoneId.of( "America/New_York" ));
		Instant instant = zonedDateTime.toInstant();
		Date qdate = Date.from(instant);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(qdate);
		
//		if(ask>0) {
//			//char c = Character.;
//			
//		}
		
		
		int week=calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
		
		if((dayOfWeek!=DayOfWeek.FRIDAY)&&(date!=endOfMonth)&&!(isRolloverDate)) {
			//E4BZ22C150000
			buf.append(week);
			String cmeDay=CMEContractDay.valueOf(dayOfWeek.toString()).getValue();
			buf.append(cmeDay);
			String cmeMonth=CMEContractMonth.valueOf(month.toString()).getValue();
			buf.append(cmeMonth);
			String str = year+""; //connverting int to string
			String digit = str.substring(str.length()-2, str.length());
			buf.append(digit);
			futureOptionContract.setRight(right);
			futureOptionContract.setFilterStrike(buf.toString());


		}
		if((dayOfWeek.equals(DayOfWeek.FRIDAY))&&(date!=endOfMonth)&&!(isRolloverDate)) {
			buf.append("W");
			buf.append(week);
			String cmeMonth=CMEContractMonth.valueOf(month.toString()).getValue();
			buf.append(cmeMonth);

			String str = year+""; //connverting int to string
			String digit = str.substring(str.length()-1);
			buf.append(digit);
			futureOptionContract.setRight(right);
			futureOptionContract.setFilterStrike(buf.toString());


		}
		if (dayOfWeekOrdinal==fridayOrdinal) {
			
			
			if(date==endOfMonth) {
				
			}
			
		}
		if(date==endOfMonth ) {
			buf.append("W");
			String cmeMonth=CMEContractMonth.valueOf(month.toString()).getValue();
			buf.append(cmeMonth);

			String str = year+""; //connverting int to string
			String digit = str.substring(str.length()-1);
			buf.append(digit);
			futureOptionContract.setRight(right);
			futureOptionContract.setFilterStrike(buf.toString());
			}
		if(isRolloverDate) {
			buf.append("S");
			String cmeMonth=CMEContractMonth.valueOf(month.toString()).getValue();
			buf.append(cmeMonth);

			String str = year+""; //connverting int to string
			String digit = str.substring(str.length()-1);
			buf.append(digit);
			futureOptionContract.setRight(right);
			futureOptionContract.setFilterStrike(buf.toString());
			}
			//EYCM3
		
		futureOptionContract.setExpirationDate(date.toString());
		return futureOptionContract;
		
	}
	
	public boolean rolloverDateCalculator(LocalDate date) {
		List<LocalDate> rolloverDates=new ArrayList<>();
		LocalDate.parse("2022-09-12", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		rolloverDates.add(LocalDate.parse("2022-09-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		rolloverDates.add(LocalDate.parse("2022-12-16", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		if(rolloverDates.contains(date))	{
			return true;
		}
		return false;
	}
	private double getOptionsAskPrice(String formattedStrike,PullFromIQFeed pfiqFeed) {
		String line;
		try {
			if(pfiqFeed==null) {
				pfiqFeed =new PullFromIQFeed(9100);
			}
			line = pfiqFeed.tickerHTXline(formattedStrike);
			if(line.contains("@")||line.equals(endOfMsg)) {
				getOptionsAskPrice(formattedStrike,null);
			}
			double ask=0;
			if(!line.equals(noData)) {
				ask=pfiqFeed.tickerHTXAskPrice(line);
			}
			return ask;
		} catch (IOException e) {
			logger.error(e);
		}
		return 0;
	}
	
	
	public double getOptionsAskPriceWatchLive(String formattedStrike) {
		String line;
		double ask=0;
		int count=0;
		try(IQFeed_Socket C_Level1IQFeed_Socket2=new IQFeed_Socket()) {
			String command = "w"+formattedStrike+"\n\r";

			C_Level1IQFeed_Socket2.ConnectSocket("localhost", 5009);
			C_Level1IQFeed_Socket2.CreateBuffers();
			

			C_Level1IQFeed_Socket2.brBufferedWriter.write(command);

			C_Level1IQFeed_Socket2.brBufferedWriter.flush();
			
			line=C_Level1IQFeed_Socket2.brBufferedReader.readLine();
			
			
			if(line.contains("@")||line.equals(endOfMsg)) {

				line=C_Level1IQFeed_Socket2.brBufferedReader.readLine();
				
				
			}
			
			while (!line.startsWith("P,@") && count<20) {
				logger.log(Level.INFO,"line is "+ line);
				
				line=C_Level1IQFeed_Socket2.brBufferedReader.readLine();
				count++;
			}
			if (line.startsWith("P,@")){
				String askStringLocation =line.split(",")[11];
				if(askStringLocation!=null&&!askStringLocation.equals("")) {
					logger.log(Level.INFO,"gold mine is saying: "+line);
					ask=Double.valueOf(askStringLocation);
				}
				else
					logger.log(Level.INFO,"***data starvation");
			}
				C_Level1IQFeed_Socket2.brBufferedWriter.write("r"+formattedStrike+"\n\r");

				C_Level1IQFeed_Socket2.brBufferedWriter.flush();
				
			return ask;
		} catch (IOException e) {
			logger.error(e);
		}
		return 0;
	}
	
	
	
	public static void main(String[] args) throws FileNotFoundException {
		UUID fileName=UUID
                .fromString(
                    "58e0a7d7-eebc-11d8-9669-0800200c9a66");
		logger.log(Level.INFO,fileName.timestamp());
		PullFromIQFeed pfiqFeed =new PullFromIQFeed(5009);
		pfiqFeed.pulldata("w@ESH23\n\r", fileName.toString()+"ondate"+fileName.timestamp(), false, true);

	}

	 class Threadimpl implements Callable<FutureOptionContract> {	
		private String singleStrike;
		
		private FutureOptionContract futureOptionContract;
		private MatlabEngine eng;
		private ConcurrentLinkedDeque<Double> deltaSelector;
		private ConcurrentHashMap<Double, Double> deltaDetStrikemap;
		private ConcurrentHashMap<Double, Double> deltaOptionsPricemap;
		private Object[] results;
		private double ask;
		private String right;

		private Pattern pattern;
		private String lastToken;
		private final CountDownLatch startSignal;
		private final CountDownLatch doneSignal;
		
		public Threadimpl(CountDownLatch startSignal, CountDownLatch doneSignal, String singleStrike, FutureOptionContract futureOptionContract, MatlabEngine eng,ConcurrentHashMap<Double, Double> deltaDetStrikemap,
				ConcurrentHashMap<Double, Double> deltaOptionsPricemap,ConcurrentLinkedDeque<Double> deltaSelector,double ask,	Pattern pattern, String right, String lastToken) {
			this.startSignal=startSignal;
			this.doneSignal=doneSignal;
			this.singleStrike=singleStrike;
			this.futureOptionContract=futureOptionContract;
			this.eng=eng;
			this.deltaOptionsPricemap=deltaOptionsPricemap;
			this.deltaSelector=deltaSelector;
			this.deltaDetStrikemap=deltaDetStrikemap;
			this.ask=ask;
			this.pattern=pattern;
			this.right=right;
			this.lastToken=lastToken;
		}

		@Override
		public FutureOptionContract call() {
			try {
				startSignal.countDown();
				String formattedStrike=singleStrike.replace(",", "").trim();
				//.substring(0,futureOptionContract.getFilterStrike().length()-1)
				if (formattedStrike.startsWith(futureOptionContract.getFilterStrike())) {
					Matcher matcher = pattern.matcher(formattedStrike);
					if(matcher.find()) {
						String strikeUnclean =matcher.group(1);
						String strike = strikeUnclean.substring(1, strikeUnclean.length());
						double det=Double.parseDouble(strike)/100;
						if((strikeUnclean.startsWith("C")&&(Math.abs(det-ask)<(0.02*ask))&& det>ask)||(strikeUnclean.startsWith("P")&&(Math.abs(det-ask)<(0.02*ask))&& det<ask)){

							int daysUntilExpiration = getFuturesDaysUntilExpiration(formattedStrike,formattedStrike,strikeUnclean,futureOptionContract);

							if(daysUntilExpiration<13 && daysUntilExpiration>=2) {
								
								double optionPrice=getOptionsAskPriceWatchLive(formattedStrike);
								if(optionPrice !=0) {
									double timeUntilExpiration= (double)daysUntilExpiration/365;
									double impliedVolatility = eng.feval("blsimpv", ask, det, 0.0529,timeUntilExpiration,optionPrice,0);
									if(!Double.isNaN(impliedVolatility)){
										results =eng.feval(2,"blsdelta", ask,det,0.0758,timeUntilExpiration,impliedVolatility,0.01);
										double callDeltaCalculated=(Double) results[0];
										double putDeltaCalculated=(Double) results[1];

										if(strikeUnclean.startsWith("C")) {
											deltaDetStrikemap.put( callDeltaCalculated,det);
											deltaOptionsPricemap.put(callDeltaCalculated, optionPrice);
											deltaSelector.add( callDeltaCalculated);
										}
										else if(strikeUnclean.startsWith("P")) {
											deltaDetStrikemap.put(putDeltaCalculated,det);
											deltaOptionsPricemap.put(putDeltaCalculated, optionPrice);
											deltaSelector.add( putDeltaCalculated);
										}
									}

								}
							}
						}
					}
				}
			
			//
			double closestStrikeDelta=0;
			if(singleStrike.equals(lastToken)) {
				System.out.println("now here: "+ System.currentTimeMillis());
				doneSignal.await(5000,TimeUnit.MILLISECONDS);
					
				
			if(right.equalsIgnoreCase("call")) {
				 closestStrikeDelta=closest(0.33,deltaSelector );
			}
			if(right.equalsIgnoreCase("put")) {
				 closestStrikeDelta=closest(-0.33,deltaSelector );

			}
				
		Double closestdet= deltaDetStrikemap.get(closestStrikeDelta);
		double closetOptionPrice = deltaOptionsPricemap.get(closestStrikeDelta);
		futureOptionContract.setStrike(closestdet);//Double.parseDouble(closestdet.replace(futureOptionContract.getFilterStrike(), "")));
		futureOptionContract.setDeltaOptimizedOptionPrice(closetOptionPrice);
			}
		return futureOptionContract;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return futureOptionContract;
		}
		private double closest(double of, ConcurrentLinkedDeque<Double> deltaSelector) {
		    double min = Double.MAX_VALUE;
		    double closest = of;

		    for (double v : deltaSelector) {
		        final double diff = Math.abs(v - of);

		        if (diff < min) {
		            min = diff;
		            closest = v;
		        }
		    }

		    return closest;
		}
	}
}
