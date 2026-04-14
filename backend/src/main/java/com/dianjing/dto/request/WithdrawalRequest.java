package com.dianjing.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class WithdrawalRequest {
    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "50.00", message = "提现金额不能低于50元")
    private BigDecimal amount;

    @NotBlank(message = "银行账号不能为空")
    private String bankAccount;

    @NotBlank(message = "银行名称不能为空")
    private String bankName;

    @NotBlank(message = "账户名称不能为空")
    private String accountName;
}
