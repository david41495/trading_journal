package com.davidserrano.tradejournal.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.davidserrano.tradejournal.model.Trade;
import com.davidserrano.tradejournal.model.TradeDirection;
import com.davidserrano.tradejournal.model.TradeStatus;
import com.davidserrano.tradejournal.model.AssetType;

public interface TradeRepository extends JpaRepository<Trade, Long> {


    List<Trade> findByOwnerIdOrderByTradeDateDescEntryTimeDesc(Long ownerId);

    Optional<Trade> findByIdAndOwnerId(Long id, Long ownerId);

    List<Trade> findByOwnerIsNull();

    List<Trade> findByOwnerIdAndSymbolIgnoreCaseOrderByTradeDateDescEntryTimeDesc(
            Long ownerId, String symbol);

    List<Trade> findByOwnerIdAndTradeDateOrderByEntryTimeDesc(
            Long ownerId, LocalDate tradeDate);

    List<Trade> findByOwnerIdAndAssetTypeOrderByTradeDateDescEntryTimeDesc(
            Long ownerId, AssetType assetType);

    List<Trade> findByOwnerIdAndDirectionOrderByTradeDateDescEntryTimeDesc(
            Long ownerId, TradeDirection direction);

    List<Trade> findByOwnerIdAndStatusOrderByTradeDateDescEntryTimeDesc(
            Long ownerId, TradeStatus status);
}
