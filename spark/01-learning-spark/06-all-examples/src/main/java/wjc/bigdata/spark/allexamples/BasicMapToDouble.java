/**
 * Illustrates a simple map to double in Java
 */
package wjc.bigdata.spark.allexamples;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.DoubleFunction;

import java.util.Arrays;

public class BasicMapToDouble {
    public static void main(String[] args) throws Exception {
        String master;
        if (args.length > 0) {
            master = args[0];
        } else {
            master = "local";
        }
        JavaSparkContext sc = new JavaSparkContext(
                master, "basicmaptodouble", System.getenv("SPARK_HOME"), System.getenv("JARS"));
        JavaRDD<Integer> rdd = sc.parallelize(Arrays.asList(1, 2, 3, 4));
        JavaDoubleRDD result = rdd.mapToDouble(
                new DoubleFunction<Integer>() {
                    @Override
                    public double call(Integer x) {
                        double y = (double) x;
                        return y * y;
                    }
                });
        System.out.println(StringUtils.join(result.collect(), ","));
    }
}
