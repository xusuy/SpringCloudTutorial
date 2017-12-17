package com.springms.cloud.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 定时调度任务Job2.
 *
 * @author hmilyylimh
 *
 * @version 0.0.1
 *
 * @date 2017/12/17
 *
 */
public class Job2 extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("==================================================================");
        System.out.println("===========        "+(new SimpleDateFormat("yyyy-mm-dd HH:mm:ss.SSSSSS")).format(new Date())+"         ============");
        System.out.println("=======================        Job2         ======================");
        System.out.println("==================================================================");
        System.out.println();
    }
}