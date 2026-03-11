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
package it.extrared.registry.dpp;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/** Class providing constants for supported RDF types. */
public class RDFTypes {

    public record RDFType(List<String> mimes) {}

    public static final RDFType JSON_LD =
            new RDFType(List.of("application/ld+json", "application/json", "text/json"));

    public static final RDFType RDF_XML =
            new RDFType(List.of("application/rdf+xml", "application/xml", "text/xml"));

    public static final RDFType TURTLE =
            new RDFType(List.of("application/x+turtle", "text/turtle"));

    public static final RDFType NTRIPLES =
            new RDFType(List.of("application/n-triples", "text/plain"));

    public static final RDFType N3 = new RDFType(List.of("text/n3", "text/rdf+n3"));

    public static final RDFType NQUADS = new RDFType(List.of("application/n-quads"));

    public static final RDFType RDF_JSON = new RDFType(List.of("application/rdf+json"));

    public static final RDFType TRIG =
            new RDFType(List.of("application/trig", "application/x-trig"));

    private static Stream<String> getSupportedContentTypesStream() {
        return Stream.of(
                        JSON_LD.mimes(),
                        RDF_XML.mimes(),
                        TURTLE.mimes(),
                        NTRIPLES.mimes(),
                        N3.mimes(),
                        NQUADS.mimes(),
                        TRIG.mimes(),
                        RDF_JSON.mimes())
                .flatMap(Collection::stream);
    }

    /**
     * @return the whole list of supported mime types by the RDF types.
     */
    public static List<String> getSupportedContentTypes() {
        return getSupportedContentTypesStream().toList();
    }
}
