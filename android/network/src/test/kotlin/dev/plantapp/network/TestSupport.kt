package dev.plantapp.network

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage
import java.io.File
import kotlinx.serialization.json.Json

/** Shared test helpers for :network DTO + schema-validation tests (D-06). */
object TestSupport {
    /** Encoder mirroring the API contract: omit defaults and nulls so optional fields
     *  are absent (matching the additionalProperties:false shared schemas). */
    val json: Json = Json {
        encodeDefaults = false
        explicitNulls = false
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private val mapper = ObjectMapper()
    private val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)

    /** Walks up from the test working dir to the repo root and resolves a shared schema. */
    fun sharedSchemaFile(name: String): File {
        var dir: File? = File(System.getProperty("user.dir"))
        while (dir != null && !File(dir, "shared-schemas").isDirectory) {
            dir = dir.parentFile
        }
        requireNotNull(dir) { "Could not locate shared-schemas/ above ${System.getProperty("user.dir")}" }
        val file = File(dir, "shared-schemas/$name.schema.json")
        require(file.isFile) { "Missing schema: $file" }
        return file
    }

    /** Validates a JSON string against shared-schemas/<name>.schema.json (2020-12). */
    fun validateAgainstSchema(name: String, jsonString: String): Set<ValidationMessage> {
        val schemaNode: JsonNode = mapper.readTree(sharedSchemaFile(name))
        val schema = factory.getSchema(schemaNode)
        val instance: JsonNode = mapper.readTree(jsonString)
        return schema.validate(instance)
    }
}
