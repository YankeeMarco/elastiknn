package com.klibisz.elastiknn.testing

import com.klibisz.elastiknn.client.ElastiknnClient
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.{ElasticClient, Executor}
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.scalatest.AsyncTestSuite

import scala.concurrent.Future

trait ElasticAsyncClient {

  this: AsyncTestSuite =>

  lazy val elasticHost: HttpHost = new HttpHost("localhost", 9200)

  // This makes sure the client executes requests on the execution context setup by the test.
  implicit def futureExecutor: Executor[Future] = Executor.FutureExecutor(this.executionContext)

  protected implicit lazy val client: ElasticClient = {
    val rc = RestClient.builder(elasticHost).build()
    val jc = new JavaClient(rc)
    ElasticClient(jc)
  }

  protected lazy val eknn: ElastiknnClient[Future] = ElastiknnClient.futureClient()

}
