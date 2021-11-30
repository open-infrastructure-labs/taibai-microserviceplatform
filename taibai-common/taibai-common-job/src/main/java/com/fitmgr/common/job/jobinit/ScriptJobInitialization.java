
package com.fitmgr.common.job.jobinit;

import java.util.Map;
import java.util.Map.Entry;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.fitmgr.common.job.properties.ElasticJobProperties;
import com.fitmgr.common.job.properties.ElasticJobProperties.ScriptConfiguration;

/**
 * @author Fitmgr
 * @date 2018/7/24 脚本任务初始
 */
public class ScriptJobInitialization extends AbstractJobInitialization {

    private Map<String, ElasticJobProperties.ScriptConfiguration> scriptConfigurationMap;

    public ScriptJobInitialization(final Map<String, ElasticJobProperties.ScriptConfiguration> scriptConfigurationMap) {
        this.scriptConfigurationMap = scriptConfigurationMap;
    }

    public void init() {
        for (Entry<String, ScriptConfiguration> job : scriptConfigurationMap.entrySet()) {
            ElasticJobProperties.ScriptConfiguration configuration = job.getValue();
            initJob(job.getKey(), configuration.getJobType(), configuration);
        }
    }

    @Override
    public JobTypeConfiguration getJobTypeConfiguration(String jobName, JobCoreConfiguration jobCoreConfiguration) {
        ElasticJobProperties.ScriptConfiguration configuration = scriptConfigurationMap.get(jobName);
        return new ScriptJobConfiguration(jobCoreConfiguration, configuration.getScriptCommandLine());
    }
}
