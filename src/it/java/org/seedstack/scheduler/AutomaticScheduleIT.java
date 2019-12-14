/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.scheduler;

import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
public class AutomaticScheduleIT {
    private static final String EXPECTED_TASK_NAME = "Task4";
    private static final String EXPECTED_TRIGGER_NAME = "Trigger4";
    public static final CountDownLatch countDownLatch = new CountDownLatch(3);
    private static int cronTimeFiredInvocationCount = 0;
    private static int listenerFiredTaskInvocationCount = 0;
    private static int configTimeFiredInvocationCount = 0;
    private static boolean beforeCalled = false;
    private static boolean afterCalled = false;
    private static boolean onExceptionCalled = false;
    private static String actualTaskName;
    private static String actualTriggerName;

    @Test
    public void testAnnotatedTimedTasks() throws Exception {
        if (!countDownLatch.await(10, TimeUnit.SECONDS))
            fail("timeout during automatically timed task wait");

        Assertions.assertThat(beforeCalled).isTrue();
        Assertions.assertThat(afterCalled).isFalse();
        Assertions.assertThat(onExceptionCalled).isTrue();

        // if the test is slow it will execute TimedTask1 multiple times
        Assertions.assertThat(cronTimeFiredInvocationCount).isGreaterThanOrEqualTo(1);
        Assertions.assertThat(listenerFiredTaskInvocationCount).isGreaterThanOrEqualTo(1);
        Assertions.assertThat(configTimeFiredInvocationCount).isGreaterThanOrEqualTo(1);

        Assertions.assertThat(actualTaskName).isEqualTo(EXPECTED_TASK_NAME);
        Assertions.assertThat(actualTriggerName).isEqualTo(EXPECTED_TRIGGER_NAME);
    }

    public static void notifyCronTimerInvocation() {
        cronTimeFiredInvocationCount += 1;
    }

    public static void notifyListenerFiredTaskInvocation() {
        listenerFiredTaskInvocationCount += 1;
    }

    public static void notifyConfigFiredTaskInvocation(SchedulingContext sc) {
        configTimeFiredInvocationCount += 1;
        actualTriggerName = sc.getTriggerName();
        actualTaskName = sc.getTaskName();
    }

    public static void notifyBeforeTriggerInvocation() {
        beforeCalled = true;
    }

    public static void notifyAfterTriggerInvocation() {
        afterCalled = true;
    }

    public static void notifyOnExcetionInvocation() {
        onExceptionCalled = true;
    }

}
