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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class OpenAPIFilterTest {

    @Test
    public void testCustomizedOpenApi() {
        JsonNode res =
                given().when()
                        .header(new Header("Accept", "application/json"))
                        .get("/q/openapi")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(JsonNode.class);
        res.get("components");
        assertTrue(res.get("components").get("schemas").has("Metadata"));
    }
}
