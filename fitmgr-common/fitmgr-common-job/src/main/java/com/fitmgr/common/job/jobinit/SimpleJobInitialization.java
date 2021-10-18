
package com.fitmgr.common.job.jobinit;

import java.util.Map;
import java.util.Map.Entry;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.fitmgr.common.job.properties.ElasticJobProperties;
import com.fitmgr.common.job.properties.ElasticJobProperties.SimpleConfiguration;

/**
 * @author Fitmgr
 * @date 2018/7/24 简单任务初始
 */
public class SimpleJobInitialization extends AbstractJobInitialization {

    private Map<String, ElasticJobProperties.SimpleConfiguration> simpleConfigurationMap;

    public SimpleJobInitialization(final Map<String, ElasticJobProperties.SimpleConfiguration> simpleConfigurationMap) {
        this.simpleConfigurationMap = simpleConfigurationMap;
    }

    public void init() {
        for (Entry<String, SimpleConfiguration> job : simpleConfigurationMap.entrySet()) {
            ElasticJobProperties.SimpleConfiguration configuration = job.getValue();
            initJob(job.getKey(), configuration.getJobType(), configuration);
        }
    }

    @Override
    public JobTypeConfiguration getJobTypeConfiguration(String jobName, JobCoreConfiguration jobCoreConfiguration) {
        ElasticJobProperties.SimpleConfiguration configuration = simpleConfigurationMap.get(jobName);
        return new SimpleJobConfiguration(jobCoreConfiguration, configuration.getJobClass());
    }
}
