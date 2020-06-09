
package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.quotes.StockQuoteServiceFactory;
import com.crio.warmup.stock.quotes.StockQuotesService;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerFactory {

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Implement the method in such a way that it will return new Instance of
  // PortfolioManager using RestTemplate provided.
  public static PortfolioManager getPortfolioManager(String provider,RestTemplate restTemplate) {
    StockQuoteServiceFactory factory = StockQuoteServiceFactory.INSTANCE;
    if (provider != null) {  
      StockQuotesService sqs = factory.getService(provider, restTemplate); 
      PortfolioManager manager = new PortfolioManagerImpl(sqs);
      return manager;
    } else {
      StockQuotesService sqs = factory.getService("tiingo", restTemplate); 
      PortfolioManager manager = new PortfolioManagerImpl(sqs);
      return manager;
    }
  }
  
  public static PortfolioManager getPortfolioManager(RestTemplate restTemplate) {
    StockQuoteServiceFactory factory = StockQuoteServiceFactory.INSTANCE;
    StockQuotesService sqs = factory.getService("tiingo", restTemplate); 
    PortfolioManager manager = new PortfolioManagerImpl(sqs);
    return manager;
  }



}
