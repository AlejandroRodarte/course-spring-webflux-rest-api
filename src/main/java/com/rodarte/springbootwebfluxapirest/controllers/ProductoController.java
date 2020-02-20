package com.rodarte.springbootwebfluxapirest.controllers;

import com.rodarte.springbootwebfluxapirest.models.documents.Producto;
import com.rodarte.springbootwebfluxapirest.models.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;

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

    @PostMapping
    public Mono<ResponseEntity<Producto>> crear(@RequestBody Producto producto) {

        if (producto.getCreatedAt() == null) {
            producto.setCreatedAt(new Date());
        }

        return this
                .productoService
                .save(producto)
                .map(
                    nuevoProducto ->
                        ResponseEntity
                            .created(URI.create("/api/productos/".concat(nuevoProducto.getId())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(nuevoProducto)
                );

    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Producto>> editar(@RequestBody Producto producto, @PathVariable String id) {

        return this
                .productoService
                .findById(id)
                .flatMap(
                    nextProducto -> {

                       nextProducto.setNombre(producto.getNombre());
                       nextProducto.setPrecio(producto.getPrecio());
                       nextProducto.setCategoria(producto.getCategoria());

                       return this.productoService.save(nextProducto);

                    }
                )
                .map(
                    productoActualizado ->
                        ResponseEntity
                            .created(URI.create("/api/productos/".concat(productoActualizado.getId())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(productoActualizado)
                )
                .defaultIfEmpty(
                    ResponseEntity
                        .notFound()
                        .build()
                );

    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar(@PathVariable String id) {

        return this
                .productoService
                .findById(id)
                .flatMap(
                    producto ->
                        this.productoService
                            .delete(producto)
                            .thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))
                )
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));

    }

}
