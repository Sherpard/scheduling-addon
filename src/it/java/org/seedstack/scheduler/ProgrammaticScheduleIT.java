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
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.seedstack.scheduler.fixtures.ProgrammaticFiredTask;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.testing.junit4.SeedITRunner;

import com.google.common.collect.ImmutableMap;

@RunWith(SeedITRunner.class)
public class ProgrammaticScheduleIT {

    private static final String TRIGGER_GROUP = "CustomGroup";
    private static final String TRIGGER_NAME = "ProgramaticTrigger";

    private static final String EXPECTED_TEST_VALUE = "testValue";

    private static String testData;
    private static String nativeTestData;
    private static CountDownLatch countDownLatch;
    private static int programmaticInvocationCount = 0;

    @Inject
    private ScheduledTasks scheduledTasks;
    private Trigger singleTrigger;

    @Before
    public synchronized void setUp() {
        countDownLatch = new CountDownLatch(1);
        programmaticInvocationCount = 0;
        testData = "";
        nativeTestData = "";
        singleTrigger = newTrigger()
                .withIdentity(triggerKey(TRIGGER_NAME, TRIGGER_GROUP))
                .withSchedule(simpleSchedule().withIntervalInSeconds(1).withRepeatCount(1))
                .build();
    }

    @Test
    public synchronized void testProgramaticallyRescheduleWithCustomTrigger() throws Exception {
        createPrototypeTask().withTrigger(singleTrigger).schedule();

        if (!countDownLatch.await(10, TimeUnit.SECONDS))
            fail("timeout during programatically timed task wait");

        countDownLatch = new CountDownLatch(1);
        TriggerKey key = singleTrigger.getKey();
        scheduledTasks.scheduledTask(ProgrammaticFiredTask.class)
                .withTrigger(singleTrigger)
                .reschedule(key.getName(), key.getGroup());

        if (!countDownLatch.await(10, TimeUnit.SECONDS))
            fail("timeout during programatically timed task wait");

        scheduledTasks.scheduledTask(ProgrammaticFiredTask.class).unschedule(TRIGGER_NAME, TRIGGER_GROUP);

    }

    @Test
    public void testProgrammaticallyTimedTask() throws Exception {
        createPrototypeTask().withTrigger(singleTrigger).schedule();

        if (!countDownLatch.await(10, TimeUnit.SECONDS))
            fail("timeout during programatically timed task wait");

        scheduledTasks.scheduledTask(ProgrammaticFiredTask.class).unschedule(TRIGGER_NAME, TRIGGER_GROUP);

        assertThat(programmaticInvocationCount).isEqualTo(1);
        assertThat(testData).isEqualTo(EXPECTED_TEST_VALUE);
        assertThat(nativeTestData).isEqualTo(EXPECTED_TEST_VALUE);
    }

    public static CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public static void markProgrammaticInvocation() {
        programmaticInvocationCount += 1;
    }

    public static void setTestData(String data, String nativeData) {
        nativeTestData = nativeData;
        testData = data;
    }

    private ScheduledTaskBuilder createPrototypeTask() {
        return scheduledTasks.scheduledTask(ProgrammaticFiredTask.class)
                .withTaskName("Task3")
                .withPriority(10)
                .withDataMap(ImmutableMap.of("testData", EXPECTED_TEST_VALUE));

    }

    @Test
    public void testReschedulingUnexistentTrigger() throws Exception {
        try {
            createPrototypeTask().withTrigger(singleTrigger).reschedule(TRIGGER_NAME, TRIGGER_GROUP);
            fail("Trigger rescheduled when key did not exist");
        } catch (SeedException se) {
            assertThat(se.getErrorCode().toString()).isEqualTo("UNRECOGNIZED_TRIGGER");
        }
    }

    @Test
    public void testThatUnschedulingUnexistentTriggerDoesNotCrash() throws Exception {

        createPrototypeTask().withTrigger(singleTrigger).unschedule(TRIGGER_NAME, TRIGGER_GROUP);

    }

}
