
package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {
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

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return 
    Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI())
    .toFile();
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
  // TODO: CRIO_TASK_MODULE_REST_API
  //  Copy the relavent code from #mainReadFile to parse the Json into PortfolioTrade list.
  //  Now That you have the list of PortfolioTrade already populated in module#1
  //  For each stock symbol in the portfolio trades,
  //  Call Tiingo api (https://api.tiingo.com/tiingo/daily/<ticker>/prices?startDate=&endDate=&token=)
  //  with
  //   1. ticker = symbol in portfolio_trade
  //   2. startDate = purchaseDate in portfolio_trade.
  //   3. endDate = args[1]
  //  Use RestTemplate#getForObject in order to call the API,
  //  and deserialize the results in List<Candle>
  //  Note - You may have to register on Tiingo to get the api_token.
  //    Please refer the the module documentation for the steps.
  //  Find out the closing price of the stock on the end_date and
  //  return the list of all symbols in ascending order by its close value on endDate
  //  Test the function using gradle commands below
  //   ./gradlew run --args="trades.json 2020-01-01"
  //   ./gradlew run --args="trades.json 2019-07-01"
  //   ./gradlew run --args="trades.json 2019-12-03"
  //  And make sure that its printing correct results.
  
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
      int lowerIndex = 0;
      int upperIndex = compStocks.length - 1;
      while (compStocks[upperIndex].getClose() == null) {
        upperIndex--;
      }
      if (compStocks != null) {
        stocks.add(calculateAnnualizedReturns(compStocks[upperIndex].getDate(),
            t,compStocks[lowerIndex].getOpen(),compStocks[upperIndex].getClose()));
      }
    }
    return stocks;
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




  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Copy the relevant code from #mainReadQuotes to parse the Json into PortfolioTrade list and
  //  Get the latest quotes from TIingo.
  //  Now That you have the list of PortfolioTrade And their data,
  //  With this data, Calculate annualized returns for the stocks provided in the Json
  //  Below are the values to be considered for calculations.
  //  buy_price = open_price on purchase_date and sell_value = close_price on end_date
  //  startDate and endDate are already calculated in module2
  //  using the function you just wrote #calculateAnnualizedReturns
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.
  //  use gralde command like below to test your code
  //  ./gradlew run --args="trades.json 2020-01-01"
  //  ./gradlew run --args="trades.json 2019-07-01"
  //  ./gradlew run --args="trades.json 2019-12-03"
  //  where trades.json is your json file

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

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  annualized returns should be calculated in two steps -
  //  1. Calculate totalReturn = (sell_value - buy_value) / buy_value
  //  Store the same as totalReturns
  //  2. calculate extrapolated annualized returns by scaling the same in years span. The formula is
  //  annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //  Store the same as annualized_returns
  //  return the populated list of AnnualizedReturn for all stocks,
  //  Test the same using below specified command. The build should be successful
  //  ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
    double totalReturns = (sellPrice - buyPrice) / buyPrice;
    Double totyears = (double) ChronoUnit.DAYS.between(trade.getPurchaseDate(),endDate) / 365.00;
    double annualisedReturn = Math.pow(1 + totalReturns,1 / totyears) - 1;
    AnnualizedReturn ar = new AnnualizedReturn(trade.getSymbol(),annualisedReturn,totalReturns);
    return ar;
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());
    printJsonObject(mainCalculateSingleReturn(args));

  }
}

