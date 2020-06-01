
package com.crio.warmup.stock.dto;

import java.util.Comparator;

public class AnnualizedReturn {
  private final String symbol;
  private final Double annualizedReturn;
  private final Double totalReturns;

  public AnnualizedReturn(String symbol, Double annualizedReturn, Double totalReturns) {
    this.symbol = symbol;
    this.annualizedReturn = annualizedReturn;
    this.totalReturns = totalReturns;
  }

  public String getSymbol() {
    return symbol;
  }

  public Double getAnnualizedReturn() {
    return annualizedReturn;
  }

  public Double getTotalReturns() {
    return totalReturns;
  }

  public static final Comparator<AnnualizedReturn> annualGrowth = 
      new Comparator<AnnualizedReturn>() {
        public int compare(AnnualizedReturn a1,AnnualizedReturn a2) {
          return (a1.annualizedReturn).compareTo(a2.annualizedReturn);
        }
      };
}
