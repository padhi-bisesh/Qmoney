package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.quotes.StockQuoteServiceFactory;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {
  private RestTemplate restTemplate;
  private StockQuotesService sqs;

  // Caution: Do not delete or modify the constructor, or else your build will
  // break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate, String provider) {
    this.restTemplate = restTemplate;
  }

  protected PortfolioManagerImpl(StockQuotesService sqs) {
    this.sqs = sqs;
  }
  


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // Now we want to convert our code into a module, so we will not call it from main anymore.
  // Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and make sure that it
  // follows the method signature.
  // Logic to read Json file and convert them into Objects will not be required further as our
  // clients will take care of it, going forward.
  // Test your code using Junits provided.
  // Make sure that all of the tests inside PortfolioManagerTest using command below -
  // ./gradlew test --tests PortfolioManagerTest
  // This will guard you against any regressions.
  // run ./gradlew build in order to test yout code, and make sure that
  // the tests and static code quality pass.

  //CHECKSTYLE:OFF




  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo thirdparty APIs to a separate function.
  //  It should be split into fto parts.
  //  Part#1 - Prepare the Url to call Tiingo based on a template constant,
  //  by replacing the placeholders.
  //  Constant should look like
  //  https://api.tiingo.com/tiingo/daily/<ticker>/prices?startDate=?&endDate=?&token=?
  //  Where ? are replaced with something similar to <ticker> and then actual url produced by
  //  replacing the placeholders with actual parameters.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
    String uri = buildUri(symbol, from, to);
    Candle[] compstocks = this.restTemplate.
        getForObject(uri,TiingoCandle[].class);
    if (compstocks!=null){
      return Arrays.asList(compstocks);
    }
    else {
    return null;
    }
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
      String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?"
          + "startDate=" + startDate.toString() + "&endDate=" + endDate.toString()
          + "&token=617816cec9dd9cf627651339b4c7a7775e7eb58b";
      return uriTemplate;
  }

  private List<Double> retrievePrice (List<Candle> compstocks,LocalDate from,LocalDate to) {
    List<Double> prices = new ArrayList<Double>();
    if (compstocks!=null) {  
      prices.add(compstocks.get(0).getOpen());
      int upperIndex = compstocks.size()-1;
      //while (compstocks.get(upperIndex)==null) {
      //  upperIndex--;
      //}
      prices.add(compstocks.get(upperIndex).getClose());
      prices.add((double) upperIndex);
      return prices;
    }
    else {
      return null;
    }
  }
  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) throws JsonProcessingException {
    List<AnnualizedReturn> ar = new ArrayList<AnnualizedReturn>();
    for (PortfolioTrade trade:portfolioTrades){ 
      LocalDate startDate = trade.getPurchaseDate();
      // StockQuoteServiceFactory factory = StockQuoteServiceFactory.INSTANCE;
      // StockQuotesService service = factory.getService(provider, this.restTemplate);
      List<Candle> compstocks = this.sqs.getStockQuote(trade.getSymbol(), startDate, endDate);
      List<Double> prices = retrievePrice(compstocks, startDate, endDate);
      Double buyPrice = prices.get(0);
      Double sellPrice = prices.get(1);
      Double totalReturns = (sellPrice - buyPrice) / buyPrice;
      int uI = (prices.get(2)).intValue();
      Double totyears = (double) 
          ChronoUnit.DAYS.between(trade.getPurchaseDate(),compstocks.get(uI).getDate()) / 365.00;
      Double annualisedReturn = Math.pow(1 + totalReturns,1 / totyears) - 1;
      ar.add(new AnnualizedReturn(trade.getSymbol(),annualisedReturn,totalReturns));
    }
    Comparator<AnnualizedReturn> ca = getComparator();
    Collections.sort(ar,ca);
//    Collections.reverse(ar);
    return ar;
  }


  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.

}
