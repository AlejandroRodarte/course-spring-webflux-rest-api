package com.rodarte.springbootwebfluxapirest.controllers;

import com.rodarte.springbootwebfluxapirest.models.documents.Producto;
import com.rodarte.springbootwebfluxapirest.models.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Value("${config.uploads.path}")
    private String path;

//    @GetMapping
//    public Flux<Producto> listar() {
//        return this.productoService.findAll();
//    }

    @PostMapping("/v2")
    public Mono<ResponseEntity<Producto>> crearConFoto(Producto producto, @RequestPart(name = "file") FilePart filePart) {

        if (producto.getCreatedAt() == null) {
            producto.setCreatedAt(new Date());
        }

        producto.setFoto(
            UUID.randomUUID().toString() +
            "-" +
            filePart.filename().replace(" ", "") +
            filePart.filename().replace(":", "") +
            filePart.filename().replace("\\", "")
        );

        return filePart
                .transferTo(new File(this.path + producto.getFoto()))
                .then(this.productoService.save(producto))
                .map(
                    productoSalvado ->
                        ResponseEntity
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(productoSalvado)
                );

    }

    @PostMapping("/upload/{id}")
    public Mono<ResponseEntity<Producto>> upload(@PathVariable String id, @RequestPart(name = "file") FilePart filePart) {

        return this
                .productoService
                .findById(id)
                .flatMap(
                    producto -> {

                        producto.setFoto(
                            UUID.randomUUID().toString() +
                            "-" +
                            filePart.filename().replace(" ", "") +
                            filePart.filename().replace(":", "") +
                            filePart.filename().replace("\\", "")
                        );

                        return filePart
                                .transferTo(new File(this.path + producto.getFoto()))
                                .then(this.productoService.save(producto));

                    }
                )
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
    public Mono<ResponseEntity<Map<String, Object>>> crear(@Valid @RequestBody Mono<Producto> monoProducto) {

        Map<String, Object> respuesta = new HashMap<>();

        return monoProducto
                .flatMap(
                    producto -> {

                        if (producto.getCreatedAt() == null) {
                            producto.setCreatedAt(new Date());
                        }

                        return this
                                .productoService
                                .save(producto)
                                .map(
                                    nuevoProducto -> {

                                        respuesta.put("producto", nuevoProducto);
                                        respuesta.put("mensaje", "Producto creado con exito");
                                        respuesta.put("timestamp", new Date());

                                        return ResponseEntity
                                                .created(URI.create("/api/productos/".concat(nuevoProducto.getId())))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .body(respuesta);

                                    }
                                );

                    }
                )
                .onErrorResume(
                    t -> Mono
                            .just(t)
                            .cast(WebExchangeBindException.class)
                            .map(WebExchangeBindException::getFieldErrors)
                            .flatMapMany(Flux::fromIterable)
                            .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                            .collectList()
                            .map(list -> {

                                respuesta.put("errores", list);
                                respuesta.put("timestamp", new Date());
                                respuesta.put("status", HttpStatus.BAD_REQUEST.value());

                                return ResponseEntity
                                        .badRequest()
                                        .body(respuesta);

                            })
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
