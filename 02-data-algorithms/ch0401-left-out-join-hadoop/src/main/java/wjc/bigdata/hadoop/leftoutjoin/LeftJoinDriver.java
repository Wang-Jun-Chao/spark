package wjc.bigdata.hadoop.leftoutjoin;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import tl.lin.data.pair.PairOfStrings;
//

/**
 * LeftJoinDriver is driver class for submitting "Left Join" job to Hadoop.
 *
 * @author Mahmoud Parsian
 */
public class LeftJoinDriver {

    public static void main(String[] args) throws Exception {
        Path transactions = new Path(args[0]);  // input
        Path users = new Path(args[1]);         // input
        Path output = new Path(args[2]);        // output

        Configuration conf = new Configuration();
        Job job = new Job(conf);
        job.setJarByClass(LeftJoinDriver.class);
        job.setJobName("Phase-1: Left Outer Join");

        // "secondary sort" is handled by setting the following 3 plug-ins:
        // 1. how the mapper generated keys will be partitioned
        job.setPartitionerClass(SecondarySortPartitioner.class);

        // 2. how the natural keys (generated by mappers) will be grouped
        job.setGroupingComparatorClass(SecondarySortGroupComparator.class);

        // 3. how PairOfStrings will be sorted
        job.setSortComparatorClass(PairOfStrings.Comparator.class);

        job.setReducerClass(org.dataalgorithms.chap04.mapreduce.LeftJoinReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        // define multiple mappers: one for users and one for transactions
        MultipleInputs.addInputPath(job, transactions, TextInputFormat.class, LeftJoinTransactionMapper.class);
        MultipleInputs.addInputPath(job, users, TextInputFormat.class, LeftJoinUserMapper.class);

        job.setMapOutputKeyClass(PairOfStrings.class);
        job.setMapOutputValueClass(PairOfStrings.class);
        FileOutputFormat.setOutputPath(job, output);

        if (job.waitForCompletion(true)) {
            return;
        } else {
            throw new Exception("Phase-1: Left Outer Join Job Failed");
        }
    }
}
