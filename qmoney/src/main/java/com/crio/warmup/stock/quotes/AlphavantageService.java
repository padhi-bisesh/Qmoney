
package com.crio.warmup.stock.quotes;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AlphavantageCandle;
import com.crio.warmup.stock.dto.AlphavantageDailyResponse;
import com.crio.warmup.stock.dto.Candle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class AlphavantageService implements StockQuotesService {

  private RestTemplate restTemplate;

  public AlphavantageService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private Comparator<Candle> getComparator() {
    return Comparator.comparing(Candle::getDate);
  }
  
  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
    if (from.getDayOfMonth() == 1 && from.getMonthValue() == 1) {
      from = from.plusDays(1);
    }
    String uri = buildUri(symbol, from, to);
    String temp = this.restTemplate
        .getForObject(uri,String.class);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    AlphavantageDailyResponse adr = objectMapper.readValue(temp, AlphavantageDailyResponse.class);
    if (adr != null) {
      Map<LocalDate,AlphavantageCandle> map = adr.getCandles();
      List<Candle> compstocks = new ArrayList<Candle>();
      AlphavantageCandle lower = new AlphavantageCandle();
      AlphavantageCandle upper = lower;
      for (Map.Entry<LocalDate, AlphavantageCandle> t:map.entrySet()) {
        AlphavantageCandle instCandle = t.getValue();
        instCandle.setDate(t.getKey());
        compstocks.add(instCandle);
        if (t.getKey().equals(from)) {
          lower = instCandle;
        }
        if (t.getKey().equals(to)) {
          upper = instCandle;
        }
      }
      Comparator<Candle> ac = getComparator();
      Collections.sort(compstocks,ac);
      List<Candle> tempstock = compstocks.subList(compstocks.indexOf(lower),
          compstocks.indexOf(upper) + 1);
      return tempstock;
    } else {
      return null;
    }
  }

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Inplement the StockQuoteService interface as per the contracts.
  //  The implementation of this functions will be doing following tasks
  //  1. Build the appropriate url to communicate with thirdparty.
  //  The url should consider startDate and endDate if it is supported by the provider.
  //  2. Perform thirdparty communication with the Url prepared in step#1
  //  3. Map the response and convert the same to List<Candle>
  //  4. If the provider does not support startDate and endDate, then the implementation
  //  should also filter the dates based on startDate and endDate.
  //  Make sure that result contains the records for for startDate and endDate after filtering.
  //  5. return a sorted List<Candle> sorted ascending based on Candle#getDate
  //  Call alphavantage service to fetch daily adjusted data for last 20 years. Refer to
  //  documentation here - https://www.alphavantage.co/documentation/
  //  Make sure you use {RestTemplate#getForObject(URI, String)} else the test will fail.
  //  Run the tests using command below and make sure it passes
  //  ./gradlew test --tests AlphavantageServiceTest
  //CHECKSTYLE:OFF
    //CHECKSTYLE:ON
  //TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  // Write a method to create appropriate url to call alphavantage service. Method should
  // be using configurations provided in the {@link @application.properties}.
  // Use thie method in #getStockQuote.
  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&" 
        + "symbol=" + symbol + "&outputsize=full&apikey=6GJ3ZRPXA2EFPOEA"; 
    return uriTemplate;
  }
}

