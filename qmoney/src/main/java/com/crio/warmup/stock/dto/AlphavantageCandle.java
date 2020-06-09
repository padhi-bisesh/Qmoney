package com.crio.warmup.stock.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Date;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

// TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
//  Implement the Candle interface in such a way that it matches the
//  parameters returned inside Json response from AlphavantageService.

//  Reference - https:www.baeldung.com/jackson-ignore-properties-on-serialization
//  Reference - https:www.baeldung.com/jackson-name-of-property
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphavantageCandle implements Candle {
  @JsonProperty("1. open")
  private Double open;
  @JsonProperty("4. close")
  private Double close;
  @JsonProperty("2. high")
  private Double high;
  @JsonProperty("3. low")
  private Double low;
  private Date date;

  @Override
  public Double getOpen() {
    return open;
  }

  @Override
  public Double getClose() {
    return close;
  }

  @Override
  public Double getHigh() {
    return high;
  }

  @Override
  public Double getLow() {
    return low;
  }

  @Override
  public LocalDate getDate() {
    return date.toLocalDate();
  }

  public void setOpen(Double open) {
    this.open = open;
  }

  public void setClose(Double close) {
    this.close = close;
  }

  public void setHigh(Double high) {
    this.high = high;
  }

  public void setLow(Double low) {
    this.low = low;
  }

  public void setDate(LocalDate date) {
    this.date = java.sql.Date.valueOf(date);
  }

  @Override
  public String toString() {
    return "AlphavantageCandle{"
            + "open=" + open
            + ", close=" + close
            + ", high=" + high
            + ", low=" + low
            + ", date=" + date
            + '}';
  }
}

