package com.plathanus.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PedidoRequest {

    @NotBlank(message = "O campo 'texto' é obrigatório e não pode estar em branco")
    private String texto;
}
