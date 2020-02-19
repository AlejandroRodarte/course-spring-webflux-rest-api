package com.rodarte.springbootwebfluxapirest.models.dao;

import com.rodarte.springbootwebfluxapirest.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String> {
}
