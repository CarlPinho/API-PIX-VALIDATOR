package com.validator.model;

import com.validator.controller.responses.TransactionResponse;
import com.validator.model.enums.FraudReason;
import com.validator.model.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    private Double value;
    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    private FraudReason fraudReason;

    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;


    public TransactionResponse toResponse() {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setId(this.id);

        transactionResponse.setSender(this.sender.toResponse());
        transactionResponse.setReceiver(this.receiver.toResponse());

        transactionResponse.setValue(this.value);
        transactionResponse.setDescription(this.description);
        transactionResponse.setStatus(this.status.name());

        if (Objects.nonNull(this.fraudReason)) {
            transactionResponse.setFraudCode(this.fraudReason.getCode());
            transactionResponse.setFraudDescription(this.fraudReason.getDescription());
        }

        return transactionResponse;
    }
}
