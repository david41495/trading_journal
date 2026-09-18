package com.davidserrano.tradejournal.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.davidserrano.tradejournal.model.AssetType;
import com.davidserrano.tradejournal.model.AppUser;
import com.davidserrano.tradejournal.exception.TradeNotFoundException;
import com.davidserrano.tradejournal.model.Trade;
import com.davidserrano.tradejournal.model.TradeDirection;
import com.davidserrano.tradejournal.model.TradeStatus;
import com.davidserrano.tradejournal.repository.TradeRepository;
import com.davidserrano.tradejournal.repository.UserRepository;

@Service
@Transactional
public class TradeService {

    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;

    public TradeService(TradeRepository tradeRepository,
            UserRepository userRepository) {
        this.tradeRepository = tradeRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Trade> getAllTrades(String email) {
        return tradeRepository
                .findByOwnerIdOrderByTradeDateDescEntryTimeDesc(
                        requireUser(email).getId());
    }

    @Transactional(readOnly = true)
    public Trade getTradeById(Long id, String email) {
        return tradeRepository.findByIdAndOwnerId(
                        id, requireUser(email).getId())
                .orElseThrow(() -> new TradeNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Trade> getTradesBySymbol(String symbol, String email) {
        return tradeRepository
                .findByOwnerIdAndSymbolIgnoreCaseOrderByTradeDateDescEntryTimeDesc(
                        requireUser(email).getId(), symbol);
    }

    @Transactional(readOnly = true)
    public List<Trade> getTradesByDate(LocalDate tradeDate, String email) {
        return tradeRepository
                .findByOwnerIdAndTradeDateOrderByEntryTimeDesc(
                        requireUser(email).getId(), tradeDate);
    }

    @Transactional(readOnly = true)
    public List<Trade> getTradesByDirection(
            TradeDirection direction, String email) {

        return tradeRepository
                .findByOwnerIdAndDirectionOrderByTradeDateDescEntryTimeDesc(
                        requireUser(email).getId(), direction);
    }

    @Transactional(readOnly = true)
    public List<Trade> getTradesByStatus(
            TradeStatus status, String email) {
        return tradeRepository
                .findByOwnerIdAndStatusOrderByTradeDateDescEntryTimeDesc(
                        requireUser(email).getId(), status);
    }

    @Transactional(readOnly = true)
    public List<Trade> getTradesByAssetType(
            AssetType assetType, String email) {
        return tradeRepository
                .findByOwnerIdAndAssetTypeOrderByTradeDateDescEntryTimeDesc(
                        requireUser(email).getId(), assetType);
    }

    public Trade createTrade(Trade trade, String email) {
        trade.setOwner(requireUser(email));
        prepareTradeForSave(trade);
        return tradeRepository.save(trade);
    }

    public Trade updateTrade(Long id, Trade updatedTrade, String email) {
        Trade existingTrade = getTradeById(id, email);

        existingTrade.setSymbol(updatedTrade.getSymbol());
        existingTrade.setDirection(updatedTrade.getDirection());
        existingTrade.setTradeDate(updatedTrade.getTradeDate());
        existingTrade.setEntryTime(updatedTrade.getEntryTime());
        existingTrade.setExitTime(updatedTrade.getExitTime());
        existingTrade.setQuantity(updatedTrade.getQuantity());
        existingTrade.setEntryPrice(updatedTrade.getEntryPrice());
        existingTrade.setExitPrice(updatedTrade.getExitPrice());
        existingTrade.setFees(updatedTrade.getFees());
        existingTrade.setStopLoss(updatedTrade.getStopLoss());
        existingTrade.setTargetPrice(updatedTrade.getTargetPrice());
        existingTrade.setSetup(updatedTrade.getSetup());
        existingTrade.setTimeframe(updatedTrade.getTimeframe());
        existingTrade.setNotes(updatedTrade.getNotes());
        existingTrade.setStatus(updatedTrade.getStatus());
        existingTrade.setAssetType(updatedTrade.getAssetType());
        existingTrade.setOptionType(updatedTrade.getOptionType());
        existingTrade.setStrikePrice(updatedTrade.getStrikePrice());
        existingTrade.setExpirationDate(updatedTrade.getExpirationDate());
        existingTrade.setContractCode(updatedTrade.getContractCode());
        existingTrade.setContractMultiplier(
                updatedTrade.getContractMultiplier());

        prepareTradeForSave(existingTrade);

        return tradeRepository.save(existingTrade);
    }

    public void deleteTrade(Long id, String email) {
        Trade trade = getTradeById(id, email);
        tradeRepository.delete(trade);
    }

    private AppUser requireUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user was not found."));
    }

    private void prepareTradeForSave(Trade trade) {
        if (trade.getAssetType() == null) {
            trade.setAssetType(AssetType.STOCK);
        }

        if (trade.getContractMultiplier() == null) {
            trade.setContractMultiplier(
                    getDefaultMultiplier(trade.getAssetType()));
        }

        if (trade.getFees() == null) {
            trade.setFees(BigDecimal.ZERO);
        }

        if (trade.getStatus() == null) {
            trade.setStatus(TradeStatus.PLANNED);
        }

        validateAssetDetails(trade);
        trade.setProfitLoss(calculateProfitLoss(trade));
    }

    private BigDecimal getDefaultMultiplier(AssetType assetType) {
        if (assetType == AssetType.OPTION) {
            return new BigDecimal("100");
        }

        return BigDecimal.ONE;
    }

    private void validateAssetDetails(Trade trade) {
        if (trade.getAssetType() == AssetType.OPTION) {
            if (trade.getOptionType() == null) {
                throw new IllegalArgumentException(
                        "An option trade requires CALL or PUT.");
            }

            if (trade.getStrikePrice() == null) {
                throw new IllegalArgumentException(
                        "An option trade requires a strike price.");
            }

            if (trade.getExpirationDate() == null) {
                throw new IllegalArgumentException(
                        "An option trade requires an expiration date.");
            }
        }
    }

    private BigDecimal calculateProfitLoss(Trade trade) {
        if (trade.getEntryPrice() == null
                || trade.getExitPrice() == null
                || trade.getQuantity() == null
                || trade.getDirection() == null) {

            return null;
        }

        BigDecimal priceDifference;

        if (trade.getDirection() == TradeDirection.LONG) {
            priceDifference = trade.getExitPrice()
                    .subtract(trade.getEntryPrice());
        } else {
            priceDifference = trade.getEntryPrice()
                    .subtract(trade.getExitPrice());
        }

        BigDecimal grossProfitLoss = priceDifference
                .multiply(BigDecimal.valueOf(trade.getQuantity()))
                .multiply(trade.getContractMultiplier());

        BigDecimal fees = trade.getFees() == null
                ? BigDecimal.ZERO
                : trade.getFees();

        return grossProfitLoss
                .subtract(fees)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
