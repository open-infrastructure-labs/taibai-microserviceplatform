
package com.taibai.common.job.jobinit;

import java.util.Map;
import java.util.Map.Entry;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.taibai.common.job.properties.ElasticJobProperties;
import com.taibai.common.job.properties.ElasticJobProperties.DataflowConfiguration;

/**
 * @author Taibai
 * @date 2018/7/24 流式任务初始
 */
public class DataflowJobInitialization extends AbstractJobInitialization {

    private Map<String, ElasticJobProperties.DataflowConfiguration> dataflowConfigurationMap;

    public DataflowJobInitialization(
            final Map<String, ElasticJobProperties.DataflowConfiguration> dataflowConfigurationMap) {
        this.dataflowConfigurationMap = dataflowConfigurationMap;
    }

    public void init() {
        for (Entry<String, DataflowConfiguration> job : dataflowConfigurationMap.entrySet()) {
            ElasticJobProperties.DataflowConfiguration configuration = job.getValue();
            initJob(job.getKey(), configuration.getJobType(), configuration);
        }
    }

    @Override
    public JobTypeConfiguration getJobTypeConfiguration(String jobName, JobCoreConfiguration jobCoreConfiguration) {
        ElasticJobProperties.DataflowConfiguration configuration = dataflowConfigurationMap.get(jobName);
        return new DataflowJobConfiguration(jobCoreConfiguration, configuration.getJobClass(),
                configuration.isStreamingProcess());
    }
}
