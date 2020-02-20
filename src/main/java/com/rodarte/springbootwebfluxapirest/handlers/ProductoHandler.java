package com.rodarte.springbootwebfluxapirest.handlers;

import com.rodarte.springbootwebfluxapirest.models.documents.Producto;
import com.rodarte.springbootwebfluxapirest.models.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Component
public class ProductoHandler {

    @Autowired
    private ProductoService productoService;

    @Value("${config.uploads.path}")
    private String path;

    public Mono<ServerResponse> upload(ServerRequest serverRequest) {

        String id = serverRequest.pathVariable("id");

        return serverRequest
                .multipartData()
                .map(multiValueMap -> multiValueMap.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(
                    filePart ->
                        this
                            .productoService
                            .findById(id)
                            .flatMap(
                                producto -> {

                                    producto.setFoto(
                                        UUID.randomUUID().toString() +
                                        "-" +
                                        filePart
                                            .filename()
                                            .replace(" ", "-")
                                            .replace(":", "")
                                            .replace("\\", "")
                                    );

                                    return filePart
                                            .transferTo(new File(this.path + producto.getFoto()))
                                            .then(this.productoService.save(producto));

                                }
                            )
                )
                .flatMap(
                    producto -> ServerResponse
                                    .created(URI.create("/api/v2/productos/" + producto.getId()))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(BodyInserters.fromValue(producto))
                )
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    public Mono<ServerResponse> listar(ServerRequest serverRequest) {

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.productoService.findAll(), Producto.class);

    }

    public Mono<ServerResponse> ver(ServerRequest serverRequest) {

        String id = serverRequest.pathVariable("id");

        return this
                .productoService
                .findById(id)
                .flatMap(
                    producto ->
                        ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(producto))
                )
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    public Mono<ServerResponse> crear(ServerRequest serverRequest) {

        Mono<Producto> producto = serverRequest.bodyToMono(Producto.class);

        return producto
                .flatMap(nuevoProducto -> {

                    if (nuevoProducto.getCreatedAt() == null) {
                        nuevoProducto.setCreatedAt(new Date());
                    }

                    return this.productoService.save(nuevoProducto);

                })
                .flatMap(productoCreado ->
                    ServerResponse
                        .created(URI.create("/api/v2/productos/".concat(productoCreado.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(productoCreado))
                );

    }

    public Mono<ServerResponse> editar(ServerRequest serverRequest) {

        String id = serverRequest.pathVariable("id");
        Mono<Producto> producto = serverRequest.bodyToMono(Producto.class);

        return this
                .productoService
                .findById(id)
                .zipWith(producto, (db, req) -> {

                    db.setNombre(req.getNombre());
                    db.setPrecio(req.getPrecio());
                    db.setCategoria(req.getCategoria());

                    return db;

                })
                .flatMap(productoSalvado ->
                    ServerResponse
                        .created(URI.create("/api/v2/productos".concat(productoSalvado.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(this.productoService.save(productoSalvado), Producto.class)
                )
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    public Mono<ServerResponse> eliminar(ServerRequest serverRequest) {

        String id = serverRequest.pathVariable("id");

        return this
                .productoService
                .findById(id)
                .flatMap(
                    producto ->
                        this
                            .productoService
                            .delete(producto)
                            .then(ServerResponse.noContent().build())
                )
                .switchIfEmpty(ServerResponse.notFound().build());

    }

}
