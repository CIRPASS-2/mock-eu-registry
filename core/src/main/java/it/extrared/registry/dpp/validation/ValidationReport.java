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
package it.extrared.registry.dpp.validation;

import java.util.List;

/** Represent the validation report of the validator service. */
public class ValidationReport {

    private boolean valid;

    private String message;

    private String validatedWith;

    private String validationType;

    private List<InvalidProperty> invalidProperties;

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public List<InvalidProperty> getInvalidProperties() {
        return invalidProperties;
    }

    public String getValidatedWith() {
        return validatedWith;
    }

    public String getValidationType() {
        return validationType;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setValidatedWith(String validatedWith) {
        this.validatedWith = validatedWith;
    }

    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }

    public void setInvalidProperties(List<InvalidProperty> invalidProperties) {
        this.invalidProperties = invalidProperties;
    }
}
