package com.rodarte.springbootwebfluxapirest.handlers;

import com.rodarte.springbootwebfluxapirest.models.documents.Producto;
import com.rodarte.springbootwebfluxapirest.models.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ProductoHandler {

    @Autowired
    private ProductoService productoService;

    public Mono<ServerResponse> listar(ServerRequest serverRequest) {

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.productoService.findAll(), Producto.class);

    }

}
