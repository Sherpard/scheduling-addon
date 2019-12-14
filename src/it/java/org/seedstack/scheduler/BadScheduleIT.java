/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.TriggerBuilder;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
public class BadScheduleIT {

    @Inject
    private ScheduledTasks scheduledTasks;

    private final static String CRON_EXPRESSION = "0 0 0 1 JAN ? 2099";

    private static final class DumbTask implements Task {
        @Override
        public void execute(SchedulingContext sc) throws Exception {
            throw new Exception("This should not be run!");
        }
    }

    @Test
    public void testThatFailsWithoutTrigger() throws Exception {
        try {
            scheduledTasks.scheduledTask(DumbTask.class).schedule();
            fail("No Cron expression / trigger has been set");
        } catch (SeedException se) {
            assertThat(se.getErrorCode().toString()).isEqualTo("MISSING_CRON_EXPRESSION");
        }
    }

    @Test
    public void testThatFailsWithTriggerAndCronExpression() throws Exception {
        try {
            scheduledTasks.scheduledTask(DumbTask.class)
                    .withCronExpression(CRON_EXPRESSION)
                    .withTrigger(TriggerBuilder.newTrigger().build())
                    .schedule();
            fail("Cron expression and a trigger has been set");
        } catch (SeedException se) {
            assertThat(se.getErrorCode().toString()).isEqualTo("IMPOSSIBLE_TO_USE_CRON_AND_TRIGGER");
        }
    }

    @Test
    public void testThatTaskNameIsUnique() throws Exception {
        try {
            scheduledTasks.scheduledTask(DumbTask.class)
                    .withCronExpression(CRON_EXPRESSION)
                    .withTaskName("Task1")
                    .schedule();

            scheduledTasks.scheduledTask(DumbTask.class)
                    .withCronExpression(CRON_EXPRESSION)
                    .withTaskName("Task1")
                    .schedule();

            fail("Two task has been declared with the same name");

        } catch (SeedException se) {
            assertThat(se.getErrorCode().toString()).isEqualTo("TRIGGER_AND_JOB_NAME_SHOULD_BE_UNIQUE");
        }
    }

    @Test
    public void testThatTaskTriggerNamesUnique() throws Exception {
        boolean updateTriggerExecuted = false;
        try {
            scheduledTasks.scheduledTask(DumbTask.class)
                    .withCronExpression(CRON_EXPRESSION)
                    .withTriggerName("Trigger1")
                    .updateExistingTrigger()
                    .schedule();

            scheduledTasks.scheduledTask(DumbTask.class)
                    .withCronExpression(CRON_EXPRESSION)
                    .withTriggerName("Trigger1")
                    .updateExistingTrigger()
                    .schedule();

            updateTriggerExecuted = true;

            scheduledTasks.scheduledTask(DumbTask.class)
                    .withCronExpression(CRON_EXPRESSION)
                    .withTriggerName("Trigger1")
                    .schedule();

            fail("Two task has been declared with the same trigger");

        } catch (SeedException se) {
            assertThat(updateTriggerExecuted).describedAs("Assert that trigger update has been called").isTrue();
            assertThat(se.getErrorCode().toString()).isEqualTo("TRIGGER_AND_JOB_NAME_SHOULD_BE_UNIQUE");
        }
    }

}
