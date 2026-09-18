package com.davidserrano.tradejournal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.davidserrano.tradejournal.model.Trade;
import com.davidserrano.tradejournal.model.TradeDirection;
import com.davidserrano.tradejournal.model.TradeStatus;
import com.davidserrano.tradejournal.model.AssetType;

public interface TradeRepository extends JpaRepository<Trade, Long> {


    List<Trade> findAllByOrderByTradeDateDescEntryTimeDesc();

    List<Trade> findBySymbolIgnoreCaseOrderByTradeDateDescEntryTimeDesc(
            String symbol);

    List<Trade> findByTradeDateOrderByEntryTimeDesc(
            LocalDate tradeDate);

    List <Trade> findByAssetTypeOrderByTradeDateDescEntryTimeDesc(
    		AssetType assetType);

    List<Trade> findByDirectionOrderByTradeDateDescEntryTimeDesc(
            TradeDirection direction);

    List<Trade> findByStatusOrderByTradeDateDescEntryTimeDesc(
            TradeStatus status);
}
