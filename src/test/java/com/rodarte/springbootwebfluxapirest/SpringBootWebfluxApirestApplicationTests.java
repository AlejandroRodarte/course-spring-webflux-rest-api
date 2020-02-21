package com.rodarte.springbootwebfluxapirest;

import com.rodarte.springbootwebfluxapirest.models.documents.Producto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootWebfluxApirestApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

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

				Assertions.assertThat(productos.size() == 9).isTrue();

			});
			// .hasSize(9);

	}

}
