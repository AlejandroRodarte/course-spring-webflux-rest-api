package com.rodarte.springbootwebfluxapirest;

import com.rodarte.springbootwebfluxapirest.models.documents.Categoria;
import com.rodarte.springbootwebfluxapirest.models.documents.Producto;
import com.rodarte.springbootwebfluxapirest.models.services.ProductoService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootWebfluxApirestApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ProductoService productoService;

	@Test
	void listarTest() {

		webTestClient
			.get()
			.uri("/api/v2/productos")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentType(MediaType.APPLICATION_JSON)
			.expectBodyList(Producto.class)
			.consumeWith(response -> {

				List<Producto> productos = response.getResponseBody();
				productos.forEach(producto -> System.out.println(producto.getNombre()));

				Assertions.assertThat(productos.size() > 0).isTrue();

			});
			// .hasSize(9);

	}

	@Test
	void verTest() {

		Producto producto = productoService.findByNombre("TV Panasonic Pantalla LCD").block();

		webTestClient
			.get()
			.uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentType(MediaType.APPLICATION_JSON)
			.expectBody(Producto.class)
			.consumeWith(response -> {

				Producto jsonProducto = response.getResponseBody();

				Assertions.assertThat(jsonProducto.getId()).isNotEmpty();
				Assertions.assertThat(jsonProducto.getId().length() > 0).isTrue();
				Assertions.assertThat(jsonProducto.getNombre()).isEqualTo("TV Panasonic Pantalla LCD");

			});
			/* .expectBody()
			.jsonPath("$.id")
			.isNotEmpty()
			.jsonPath("$.nombre")
			.isEqualTo("TV Panasonic Pantalla LCD"); */

	}

	@Test
	public void crearTest() {

		Categoria categoria = productoService.findCategoriaByNombre("Muebles").block();

		Producto producto = new Producto("Mesa comedor", 100.00, categoria);

		webTestClient
			.post()
			.uri("/api/v2/productos")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.body(Mono.just(producto), Producto.class)
			.exchange()
			.expectStatus()
			.isCreated()
			.expectHeader()
			.contentType(MediaType.APPLICATION_JSON)
			.expectBody(Producto.class)
			.consumeWith(response -> {

				Producto responseProducto = response.getResponseBody();

				Assertions.assertThat(responseProducto.getId()).isNotEmpty();
				Assertions.assertThat(responseProducto.getNombre()).isEqualTo("Mesa comedor");
				Assertions.assertThat(responseProducto.getCategoria().getNombre()).isEqualTo("Muebles");

			});
			/* .expectBody()
			.jsonPath("$.id")
			.isNotEmpty()
			.jsonPath("$.nombre")
			.isEqualTo("Mesa comedor")
			.jsonPath("$.categoria.nombre")
			.isEqualTo("Muebles"); */

	}

	@Test
	public void editarTest() {

		Producto producto = productoService.findByNombre("Sony Notebook").block();
		Categoria categoria = productoService.findCategoriaByNombre("Electronico").block();

		Producto productoEditado = new Producto("Asus Notebook", 700.00, categoria);

		webTestClient
			.put()
			.uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.body(Mono.just(productoEditado), Producto.class)
			.exchange()
			.expectStatus()
			.isCreated()
			.expectHeader()
			.contentType(MediaType.APPLICATION_JSON)
			.expectBody(Producto.class)
			.consumeWith(response -> {

				Producto productoActualizado = response.getResponseBody();

				Assertions.assertThat(productoActualizado.getId()).isEqualTo(producto.getId());
				Assertions.assertThat(productoActualizado.getNombre()).isEqualTo(productoEditado.getNombre());
				Assertions.assertThat(productoActualizado.getCategoria().getNombre()).isEqualTo(productoEditado.getCategoria().getNombre());

			});

	}

	@Test
	public void eliminarTest() {

		Producto producto = productoService.findByNombre("Mica Cómoda 5 Cajones").block();

		webTestClient
			.delete()
			.uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
			.exchange()
			.expectStatus()
			.isNoContent()
			.expectBody()
			.isEmpty();

		webTestClient
			.get()
			.uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
			.exchange()
			.expectStatus()
			.isNotFound()
			.expectBody()
			.isEmpty();

	}

}
