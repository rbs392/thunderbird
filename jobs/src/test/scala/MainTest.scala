package thunderbird

import cascading.tuple.Fields
import com.indix.models.utils.ThriftUtils
import com.indix.thrift.models.analytics.{AggregatedRating, Product}
import com.twitter.scalding._
import org.apache.hadoop.io.BytesWritable
import org.scalatest.FlatSpec
import scalding.extensions.source.TWritableSequenceFile

import scala.collection.JavaConversions._
import scala.collection.mutable
/**
  * Created by bala on 6/11/17.
  */
class MainTest extends FlatSpec {

  val products = List(
    new Product()
      .setCategoryNamePath("a > b > c")
      .setImageSourceUrl("1.jpg")
      .setAggregatedUpcs(List("upc1","upc2", "upc3"))
      .setBrandId(1)
      .setCategoryIdPath("a > b > c")
      .setAggregatedMpns(List("mpn1", "mpn2", "mpn3"))
      .setCountryCode("IN")
      .setCurrency("Rupees")
      .setModelTitle("title1")
      .setPfid("pfid1")
      .setMpidStr("mpid1")
      .setModelPidStr("modelpid1")
      .setAggregatedRating(new AggregatedRating() )
  )

  def toThriftProducts(products: List[Product]) = {
    products.map { product =>
      (product.getModelPidStr, new BytesWritable(ThriftUtils.serializeModel(product)))
    }
  }

  def validate(output: mutable.Buffer[String]) = {
    output.map(println)
    println(output.length)
  }

  def validate2(output: mutable.Buffer[(String, String, String, String)]) = {
    output.map(println)
    println(output.length)
  }



  JobTest(new Main(_))
  .arg("input", "input")
  .arg("ES_HOST", "10.181.27.25")
  .arg("ES_PORT", "9200")
  .arg("ES_RESOURCE", "dev/test")
  .source(TWritableSequenceFile("input", new Fields("mpid", "products")), toThriftProducts(products))
  .sink(EsSource("test", 80, "dev/test", new Fields("title", "storesCount", "offersCount", "random")))(validate2)
  .run
  .finish
}
