package com.enzobnl.sparkscalaexpe.playground

import org.apache.spark.ml.feature.{HashingTF, IDF}
import org.apache.spark.ml.{Pipeline, PipelineStage}
import org.apache.spark.sql._
import org.apache.spark.sql.functions._

import scala.io.Source

/**
 * import sys
 * from pyspark.sql.window import Window
 * import pyspark.sql.functions as func
 * windowSpec = \
 * Window
 * .partitionBy(df['category']) \
 * .orderBy(df['revenue'].desc()) \
 * .rangeBetween(-sys.maxsize, sys.maxsize)
 * dataFrame = sqlContext.table("productRevenue")
 * revenue_difference = \
 * (func.max(dataFrame['revenue']).over(windowSpec) - dataFrame['revenue'])
 * dataFrame.select(
 * dataFrame['product'],
 * dataFrame['category'],
 * dataFrame['revenue'],
 * revenue_difference.alias("revenue_difference"))
 */
object Sb3 extends Runnable {
  final val DAMPING_FACTOR: Double = 0.85
  var N_ITER: Int = 3
  lazy val spark: SparkSession = {
    val spark =
      SparkSession
        .builder
        .config("spark.default.parallelism", "12")
        .master("local[*]")
        .appName("GraphxVsGraphFramesPageRanksApp")
        .getOrCreate
    spark.sparkContext.setLogLevel("ERROR")
    spark
  }

  /*
  spark.conf.set("spark.sql.autoBroadcastJoinThreshold", -1)
      val VertexDataTable = spark.range(1000000)
        .withColumn("v", $"id" + "url")
        .repartition($"id")

      val VertexMap = spark.range(1000000)
        .withColumn("pid", pmod($"id", lit(5)))
        .repartition($"id")

      val EdgeTable = spark.range(1000000)
        .withColumnRenamed("id", "pid")
        .withColumn("src", pmod($"pid", lit(5)))
        .withColumn("dst", pmod($"pid", lit(3)))
        .repartition($"pid")

      VertexDataTable.as("v")
          .join(VertexMap.as("vm"),
            $"v.id" === $"vm.id")
          .join(EdgeTable.as("e"),
            $"e.pid" === $"vm.pid" && ($"e.src" === $"v.id" || $"e.dst" === $"v.id"),
            "right_outer").explain

      VertexDataTable.as("v")
        .join(EdgeTable.as("e"),
          ($"e.src" === $"v.id" || $"e.dst" === $"v.id"),
          "right_outer").explain
   */


  //  class ShowableDeltaTable extends DeltaTable{
  //    def show() = {
  //      deltaTable.toDF.show()
  //    }
  //  }

  override def run(): Unit = {

    /*
    import html2text
    import requests
    import os

    proc = html2text.HTML2Text()
    proc.ignore_links = True
    proc.ignore_images = True
    proc.emphasis_mark = ""
    proc.ul_item_mark = ""
    proc.strong_mark = ""

    def url_response_to_md(url):
        raw_text = requests.get(url).text
        return proc.handle(raw_text)
        # return list(filter(lambda e: len(e), md_text.replace("\n", " ").split(" ")))

    urls = [
        "https://www.oncrawl.com/seo-crawler/",
        "https://www.oncrawl.com/seo-log-analyzer/",
        "https://www.oncrawl.com/oncrawl-analytics/",
        "https://www.oncrawl.com/seo-crawl-logs-analysis/",
        "https://www.oncrawl.com/oncrawl-rankings/",
        "https://www.oncrawl.com/oncrawl-backlinks/",
        "https://www.oncrawl.com/oncrawl-platform/",
        "https://www.oncrawl.com/google-analytics-oncrawl/",
        "https://www.oncrawl.com/google-search-console-oncrawl/",
        "https://www.oncrawl.com/adobe-analytics-oncrawl/",
        "https://www.oncrawl.com/majestic-oncrawl/",
        "https://www.oncrawl.com/at-internet-oncrawl/",
        "https://toolbox.oncrawl.com/",
        "http://developer.oncrawl.com/"
    ]
    for url in urls:
        with open(url.replace("/", "_"), "w") as f:
            f.write(url_response_to_md(url))
     */
    val urls = Seq(
      "https://www.oncrawl.com/seo-crawler/",
      "https://www.oncrawl.com/seo-log-analyzer/",
      "https://www.oncrawl.com/oncrawl-analytics/",
      "https://www.oncrawl.com/seo-crawl-logs-analysis/",
      "https://www.oncrawl.com/oncrawl-rankings/",
      "https://www.oncrawl.com/oncrawl-backlinks/",
      "https://www.oncrawl.com/oncrawl-platform/",
      "https://www.oncrawl.com/google-analytics-oncrawl/",
      "https://www.oncrawl.com/google-search-console-oncrawl/",
      "https://www.oncrawl.com/adobe-analytics-oncrawl/",
      "https://www.oncrawl.com/majestic-oncrawl/",
      "https://www.oncrawl.com/at-internet-oncrawl/",
      "https://toolbox.oncrawl.com/",
      "http://developer.oncrawl.com/"
    )

    def fileContent(url: String): String = {
      val modifiedURL = url.replace("/", "_")
      Source.fromFile("./pages/" + modifiedURL).mkString
    }

    val contents = urls
      .map(url => (url, fileContent(url)))

    val df = spark.createDataFrame(contents).toDF("url", "content")
      .withColumn("words", udf(
        (content: String) =>
          Seq(
            ("\n", " "),
            (".", " "),
            (",", " "),
            (":", " "),
            (";", " "),
            ("!", " "),
            ("?", " "),
            ("'", " ")
          ).foldLeft(content.toLowerCase())((content, pair) => content.replace(pair._1, pair._2))
            .split(" ")
            .filter(e => !e.isEmpty)
      ).apply(col("content")))

    // ML
    var stages: Array[PipelineStage] = Array()
    // TF IDF  TODO: adaptativenumFeatures for collision
    stages = stages :+ new HashingTF().setNumFeatures(2000000).setInputCol("words").setOutputCol("tf")
    stages = stages :+ new IDF() /*.setMinDocFreq()*/ .setInputCol("tf").setOutputCol("tfidf")

    val pipe = new Pipeline().setStages(stages)
    val model = pipe.fit(df)

    // mapping
    import spark.implicits._
    val indexToWord: Map[Long, String] = model.transform(
      df
        .select("words")
        .withColumn("words", explode(col("words")))
        .distinct()
        .withColumn("words", array("words")
        )
    )
      .withColumn("indices",
        udf((v: org.apache.spark.ml.linalg.SparseVector) => v.indices(0)).apply(col("tfidf")
        )
      )
      .withColumn("words", col("words")(0))
      .select("indices", "words").as[(Long, String)].collect().toMap

    model
      .transform(df)
      .map(page => {
        val topX = 5
        val url: String = page.getAs("url")
        val vector = page.getAs[org.apache.spark.ml.linalg.SparseVector]("tfidf")
        val threshhold = vector.values.sorted.takeRight(topX).min
        val keywords = vector.indices.foldLeft(List[(String, Double)]())((acc, index) => {
          val v = vector(index)
          if (v >= threshhold && acc.size < topX) acc :+ (indexToWord(index), v) else acc
        }
        )
        (url, keywords)
      }
      ).show(false)


    // TODO: lemmatization
  }
}
