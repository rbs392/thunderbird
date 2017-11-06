package thunderbird

import cascading.tap.Tap
import cascading.tuple.Fields
import com.indix.models.utils.ThriftUtils
import com.indix.thrift.models.analytics.{Product, StoreProduct, Variant}
import com.twitter.scalding.{AccessMode, Args, Mode, Source}
import org.apache.hadoop.io.{BytesWritable, Text}
import scalding.extensions.jobs.IndixJob
import scalding.extensions.source.TWritableSequenceFile

import scala.collection.JavaConversions._
import org.elasticsearch.hadoop.cascading.EsTap


case class Image(url: String, sourceUrl: String)
case class Ratings(ratingCount: Long, ratingValue: Double, worstRating: Double, bestRating: Double)
case class PriceRange(salePrice: Double, storeId: Long, storeName: String, seller: String)
case class ProductRecord(
                          categoryNamePath: String,
                          image: Image,
                          upcs: List[String],
                          brandName: String,
                          aggregatedRatings: Ratings,
                          brandId: Long,
                          categoryIdPath: String,
                          mpns: List[String],
                          countryCode: String,
                          currency: String,
                          priceRange: List[PriceRange],
                          title: String,
                          pfid: String,
                          detailsUrl: String,
                          offersCount: Long,
                          storesCount: Long
                        )

case class EsSource(host: String, port: Int, resource: String, fields: Fields) extends Source {
  override def createTap(readOrWrite: AccessMode)(implicit mode: Mode): Tap[_, _, _] = {
    new EsTap(host, port, resource, fields)
  }
}

class Main(args: Args) extends IndixJob(args) {
  val INPUT = args.getOrElse("input", "")
  val ES_HOST
  val output = EsSource("", 9200, "", ('test))



  def toProducts( productBytes: Array[Byte] ) = ThriftUtils.deserializeModel(new Product(), productBytes)

  def getProductRecord(product: StoreProduct) = {
    ""
  }

  def toProductRecord( mpid: String, products: Product) = {
    lazy val categoryNamePath = products.getCategoryIdPath
    lazy val ratings = products.getAggregatedRating
    products.getCategoryNamePath

    ProductRecord(
      products.getCategoryNamePath,
      Image(products.getModelImageUrl, products.getImageSourceUrl),
      products.getAggregatedUpcs.toList,
      products.getBrandName,
      Ratings(ratings.getRatingCount, ratings.getRatingValue, ratings.getWorstRating, ratings.getBestRating),
      products.getBrandId,
      products.getCategoryIdPath,
      products.getAggregatedMpns.toList,
      products.getCountryCode,
      products.getCurrency,
      List(PriceRange(1, 1, "", "")),
      products.getModelTitle,
      products.getPfid,
      "",
      products.getStoreProductsSize,
      products.getStoreProductsSize
    )
  }


  TWritableSequenceFile[Text, BytesWritable](input, ('mpid, 'productsInBytes))
    .map('productInBytes -> 'products)(toProducts)
    .map(('mpid, 'products) -> 'productRecord)({
      case (mpid: String, products: Product) => toProductRecord(mpid, products)
    })
    .write(output)
}