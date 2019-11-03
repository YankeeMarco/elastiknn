package com.klibisz.elastiknn

import com.sksamuel.elastic4s.{ElasticRequest, Handler, HttpEntity}
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json, JsonObject}
import scalapb_circe.JsonFormat

/** Utilities for elastic4s library. */
object elastic4s {

  case class PipelineRequest(name: String, pipeline: Pipeline)

  object PipelineRequest {
    implicit object PipelineRequestHandler
        extends Handler[PipelineRequest, PipelineResponse] {
      override def build(t: PipelineRequest): ElasticRequest = {
        // See: https://www.elastic.co/guide/en/elasticsearch/reference/current/ingest-processors.html
        val body: Json = Json.fromJsonObject(
          JsonObject(
            "description" -> Json.fromString(t.pipeline.description),
            "processors" -> Json.fromValues(t.pipeline.processors.map { p =>
              Json.fromJsonObject(
                JsonObject(p.name -> JsonFormat.toJson(p.opts))
              )
            })
          ))
        val endpoint = s"_ingest/pipeline/${t.name}"
        ElasticRequest("PUT", endpoint, HttpEntity(body.noSpaces))
      }
    }
  }

  case class Pipeline(description: String, processors: Seq[Processor])

  case class Processor(name: String, opts: ProcessorOptions)

  case class PipelineResponse(acknowledged: Boolean)

}