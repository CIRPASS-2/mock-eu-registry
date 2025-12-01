/*
 * Copyright 2024-2027 CIRPASS-2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.extrared.registry.api.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import it.extrared.registry.utils.JsonUtils;
import java.io.IOException;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class SchemaResourceTest {

    @Test
    public void testAddGetAddGetRemoveGet() throws IOException {
        JsonNode node = JsonUtils.loadClasspathJsonTemplate("db-schema1.json");
        given().when()
                .body(node)
                .contentType(ContentType.JSON)
                .post("/schema/v1")
                .then()
                .statusCode(201);
        JsonNode schema =
                given().when()
                        .get("/schema/v1/current")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(JsonNode.class);
        assertEquals(node, schema);
        JsonNode node2 = JsonUtils.loadClasspathJsonTemplate("db-schema2.json");
        given().when()
                .body(node2)
                .contentType(ContentType.JSON)
                .post("/schema/v1")
                .then()
                .statusCode(201);
        schema =
                given().when()
                        .get("/schema/v1/current")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(JsonNode.class);
        assertEquals(node2, schema);
        given().when().delete("/schema/v1/current").then().statusCode(204);
        schema =
                given().when()
                        .get("/schema/v1/current")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(JsonNode.class);
        assertEquals(node, schema);
    }
}
