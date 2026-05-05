package com.plathanus.backend.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponse {

    private Long id;
    private String cliente;
    private String textoOriginal;
    private LocalDate dataEntrega;
    private List<ItemResponse> itens;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemResponse {
        private String produto;
        private Integer quantidade;
        private String unidade;
    }
}
