package com.validator.controller.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.validator.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

    private Long id;
    private UserResponse sender;
    private UserResponse receiver;

    private Double value;
    private String description;
    private String status;
    private String fraudCode;
    private String fraudDescription;

}
