DO $$ DECLARE r RECORD; BEGIN FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = current_schema()) LOOP EXECUTE 'DROP TABLE IF EXISTS ' || quote_ident(r.tablename) || ' CASCADE'; END LOOP; END $$;
CREATE SEQUENCE IF NOT EXISTS dpp_metadata_seq;

CREATE TABLE IF NOT EXISTS dpp_metadata (
id BIGINT PRIMARY KEY DEFAULT nextval('dpp_metadata_seq'),
registry_id VARCHAR(36) NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
metadata JSONB NOT NULL,
dpp_hash VARCHAR(71),
dpp_content_type VARCHAR(50)
);

CREATE SEQUENCE IF NOT EXISTS json_schema_seq;

CREATE TABLE IF NOT EXISTS json_schemas (
id BIGINT PRIMARY KEY DEFAULT nextval('json_schema_seq'),
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
data_schema JSONB NOT NULL
);

INSERT INTO json_schemas (data_schema)
VALUES ('{"$schema":"https://json-schema.org/draft/2020-12/schema","title":"Test EU DPP Registry Metadata","description":"Test Schema for Digital Product Passport registration metadata in the EU Registry","type":"object","required":["upi","reoId","commodityCode"],"properties":{"upi":{"type":"string","description":"Unique Product Identifier - the unique identifier of the product","minLength":1,"maxLength":200,"examples":["urn:epc:id:sgtin:0614141.107346.2017"]},"reoId":{"type":"string","description":"Responsible Economic Operator ID","minLength":1,"maxLength":50,"examples":["LEI-529900T8BM49AURSDO55","EORI-IT123456789"]},"commodityCode":{"type":["string","null"],"description":"The commodity code of the product (e.g., HS Code, TARIC)","pattern":"^[0-9]{4,10}$","examples":["85176200","8517620090"]},"dataCarrierTypes":{"type":"array","description":"Types of data carriers associated with the product","uniqueItems":true,"minItems":1,"examples":[["QR_CODE","RFID"]],"items":{"type":"string","enum":["QR_CODE","DATA_MATRIX","BARCODE_EAN","BARCODE_UPC","BARCODE_GS1","RFID","NFC","AZTEC_CODE","PDF417"]}}}}'::jsonb);

INSERT INTO dpp_metadata (registry_id, metadata)
VALUES ('550e8400-e29b-41d4-a716-446655440000', '{"upi":"urn:epc:id:sgtin:0614141.107346.2017","reoId":"LEI-529900T8BM49AURSDO55","commodityCode":"85176200","dataCarrierTypes":["QR_CODE","RFID","NFC"]}'::jsonb);