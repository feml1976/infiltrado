package com.transer.infiltrado.catalogo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transer.infiltrado.shared.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
class CatalogoAdminIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtService jwtService;

    private String tokenAdmin;
    private String tokenUsuario;

    @BeforeEach
    void setUp() {
        tokenAdmin   = jwtService.generar(UUID.randomUUID(), "Administrador", true);
        tokenUsuario = jwtService.generar(UUID.randomUUID(), "Usuario Normal",  false);
    }

    // ── CRUD admin ────────────────────────────────────────────────────────────

    @Test
    void crear_cosa_tipo_palabra_devuelve_201() throws Exception {
        mockMvc.perform(post("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("lapicero", "PALABRA", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre",  is("lapicero")))
                .andExpect(jsonPath("$.tipo",    is("PALABRA")))
                .andExpect(jsonPath("$.activo",  is(true)))
                .andExpect(jsonPath("$.id",      notNullValue()));
    }

    @Test
    void nombre_se_normaliza_a_minusculas_con_trim() throws Exception {
        mockMvc.perform(post("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("  MESA  ", "PALABRA", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre", is("mesa")));
    }

    @Test
    void crear_nombre_duplicado_devuelve_409() throws Exception {
        mockMvc.perform(post("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("duplicado", "PALABRA", null)))
                .andExpect(status().isCreated());

        // mismo nombre en mayúsculas → debe rechazarse
        mockMvc.perform(post("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("DUPLICADO", "PALABRA", null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void listar_no_incluye_imagen_base64() throws Exception {
        mockMvc.perform(post("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("pizarron", "IMAGEN", pngBase64Pequeno())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].imagenBase64").doesNotExist());
    }

    @Test
    void obtener_detalle_incluye_imagen_base64() throws Exception {
        String respCrear = mockMvc.perform(post("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("foco", "IMAGEN", pngBase64Pequeno())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(respCrear).get("id").asText();

        mockMvc.perform(get("/api/catalogo/cosas/" + id)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagenBase64", not(emptyString())));
    }

    @Test
    void actualizar_cambia_nombre() throws Exception {
        String resp = mockMvc.perform(post("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("antigua_cosa", "PALABRA", null)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(resp).get("id").asText();

        mockMvc.perform(put("/api/catalogo/cosas/" + id)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("nueva_cosa", "PALABRA", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("nueva_cosa")));
    }

    @Test
    void actualizar_nombre_duplicado_en_otra_cosa_devuelve_409() throws Exception {
        mockMvc.perform(post("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("cosaa", "PALABRA", null)))
                .andExpect(status().isCreated());

        String resp = mockMvc.perform(post("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("cosab", "PALABRA", null)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(resp).get("id").asText();

        // Intentar renombrar cosab → cosaa (ya existe)
        mockMvc.perform(put("/api/catalogo/cosas/" + id)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("cosaa", "PALABRA", null)))
                .andExpect(status().isConflict());
    }

    @Test
    void desactivar_devuelve_204_y_deja_de_aparecer_en_listado() throws Exception {
        String resp = mockMvc.perform(post("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("efimera", "PALABRA", null)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(resp).get("id").asText();

        mockMvc.perform(delete("/api/catalogo/cosas/" + id)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + id + "')]").isEmpty());
    }

    @Test
    void obtener_cosa_inexistente_devuelve_404() throws Exception {
        mockMvc.perform(get("/api/catalogo/cosas/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }

    // ── Autorización ──────────────────────────────────────────────────────────

    @Test
    void usuario_sin_admin_obtiene_403_en_get() throws Exception {
        mockMvc.perform(get("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuario_sin_admin_obtiene_403_en_post() throws Exception {
        mockMvc.perform(post("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("intento", "PALABRA", null)))
                .andExpect(status().isForbidden());
    }

    @Test
    void sin_token_devuelve_401() throws Exception {
        mockMvc.perform(get("/api/catalogo/cosas"))
                .andExpect(status().isUnauthorized());
    }

    // ── Validación de imagen ──────────────────────────────────────────────────

    @Test
    void imagen_tipo_imagen_sin_datos_devuelve_422() throws Exception {
        mockMvc.perform(post("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("sinimagen", "IMAGEN", null)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje", containsString("Base64")));
    }

    @Test
    void imagen_formato_invalido_devuelve_422() throws Exception {
        String invalido = Base64.getEncoder().encodeToString("no es imagen".getBytes());
        mockMvc.perform(post("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("mala_firma", "IMAGEN", invalido)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje", containsString("Formato")));
    }

    @Test
    void imagen_mayor_200kb_devuelve_422() throws Exception {
        mockMvc.perform(post("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("gigante", "IMAGEN", pngBase64Gigante())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje", containsString("200 KB")));
    }

    @Test
    void imagen_con_prefijo_data_uri_es_aceptada() throws Exception {
        String conPrefijo = "data:image/png;base64," + pngBase64Pequeno();
        mockMvc.perform(post("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cosaJson("con_prefijo", "IMAGEN", conPrefijo)))
                .andExpect(status().isCreated());
    }

    // ── Seed ─────────────────────────────────────────────────────────────────

    @Test
    void seed_carga_palabras_esperadas() throws Exception {
        String respuesta = mockMvc.perform(get("/api/catalogo/cosas")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(respuesta).contains("silla", "perro", "guitarra", "elefante");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String cosaJson(String nombre, String tipo, String imagenBase64) throws Exception {
        var map = new LinkedHashMap<String, Object>();
        map.put("nombre", nombre);
        map.put("tipo",   tipo);
        if (imagenBase64 != null) map.put("imagenBase64", imagenBase64);
        return objectMapper.writeValueAsString(map);
    }

    /** PNG 1×1 transparente — firma válida, tamaño << 200 KB. */
    private static String pngBase64Pequeno() {
        return "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
    }

    /** PNG con magic bytes válidos pero tamaño decodificado > 200 KB. */
    private static String pngBase64Gigante() {
        byte[] bytes = new byte[205_000];
        bytes[0] = (byte) 0x89; bytes[1] = 0x50; bytes[2] = 0x4E; bytes[3] = 0x47;
        bytes[4] = 0x0D;        bytes[5] = 0x0A; bytes[6] = 0x1A; bytes[7] = 0x0A;
        return Base64.getEncoder().encodeToString(bytes);
    }
}
