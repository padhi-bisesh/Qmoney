
package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {
  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {
    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = 
        "/home/crio-user/workspace/bkp10-iitbbs-ac-ME_QMONEY/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@5aac4250";
    String functionNameFromTestFileInStackTrace = "PortfolioManagerApplicationTest.mainReadFile";
    String lineNumberFromTestFileInStackTrace = "22:1";

    return Arrays.asList(new String[] { valueOfArgument0, resultOfResolveFilePathArgs0, 
      toStringOfObjectMapper, functionNameFromTestFileInStackTrace, 
      lineNumberFromTestFileInStackTrace });
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return 
    Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI())
    .toFile();
  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    ObjectMapper objMapper = getObjectMapper();
    List<PortfolioTrade> trades = Arrays
        .asList(objMapper.readValue(resolveFileFromResources(args[0]), PortfolioTrade[].class));
    List<TotalReturnsDto> stocks = dtoReturn(args,trades);
    Collections.sort(stocks,TotalReturnsDto.edClosePrice);
    List<String> symbol = new ArrayList<String>();
    for (TotalReturnsDto trd:stocks) {
      symbol.add(trd.getSymbol());
    }
    return symbol;
  }

  public static List<TotalReturnsDto> dtoReturn(String[] args,List<PortfolioTrade> trades) {
    List<TotalReturnsDto> stocks = new ArrayList<TotalReturnsDto>();
    for (PortfolioTrade t:trades) {
      String uri = "https://api.tiingo.com/tiingo/daily/" + t.getSymbol() + "/prices?startDate="
          + t.getPurchaseDate().toString() + "&endDate=" + args[1] 
          + "&token=617816cec9dd9cf627651339b4c7a7775e7eb58b";
      RestTemplate rt = new RestTemplate();
      TiingoCandle[] compStocks = rt.getForObject(uri, TiingoCandle[].class);
      if (compStocks != null) {
        stocks.add(new TotalReturnsDto(t.getSymbol(), 
            compStocks[compStocks.length - 1].getClose()));
      }
    }
    return stocks;
  }

  public static List<AnnualizedReturn> dtoReturn2(String[] args,List<PortfolioTrade> trades) {
    List<AnnualizedReturn> stocks = new ArrayList<AnnualizedReturn>();
    for (PortfolioTrade t:trades) {
      String uri = "https://api.tiingo.com/tiingo/daily/" + t.getSymbol() + "/prices?startDate="
          + t.getPurchaseDate().toString() + "&endDate=" + args[1] 
          + "&token=617816cec9dd9cf627651339b4c7a7775e7eb58b";
      RestTemplate rt = new RestTemplate();
      TiingoCandle[] compStocks = rt.getForObject(uri, TiingoCandle[].class);
      if (compStocks != null) {  
        int upperIndex = compStocks.length - 1;
        while (compStocks[upperIndex] == null) {
          upperIndex--;
        }
        stocks.add(calculateAnnualizedReturns(compStocks[upperIndex].getDate(),
            t,compStocks[0].getOpen(),compStocks[upperIndex].getClose()));
      }
    }
    return stocks;
  }

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    ObjectMapper objMapper = getObjectMapper();
    List<PortfolioTrade> trades = Arrays
        .asList(objMapper.readValue(resolveFileFromResources(args[0]), PortfolioTrade[].class));
    List<String> symbols = new ArrayList<String>();
    for (PortfolioTrade t : trades) {
      symbols.add(t.getSymbol());
    }
    return symbols;
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
    ObjectMapper objMapper = getObjectMapper();
    List<PortfolioTrade> trades = Arrays
        .asList(objMapper.readValue(resolveFileFromResources(args[0]), PortfolioTrade[].class));
    List<AnnualizedReturn> stocks = dtoReturn2(args,trades);
    Collections.sort(stocks,AnnualizedReturn.annualGrowth);
    Collections.reverse(stocks);
    return stocks; 
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
    double totalReturns = (sellPrice - buyPrice) / buyPrice;
    Double totyears = (double) ChronoUnit.DAYS.between(trade.getPurchaseDate(),endDate) / 365.00;
    double annualisedReturn = Math.pow(1 + totalReturns,1 / totyears) - 1;
    return new AnnualizedReturn(trade.getSymbol(),annualisedReturn,totalReturns);
  }
  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory,
  //  Create PortfolioManager using PortfoliomanagerFactory,
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.
  //  Test the same using the same commands as you used in module 3
  //  use gralde command like below to test your code
  //  ./gradlew run --args="trades.json 2020-01-01"
  //  ./gradlew run --args="trades.json 2019-07-01"
  //  ./gradlew run --args="trades.json 2019-12-03"
  //  where trades.json is your json file
  //  Confirm that you are getting same results as in Module3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
    String file = args[0];
    LocalDate endDate = LocalDate.parse(args[1]);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] portfolioTrades = objectMapper
        .readValue(resolveFileFromResources(file),PortfolioTrade[].class);
    //String contents = objectMapper.writeValueAsString(file);
    RestTemplate restTemplate = new RestTemplate();
    PortfolioManager portfolioManager = PortfolioManagerFactory
        .getPortfolioManager("tiingo", restTemplate);
    return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());
    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

