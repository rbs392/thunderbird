package thunderbird

import java.util.Properties

import cascading.scheme.NullScheme
import cascading.tap.SinkMode
import cascading.tuple.Fields
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.indix.models.utils.ThriftUtils
import com.indix.thrift.models.analytics.Product
import com.twitter.scalding._
import org.apache.hadoop.io.BytesWritable
import scalding.extensions.jobs.IndixJob
import scalding.extensions.source.TWritableSequenceFile

import scala.collection.JavaConversions._
import org.elasticsearch.hadoop.cascading.EsTap


case class Image(url: String, sourceUrl: String)
case class Ratings(ratingCount: Long, ratingValue: Double, worstRating: Double, bestRating: Double)
case class PriceRange(salePrice: Double, storeId: Long, storeName: String, seller: String)
case class ProductRecord(
                          mpid: String,
                          categoryNamePath: String,
//                          image: Image,
                          upcs: List[String],
                          brandName: String,
//                          aggregatedRatings: Ratings,
                          brandId: Long,
                          categoryIdPath: String,
                          mpns: List[String],
                          countryCode: String,
                          currency: String,
//                          priceRange: List[PriceRange],
                          title: String,
                          pfid: String,
                          detailsUrl: String,
                          offersCount: Long,
                          storesCount: Long
                        )


case class EsSource(host: String, port: Int, resource: String, fields: Fields) extends Source {
  override def createTap(readOrWrite: AccessMode)(implicit mode: Mode) = mode match {
    case Local(_) | Hdfs(_, _) => new EsTap(host, port, resource, null, Some(fields).getOrElse(null), null)
    case _ => TestTapFactory(this, Some(fields).get, SinkMode.REPLACE).createTap(readOrWrite)
  }
}

object Helper {
  val objMapper = new ObjectMapper() with ScalaObjectMapper
  objMapper.registerModule(DefaultScalaModule)

}

class Main(args: Args) extends IndixJob(args) {

  val INPUT = args.getOrElse("input", "")
  val ES_HOST = args.getOrElse("ES_HOST", "localhost")
  val ES_PORT = args.getOrElse("ES_PORT", "9200").toInt
  val ES_RESOURCE = args.getOrElse("ES_RESOURCE", "dev")
  val output = EsSource(ES_HOST, ES_PORT, ES_RESOURCE, new Fields("title", "storesCount", "offersCount", "random"))


  def toProducts(productBytes: BytesWritable ) =
    ThriftUtils.deserializeModel(new Product(), productBytes.getBytes)

  def toProductRecord( mpid: String, products: Product) = {
    lazy val categoryNamePath = products.getCategoryIdPath
    lazy val ratings = products.getAggregatedRating

    ProductRecord(
      mpid,
      products.getCategoryNamePath,
//      Image(products.getModelImageUrl, products.getImageSourceUrl),
      products.getAggregatedUpcs.toList,
      products.getBrandName,
//      Ratings(ratings.getRatingCount, ratings.getRatingValue, ratings.getWorstRating, ratings.getBestRating),
      products.getBrandId,
      products.getCategoryIdPath,
      products.getAggregatedMpns.toList,
      products.getCountryCode,
      products.getCurrency,
//      List(PriceRange(0,0, "1", "3")),
      products.getModelTitle,
      products.getPfid,
      "",
      products.getStoreProductsSize,
      products.getStoreProductsSize
    )
  }

  def toFields(x: (String, Product)) = x match {
    case (mpid: String, products: Product) => {
      val tmp = toProductRecord(mpid, products)
      (tmp.title, tmp.mpid, tmp.pfid, "random")
    }
//      Helper.objMapper.writeValueAsString(toProductRecord(mpid, products))
  }


  val props = new Properties()
  props.setProperty("es.input.json", "true")

  TWritableSequenceFile(INPUT, ('mpid, 'bytes))
    .map('bytes -> 'products)(toProducts)
    .map(('mpid, 'products) -> ('title, 'storesCount, 'offersCount, 'random))(toFields)
    .project('title, 'storesCount, 'offersCount, 'random)
    .write(output)
    .write(Csv("output"))
}