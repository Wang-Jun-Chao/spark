package wjc.bigdata.spark.working_with_different_types_of_data;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wjc.bigdata.spark.util.PathUtils;

import java.util.Arrays;

/**
 * @author: wangjunchao(王俊超)
 * @time: 2019-03-24 09:30
 **/
public class DifferentTypesOfData {
    private final static Logger logger = LoggerFactory.getLogger(DifferentTypesOfData.class);


    public static void main(String[] args) {

        SparkSession spark = SparkSession
                .builder()
                .master("local[*]")
                .appName("ch0601-working-with-different-types-of-data")
                .getOrCreate();

        Dataset<Row> df = spark.read().format("csv")
                .option("header", "true")
                .option("inferSchema", "true")
                .load(PathUtils.workDir("../../../data/retail-data/by-day/2010-12-01.csv"));
        df.printSchema();
        df.createOrReplaceTempView("dfTable");

        df.select(
                functions.lit(5),
                functions.lit("five"),
                functions.lit(5.0)
        ).show(5);

        df.where(functions.column("InvoiceNo").equalTo(536365))
                .select("InvoiceNo", "Description")
                .show(5, false);

        df.where("InvoiceNo = 536365")
                .show(5, false);

        df.where("InvoiceNo <> 536365")
                .show(5, false);

        Column priceFilter = functions.column("UnitPrice").$greater(600);
        Column descripFilter = functions.column("Description").contains("POSTAGE");

        df.where(functions.col("StockCode").isin("DOT")).where(priceFilter.or(descripFilter))
                .show();

        Column dotCodeFilter = functions.column("StockCode").equalTo("DOT");
        df.withColumn("isExpensive", dotCodeFilter.and(priceFilter.or(descripFilter)))
                .where("isExpensive")
                .select("unitPrice", "isExpensive")
                .show(5);

        df.withColumn("isExpensive", functions.not(functions.column("UnitPrice").leq(250)))
                .filter("isExpensive")
                .select("Description", "UnitPrice")
                .show(5);
        df.withColumn("isExpensive", functions.expr("NOT UnitPrice <= 250"))
                .filter("isExpensive")
                .select("Description", "UnitPrice")
                .show(5);

        df.where(functions.column("Description").eqNullSafe("hello")).show();

        Column fabricatedQuantity = functions.pow(
                functions.column("Quantity").multiply(functions.column("UnitPrice")),
                2)
                .$plus(5);

        df.select(functions.expr("CustomerId"), fabricatedQuantity.alias("realQuantity"))
                .show(2);

        df.selectExpr(
                "CustomerId",
                "(POWER((Quantity * UnitPrice), 2.0) + 5) as realQuantity")
                .show(2);

        df.select(functions.round(
                functions.column("UnitPrice"), 1).alias("rounded"),
                functions.column("UnitPrice"))
                .show(5);

        df.select(
                functions.round(functions.lit("2.5")),
                functions.bround(functions.lit("2.5")))
                .show(2);

        df.stat().corr("Quantity", "UnitPrice");
        df.select(functions.corr("Quantity", "UnitPrice"))
                .show();

        df.describe().show();

        String colName = "UnitPrice";
        double[] quantileProbs = new double[]{0.5};
        double relError = 0.05;
        double[] approxQuantile = df.stat().approxQuantile(colName, quantileProbs, relError); // 2.51
        System.out.println(Arrays.toString(approxQuantile));

        df.stat().crosstab("StockCode", "Quantity")
                .show();

        df.stat().freqItems(new String[]{"StockCode", "Quantity"})
                .show();

        df.select(functions.monotonically_increasing_id())
                .show(2);

        df.select(functions.initcap(functions.column("Description")))
                .show(2, false);

        df.select(
                functions.col("Description"),
                functions.lower(functions.col("Description")),
                functions.upper(functions.lower(functions.col("Description"))))
                .show(2);

        df.select(
                functions.ltrim(functions.lit("    HELLO    ")).as("ltrim"),
                functions.rtrim(functions.lit("    HELLO    ")).as("rtrim"),
                functions.trim(functions.lit("    HELLO    ")).as("trim"),
                functions.lpad(functions.lit("HELLO"), 3, " ").as("lp"),
                functions.rpad(functions.lit("HELLO"), 10, " ").as("rp"))
                .show(2);


        String regexString = StringUtils.join("black", "white", "red", "green", "blue")
                .toUpperCase();
        // the | signifies `OR` in regular expression syntax
        df.select(
                functions.regexp_replace(
                        functions.col("Description"),
                        regexString,
                        "COLOR")
                        .alias("color_clean"),
                functions.col("Description"))
                .show(2);

        df.select(
                functions.translate(
                        functions.col("Description"),
                        "LEET", "1337"),
                functions.col("Description"))
                .show(2);

        df.select(
                functions.regexp_extract(functions.column("Description"), regexString, 1)
                        .alias("color_clean"),
                functions.column("Description"))
                .show(2);

        Column containsBlack = functions.column("Description").contains("BLACK");
        Column containsWhite = functions.column("DESCRIPTION").contains("WHITE");
        df.withColumn("hasSimpleColor", containsBlack.or(containsWhite))
                .where("hasSimpleColor")
                .select("Description")
                .show(3, false);

    }
}
