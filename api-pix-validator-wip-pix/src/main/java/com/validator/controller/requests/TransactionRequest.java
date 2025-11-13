package com.validator.controller.requests;

import com.validator.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {

    private Long id;
    private UserRequest sender;
    private UserRequest receiver;
    private Double value;
    private String description;

    public Transaction toEntity() {
        Transaction transaction = new Transaction();

        transaction.setId(this.id);

        transaction.setSender(this.sender.toEntity());
        transaction.setReceiver(this.receiver.toEntity());

        transaction.setValue(this.value);
        transaction.setDescription(this.description);

        return transaction;
    }


}