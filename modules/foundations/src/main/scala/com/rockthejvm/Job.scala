package com.rockthejvm

import zio.json.{DeriveJsonCodec, JsonCodec}

case class Job(
    id: String,
    title: String,
    url: String,
    company: String,
    relatedJob: Option[String]
)

object Job {
  given codec: JsonCodec[Job] = DeriveJsonCodec.gen[Job]
}
