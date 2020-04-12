package models

import com.klibisz.elastiknn.api._
import com.klibisz.elastiknn.client.ElastiknnRequests
import com.sksamuel.elastic4s.requests.indexes.PutMappingBuilderFn
import com.sksamuel.elastic4s.requests.mappings.PutMappingRequest
import com.sksamuel.elastic4s.requests.searches.{SearchBodyBuilderFn, SearchRequest}
import io.circe.generic.semiauto._
import io.circe.{Encoder, Json}

case class Dataset(prettyName: String, name: String, examples: Seq[Example])

case class Example(name: String, index: String, mapping: PutMappingRequest, query: SearchRequest)

object Example {
  implicit val encodePutMapping: Encoder[PutMappingRequest] = (a: PutMappingRequest) => Json.fromString(PutMappingBuilderFn(a).string())
  implicit val encodeSearchRequest: Encoder[SearchRequest] = (a: SearchRequest) => Json.fromString(SearchBodyBuilderFn(a).string())
  implicit val encoder: Encoder[Example] = deriveEncoder[Example]
}

object Dataset extends ElastiknnRequests {

  private def example(name: String, index: String, mapping: Mapping, mkQuery: (String, Vec) => NearestNeighborsQuery): Example =
    Example(
      name,
      index,
      putMapping(index, "vec", mapping),
      nearestNeighborsQuery(index, mkQuery("vec", Vec.Indexed(index, "1", "vec")), 10, true)
    )

  val defaults: Seq[Dataset] = Seq(
    Dataset(
      "MNIST with Jaccard Similarity",
      "mnist_binary",
      Seq(
        example("Exact",
                "mnist-jaccard-exact",
                Mapping.SparseBool(784),
                (field, vec) => NearestNeighborsQuery.Exact(field, vec, Similarity.Jaccard)),
        example("Sparse Indexed",
                "mnist-jaccard-sparse-indexed",
                Mapping.SparseBool(784),
                (f, v) => NearestNeighborsQuery.SparseIndexed(f, v, Similarity.Jaccard)),
        example("Jaccard LSH 1",
                "mnist-jaccard-lsh-1",
                Mapping.JaccardLsh(784, 100, 1),
                (f, v) => NearestNeighborsQuery.JaccardLsh(f, v, 100)),
        example("Jaccard LSH 1",
                "mnist-jaccard-lsh-1",
                Mapping.JaccardLsh(784, 100, 1),
                (f, v) => NearestNeighborsQuery.JaccardLsh(f, v, 100)),
      )
    )
  )
}