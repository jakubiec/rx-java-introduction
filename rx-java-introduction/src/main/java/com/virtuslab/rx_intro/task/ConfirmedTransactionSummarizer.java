package com.virtuslab.rx_intro.task;


import io.reactivex.Observable;
import io.reactivex.Single;

import java.math.BigDecimal;
import java.util.function.Supplier;

/**
 * ConfirmedTransactionSummarizer is responsible for calculation of total confirmed transactions value.
 * HINT:
 * - Use zip operator to match transactions with confirmations. They will appear in order
 * - Filter only confirmed
 * - Aggregate value of confirmed transactions
 *
 * HINT2:
 * - add error handling which will wrap an error into SummarizationException
 *
 */
class ConfirmedTransactionSummarizer {

    private final Supplier<Observable<Transaction>> transactions;
    private final Supplier<Observable<Confirmation>> confirmations;

    ConfirmedTransactionSummarizer(Supplier<Observable<Transaction>> transactions,
                                   Supplier<Observable<Confirmation>> confirmations) {
        this.transactions = transactions;
        this.confirmations = confirmations;
    }

    Single<BigDecimal> summarizeConfirmedTransactions() {
        return transactions.get()
                .zipWith(confirmations.get(), ConfirmedTransaction::new)
                .filter(ct -> ct.confirmation.isConfirmed)
                .reduce(BigDecimal.ZERO, (v, ct) -> v.add(ct.transaction.value))
                .onErrorResumeNext(error -> Single.error(new SummarizationException(error.getMessage())));
    }

    private static class ConfirmedTransaction {
        final Transaction transaction;
        final Confirmation confirmation;

        private ConfirmedTransaction(Transaction transaction, Confirmation confirmation) {
            this.transaction = transaction;
            this.confirmation = confirmation;
        }
    }

    static class SummarizationException extends RuntimeException {

        SummarizationException(String message) {
            super(message);
        }
    }
}