package com.plathanus.backend.service;

import com.plathanus.backend.dto.PedidoResponse;
import com.plathanus.backend.entity.Item;
import com.plathanus.backend.entity.Pedido;
import com.plathanus.backend.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final AiParserService aiParserService;

    public PedidoResponse criarPedido(String texto) {
        AiParserService.ParsedPedido parsed = aiParserService.parse(texto);

        List<Item> itens = parsed.itens().stream()
                .map(ai -> Item.builder()
                        .produto(ai.produto())
                        .quantidade(ai.quantidade())
                        .unidade(ai.unidade())
                        .build())
                .collect(Collectors.toList());

        Pedido pedido = Pedido.builder()
                .cliente(parsed.cliente())
                .textoOriginal(texto)
                .dataEntrega(parsed.dataEntrega())
                .itens(itens)
                .build();

        Pedido saved = pedidoRepository.save(pedido);
        return toResponse(saved);
    }

    public List<PedidoResponse> listarPedidos() {
        return pedidoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PedidoResponse buscarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Pedido não encontrado: " + id));
        return toResponse(pedido);
    }

    private PedidoResponse toResponse(Pedido pedido) {
        List<PedidoResponse.ItemResponse> itensResponse = pedido.getItens().stream()
                .map(item -> PedidoResponse.ItemResponse.builder()
                        .produto(item.getProduto())
                        .quantidade(item.getQuantidade())
                        .unidade(item.getUnidade())
                        .build())
                .collect(Collectors.toList());

        return PedidoResponse.builder()
                .id(pedido.getId())
                .cliente(pedido.getCliente())
                .textoOriginal(pedido.getTextoOriginal())
                .dataEntrega(pedido.getDataEntrega())
                .itens(itensResponse)
                .createdAt(pedido.getCreatedAt())
                .build();
    }
}
