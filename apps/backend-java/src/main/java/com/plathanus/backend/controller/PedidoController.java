package com.plathanus.backend.controller;

import com.plathanus.backend.dto.PedidoRequest;
import com.plathanus.backend.dto.PedidoResponse;
import com.plathanus.backend.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping("/pedido")
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criarPedido(@Valid @RequestBody PedidoRequest request) {
        return pedidoService.criarPedido(request.getTexto());
    }

    @GetMapping("/pedidos")
    public List<PedidoResponse> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @GetMapping("/pedido/{id}")
    public PedidoResponse buscarPedido(@PathVariable Long id) {
        return pedidoService.buscarPedido(id);
    }
}
