package com.validator.model.enums;

import lombok.Getter;

@Getter
public enum FraudReason {

    STRANGE_VALUE("STRANGE_VALUE", "Valor da transação é atípico ou muito alto"),
    SUSPICIOUS_PIX_KEY("SUSPICIOUS_PIX_KEY", "Chave PIX contém palavra suspeita"),
    HIGH_FREQUENCY("HIGH_FREQUENCY", "Muitas transações em um curto período de tempo (5 min)"),
    OUT_OF_AVERAGE_VALUE("OUT_OF_AVERAGE_VALUE", "Valor fora da média das últimas 5 transações"),
    SUSPICIOUS_DESCRIPTION("SUSPICIOUS_DESCRIPTION", "Descrição contém termos suspeitos ou proibidos"),
    USER_IN_BLACKLIST("USER_IN_BLACKLIST", "Remetente ou destinatário presente em blacklist");

    private final String code;
    private final String description;

    FraudReason(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
