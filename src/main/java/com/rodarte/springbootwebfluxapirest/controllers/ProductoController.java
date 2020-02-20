package com.rodarte.springbootwebfluxapirest.controllers;

import com.rodarte.springbootwebfluxapirest.models.documents.Producto;
import com.rodarte.springbootwebfluxapirest.models.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

//    @GetMapping
//    public Flux<Producto> listar() {
//        return this.productoService.findAll();
//    }

    @GetMapping
    public Mono<ResponseEntity<Flux<Producto>>> listar() {
        return Mono.just(
            ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.productoService.findAll())
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Producto>> ver(@PathVariable String id) {

        return this
                .productoService
                .findById(id)
                .map(
                    producto ->
                        ResponseEntity
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(producto)
                )
                .defaultIfEmpty(
                    ResponseEntity
                        .notFound()
                        .build()
                );

    }

}
