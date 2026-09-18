package com.davidserrano.tradejournal.controller;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.davidserrano.tradejournal.model.AssetType;
import com.davidserrano.tradejournal.model.Trade;
import com.davidserrano.tradejournal.model.TradeDirection;
import com.davidserrano.tradejournal.model.TradeStatus;
import com.davidserrano.tradejournal.service.TradeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping
    public ResponseEntity<List<Trade>> getAllTrades(Principal principal) {
        return ResponseEntity.ok(
                tradeService.getAllTrades(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trade> getTradeById(
            @PathVariable Long id,
            Principal principal) {

        return ResponseEntity.ok(tradeService.getTradeById(
                id, principal.getName()));
    }

    @GetMapping("/search/symbol")
    public ResponseEntity<List<Trade>> getTradesBySymbol(
            @RequestParam String symbol,
            Principal principal) {

        return ResponseEntity.ok(
                tradeService.getTradesBySymbol(symbol, principal.getName()));
    }

    @GetMapping("/search/date")
    public ResponseEntity<List<Trade>> getTradesByDate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate tradeDate,
            Principal principal) {

        return ResponseEntity.ok(
                tradeService.getTradesByDate(
                        tradeDate, principal.getName()));
    }

    @GetMapping("/search/asset-type")
    public ResponseEntity<List<Trade>> getTradesByAssetType(
            @RequestParam AssetType assetType,
            Principal principal) {

        return ResponseEntity.ok(
                tradeService.getTradesByAssetType(
                        assetType, principal.getName()));
    }

    @GetMapping("/search/direction")
    public ResponseEntity<List<Trade>> getTradesByDirection(
            @RequestParam TradeDirection direction,
            Principal principal) {

        return ResponseEntity.ok(
                tradeService.getTradesByDirection(
                        direction, principal.getName()));
    }

    @GetMapping("/search/status")
    public ResponseEntity<List<Trade>> getTradesByStatus(
            @RequestParam TradeStatus status,
            Principal principal) {

        return ResponseEntity.ok(
                tradeService.getTradesByStatus(
                        status, principal.getName()));
    }

    @PostMapping
    public ResponseEntity<Trade> createTrade(
            @Valid @RequestBody Trade trade,
            Principal principal) {

        Trade savedTrade = tradeService.createTrade(
                trade, principal.getName());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedTrade.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(savedTrade);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trade> updateTrade(
            @PathVariable Long id,
            @Valid @RequestBody Trade trade,
            Principal principal) {

        Trade updatedTrade = tradeService.updateTrade(
                id, trade, principal.getName());
        return ResponseEntity.ok(updatedTrade);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrade(
            @PathVariable Long id,
            Principal principal) {

        tradeService.deleteTrade(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
