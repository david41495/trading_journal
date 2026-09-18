package com.davidserrano.tradejournal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private AppUser owner;

    @NotBlank
    @Column(nullable = false, length = 25)
    private String symbol;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AssetType assetType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TradeDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private OptionType optionType;

    @Column(precision = 12, scale = 4)
    private BigDecimal strikePrice;

    private LocalDate expirationDate;

    @Column(length = 30)
    private String contractCode;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @DecimalMin(value = "0.0001")
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal contractMultiplier;

    @NotNull
    @Column(nullable = false)
    private LocalDate tradeDate;

    @NotNull
    @Column(nullable = false)
    private LocalTime entryTime;

    private LocalTime exitTime;

    @NotNull
    @DecimalMin(value = "0.0001")
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal entryPrice;

    @DecimalMin(value = "0.0001")
    @Column(precision = 12, scale = 4)
    private BigDecimal exitPrice;

    @DecimalMin(value = "0.00")
    @Column(precision = 12, scale = 4)
    private BigDecimal fees;

    @Column(precision = 12, scale = 4)
    private BigDecimal stopLoss;

    @Column(precision = 12, scale = 4)
    private BigDecimal targetPrice;

    @Column(precision = 14, scale = 2)
    private BigDecimal profitLoss;

    @Column(length = 50)
    private String setup;

    @Column(length = 20)
    private String timeframe;

    @Lob
    private String notes;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TradeStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Trade() {
    }

    @PrePersist
    public void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        applyDefaults();
        normalizeSymbol();
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();

        applyDefaults();
        normalizeSymbol();
    }

    private void applyDefaults() {
        if (assetType == null) {
            assetType = AssetType.STOCK;
        }

        if (contractMultiplier == null) {
            contractMultiplier = defaultMultiplierFor(assetType);
        }

        if (status == null) {
            status = TradeStatus.PLANNED;
        }

        if (fees == null) {
            fees = BigDecimal.ZERO;
        }
    }

    private BigDecimal defaultMultiplierFor(AssetType type) {
        if (type == AssetType.OPTION) {
            return new BigDecimal("100");
        }

        return BigDecimal.ONE;
    }

    private void normalizeSymbol() {
        if (symbol != null) {
            symbol = symbol.trim().toUpperCase();
        }

        if (contractCode != null) {
            contractCode = contractCode.trim().toUpperCase();
        }
    }

    public Long getId() {
        return id;
    }

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public TradeDirection getDirection() {
        return direction;
    }

    public void setDirection(TradeDirection direction) {
        this.direction = direction;
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public void setOptionType(OptionType optionType) {
        this.optionType = optionType;
    }

    public BigDecimal getStrikePrice() {
        return strikePrice;
    }

    public void setStrikePrice(BigDecimal strikePrice) {
        this.strikePrice = strikePrice;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getContractCode() {
        return contractCode;
    }

    public void setContractCode(String contractCode) {
        this.contractCode = contractCode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getContractMultiplier() {
        return contractMultiplier;
    }

    public void setContractMultiplier(BigDecimal contractMultiplier) {
        this.contractMultiplier = contractMultiplier;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public LocalTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalTime exitTime) {
        this.exitTime = exitTime;
    }

    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(BigDecimal entryPrice) {
        this.entryPrice = entryPrice;
    }

    public BigDecimal getExitPrice() {
        return exitPrice;
    }

    public void setExitPrice(BigDecimal exitPrice) {
        this.exitPrice = exitPrice;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    public BigDecimal getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(BigDecimal stopLoss) {
        this.stopLoss = stopLoss;
    }

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(BigDecimal targetPrice) {
        this.targetPrice = targetPrice;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(BigDecimal profitLoss) {
        this.profitLoss = profitLoss;
    }

    public String getSetup() {
        return setup;
    }

    public void setSetup(String setup) {
        this.setup = setup;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public TradeStatus getStatus() {
        return status;
    }

    public void setStatus(TradeStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
