/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.scheduler;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import java.io.Serializable;
import javax.inject.Inject;
import org.seedstack.seed.Logging;
import org.slf4j.Logger;

public class TimedTaskListener implements Serializable, TaskListener<TimedTask1> {
    @Logging
    private Logger logger;

    @Inject
    private ScheduledTasks scheduledTasks;

    @Override
    public void before(SchedulingContext sc) {
        logger.info("Before timed task of Task {} on trigger {}", sc.getTaskName(), sc.getTriggerName());
        AutomaticScheduleIT.beforeCalled = true;
    }

    @Override
    public void after(SchedulingContext sc) {
        logger.info("After timed task from Task {} on trigger {}", sc.getTaskName(), sc.getTriggerName());
        AutomaticScheduleIT.afterCalled = true;
    }

    @Override
    public void onException(SchedulingContext sc, Exception e) {
        AutomaticScheduleIT.onExceptionCalled = true;

        logger.info("Rescheduling timed task 2 from task {} on trigger {}", sc.getTaskName(), sc.getTriggerName());
        scheduledTasks.scheduledTask(TimedTask2.class).withTaskName("Task2")
                .withTrigger(newTrigger()
                        .withIdentity(triggerKey("Trigger2"))
                        .withSchedule(simpleSchedule()
                                .withIntervalInSeconds(1)).build()).schedule();
    }
}
